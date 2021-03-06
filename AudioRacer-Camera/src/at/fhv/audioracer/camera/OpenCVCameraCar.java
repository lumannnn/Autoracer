package at.fhv.audioracer.camera;

import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.opencv.core.Scalar;

import at.fhv.audioracer.core.model.Car;
import at.fhv.audioracer.core.util.Direction;
import at.fhv.audioracer.core.util.Position;
import at.fhv.audioracer.ui.pivot.MapComponent;

public class OpenCVCameraCar {
	
	private Car<?> _car;
	private Scalar _lowerBound;
	private Scalar _upperBound;
	
	/**
	 * call to create a detected Object as Car in Model
	 * 
	 * @param id
	 * @param position
	 * @param direction
	 * @return true if car was created
	 */
	public boolean carDetected(byte id, Position position, Direction direction) {
		try {
			URL carImage;
			if (id % 2 == 0) {
				carImage = MapComponent.class.getResource("car-red.png");
			} else {
				carImage = MapComponent.class.getResource("car-blue.png");
			}
			_car = new Car<>(id, position, direction, ImageIO.read(carImage));
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public void updatePosition(Position position, Direction direction) {
		_car.updatePosition(position, direction);
	}
	
	public void setHueRange(Scalar lowerBound, Scalar upperBound) {
		
		_lowerBound = lowerBound.clone();
		_upperBound = upperBound.clone();
		
	}
	
	public Scalar getLowerHueBound() {
		return _lowerBound;
	}
	
	public Scalar getUpperHueBound() {
		return _upperBound;
	}
	
	public Car<?> getCar() {
		return _car;
	}
	
	public byte getID() {
		return _car.getCarId();
	}
}
