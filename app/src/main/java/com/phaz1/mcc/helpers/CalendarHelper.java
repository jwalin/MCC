package com.phaz1.mcc.helpers;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Random;

/**
 * Created by altaf on 08/05/2017.
 */

public class CalendarHelper {
    public static long addToDeviceCalendar(Activity activity, long sDate, long endDate, String title,
                                           String desc, String rrule, int selectedReminderValue) {
        try {
            ContentResolver cr = activity.getContentResolver();
            ContentValues values = new ContentValues();
            values.put(CalendarContract.Events.DTSTART, sDate);
            if (endDate == 0)
                values.put(CalendarContract.Events.DTEND, sDate + 300000);
            else
                values.put(CalendarContract.Events.DTEND, endDate);
            values.put(CalendarContract.Events.TITLE, title);
            values.put(CalendarContract.Events.DESCRIPTION, desc);
            values.put(CalendarContract.Events.RRULE, rrule);
            values.put(CalendarContract.Events.CALENDAR_ID, 1);
            values.put(CalendarContract.Events.EVENT_COLOR, Color.parseColor("#87ceeb"));
            //values.put(CalendarContract.Calendars.OWNER_ACCOUNT, true);
            values.put(CalendarContract.Events.HAS_ATTENDEE_DATA, 1);

            //String calendarOwner = calendarCursor.getString(calendarCursor.getColumnIndex(CalendarContract.Calendars.OWNER_ACCOUNT));

            //values.put(Events._ID, 900054);
            values.put(CalendarContract.Events.HAS_ALARM, 1);

            values.put(CalendarContract.Events.EVENT_TIMEZONE, Calendar.getInstance().getTimeZone().getID());
            int callbackId = 42;
            String[] permissions = new String[]{Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR, Manifest.permission.CALL_PHONE};
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_CALENDAR) !=
                    PackageManager.PERMISSION_GRANTED) {
                return 0;
            }
            Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
            long eventId = Long.parseLong(uri.getLastPathSegment());
            ContentValues reminders = new ContentValues();
            reminders.put(CalendarContract.Reminders.EVENT_ID, eventId);
            reminders.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
            reminders.put(CalendarContract.Reminders.MINUTES, selectedReminderValue);
            Uri ri = cr.insert(CalendarContract.Reminders.CONTENT_URI, reminders);
            Log.d("FAI",uri.toString());
            return Long.parseLong(ri.getLastPathSegment());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }


    public static AlertDialog.Builder showDialog(Context ctx, String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle(title).setMessage(message);
        return builder;
    }

    public static long getRandomDate(long min, long max) {
        long diff = max - min;
        Random rand = new Random();
        long t = (long) (min + (rand.nextFloat() * diff));
        long randomTime = new Timestamp(t).getTime();
        return randomTime;
    }
}