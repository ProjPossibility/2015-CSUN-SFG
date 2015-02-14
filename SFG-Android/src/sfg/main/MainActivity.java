package sfg.main;

import com.newbillity.sfg_android.R;

import sfg.location.GPS;
import sfg.sensors.SensorM;

import android.os.Bundle;
import android.app.Activity;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	  private GPS location;
	  private SensorM sensorManager;

	  @Override
	  public void onCreate(Bundle savedInstanceState) {
		  super.onCreate(savedInstanceState);
		  setContentView(R.layout.activity_main);
		  
		  //set up location tracking
		  TextView latituteField = (TextView) findViewById(R.id.latitudevalue);
		  TextView longitudeField = (TextView) findViewById(R.id.longitudevalue);
		  location = new GPS(this, latituteField, longitudeField);
		  
		  sensorManager = new SensorM(this, this);
	  }

	  @Override
	  protected void onResume() {
		  super.onResume();
		  location.onResume();
		  sensorManager.onResume();
	  }

	  @Override
	  protected void onPause() {
		  super.onPause();
		  location.onPause();
		  sensorManager.onPause();
	  }
}
