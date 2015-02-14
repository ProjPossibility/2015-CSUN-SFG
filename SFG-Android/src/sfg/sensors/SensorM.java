package sfg.sensors;

import com.newbillity.sfg_android.R;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.widget.TextView;

public class SensorM {

	private SensorManager mSensorManager;
	private Context context;
	
	private Gyroscope gyro;
	private Accelerometer acc;
	private Sensor gyroSensor, accSensor;
	
	public SensorM(Context context, Activity activity) {
		this.context = context;
		
		TextView accx = (TextView) activity.findViewById(R.id.accx);
		TextView accy = (TextView) activity.findViewById(R.id.accy);
		TextView accz = (TextView) activity.findViewById(R.id.accz);
		gyro = new Gyroscope(accx, accy, accz);
		
		TextView gyrox = (TextView) activity.findViewById(R.id.gyrox);
		TextView gyroy = (TextView) activity.findViewById(R.id.gyroy);
		TextView gyroz = (TextView) activity.findViewById(R.id.gyroz);
		acc = new Accelerometer(gyrox, gyroy, gyroz);
		
		mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		gyroSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		accSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	}
	
	public void onResume() {
		mSensorManager.registerListener(gyro, gyroSensor, SensorManager.SENSOR_DELAY_NORMAL);
		mSensorManager.registerListener(acc, accSensor, SensorManager.SENSOR_DELAY_NORMAL);
	}
	
	public void onPause() {
		mSensorManager.unregisterListener(gyro, gyroSensor);
		mSensorManager.unregisterListener(acc, accSensor);
	}
}
