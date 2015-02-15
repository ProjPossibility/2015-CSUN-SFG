package sfg.sensors;

import java.text.DecimalFormat;

import sfg.devices.Internet;
import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

public class Accelerometer implements SensorEventListener {

	private Activity activity;
	private TextView xField, yField, zField;
	public float x, y, z;
	private DecimalFormat df;
	
	private boolean hasStartedRunning = false;
	private static final int TIME_UNTIL_END_RUN_PROMPT = 10 * 1000;
	private long lastStepTakenAt;
	
	//private Handler mHandler = new Handler();
	
	public Accelerometer(Activity activity, TextView x, TextView y, TextView z) {
		this.activity = activity;
		this.xField = x;
		this.yField = y;
		this.zField = z;
		
		df = new DecimalFormat("#.##");
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		this.x = event.values[0];
		this.y = event.values[1];
		this.z = event.values[2];
		
		xField.setText(df.format(event.values[0])+",");
		yField.setText(df.format(event.values[1])+",");
		zField.setText(df.format(event.values[2]));
		
		if(hasStartedRunning) {
		    if(z > 18) {
		        lastStepTakenAt = System.currentTimeMillis();
		   }
		   else {
		        if(System.currentTimeMillis() - lastStepTakenAt > TIME_UNTIL_END_RUN_PROMPT) {
		            //activity.stopTrackingMileage();
		        }
		   }
		}
	}

}
