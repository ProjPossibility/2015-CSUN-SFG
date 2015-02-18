package sfg.main;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import sfg.accessibility.VoiceEngineHelper;
import sfg.accessibility.Voice_Engine;
import sfg.achievements.Achievements;
import sfg.devices.ArduinoHelper;
import sfg.devices.Internet;
import sfg.io.PreferencesHelper;
import sfg.location.GPS;
import sfg.sensors.SensorM;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.TotalCaptureResult;
import android.widget.TextView;

import com.newbillity.sfg_android.R;

public class MainActivity extends Activity implements OnInitListener {

	private static final String TAG = "sfg";

	private GPS location;
	private SensorM sensorManager;

	private static TextToSpeech textToSpeech;
	private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;

	private static HashMap<String, String> map = new HashMap<String, String>();
	private Handler mHandler = new Handler();

	// shared preference variables
	private boolean isFirstLaunch;
	private String sharedPrefFirstLaunch;

	private boolean isVoice;
	private String sharedPrefVoice;


	// important to use with VR
	private boolean isConnected = false;
	private boolean isWarningSound;

	// accelerometer stuff
	private SensorManager mSensorManager;
	private Sensor acc;
	private SensorEventListener accListener;
	private TextView xField, yField, zField;
	private float xAcc, yAcc, zAcc;
	private DecimalFormat df;
	private long startTime;
	private long endTime;

	private boolean hasStartedRunning = false;
	private static final int TIME_UNTIL_END_RUN_PROMPT = 10 * 1000;
	private long lastStepTakenAt;
	private static final int TIME_BETWEEN_WEB_CALLS = 16;
	private long webStartTime;

	//ui/voice recog stuff
	private boolean isAskingShareFacebook = false;
	private boolean isAskingEndRun = false;
	private String timeOfRun;
	private String distanceTraveled;
	
	//achievements
	private PreferencesHelper prefHelper;
	
	private static final int RUN_THRESHOLD = 17;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		
		prefHelper = new PreferencesHelper(this);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		// accelerometer stuff
		df = new DecimalFormat("#.##");
		xField = (TextView) findViewById(R.id.accx);
		yField = (TextView) findViewById(R.id.accy);
		zField = (TextView) findViewById(R.id.accz);
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		acc = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		accListener = new SensorEventListener() {
			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {

			}

			@Override
			public void onSensorChanged(SensorEvent event) {
				xAcc = Math.abs(event.values[0]);
				yAcc = Math.abs(event.values[1]);
				zAcc = Math.abs(event.values[2]);

				xField.setText(df.format(event.values[0]) + ",");
				yField.setText(df.format(event.values[1]) + ",");
				zField.setText(df.format(event.values[2]));

				float axis = zAcc;
				if(xAcc > yAcc && xAcc > zAcc)
					axis = xAcc;
				else if(yAcc > xAcc && yAcc > zAcc)
					axis = yAcc;
				
				if (!hasStartedRunning && axis > RUN_THRESHOLD) {// start the
																	// run
					startTime = System.currentTimeMillis();
					hasStartedRunning = true;
					lastStepTakenAt = System.currentTimeMillis();
				}

				if (hasStartedRunning && !isAskingEndRun) {
					if(webStartTime == 0)
						webStartTime = System.currentTimeMillis();
					if(System.currentTimeMillis() - webStartTime > TIME_BETWEEN_WEB_CALLS) {
						webStartTime = 0;
						new Thread(new Runnable() {
							@Override
							public void run() {
								String json = Internet.getJSON("http://iot.candybrie.com/arduino.php?get=1");
								Log.i("sfg", json);
								ArduinoHelper helper = Internet.parseJSON(json);
								if(helper.distance != -1)
									speakText("Object "+helper.distance+" feet away");
								
								if(helper.direction.equals("left"))
									speakText("Turn left");
								else if(helper.direction.equals("right"))
									speakText("Turn right");
							}
						}).start();
					}
					if (axis > RUN_THRESHOLD) {
						lastStepTakenAt = System.currentTimeMillis();
					} else {
						if (System.currentTimeMillis() - lastStepTakenAt > TIME_UNTIL_END_RUN_PROMPT) {
							isAskingEndRun = true;
							speakText("You are slowing down, do you want to stop?");
						}
					}
				}
			}
		};

		// tts create code
		textToSpeech = new TextToSpeech(this, this);
		textToSpeech.setLanguage(Locale.US);
		textToSpeech.setPitch(8 / 10);
		textToSpeech.setSpeechRate(15 / 12);

		mHandler.postDelayed(new Runnable() {
			public void run() {
				startUpVoiceOver();
			}
		}, 2000);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (location != null)
			location.onResume();

		// sensorManager.onResume();
		// mSensorManager.registerListener(accListener, acc,
		// SensorManager.SENSOR_DELAY_NORMAL);
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (location != null)
			location.onPause();

		// sensorManager.onPause();
		// mSensorManager.unregisterListener(accListener, acc);

		Log.d(TAG, "onPause()");
		if ((isVoice == true) && (textToSpeech.isSpeaking() == true)) {
			disableVoiceEngine();
			textToSpeech.stop();
		}
	}

	@Override
	public void onInit(int status) {
		Log.e(TAG, "ONINIT(): Text-to-speech initialized!");

		if (status == TextToSpeech.SUCCESS) {
			Log.i(TAG, "tts success");
			textToSpeech
					.setOnUtteranceProgressListener(new UtteranceProgressListener() {

						@Override
						public void onStart(String utteranceId) {
							// TODO Auto-generated method stub
							Log.d(TAG, "onStart(String utteranceId): "
									+ utteranceId);
						}

						@Override
						public void onError(String utteranceId) {
							// TODO Auto-generated method stub
							Log.e(TAG, "onError(String utteranceId): "
									+ utteranceId);
						}

						@Override
						public void onDone(String utteranceId) {
							// TODO Auto-generated method stub
							Log.d(TAG, "onDone(String utteranceId): "
									+ utteranceId);

							startVoiceRecognition();

							if (isVoice == true) {
								isConnected = hasInternetAccess();
								if (isConnected == true) {
									/*
									 * while in communication mode, do not allow
									 * voice recognition if face detection is
									 * active
									 */
									// stops the voice engine from starting when
									// the onDone is called

									// start voice after TTS is done
									// if
									// (VoiceEngineHelper.getVoiceController()
									// == false) {
									// Log.d(TAG, "Starting Voice_Engine");
									// Intent intent = new Intent(
									// MainActivity.this,
									// Voice_Engine.class);
									// startActivityForResult(intent,
									// VOICE_RECOGNITION_REQUEST_CODE);
									// }
								}
							}
						}
					}); // end OnUtteranceProgressListener()

			map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "UniqueID");

			/*
			 * booleans assigned from sharedPreferences are used to setup
			 * different messages depending upon the options the user has
			 * saved/setup
			 */
		} else if (status == TextToSpeech.ERROR) {
			// initialization of TTS failed so reinitialize new TTS Engine
			Log.e(TAG, "TextToSpeech ERROR");
			textToSpeech = new TextToSpeech(MainActivity.this, this);
			textToSpeech.setLanguage(Locale.US);
			textToSpeech.setPitch(8 / 10);
			textToSpeech.setSpeechRate(15 / 12);
		}

	} // end onInit() method

	// Voice Recognition Function
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i(TAG, "requestCode: " + requestCode + "\nresultCode: "
				+ resultCode + "\ndata: " + data);

		if (resultCode != RESULT_CANCELED) {
			if (Voice_Engine.singletonVE != null) {
				try {
					Log.e(TAG, "Voice_Engine class force closed");
					Voice_Engine.singletonVE.finish();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			if (requestCode == VOICE_RECOGNITION_REQUEST_CODE) {
				Log.d(TAG, "start checking matches");
				ArrayList<String> matches = data
						.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
				int listSize = matches.size();

				// compile list of recognized words by Voice_Engine class
				for (int i = 0; i < listSize; i++) {
					Log.i(TAG, "Recognized words: " + matches.get(i));
				}

				
				if (matches.contains("options") || matches.contains("command")
						|| matches.contains("option")
						|| matches.contains("auctions")
						|| matches.contains("auction")
						|| matches.contains("action")
						|| matches.contains("auctions")) {
					Log.i(TAG, "options");
				} else if (matches.contains("start run")
						|| matches.contains("start")
						|| matches.contains("start to run")
						|| matches.contains("start runs")) {
					Log.i(TAG, "start run");
					startTrackingMilieage();
				} else if (matches.contains("pause run")
						|| matches.contains("pause")
						|| matches.contains("paws run")
						|| matches.contains("passe run")) {
					Log.i(TAG, "pause run");
				} else if (matches.contains("stop")
						|| matches.contains("stop run")
						|| matches.contains("stop the run")
						|| matches.contains("stop running")) {
					Log.i(TAG, "stop run");
					stopTrackingMilieage();
				} else if (matches.contains("end run")
						|| matches.contains("and runs")
						|| matches.contains("end runs")
						|| matches.contains("and runs")
						|| matches.contains("end")) {
					Log.i(TAG, "end run");
				} else if (matches.contains("yes")) {
					Log.i(TAG, "yes");
					if(isAskingEndRun) {
						endTime = System.currentTimeMillis();
						location.clearBuffer();
						textToSpeachEndRun();
						stopTrackingMilieage();
						isAskingShareFacebook = true;
						isAskingEndRun = false;
					}
					else if(isAskingShareFacebook) {
						sendFacebookMessage();
						isAskingShareFacebook = false;
						promptForCommand();
					}

				} else if (matches.contains("no")) {
					Log.i(TAG, "no");
					isAskingEndRun = false;
					lastStepTakenAt = System.currentTimeMillis();
					if(isAskingShareFacebook) {
						promptForCommand();
						isAskingShareFacebook = false;
					}
				}
				else if (matches.contains("exit")
						|| matches.contains("except")
						|| matches.contains("quit")) {
					finish();
				}
				
				else {
					Log.i(TAG, "nothing capture, starting again");
					mHandler.postDelayed(new Runnable() {
						public void run() {
							Log.i(TAG, "handler called");
							enableVoiceEngine();
							startVoiceRecognition();
						}
					}, 2000);
				}
				// Log.i(TAG, "nothing capture, starting again");
				// mHandler.postDelayed(new Runnable() {
				// public void run() {
				// Log.i(TAG, "handler called");
				// enableVoiceEngine();
				// startVoiceRecognition();
				// }
				// }, 2000);
			}
		} else {
			Log.i(TAG, "VR == canceled");
			// mHandler.postDelayed(new Runnable() {
			// public void run() {
			// Log.i(TAG, "handler in VR == canceled called");
			// enableVoiceEngine();
			// startVoiceRecognition();
			// }
			// }, 2000);
		}
	}

	private String checkForAchievements() {
		StringBuilder builder = new StringBuilder();
		double distance = Double.parseDouble(distanceTraveled);
		
		prefHelper.clearPreferences(this);
		
		if(distance > Achievements.MILESTONE_ONE) {
		//if(true) {
			if(!prefHelper.hasAchievement(Achievements.DISTANCE_ONE)) {
				prefHelper.addAchievement(Achievements.DISTANCE_ONE, true);
				builder.append(""+Achievements.DISTANCE_ONE_DESCRIPTION+" achievement unlocked   ");
			}
		}
		
		if(distance > Achievements.MILESTONE_TWO) {
		//if(true) {
			if(!prefHelper.hasAchievement(Achievements.DISTANCE_TWO)) {
				prefHelper.addAchievement(Achievements.DISTANCE_TWO, true);
				builder.append(Achievements.DISTANCE_TWO_DESCRIPTION+" achievement unlocked");
			}
		}
		
		if(distance > Achievements.MILESTONE_THREE) {
			if(!prefHelper.hasAchievement(Achievements.DISTANCE_THREE)) {
				prefHelper.addAchievement(Achievements.DISTANCE_THREE, true);
				builder.append(Achievements.DISTANCE_THREE_DESCRIPTION+" achievement unlocked");
			}
		}
		
		if(distance > Achievements.MILESTONE_FOUR) {
			if(!prefHelper.hasAchievement(Achievements.DISTANCE_FOUR)) {
				prefHelper.addAchievement(Achievements.DISTANCE_FOUR, true);
				builder.append(Achievements.DISTANCE_FOUR_DESCRIPTION+" achievement unlocked");
			}
		}
		
		if(distance > Achievements.MILESTONE_DEMO_ONE) {
			if(!prefHelper.hasAchievement(Achievements.DISTANCE_DEMO_ONE)) {
				prefHelper.addAchievement(Achievements.DISTANCE_DEMO_ONE, true);
				speakText(Achievements.DISTANCE_DEMO_ONE_DESCRIPTION+" achievement unlocked");
			}
		}
		
		if(distance > Achievements.MILESTONE_DEMO_TWO) {
			if(!prefHelper.hasAchievement(Achievements.DISTANCE_DEMO_TWO)) {
				prefHelper.addAchievement(Achievements.DISTANCE_DEMO_TWO, true);
				speakText(Achievements.DISTANCE_DEMO_TWO_DESCRIPTION+" achievement unlocked");
			}
		}
		return builder.toString();
	}

	private void textToSpeachEndRun() {
		distanceTraveled = df.format(location.getDistanceTraveled());
		long timeOfRunInMilli = endTime - startTime;
		long totalHours = TimeUnit.MILLISECONDS.toHours(timeOfRunInMilli);
		long totalMinutes = TimeUnit.MILLISECONDS.toMinutes(timeOfRunInMilli);
		long totalSeconds = TimeUnit.MILLISECONDS.toSeconds(timeOfRunInMilli);
		if(totalHours != 0) {
			if(totalHours == 1) {
				timeOfRun = totalHours + " hour ";
			}
			else {
				timeOfRun = totalHours + " hours ";
			}
			timeOfRun = timeOfRun + ", " + totalMinutes + " minutes and " + totalSeconds + " seconds";
		}
		else {
			timeOfRun = ", " + totalMinutes + " minutes and " + totalSeconds + " seconds";
		}
		String achievementsString = checkForAchievements();
		speakText("You have run " + distanceTraveled + " meters in " + timeOfRun + achievementsString + "Would you like to share results to a friend on FaceBook?");
		
	}

	private void sendFacebookMessage() {
		Intent facebookIntent = getShareIntent("facebook", "RunFit Assist",
				"RunFit Assist has helped me run " + distanceTraveled + " meters in " + 
						timeOfRun);
		startActivity(facebookIntent);

	}

	private void disableVoiceEngine() {
		try {
			VoiceEngineHelper.setVoiceController(true);
		} catch (Exception e) {
			Log.e(TAG, "disable voice engine error");
		}
	}

	/**
	 * Enables the VoiceEngine. When
	 * VoiceEngineHelper.setVoiceController(false), voice engine is active,
	 * otherwise inactive
	 */
	private void enableVoiceEngine() {
		Log.e(TAG, "enable voice engine new helper method");
		VoiceEngineHelper.setVoiceController(false);
	}

	/**
	 * Called after onCreate(Bundle) � or after onRestart() when the activity
	 * had been stopped, but is now again being displayed to the user. It will
	 * be followed by onResume().
	 */
	@Override
	public void onStart() {

		if (textToSpeech == null) {
			textToSpeech = new TextToSpeech(MainActivity.this, this);
			textToSpeech.setLanguage(Locale.US);
			textToSpeech.setPitch(8 / 10);
			textToSpeech.setSpeechRate(15 / 12);
		} else {
			if (isVoice == true) {
				// if(isOptionController == true ){
				// isOptionController = false;
				// }
				if (VoiceEngineHelper.getVoiceController() == true) {
					enableVoiceEngine();
				}

			}
		}
		super.onStart();
	}

	/**
	 * Called when you are no longer visible to the user.
	 */
	@Override
	public void onStop() {
		if ((isVoice == true) && (textToSpeech.isSpeaking() == true)) {
			disableVoiceEngine();
			textToSpeech.stop();
		}
		if (textToSpeech != null) {
			textToSpeech.stop();
			textToSpeech.shutdown();
			textToSpeech = null;
		}
		super.onStop();

	}

	/**
	 * Called by the system to remove the Service when it is no longer used.
	 * Ends textToSpeech and Voice_Engine, as well as calling Activity's
	 * onDestroy(). The service should clean up any resources it holds (threads,
	 * registered receivers, etc) at this point. Upon return, there will be no
	 * more calls in to this Service object and it is effectively dead. Do not
	 * call this method directly.
	 */
	@Override
	public void onDestroy() {
		disableVoiceEngine();
		if (textToSpeech != null) {
			textToSpeech.stop();
			textToSpeech.shutdown();
			textToSpeech = null;
		}
		super.onDestroy();
	}

	@SuppressWarnings("deprecation")
	public static void speakText(String text) {
		if(textToSpeech == null)
			return;
		
		if (textToSpeech.isSpeaking()) {
			Log.i(TAG, "tts is speaking");
			textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, map);
			return;
		} else {
			Log.i(TAG, "else");
			textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, map);
		}
	}

	public void startTrackingMilieage() {
		hasStartedRunning = false;
		
		// set up location tracking
		TextView latituteField = (TextView) findViewById(R.id.latitudevalue);
		TextView longitudeField = (TextView) findViewById(R.id.longitudevalue);
		location = new GPS(this, latituteField, longitudeField);

		mSensorManager.registerListener(accListener, acc,
				SensorManager.SENSOR_DELAY_NORMAL);

	}

	public void stopTrackingMilieage() {
		if (location != null)
			location.stopMilieage();
		location.onPause();
		location = null;

		mSensorManager.unregisterListener(accListener, acc);
	}

	public void startVoiceRecognition() {
		Log.d(TAG, "Starting Voice_Engine");
		Intent intent = new Intent(MainActivity.this, Voice_Engine.class);
		startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
	}

	public boolean hasInternetAccess() {
		ConnectivityManager cm = (ConnectivityManager) this
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
	}

	// user function
	private Intent getShareIntent(String type, String subject, String text) {
		boolean found = false;
		Intent share = new Intent(android.content.Intent.ACTION_SEND);
		share.setType("text/plain");

		// gets the list of intents that can be loaded.
		List<ResolveInfo> resInfo = this.getPackageManager()
				.queryIntentActivities(share, 0);
		System.out.println("resinfo: " + resInfo);
		if (!resInfo.isEmpty()) {
			for (ResolveInfo info : resInfo) {
				if (info.activityInfo.packageName.toLowerCase().contains(type)
						|| info.activityInfo.name.toLowerCase().contains(type)) {
					share.putExtra(Intent.EXTRA_SUBJECT, subject);
					share.putExtra(Intent.EXTRA_TEXT, text);
					share.setPackage(info.activityInfo.packageName);
					found = true;
					break;
				}
			}
			if (!found)
				return null;
			return share;
		}
		return null;
	} // end getShareIntent()
	
	public void startUpVoiceOver() {
		speakText("Welcome to Run Fit Assist, please say a command, for a list of all options, say commands");
		//speakText("hi");
	}
	
	public void promptForCommand() {
		speakText("Please say a command, for a list of all options, say commands");
	}

}
