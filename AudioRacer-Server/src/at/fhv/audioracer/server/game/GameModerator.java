package at.fhv.audioracer.server.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.fhv.audioracer.communication.player.message.ConnectResponseMessage;
import at.fhv.audioracer.communication.player.message.FreeCarsMessage;
import at.fhv.audioracer.communication.player.message.PlayerConnectedMessage;
import at.fhv.audioracer.communication.player.message.SelectCarResponseMessage;
import at.fhv.audioracer.core.model.Car;
import at.fhv.audioracer.core.model.Player;
import at.fhv.audioracer.server.PlayerConnection;
import at.fhv.audioracer.server.PlayerServer;

public class GameModerator {
	
	private static Logger _logger = LoggerFactory.getLogger(GameModerator.class);
	private PlayerServer _playerServer;
	private Ranking _ranking;
	
	private HashMap<Integer, Player> _playerList = new HashMap<Integer, Player>();
	private int _plrId = 0;
	
	private HashMap<Integer, Car> _carList = new HashMap<Integer, Car>();
	
	private Object _lockObject = new Object();
	private boolean _gameRunning = false;
	
	// next conditions must be true for game start
	private boolean _mapConfigured = false;
	private boolean _detectionFinished = false;
	private boolean _allZigBeeConnected = false;
	
	public GameModerator(PlayerServer playerServer) {
		_playerServer = playerServer;
	}
	
	/**
	 * called on Player "connect" request
	 * 
	 * @param playerConnection
	 *            the socket connection of this player
	 * @param loginName
	 *            name of player
	 */
	public void connect(PlayerConnection playerConnection, String loginName) {
		Player player = playerConnection.getPlayer();
		player.setLoginName(loginName);
		int id = -1;
		synchronized (_playerList) {
			id = ++_plrId;
			player.setPlayerId(_plrId);
			_playerList.put(_plrId, player);
			_logger.debug("added player {} with playerId {}", loginName, id);
		}
		ConnectResponseMessage resp = new ConnectResponseMessage();
		resp.playerId = id;
		_playerServer.sendToTCP(playerConnection.getID(), resp);
	}
	
	/**
	 * called on Camera "carDetected" request
	 * 
	 * @param newCar
	 *            the detected car
	 */
	public void carDetected(Car newCar) {
		
		synchronized (_lockObject) {
			if (_gameRunning || _detectionFinished) {
				_logger.warn("carDetected not allowed!" + " Game running: {}, car detection finished: {}", _gameRunning, _detectionFinished);
			} else {
				if (!_carList.containsKey(newCar.getCarId())) {
					_logger.debug("carDetected - id: {}", newCar.getCarId());
					_allZigBeeConnected = false;
					_carList.put(newCar.getCarId(), newCar);
				} else {
					_logger.warn("Car with id: {} allready known!", newCar.getCarId());
				}
			}
		}
		
		broadcastFreeCars();
	}
	
	public void configureMap(int sizeX, int sizeY) {
		_logger.debug("configureMap with sizeX: {} and sizeY: {} called", sizeX, sizeY);
		
		synchronized (_lockObject) {
			if (_gameRunning) {
				_logger.warn("configureMap not allowed while game is running!");
			} else {
				_ranking = new Ranking(sizeX, sizeY);
				_mapConfigured = true;
			}
		}
	}
	
	public void detectionFinished() {
		_logger.debug("detectionFinihsed called");
		
		synchronized (_lockObject) {
			_detectionFinished = true;
		}
	}
	
	public void updateCar(int carId, float posX, float poxY, float direction) {
		_logger.debug("implement me - updateCar called for carId: {}", carId);
	}
	
	public void selectCar(PlayerConnection playerConnection, int carId) {
		_logger.debug("selectCar called from player with id: {} and name: {}", playerConnection.getPlayer().getPlayerId(), playerConnection.getPlayer()
				.getLoginName());
		
		SelectCarResponseMessage selectResponse = new SelectCarResponseMessage();
		selectResponse.selectionSuccess = false;
		
		if (playerConnection.getPlayer().getLoginName() == null) {
			_logger.warn("selectCar - player has to send ConnectRequestMessage first" + " and set a login name for himself!");
		} else {
			synchronized (_lockObject) {
				if (_gameRunning) {
					_logger.warn("selectCar not allowed while game is running!");
				} else {
					if (_carList.containsKey(carId) && _carList.get(carId).getPlayer() == null) {
						Car carToSelect = _carList.get(carId);
						carToSelect.setPlayer(playerConnection.getPlayer());
						playerConnection.getPlayer().setCar(carToSelect);
						selectResponse.selectionSuccess = true;
					} else {
						// for development purposes only
						if (_carList.containsKey(carId) == false) {
							_logger.warn("car with id: {} doesn't exist!", carId);
						} else {
							Player player = _carList.get(carId).getPlayer();
							_logger.warn("car with id: {} allready owned by login: {}", carId, player.getLoginName());
						}
					}
				}
			}
		}
		
		_playerServer.sendToTCP(playerConnection.getID(), selectResponse);
		
		if (selectResponse.selectionSuccess) {
			
			PlayerConnectedMessage plrConnectedMsg = new PlayerConnectedMessage();
			plrConnectedMsg.id = playerConnection.getPlayer().getPlayerId();
			plrConnectedMsg.loginName = playerConnection.getPlayer().getLoginName();
			_playerServer.sendToAllExceptTCP(playerConnection.getID(), plrConnectedMsg);
			
			broadcastFreeCars();
		}
	}
	
	/**
	 * Send currently free cars to all Players.
	 */
	private void broadcastFreeCars() {
		Iterator<Entry<Integer, Car>> it = _carList.entrySet().iterator();
		ArrayList<Integer> freeCars = new ArrayList<Integer>();
		Entry<Integer, Car> entry = null;
		Car car = null;
		while (it.hasNext()) {
			entry = it.next();
			car = entry.getValue();
			if (car.getPlayer() == null) {
				freeCars.add(entry.getKey());
			}
		}
		FreeCarsMessage freeCarsMessage = new FreeCarsMessage();
		int free[] = new int[freeCars.size()];
		for (int i = 0; i < free.length; i++) {
			free[i] = freeCars.get(i).intValue();
		}
		freeCarsMessage.freeCars = free;
		_playerServer.sendToAllTCP(freeCarsMessage);
	}
}