package sfg.io;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class PreferencesHelper {

	private SharedPreferences sharedPreferences;
	private Editor editor;
	private static final String APP_SHARED_PREFS = 
			PreferencesHelper.class.getSimpleName(); //name of the file
	
	public PreferencesHelper(Context context) {
		this.sharedPreferences = context.getSharedPreferences
				(APP_SHARED_PREFS, Activity.MODE_PRIVATE);
		this.editor = sharedPreferences.edit();
	}

	public void clearPreferences(Context context) {
		this.sharedPreferences = context.getSharedPreferences
				(APP_SHARED_PREFS, Activity.MODE_PRIVATE);
		this.editor.clear();
		this.editor.commit();
	}
	
	public Boolean getPrefFirstLaunch(String strFirstLaunch) {
		//get string from prefs or return false
		return sharedPreferences.getBoolean("strFirstLaunch", false);
		
	}
	
	public void savePrefFirstLaunch(String strFirstLaunch, Boolean bool) {
		editor.putBoolean(strFirstLaunch, bool);
		editor.commit();
	}

	public Boolean getPrefVoice(String strVoice) {
		//get string from prefs or return false
		return sharedPreferences.getBoolean("strVoice", false);
		
	}
	
	public void savePrefVoice(String strVoice, Boolean bool) {
		editor.putBoolean(strVoice, bool);
		editor.commit();
	}	
	
	
} //end PreferenceHelper class
