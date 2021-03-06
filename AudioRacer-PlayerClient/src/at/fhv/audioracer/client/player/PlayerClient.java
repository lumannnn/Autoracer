package at.fhv.audioracer.client.player;

import java.io.IOException;
import java.util.HashMap;

import at.fhv.audioracer.communication.player.IPlayerClient;
import at.fhv.audioracer.communication.player.IPlayerServer;
import at.fhv.audioracer.communication.player.PlayerNetwork;
import at.fhv.audioracer.communication.player.message.FreeCarsMessage;
import at.fhv.audioracer.communication.player.message.PlayerConnectedMessage;
import at.fhv.audioracer.communication.player.message.PlayerMessage;
import at.fhv.audioracer.communication.player.message.UpdateCheckPointDirectionMessage;
import at.fhv.audioracer.communication.player.message.UpdateGameStateMessage;
import at.fhv.audioracer.core.model.Player;
import at.fhv.audioracer.core.util.ListenerList;
import at.fhv.audioracer.core.util.Position;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

public class PlayerClient extends Listener implements IPlayerClient {
	
	private static class PlayerClientListenerList extends ListenerList<IPlayerClientListener>
			implements IPlayerClientListener {
		
		@Override
		public void onUpdateGameState(Player player) {
			
			for (IPlayerClientListener listener : listeners()) {
				listener.onUpdateGameState(player);
			}
			
		}
		
		@Override
		public void onUpdateCheckpointDirection(Position position) {
			
			for (IPlayerClientListener listener : listeners()) {
				listener.onUpdateCheckpointDirection(position);
			}
			
		}
		
		@Override
		public void onUpdateFreeCars() {
			
			for (IPlayerClientListener listener : listeners()) {
				listener.onUpdateFreeCars();
			}
			
		}
		
		@Override
		public void onPlayerConnected(int playerId) {
			
			for (IPlayerClientListener listener : listeners()) {
				listener.onPlayerConnected(playerId);
			}
			
		}
		
		@Override
		public void onPlayerDisconnected(int playerId) {
			
			for (IPlayerClientListener listener : listeners()) {
				listener.onPlayerDisconnected(playerId);
			}
			
		}
		
		@Override
		public void onGameStarts() {
			
			for (IPlayerClientListener listener : listeners()) {
				listener.onGameStarts();
			}
			
		}
		
		@Override
		public void onGameEnd() {
			
			for (IPlayerClientListener listener : listeners()) {
				listener.onGameEnd();
			}
			
		}
		
	}
	
	/*
	 * holds known connected players
	 */
	private HashMap<Integer, Player> _players;
	
	/**
	 * currently known cars. <Integer, CarId>
	 */
	private HashMap<Byte, Byte> _cars;
	
	/*
	 * ids of free cars
	 */
	private byte[] _freeCarIds;
	
	/*
	 * player of this client
	 */
	private Player _player;
	
	/*
	 * speed between -1 (reverse) and 1 (forward)
	 */
	private float speed;
	
	/*
	 * Connection client
	 */
	private Client _client;
	
	/*
	 * 
	 */
	boolean _connected;
	
	public float getSpeed() {
		return speed;
	}
	
	public void setSpeed(float speed) {
		this.speed = speed;
	}
	
	public float getDirection() {
		return direction;
	}
	
	public void setDirection(float direction) {
		this.direction = direction;
	}
	
	/*
	 * direction between -1 (left) and 1 (right)
	 */
	private float direction;
	
	/*
	 * direction of next checkpoint
	 */
	private Position _nextCheckpoint;
	
	/*
	 * list of PlayerClientListener
	 */
	private PlayerClientListenerList _listenerList;
	
	/*
	 * PlayerClientManager of this Client
	 */
	private IPlayerServer _playerServer;
	
	public PlayerClient() {
		super();
		_players = new HashMap<Integer, Player>();
		_cars = new HashMap<Byte, Byte>();
		_player = new Player();
		_listenerList = new PlayerClientListenerList();
		_connected = false;
		
		_client = new Client();
		_client.start();
		PlayerServerClient serverClient = new PlayerServerClient(_client);
		PlayerNetwork.register(_client);
		_client.addListener(serverClient);
		_client.addListener(this);
		setPlayerServer(serverClient);
	}
	
	@Override
	public void updateGameState(int playerId, int coinsLeft, int time) {
		
		if (_players.containsKey(Integer.valueOf(playerId))) {
			Player p = _players.get(Integer.valueOf(playerId));
			p.setCoinsLeft(coinsLeft);
			p.setTime(time);
			
			_listenerList.onUpdateGameState(p);
		}
		
	}
	
	@Override
	public void playerConnected(int playerId, String playerName) {
		
		if (!_players.containsKey(Integer.valueOf(playerId))) {
			Player p = new Player();
			p.setPlayerId(playerId);
			p.setName(playerName);
			_players.put(playerId, p);
		}
		
		_listenerList.onPlayerConnected(playerId);
		
	}
	
	@Override
	public void playerDisconnected(int playerId) {
		
		if (_players.containsKey(Integer.valueOf(playerId))) {
			Player p = _players.get(Integer.valueOf(playerId));
			_players.remove(p);
		}
		
		_listenerList.onPlayerDisconnected(playerId);
		
	}
	
	@Override
	public void updateCheckpointDirection(float directionX, float directionY) {
		_nextCheckpoint = new Position(directionX, directionY);
		_listenerList.onUpdateCheckpointDirection(getNextCheckpoint());
		
	}
	
	@Override
	public void updateFreeCars(byte[] carIds) {
		
		// check list for unknown free cars and add them
		for (int i = 0; i < carIds.length; i++) {
			if (!_cars.containsKey(carIds[i])) {
				_cars.put(carIds[i], carIds[i]);
			}
		}
		
		_freeCarIds = carIds;
		
		_listenerList.onUpdateFreeCars();
		
	}
	
	@Override
	public void gameOver() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void invalidCommand() {
		// TODO Auto-generated method stub
		System.out.println("invalid command");
	}
	
	@Override
	public void gameStarts() {
		// TODO Auto-generated method stub
		
	}
	
	public HashMap<Integer, Player> getPlayers() {
		return _players;
	}
	
	public HashMap<Byte, Byte> getCars() {
		return _cars;
	}
	
	public byte[] getFreeCarIds() {
		return _freeCarIds;
	}
	
	public Player getPlayer() {
		return _player;
	}
	
	public Position getNextCheckpoint() {
		return _nextCheckpoint;
	}
	
	public IPlayerServer getPlayerServer() {
		return _playerServer;
	}
	
	public void setPlayerServer(IPlayerServer playerServer) {
		_playerServer = playerServer;
	}
	
	public ListenerList<IPlayerClientListener> getListenerList() {
		return _listenerList;
	}
	
	public void startClient() throws IOException {
		startClient(getPlayer().getName(), getServerUrl());
	}
	
	public void startClient(String playerName, String host) throws IOException {
		if (_client.isConnected()) {
			_client.close();
		}
		_client.connect(5000, host, PlayerNetwork.PLAYER_SERVICE_PORT,
				PlayerNetwork.PLAYER_SERVICE_PORT);
		
		_connected = false;
		if (_player.getPlayerId() != Player.INVALID_PLAYER_ID) {
			// player already has a valid id, reconnect
			System.out.println("reconnect with player-id: " + _player.getPlayerId());
			_connected = _playerServer.reconnect(getPlayer().getPlayerId());
			System.out.println("reconnecting succeded: " + _connected);
		}
		if (!_connected) {
			_player.setPlayerId(getPlayerServer().setPlayerName(playerName));
			
			addPlayer(_player.getPlayerId(), playerName);
			
			_connected = true;
		}
	}
	
	private void addPlayer(int playerId, String playerName) {
		Player player;
		if (_players.containsKey(playerId)) {
			player = _players.get(playerId);
		} else {
			player = new Player();
			_players.put(playerId, player);
			player.setPlayerId(playerId);
		}
		player.setName(playerName);
	}
	
	public void stopClient() {
		_client.close();
		_connected = false;
	}
	
	public boolean hasConnection() {
		return _connected;
	}
	
	@Override
	public void received(Connection connection, Object object) {
		if (object instanceof PlayerMessage) {
			PlayerMessage message = (PlayerMessage) object;
			switch (message.messageId) {
				case UPDATE_FREE_CARS:
					System.out.println("Received UPDAT_FREE_CARS: "
							+ ((FreeCarsMessage) message).freeCars.length);
					updateFreeCars(((FreeCarsMessage) message).freeCars);
					break;
				case UPDATE_CHECKPOINT_DIRECTION:
					UpdateCheckPointDirectionMessage updateDirection = (UpdateCheckPointDirectionMessage) message;
					updateCheckpointDirection(updateDirection.posX, updateDirection.posY);
					break;
				case UPDATE_GAME_STATE:
					UpdateGameStateMessage updateGS = (UpdateGameStateMessage) message;
					System.out.println("GameState update received for player id: "
							+ updateGS.playerId + " coins left: " + updateGS.coinsLeft
							+ " current time: " + updateGS.time);
					updateGameState(updateGS.playerId, updateGS.coinsLeft, updateGS.time);
					break;
				case PLAYER_CONNECTED:
					PlayerConnectedMessage connectedMsg = (PlayerConnectedMessage) message;
					System.out.println("Other player conneted with name: "
							+ connectedMsg.playerName);
					
					addPlayer(connectedMsg.id, connectedMsg.playerName);
					break;
				case GAME_END:
					System.out.println("Game over.");
					_listenerList.onGameEnd();
					break;
				default:
					// System.out.println("Message with id: " + message.messageId
					// + " not known in PlayerClient!");
					break;
			}
		}
	}
	
	private String _serverUrl = null;
	
	public void setServerUrl(String serverUrl) {
		_serverUrl = serverUrl;
	}
	
	public String getServerUrl() {
		return _serverUrl;
	}
}
