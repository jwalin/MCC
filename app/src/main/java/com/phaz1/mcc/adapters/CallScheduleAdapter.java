package com.phaz1.mcc.adapters;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.orm.query.Select;

import com.phaz1.mcc.AppMsg;
import com.phaz1.mcc.R;
import com.phaz1.mcc.activities.TabActivity;
import com.phaz1.mcc.datalayer.CallTemplate;
import com.phaz1.mcc.helpers.CalendarHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import me.everything.providers.android.calendar.Calendar;
import me.everything.providers.android.calendar.CalendarProvider;
import me.everything.providers.android.calendar.Event;

/**
 * Created by altaf on 03/05/2017.
 */

public class CallScheduleAdapter extends BaseAdapter {
    public final TabActivity context;
    private final LayoutInflater inflator;
    List<CallTemplate> templates;
    int complete;

    public CallScheduleAdapter(TabActivity context, List<CallTemplate> templates, int complete) {
        inflator = context.getLayoutInflater();
        this.templates = templates;
        this.context = context;
        this.complete = complete;
        //R.layout.activity_schedule_list_adapter
    }

    @Override
    public int getCount() {
        return templates.size();
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        final CallTemplate template = templates.get(i);
        view = inflator.inflate(R.layout.activity_schedule_list_adapter, null);
        if (complete != 0) {
            view.findViewById(R.id.backgroundCell).setBackgroundColor(Color.parseColor("#D3D3D3"));
            view.findViewById(R.id.timely).setBackgroundColor(Color.parseColor("#939393"));
        }
        TextView titleTextView = (TextView) view.findViewById(R.id.textViewPhoneNumber);
        titleTextView.setText(template.getContactPhoneNumber());

        TextView descriptionTextView = (TextView) view.findViewById(R.id.textViewDescription);
        descriptionTextView.setText(template.getTitle());

        //MM/dd/yyyy HH:mm
        TextView textViewDate = (TextView) view.findViewById(R.id.textViewDate);
        textViewDate.setText(getFormattedDate(template.getStartDate(), "MM/dd/yy"));

        TextView textViewTime = (TextView) view.findViewById(R.id.textViewTime);
        textViewTime.setText(getFormattedDate(template.getStartDate(), "HH:mm"));

        TextView textViewTypeRepeatBefore = (TextView) view.findViewById(R.id.textViewAdditionalDesc);
        String rule = "";
        rule = template.getRecurringRule() != null ? template.getRecurringRule() : "No Repeat";
        String callBefore = "";
        if (template.getAlertBefore() > 60) {
            if (template.getAlertBefore() / 60 > 1)
                callBefore = template.getAlertBefore() / 60 + " hrs and " + template.getAlertBefore() % 60 + " mins " + context.getString(R.string.before_call);
            else
                callBefore = template.getAlertBefore() / 60 + " hr and " + template.getAlertBefore() % 60 + " mins " + context.getString(R.string.before_call);
            callBefore = template.getAlertBefore() / 60 + " hr/s and " + template.getAlertBefore() % 60 + " mins " + context.getString(R.string.before_call);
        } else if (template.getAlertBefore() > 0) {
            callBefore = template.getAlertBefore() + " mins " + context.getString(R.string.before_call);
        } else {
            callBefore = " Auto Dial";
        }

        textViewTypeRepeatBefore.setText(template.getType() + " : " + rule + " : " + callBefore);

        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                AlertDialog.Builder builder = CalendarHelper.showDialog(context, context.getString(R.string.app_name),
                        context.getString(R.string.want_delete) + " " + template.getTitle() + " ?");
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        delete(template.getId());
                    }
                }).setNegativeButton("Edit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        AppMsg.makeText(context, "Coming Soon, Stay Connected!!!", AppMsg.STYLE_INFO).show();
                    }
                }).setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                builder.create().show();
                return false;
            }
        });
        return view;
    }

    void delete(Long id) {
        CallTemplate book = CallTemplate.findById(CallTemplate.class, id);
        book.delete();
        templates = Select.from(CallTemplate.class)
//                .where(Condition.prop("complete").eq(complete))
                .list();
        notifyDataSetChanged();
        deleteEvent(id.toString());
    }

    public void deleteEvent(String id) {
        //get calendar id
        String description = "Forget About It : " + id;
        Cursor cursor = context.getContentResolver()
                .query(
                        Uri.parse("content://com.android.calendar/events"),
                        new String[]{"calendar_id", "title", "description",
                                "dtstart", "dtend", "eventLocation"}, null,
                        null, null);
        cursor.moveToFirst();
        // fetching calendars name
        String CNames[] = new String[cursor.getCount()];

        // fetching calendars id

        for (int i = 0; i < CNames.length; i++) {
            String d = cursor.getString(2);
            String c = cursor.getString(1);
            String b = cursor.getString(0);
            if (cursor
                    .getString(2) != null && cursor
                    .getString(2).contains(description)) {
                id = cursor.getString(0);
            }
            cursor.moveToNext();
        }
        CalendarProvider provider = new CalendarProvider(context);
        List<Calendar> calendars = provider.getCalendars().getList();
        for (Calendar c : calendars) {
            try {
                List<Event> events = provider.getEvents(c.id).getList();
                try {
                    for (Event ev : events) {
                        Log.d("FAI", ev.description + ":" + ev.displayName + " : " + description);
                        if (ev.description != null && ev.description.contains(description)) {
                            ev.deleted = true;
                            // provider.update(ev);
                            id = String.valueOf(ev.id);
                            break;
                        }
                    }
                } catch (Exception e) {

                }
            } catch (Exception e) {

            }
        }
        Uri uri = CalendarContract.Events.CONTENT_URI;
        String mSelectionClause = CalendarContract.Events._ID + " = ?";
        String[] mSelectionArgs = {id};

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        context.getContentResolver().delete(uri, mSelectionClause, mSelectionArgs);
    }

    String getFormattedDate(long date, String fmt) {
        SimpleDateFormat format = new SimpleDateFormat(fmt);
        Date startDate = new Date(date);
        return format.format(startDate);
    }
}