package sfg.location;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.FloatMath;
import android.util.Log;
import android.widget.TextView;

public class GPS implements LocationListener {
	
	private TextView latitudeField, longitudeField;
	private LocationManager locationManager;
	private String provider;
	
	private List<Location> listOfLocations = new ArrayList<Location>();
	private float totalDistanceTraveled = 0;
	public static final int MAX_LOCATION_BUFFER = 50;
	
	public static final double RADIUS = 6372797.560856;
	
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
			//onLocationChanged(location);
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
		longitudeField.setText("finished");
		listOfLocations.clear();
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
	
	public void clearBuffer() {
		totalDistanceTraveled += calculateDistanceTraveled();
		listOfLocations.clear();
	}
	
	public float calculateDistanceTraveled() {
		float total = 0;
		for(int i = 0; i < listOfLocations.size() - 1; i++) {
			//total += calculationByDistance(listOfLocations.get(i), listOfLocations.get(i+1));
			total += listOfLocations.get(i).distanceTo(listOfLocations.get(i+1));
			Log.i("sfg", ""+total);
			longitudeField.setText(Float.toString(total));
		}
		return total;
	}

	public float getDistanceTraveled() {
		return totalDistanceTraveled;
	}
	
	public double calculationByDistance(Location StartP, Location EndP) {  
	      double lat1 = StartP.getLatitude()/1E6;
	      double lat2 = EndP.getLatitude()/1E6;
	      double lon1 = StartP.getLongitude()/1E6;
	      double lon2 = EndP.getLongitude()/1E6;
	      double dLat = Math.toRadians(lat2-lat1);  
	      double dLon = Math.toRadians(lon2-lon1);  
	      double a = Math.sin(dLat/2) * Math.sin(dLat/2) +  
	         Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *  
	         Math.sin(dLon/2) * Math.sin(dLon/2);  
	      double c = 2 * Math.asin(Math.sqrt(a));  
	      return RADIUS * c;  
	   }  
	
}
