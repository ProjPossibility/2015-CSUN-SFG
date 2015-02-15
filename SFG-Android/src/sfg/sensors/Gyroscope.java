package sfg.sensors;

import java.text.DecimalFormat;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.widget.TextView;

public class Gyroscope implements SensorEventListener {

	private Activity activity;
	private TextView x, y, z;
	private DecimalFormat df;
	
	public Gyroscope(Activity activity, TextView x, TextView y, TextView z) {
		this.activity = activity;
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
