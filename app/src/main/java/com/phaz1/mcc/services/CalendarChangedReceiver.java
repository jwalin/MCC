package com.phaz1.mcc.services;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;

import com.phaz1.mcc.activities.TabActivity;

/**
 * Created by altaf on 08/05/2017.
 */

public class CalendarChangedReceiver extends BroadcastReceiver {
    public CalendarChangedReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        Uri uri = intent.getData();
        ///Alert OK or Cancel

        String alertTime = uri.getLastPathSegment();
        String selection = CalendarContract.CalendarAlerts.ALARM_TIME + "=?";

        Cursor cursor = context.getContentResolver().query(CalendarContract.CalendarAlerts.CONTENT_URI_BY_INSTANCE,
                new String[]{CalendarContract.CalendarAlerts.EVENT_ID}, selection, new String[]{alertTime}, null);
        int eventId = 0;
        while (cursor.moveToNext()) {
            eventId = cursor.getInt(0);
        }
        //Toast.makeText(context,readCalendar(context,eventId),Toast.LENGTH_SHORT).show();
        String description = readCalendar(context, eventId);
        Intent notifyIntent = new Intent(context, TabActivity.class);
        notifyIntent.putExtra("description", description);
        if (description.contains("Forget About It")) {
            notifyIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(notifyIntent);
        }

        //String str = readCalendar(context,eventId);
    }

    String readCalendar(Context context, int id) {
        Cursor cur = null;
        ContentResolver cr = context.getContentResolver();
        String[] mProjection =
                {
                        "_id",
                        CalendarContract.Events.TITLE,
                        CalendarContract.Events.DESCRIPTION,
                        CalendarContract.Events.DTSTART,
                        CalendarContract.Events.DTEND,
                };

        String selection = CalendarContract.Events._ID + " = ? ";
        String[] selectionArgs = new String[]{"" + id};

        Uri uri = CalendarContract.Events.CONTENT_URI;
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        cur = cr.query(uri, mProjection, selection, selectionArgs, null);
        while (cur.moveToNext()) {
            String title = cur.getString(cur.getColumnIndex(CalendarContract.Events.DESCRIPTION));
            return title;
        }
        return selection;
    }
}