package sfg.sensors;

import java.text.DecimalFormat;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.widget.TextView;

public class Accelerometer implements SensorEventListener {

	private TextView x, y, z;
	private DecimalFormat df;
	
	public Accelerometer(TextView x, TextView y, TextView z) {
		this.x = x;
		this.y = y;
		this.z = z;
		
		df = new DecimalFormat("#.##");
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		x.setText(df.format(event.values[0])+",");
		y.setText(df.format(event.values[1])+",");
		z.setText(df.format(event.values[2]));
	}

}
