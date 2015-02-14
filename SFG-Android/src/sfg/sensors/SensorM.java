package sfg.sensors;

import com.newbillity.sfg_android.R;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.widget.TextView;

public class SensorM {

	private SensorManager mSensorManager;
	private Sensor mSensor;
	private Context context;
	
	public SensorM(Context context, Activity activity) {
		this.context = context;
		
		TextView accx = (TextView) activity.findViewById(R.id.accx);
		TextView accy = (TextView) activity.findViewById(R.id.accy);
		TextView accz = (TextView) activity.findViewById(R.id.accz);
		 
		TextView gyrox = (TextView) activity.findViewById(R.id.gyrox);
		TextView gyroy = (TextView) activity.findViewById(R.id.gyroy);
		TextView gyroz = (TextView) activity.findViewById(R.id.gyroz);
		
		mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	}
	
	public void onResume() {
		
	}
	
	public void onPause() {
		
	}
}
