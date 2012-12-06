package com.example.acctest;

import java.util.List;

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;
//import com.example.servicebackpendingintent.MainActivity;
//import com.example.servicebackpendingintent.MyService;

public class MyService extends Service implements SensorEventListener {
	final String LOG_TAG = "myLogs";

	SensorManager mSensorManager;
	Sensor mAccelerometerSensor;

	float axisX = 0;
	float axisY = 0;
	float axisZ = 0;

	long timeStart = 0;
	long timeCurrent = 0;
	float timer;

	DBHelper myDBHelper;
	ContentValues myCV = new ContentValues();

	public void onCreate() {
		super.onCreate();
		Log.d(LOG_TAG, "MyService: onCreate()");

		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		timeStart = System.currentTimeMillis();
		// объект для создания и управления версиями БД
		myDBHelper = new DBHelper(this);

		List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
		if (sensors.size() > 0) {
			for (Sensor sensor : sensors) {
				switch (sensor.getType()) {
				case Sensor.TYPE_ACCELEROMETER:

					if (mAccelerometerSensor == null)
						mAccelerometerSensor = sensor;
					Log.d(LOG_TAG, "MyService: находим сенсор");
					break;
				default:
					break;
				}
			}
		}

		mSensorManager.registerListener(this, mAccelerometerSensor,
				SensorManager.SENSOR_DELAY_NORMAL);
	}

	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(LOG_TAG, "MyService: onStartCommand()");
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public void onDestroy() {
		super.onDestroy();
		Log.d(LOG_TAG, "MyService: onDestroy()");
		mSensorManager.unregisterListener(this);
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		Intent intent = new Intent("com.example.acctest");
		switch (event.sensor.getType()) {
		case Sensor.TYPE_ACCELEROMETER: {
			// получаем данные из сенсора
			axisX = event.values[SensorManager.DATA_X];
			axisY = event.values[SensorManager.DATA_Y];
			axisZ = event.values[SensorManager.DATA_Z];

			// устанавливаем счетчик времени
			timeCurrent = System.currentTimeMillis();
			timer = (timeCurrent - timeStart);

			// подключаемся к БД
			SQLiteDatabase myDBAcc = myDBHelper.getWritableDatabase();
			// подготовим данные для вставки в виде пар: наименование столбца -
			// значение
			myCV.put("axisX", axisX);
			myCV.put("axisY", axisY);
			myCV.put("axisZ", axisZ);
			myCV.put("timer", timer);

			// вставляем запись и получаем ее ID
			long rowID = myDBAcc.insert("mytable", null, myCV);
			int mID = (int) rowID; 
			intent.putExtra("id", mID);
			intent.putExtra("axisX", axisX);
			intent.putExtra("axisY", axisY);
			intent.putExtra("axisZ", axisZ);
			intent.putExtra("timer", timer);
			
			sendBroadcast(intent);
			//Log.d(LOG_TAG, "MyService: row inserted, ID = " + rowID);			
			
			// отключаемся от БД
			myDBAcc.close();
			
			
		}
			break;
		}

	}

	class DBHelper extends SQLiteOpenHelper {

		public DBHelper(Context context) {
			// конструктор суперкласса
			super(context, "myDBAccel", null, 1);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.d(LOG_TAG, "MyService: --- onCreate database myDBAccel ---");
			// создаем таблицу mytable с полями
			db.execSQL("create table mytable ("
					+ "id integer primary key autoincrement," + "axisX float,"
					+ "axisY float," + "axisZ float," + "timer float" + ");");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		}

	}

}
