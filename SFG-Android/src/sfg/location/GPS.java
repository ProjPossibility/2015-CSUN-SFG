package sfg.location;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class GPS implements LocationListener {

	private TextView latitudeField, longitudeField;
	private LocationManager locationManager;
	private String provider;
	private Context context;
	
	public GPS(Context context, TextView lat, TextView longitude) {
		this.context = context;
		this.latitudeField = lat;
		this.longitudeField = longitude;
		
		// Get the location manager
		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		
		//set criteria for getting location provider, use defaults
		Criteria criteria = new Criteria();
		provider = locationManager.getBestProvider(criteria, false);
		Location location = locationManager.getLastKnownLocation(provider);
		
		//Initialize the location fields
		if (location != null) {
		 System.out.println("Provider " + provider + " has been selected.");
		 onLocationChanged(location);
		}
		else {
			latitudeField.setText("Location not available");
			longitudeField.setText("Location not available");
		}
	}
	
	public void onResume() {
		locationManager.requestLocationUpdates(provider, 400, 1, this);
	}
	
	public void onPause() {
		locationManager.removeUpdates(this);
	}
	
	@Override
	public void onLocationChanged(Location location) {
		double lat = location.getLatitude();
		double lng = location.getLongitude();
		latitudeField.setText(String.valueOf(lat));
		longitudeField.setText(String.valueOf(lng));
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		Toast.makeText(context, "Enabled new provider " + provider, Toast.LENGTH_SHORT).show();

	}

	@Override
	public void onProviderDisabled(String provider) {
		Toast.makeText(context, "Disabled provider " + provider, Toast.LENGTH_SHORT).show();
	}
	
}
