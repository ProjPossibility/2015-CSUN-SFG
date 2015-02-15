package sfg.location;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class GPS implements LocationListener {

	private TextView latitudeField, longitudeField;
	private LocationManager locationManager;
	private String provider;
	
	private List<Location> listOfLocations = new ArrayList<Location>();
	private float totalDistanceTraveled = 0;
	public static final int MAX_LOCATION_BUFFER = 50;
	
	public GPS(Context context, TextView lat, TextView longitude) {
		this.latitudeField = lat;
		this.longitudeField = longitude;
		
		//get the location manager
		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		
		//set criteria for getting location provider, use defaults for now
		Criteria criteria = new Criteria();
		provider = locationManager.getBestProvider(criteria, false);
		Location location = locationManager.getLastKnownLocation(provider);
		
		//initialize the location fields
		if(location != null) {
			Log.i("sfg", "Provider " + provider + " has been selected.");
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
	
	public void stopMilieage() {
		totalDistanceTraveled += calculateDistanceTraveled();
		latitudeField.setText(Float.toString(totalDistanceTraveled));
	}
	
	@Override
	public void onLocationChanged(Location location) {
		listOfLocations.add(location);
		double lat = location.getLatitude();
		double lng = location.getLongitude();
		latitudeField.setText(String.valueOf(lat));
		longitudeField.setText(String.valueOf(lng));
		
		//if we pass our buffer limit, calculate how far we've traveled and clear the buffer
		if(listOfLocations.size() > MAX_LOCATION_BUFFER) {
			totalDistanceTraveled += calculateDistanceTraveled();
			listOfLocations.clear();
		}
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		Log.i("sfg", "Enabled new provider " + provider);

	}

	@Override
	public void onProviderDisabled(String provider) {
		Log.i("sfg", "Disabled provider " + provider);
	}
	
	public float calculateDistanceTraveled() {
		float total = 0;
		for(int i = 0; i < listOfLocations.size() - 1; i++)
			total += listOfLocations.get(i).distanceTo(listOfLocations.get(i+1));
		return total;
	}
	
}
