package sfg.sensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.widget.TextView;

public class Gyroscope implements SensorEventListener {

	private TextView x, y, z;
	
	public Gyroscope(TextView x, TextView y, TextView z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		
	}

	@Override
	public void onSensorChanged(SensorEvent arg0) {
		
	}
	
	public void onResume() {
		
	}
	
	public void onPause() {
		
	}

}
