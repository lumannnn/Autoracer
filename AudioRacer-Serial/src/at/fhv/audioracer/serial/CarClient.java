package at.fhv.audioracer.serial;

import at.fhv.audioracer.communication.world.ICarClient;
import at.fhv.audioracer.core.util.ListenerList;

public class CarClient implements ICarClient {
	private static final int MAX_CONTROL_VALUE = 250;
	
	private class CarClientListenerList extends ListenerList<ICarClientListener> implements
			ICarClientListener {
		
		@Override
		public void onVelocityChanged(CarClient carClient) {
			for (ICarClientListener listener : listeners()) {
				listener.onVelocityChanged(carClient);
			}
		}
		
	}
	
	class Velocity {
		byte speed;
		byte direction;
	}
	
	private final Object _lock;
	
	private final byte _id;
	
	private volatile Velocity _velocity;
	
	private CarClientListenerList _listenerList;
	
	public CarClient(byte id) {
		_lock = new Object();
		
		_id = id;
		
		_velocity = null;
		
		_listenerList = new CarClientListenerList();
	}
	
	@Override
	public byte getCarClientId() {
		return _id;
	}
	
	public Velocity getVelocity() {
		Velocity velocity;
		synchronized (_lock) {
			velocity = _velocity;
			_velocity = null;
		}
		return velocity;
	}
	
	public ListenerList<ICarClientListener> getListenerList() {
		return _listenerList;
	}
	
	@Override
	public void updateVelocity(float speed, float direction) {
		int realSpeed = (int) (((speed * -1) * (MAX_CONTROL_VALUE / 2.0)) + (MAX_CONTROL_VALUE / 2.0));
		int realDirection = (int) (((direction * -1) * (MAX_CONTROL_VALUE / 2.0)) + (MAX_CONTROL_VALUE / 2.0));
		
		Velocity velocity = new Velocity();
		velocity.speed = (byte) realSpeed;
		velocity.direction = (byte) realDirection;
		
		synchronized (_lock) {
			_velocity = velocity;
		}
		_listenerList.onVelocityChanged(this);
	}
}