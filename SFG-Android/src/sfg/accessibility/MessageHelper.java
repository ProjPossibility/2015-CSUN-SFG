package sfg.accessibility;

import android.util.Log;

public class MessageHelper {
	
	private static String TAG = MessageHelper.class.getSimpleName();

	public static String metaString;
    // saves the last path in case the user exits the app

	/**
	 * Initializes textToSpeech and lays out the available options for the user,
	 * as well as creating a usage path for the user to take.
	 * 
	 * @param id
	 *            The program path that is being taken
	 */
	public static void ttsPath(final int id) {
		switch (id)
		{
			case 0: {
				Log.i(TAG, "ttsPath: case 0");
				metaString = "Welcome to CamAcc. Please say Picture to take a picture, "
						+ "or Detection to detect faces or Options to change "
						+ "settings or Help for a list of commands.";
				//MainActivity.speakText(metaString);
				break;
			}
			case 1: {
				Log.i(TAG, "ttsPath: case 1");
				metaString = "Your photo has been successfully saved. "
						+ "Say Picture at any time to take another picture, or say "
						+ "Detection to start face detection, or say "
						+ "Filter to apply a filter to the picture you just took.";
				//MainActivity.speakText(metaString);
				break;
			}
		}
	}
}
