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
	
	public void addAchievement(String key, String value) {
		editor.putString(key, value);
		editor.commit();
	}
	
	public boolean hasAchievement(String key) {
		String value = sharedPreferences.getString(key, null);
		if(value != null)
			return true;
		else 
			return false;
	}
	
	
} //end PreferenceHelper class
