package com.nawin.myapplication;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.view.View.OnClickListener;
import android.widget.Toast;


import android.os.Bundle;

import java.util.GregorianCalendar;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 1;

    private PendingIntent pendingIntent;
    GregorianCalendar calendar;
    Button b1;
    private Handler handler = new Handler() {
        public void handleMessage(Message message) {
            Object path = message.obj;
            if (message.arg1 == RESULT_OK && path != null) {
                Toast.makeText(MainActivity.this,
                        "Downloaded" + path.toString(), Toast.LENGTH_LONG)
                        .show();
                readMessage(path.toString());

            } else {
                Toast.makeText(MainActivity.this, "Download failed.",
                        Toast.LENGTH_LONG).show();
            }

        };
    };
    AlarmManager alarmManager;
    TextView messageText;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        messageText = (TextView) findViewById(R.id.textView);
        b1 = (Button) findViewById(R.id.button1);
        readMessage("/storage/sdcard/message.json");
        b1.setOnClickListener(new OnClickListener() {

            private boolean flag = true;

            @Override
            public void onClick(View v) {
                if(checkPermission())
                {
                    if (flag) {
                        flag = false;
                        b1.setText("Stop update");
                        alarmManager.setRepeating(AlarmManager.RTC,
                                calendar.getTimeInMillis(), 5000, pendingIntent);

                    } else {
                        flag = true;
                        b1.setText("Start update");

                        alarmManager.cancel(pendingIntent);

                    }
                }
                else {
                    Toast.makeText(MainActivity.this, "Permission denied", Toast.LENGTH_SHORT).show();
                }

            }
        });
        calendar = (GregorianCalendar) Calendar.getInstance();
        Intent myIntent = new Intent(MainActivity.this, UpdatingService.class);
        Messenger messenger = new Messenger(handler);
        myIntent.putExtra("MESSENGER", messenger);
        myIntent.setData(Uri
                .parse("http://10.0.2.2/message/message.json"));// nawinsandroidtutorial.site90.com/message
        myIntent.putExtra("urlpath",
                "http://10.0.2.2/message/message.json");
        pendingIntent = PendingIntent.getService(MainActivity.this, 0,
                myIntent, 0);

        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("per","Permission is granted1");

            } else {

               requestPermission();
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v("per","Permission is granted1");

        }

   //alarmManager.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(),
  //       15000, pendingIntent);



    } // end onCreate
    public boolean checkPermission() {

        int CallPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);


        return CallPermissionResult == PackageManager.PERMISSION_GRANTED;

    }

    private void requestPermission() {

        ActivityCompat.requestPermissions(MainActivity.this, new String[]
                {
                        Manifest.permission.WRITE_EXTERNAL_STORAGE

                }, PERMISSION_REQUEST_CODE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {

            case PERMISSION_REQUEST_CODE:


                if (grantResults.length > 0) {

                    boolean CallPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean ReadContactsPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (CallPermission && ReadContactsPermission) {

                        Toast.makeText(MainActivity.this,
                                "Permission accepted", Toast.LENGTH_LONG).show();
                        b1.setEnabled(true);
//If permission is denied...//

                    } else {
                        Toast.makeText(MainActivity.this,
                                "Permission denied", Toast.LENGTH_LONG).show();

//....disable the Call and Contacts buttons//

                        b1.setEnabled(false);

                    }
                    break;
                }
        }
    }


    void readMessage(String path) {
        File myFile = new File(path.toString());// Environment.getExternalStorageDirectory().getPath()+"/message.json");
        FileInputStream fIn;
        try {
            fIn = new FileInputStream(myFile);
            BufferedReader myReader = new BufferedReader(new InputStreamReader(
                    fIn));
            String aDataRow = "";
            String aBuffer = "";

            while ((aDataRow = myReader.readLine()) != null) {
                aBuffer += aDataRow;

            }
            myReader.close();
            try {
                JSONObject jObj = new JSONObject(aBuffer);
                aBuffer = jObj.getString("message");
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            messageText.setText(aBuffer);
        } catch (FileNotFoundException e) {
            messageText.setText("No file");
            e.printStackTrace();
        } catch (IOException e) {
            messageText.setText("IO Error");
            e.printStackTrace();
        }
    }


}
