package com.happy.happywidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * Created by sto on 7/1/15.
 */
public class HappyWidgetProvider extends AppWidgetProvider {
    private static final String MyOnClick1 = "myOnClickTag";
    LocationManager locationManager;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        final int N = appWidgetIds.length;

        for (int i = 0; i < N; i++) {
            int appWidgetId = appWidgetIds[i];

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.hellowidget_layout);

            remoteViews.setOnClickPendingIntent(R.id.button_send, getPendingSelfIntent(context, MyOnClick1));
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }
    }

    protected PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Toast.makeText(context, "Button1", Toast.LENGTH_SHORT).show();
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    URL url = new URL("http://172.16.0.34:3000/happinesses");

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setDoInput(true);
                    OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                    Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    String loc = "{\"loc\": [" + longitude + "," + latitude + "], \"level\": 1}";
                    writer.write(loc);
                    Log.d("Foo", loc);

                    writer.flush();

                    String line;
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    while ((line = reader.readLine()) != null) {
                        Log.d("Foo", line);
                    }
                    writer.close();
                    reader.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        };
        t.start();
    }
}
