package com.example.acctest;

import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener {

	final String LOG_TAG = "myLogs";

	Button mServiceStartButton;
	Button mServiceStopButton;
	Button mShowButton;

	TextView mTimerValueText;
	TextView mXValueText;
	TextView mYValueText;
	TextView mZValueText;
	TextView mID;
	
	BroadcastReceiver br;
	public final static String BROADCAST_ACTION = "com.example.acctest";
	
	DBHelper myDBHelper;
	ContentValues myCV = new ContentValues();

		/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		Log.d(LOG_TAG, "MainActivity: onCreate()");

		// создаем объект для создания и управления версиями БД
		myDBHelper = new DBHelper(this);

		mTimerValueText = (TextView) findViewById(R.id.value_timer);
		mXValueText = (TextView) findViewById(R.id.value_x);
		mYValueText = (TextView) findViewById(R.id.value_y);
		mZValueText = (TextView) findViewById(R.id.value_z);
		mID = (TextView) findViewById(R.id.value_id);
		
		mServiceStartButton = (Button) findViewById(R.id.button_startService);
		mServiceStopButton = (Button) findViewById(R.id.button_stopService);
		mShowButton = (Button) findViewById(R.id.button_show);
				
		mServiceStartButton.setOnClickListener(this);
		mServiceStopButton.setOnClickListener(this);
		mShowButton.setOnClickListener(this);		
		
		// создаем BroadcastReceiver
	    br = new BroadcastReceiver(){
	    	public void onReceive(Context context, Intent intent){
	    		int id = intent.getIntExtra("id", 0);
	    		String idS = String.valueOf(id); 
	    			    		
	    		mXValueText.setText("axisX: " + String.format("%1.3f", intent.getFloatExtra("axisX", 0)));
	    		mYValueText.setText("axisY: " + String.format("%1.3f", intent.getFloatExtra("axisY", 0)));
	    		mZValueText.setText("axisZ: " + String.format("%1.3f", intent.getFloatExtra("axisZ", 0)));	    		
	    		mTimerValueText.setText("timer: " + String.format("%1.0f", intent.getFloatExtra("timer", 0)));	    		
	    		mID.setText("id: "+ idS);
	    	}	    	
	    };
	    
	    // создаем фильтр для BroadcastReceiver
	    IntentFilter intFilt = new IntentFilter(BROADCAST_ACTION);
	    // регистрируем (включаем) BroadcastReceiver
	    registerReceiver(br, intFilt);
	}

	@Override
	protected void onPause() {		
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(LOG_TAG, "MainActivity: регистрируем сенсор");		
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(LOG_TAG, "MainActivity: onDestroy()");
		// дерегистрируем (выключаем) BroadcastReceiver
	    unregisterReceiver(br);
	}

	class DBHelper extends SQLiteOpenHelper {

		public DBHelper(Context context) {
			// конструктор суперкласса
			super(context, "myDBAccel", null, 1);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.d(LOG_TAG, "MainActivity: --- onCreate database myDBAccel ---");
			// создаем таблицу mytable с полями - названиями осей и таймером
			db.execSQL("create table mytable ("
					+ "id integer primary key autoincrement," + "axisX float,"
					+ "axisY float," + "axisZ float," + "timer float" + ");");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		}

	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.button_startService:
			startService(new Intent(this, MyService.class));
			break;

		case R.id.button_stopService:
			stopService(new Intent(this, MyService.class));
			break;

		case R.id.button_show:
			Log.d(LOG_TAG, "MainActivity: --- Rows in mytable: ---");

			// подключаемся к БД
			SQLiteDatabase myDBAcc = myDBHelper.getWritableDatabase();

			// делаем запрос всех данных из таблицы mytable, получаем Cursor
			Cursor c = myDBAcc.query("mytable", null, null, null, null, null,
					null);

			// ставим позицию курсора на первую строку выборки
			// если в выборке нет строк, вернется false
			if (c.moveToFirst()) {
				// определяем номера столбцов по имени в выборке
				int idColIndex = c.getColumnIndex("id");
				int axisXColIndex = c.getColumnIndex("axisX");
				int axisYColIndex = c.getColumnIndex("axisY");
				int axisZColIndex = c.getColumnIndex("axisZ");
				int timerColIndex = c.getColumnIndex("timer");

				do {
					// получаем значения по номерам столбцов и пишем все в лог
					Log.d(LOG_TAG, "ID = " + c.getInt(idColIndex)
							+ ", axisX = " + c.getFloat(axisXColIndex)
							+ ", axisY = " + c.getFloat(axisYColIndex)
							+ ", axisZ = " + c.getFloat(axisZColIndex)
							+ ", timer = " + c.getFloat(timerColIndex));
					// переход на следующую строку
					// а если следующей нет (текущая - последняя), то false -
					// выходим из цикла
				} while (c.moveToNext());
			} else
				Log.d(LOG_TAG, "0 rows");
			c.close();

			break;
		}
	}

}
