package com.phaz1.mcc.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;


import com.fastaccess.datetimepicker.DatePickerFragmentDialog;
import com.fastaccess.datetimepicker.DateTimeBuilder;
import com.fastaccess.datetimepicker.TimePickerFragmentDialog;
import com.fastaccess.datetimepicker.callback.DatePickerCallback;
import com.fastaccess.datetimepicker.callback.TimePickerCallback;

import com.phaz1.mcc.AppMsg;
import com.phaz1.mcc.R;
import com.phaz1.mcc.activities.TabActivity;
import com.phaz1.mcc.datalayer.CallTemplate;
import com.phaz1.mcc.datalayer.RandomAlarm;
import com.phaz1.mcc.helpers.CalendarHelper;
import com.phaz1.mcc.helpers.DurationBefore;
import com.phaz1.mcc.helpers.ObjectHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import be.billington.calendar.recurrencepicker.EventRecurrence;
import be.billington.calendar.recurrencepicker.EventRecurrenceFormatter;
import be.billington.calendar.recurrencepicker.RecurrencePickerDialog;

import static android.app.Activity.RESULT_OK;
import static android.app.AlertDialog.Builder;
import static android.content.Context.INPUT_METHOD_SERVICE;

/**
 * A simple {@link Fragment} subclass.
 */
public class CreateFragment extends Fragment implements DatePickerCallback, TimePickerCallback {
    public TabActivity activity;
    static public long startCallDate, endCallDate, startCallTime, endCallTime = 0;
    Button datePickerStartDateButton, datePickerEndDateButton, buttonSave, btnSTime, btnETime, buttonRepeat;
    static boolean isEndCall, isRandom;
    RadioButton radioButtonCallBefore, radioButtonAutoCall;
    String rrule;
    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
    private EditText txtContactNo, txtTitle;
    private int REQUEST_CODE = 1245;
    private RadioGroup randomGroup;
    private int minuteBefore = 0;
    String repeatString;

    public CreateFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (TabActivity) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_create, container, false);
        datePickerStartDateButton = (Button) view.findViewById(R.id.btnStartDate);
        datePickerEndDateButton = (Button) view.findViewById(R.id.btnEndDate);
        btnSTime = (Button) view.findViewById(R.id.btnSTime);
        btnETime = (Button) view.findViewById(R.id.btnETime);
        buttonSave = (Button) view.findViewById(R.id.btnSave);
        buttonRepeat = (Button) view.findViewById(R.id.btnRepeat);
        txtContactNo = (EditText) view.findViewById(R.id.txtContactNo);
        txtTitle = (EditText) view.findViewById(R.id.txtTitle);
        randomGroup = (RadioGroup) view.findViewById(R.id.randomGroup);


        buttonRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideSoftKeyboard();
                final RecurrencePickerDialog rDialog = new RecurrencePickerDialog();
                rDialog.setOnRecurrenceSetListener(new RecurrencePickerDialog.OnRecurrenceSetListener() {
                    @Override
                    public void onRecurrenceSet(String rrule) {
                        try {
                            CreateFragment.this.rrule = rrule;
                            EventRecurrence mRecurrence = new EventRecurrence();
                            mRecurrence.parse(rrule);

                            if (!TextUtils.isEmpty(rrule)) {
                                repeatString = EventRecurrenceFormatter.getRepeatString(activity, activity.getResources(),
                                        mRecurrence, true);
                                buttonRepeat.setText(repeatString);
                            }
                        } catch (Exception ex) {
                        }
                    }
                });
                rDialog.show(getChildFragmentManager(), "recurrencePicker");
            }
        });

//        startCallDate = new Date().getTime();
//        endCallDate = new Date().getTime();

        datePickerStartDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideSoftKeyboard();
                CreateFragment.isEndCall = false;
                showPicker(startCallDate);
            }
        });

        datePickerEndDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideSoftKeyboard();
                CreateFragment.isEndCall = true;
                showPicker(endCallDate);
            }
        });

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String contactNo = txtContactNo.getText().toString();
                String title = txtTitle.getText().toString();
                if ((contactNo.length() == 0) || (title.length() == 0)) {
//            Builder builder = new Builder(getContext());
//            builder.setTitle(getString(R.string.app_name));
//            builder.setMessage(R.string.num_title_req);
//            builder.setPositiveButton(getString(R.string.ok), null);
//            builder.create().show();
                    AppMsg.makeText(getActivity(), getString(R.string.num_title_req), AppMsg.STYLE_ALERT).show();
                    return;
                }
                if (txtContactNo.getText().toString().length() < 10) {
                    AppMsg.makeText(getActivity(), "Length of contact number should be greater than 10 digits !!!", AppMsg.STYLE_ALERT).show();
                    return;
                }
                if (startCallDate + startCallTime < new Date().getTime()) {
                    AppMsg.makeText(getActivity(), "Past Date/Time is not allowed !!!", AppMsg.STYLE_ALERT).show();
                    return;
                }
                Long eventId = saveEvent();
                if (eventId == null)
                    return;
                String desc = getString(R.string.app_name) + " : " + eventId + " - " + title;
                if (isRandom) {
                    long randomDate = CalendarHelper.getRandomDate(startCallDate, endCallDate);
                    try {
                        String minTime = btnSTime.getText().toString();
                        String maxTime = btnETime.getText().toString();
                        long randomStartTime = timeFormat.parse(minTime).getTime();
                        long randomEndTime = timeFormat.parse(maxTime).getTime();
                        long randomTime = CalendarHelper.getRandomDate(randomStartTime, randomEndTime);
                        Calendar calRandomTime = Calendar.getInstance();
                        calRandomTime.setTimeInMillis(randomTime);

                        Calendar calRandomDate = Calendar.getInstance();
                        calRandomDate.setTimeInMillis(randomDate);
                        calRandomDate.set(Calendar.HOUR, calRandomTime.get(Calendar.HOUR));
                        calRandomDate.set(Calendar.MINUTE, calRandomTime.get(Calendar.MINUTE));

                        RandomAlarm rAlarm = new RandomAlarm();
                        rAlarm.setEventId(eventId);
                        rAlarm.setEndTime(randomEndTime);
                        rAlarm.setStartTime(randomStartTime);
                        rAlarm.setRandomDate(calRandomDate.getTimeInMillis());
                        CalendarHelper.addToDeviceCalendar(activity, rAlarm.randomDate, rAlarm.randomDate,
                                title, desc, null, minuteBefore);

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                } else {
                    CalendarHelper.addToDeviceCalendar(activity, startCallDate + startCallTime, endCallDate + endCallTime, title,
                            desc, rrule, minuteBefore);
                }
            }
        });
        updateDateButtons();
        ImageView imageViewAddContact = (ImageView) view.findViewById(R.id.btnAddContact);
        imageViewAddContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("content://contacts");
                Intent intent = new Intent(Intent.ACTION_PICK, uri);
                intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

        radioButtonCallBefore = (RadioButton) view.findViewById(R.id.radioButtonCallBefore);
        radioButtonAutoCall = (RadioButton) view.findViewById(R.id.radioButtonAutoCall);
        radioButtonCallBefore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideSoftKeyboard();
                Resources res = getResources();
                Builder builderSingle = new Builder(activity);
                builderSingle.setIcon(R.drawable.ic_launcher_c);
                builderSingle.setTitle(R.string.before_dialog_title);


                ArrayAdapter adapter = new ArrayAdapter<DurationBefore>(getContext(), android.R.layout.simple_list_item_1, ObjectHelper.getDurationBefore()) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        TextView view = (TextView) super.getView(position, convertView, parent);
                        view.setText(getItem(position).title);
                        return view;
                    }
                };


                builderSingle.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        radioButtonAutoCall.setChecked(true);
                    }
                });
                builderSingle.setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        DurationBefore db = ObjectHelper.getDurationBefore().get(i);
                        minuteBefore = db.minutes; //Integer.parseInt(choices[i]);
//                        Calendar c = Calendar.getInstance();
//                        c.setTimeInMillis(startCallDate);
//                        c.add(Calendar.MINUTE, minuteBefore);
//                        endCallDate = c.getTimeInMillis();
                        radioButtonCallBefore.setText(db.title + " " + getString(R.string.before_call));

                        updateDateButtons();
                    }
                });
                AlertDialog dialog = builderSingle.create();
                ListView listView = dialog.getListView();
                listView.setDivider(new ColorDrawable(Color.GRAY)); // set color
                listView.setDividerHeight(2);
                dialog.show();
            }
        });
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                hideSoftKeyboard();
                return true;
            }
        });

        randomGroup.setOnCheckedChangeListener(
                new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        hideSoftKeyboard();
                        endCallTime = endCallDate = startCallTime = startCallDate = 0;
                        updateDateButtons();
                        if (checkedId == R.id.radioFix) {
                            isRandom = false;
                            btnSTime.setText("Set Call Time");
                            btnETime.setText("End Time");
                            ((Button) view.findViewById(R.id.btnStartDate)).setText("Set Call Date");
                            view.findViewById(R.id.btnStartDate).setPadding(0, 0, 5, 0);
                            view.findViewById(R.id.btnETime).setVisibility(View.GONE);
                            view.findViewById(R.id.emptyContaine).setVisibility(View.GONE);
                            view.findViewById(R.id.btnEndDate).setVisibility(View.GONE);
                            view.findViewById(R.id.repeatContainer).setVisibility(View.VISIBLE);
                        } else {
                            isRandom = true;
                            btnSTime.setText("Start Time");
                            btnETime.setText("End Time");
                            ((Button) view.findViewById(R.id.btnStartDate)).setText("Start Date");
                            view.findViewById(R.id.btnETime).setVisibility(View.VISIBLE);
                            view.findViewById(R.id.btnEndDate).setVisibility(View.VISIBLE);
                            view.findViewById(R.id.emptyContaine).setVisibility(View.VISIBLE);
                            view.findViewById(R.id.repeatContainer).setVisibility(View.GONE);
                        }
                    }
                }
        );

        addEditorAction(txtTitle, R.id.edTitle);
        addEditorAction(txtContactNo, R.id.login);
        btnSTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideSoftKeyboard();
                CreateFragment.isEndCall = false;
                TimePickerFragmentDialog.newInstance(false).show(getChildFragmentManager(), "TimePickerFragmentDialog");
            }
        });
        btnETime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideSoftKeyboard();
                CreateFragment.isEndCall = true;
                if (startCallDate > 0)
                    TimePickerFragmentDialog.newInstance(DateTimeBuilder.newInstance().withSelectedDate(startCallDate + startCallTime).with24Hours(false)).show(getChildFragmentManager(), "TimePickerFragmentDialog");
                else
                    TimePickerFragmentDialog.newInstance(false).show(getChildFragmentManager(), "TimePickerFragmentDialog");
            }
        });
        return view;
    }

    private Long saveEvent() {
        String contactNo = txtContactNo.getText().toString();
        String title = txtTitle.getText().toString();
//            Builder builder = new Builder(getContext());
//            builder.setTitle(getString(R.string.app_name));
//            builder.setMessage(R.string.num_title_req);
//            builder.setPositiveButton(getString(R.string.ok), null);
//            builder.create().show();
        CallTemplate t = new CallTemplate();
        t.setComplete(0);
        t.setAlertBefore(minuteBefore);
        t.setContactPhoneNumber(contactNo);
        t.setTitle(title);
        t.setType(isRandom ? "Random" : "Fixed");
        t.setRecurringRule(repeatString);
        t.setStartDate(startCallDate + startCallTime);
        if (endCallDate == 0)
            t.setEndDate(startCallDate + startCallTime + 60 * 60 * 1000);
        else
            t.setEndDate(endCallDate + endCallTime);
        List<CallTemplate> recordExisting = new ArrayList<>();
        try {
            recordExisting = CallTemplate.find(CallTemplate.class, "title = ?", title);
        } catch (Exception e) {

        }
        if (!recordExisting.isEmpty()) {
            AppMsg.
                    makeText(getActivity(), "Duplicate titles now allowed. Enter a unique title.", AppMsg.STYLE_ALERT).show();
            return null;
        } else {
            t.save();
            activity.nav.setSelectedItemId(R.id.navigation_notifications);
            return t.getId();
        }
    }


    void addEditorAction(EditText editText, final int rId) {
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == rId || id == EditorInfo.IME_NULL) {
                    hideSoftKeyboard();
                    return true;
                }
                return false;
            }
        });
    }


    void showPicker(long selectedDate) {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(selectedDate);
        DatePickerFragmentDialog.newInstance(
                DateTimeBuilder.newInstance()
                        //.withTime(true)
//                        .with24Hours(false)
                        .withCurrentHour(0)
                        .withCurrentMinute(0)
                        .withSelectedDate(selectedDate))
                .show(getChildFragmentManager(), "DatePickerFragmentDialog");
    }

    @Override
    public void onDateSet(long date) {
        if (CreateFragment.isEndCall) {
            Calendar date1 = Calendar.getInstance();
            date1.setTimeInMillis(date);
// reset hour, minutes, seconds and millis
            date1.set(Calendar.HOUR_OF_DAY, 0);
            date1.set(Calendar.MINUTE, 0);
            date1.set(Calendar.SECOND, 0);
            date1.set(Calendar.MILLISECOND, 0);
            if (startCallDate <= date1.getTimeInMillis())
                endCallDate = date1.getTimeInMillis();
            else {
                AppMsg.makeText(getActivity(), "End time should be greater that Start time", AppMsg.STYLE_ALERT).show();
            }
            updateDateButtons();
            //open start time picker
            hideSoftKeyboard();
            CreateFragment.isEndCall = false;
            TimePickerFragmentDialog.newInstance(false).show(getChildFragmentManager(), "TimePickerFragmentDialog");
        } else {
            Calendar date1 = Calendar.getInstance();
            date1.setTimeInMillis(date);
// reset hour, minutes, seconds and millis
            date1.set(Calendar.HOUR_OF_DAY, 0);
            date1.set(Calendar.MINUTE, 0);
            date1.set(Calendar.SECOND, 0);
            date1.set(Calendar.MILLISECOND, 0);
            startCallDate = date1.getTimeInMillis();
            updateDateButtons();   //open end date picker
            if (isRandom) {
                hideSoftKeyboard();
                CreateFragment.isEndCall = true;
                showPicker(endCallDate);
            } else {
                hideSoftKeyboard();
                CreateFragment.isEndCall = false;
                TimePickerFragmentDialog.newInstance(false).show(getChildFragmentManager(), "TimePickerFragmentDialog");
            }
        }
    }

    @Override
    public void onTimeSet(long timeOnly, long dateWithTime) {
        if (CreateFragment.isEndCall) {
            Date dt = new Date(dateWithTime);
            // today
            Calendar date = Calendar.getInstance();
            date.set(Calendar.HOUR_OF_DAY, 0);
            date.set(Calendar.MINUTE, 0);
            date.set(Calendar.SECOND, 0);
            date.set(Calendar.MILLISECOND, 0);
            endCallTime = timeOnly - date.getTimeInMillis();
            btnETime.setText(timeFormat.format(dt));
            updateDateButtons();
        } else {
            Date dt = new Date(dateWithTime);
            // today
            Calendar date = Calendar.getInstance();
// reset hour, minutes, seconds and millis
            date.set(Calendar.HOUR_OF_DAY, 0);
            date.set(Calendar.MINUTE, 0);
            date.set(Calendar.SECOND, 0);
            date.set(Calendar.MILLISECOND, 0);

            startCallTime = timeOnly - date.getTimeInMillis();
            btnSTime.setText(timeFormat.format(dt));
            updateDateButtons();
            if (isRandom) {
                hideSoftKeyboard();
                CreateFragment.isEndCall = true;
                TimePickerFragmentDialog.newInstance(false).show(getChildFragmentManager(), "TimePickerFragmentDialog");
            }
        }
        updateDateButtons();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == this.REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                String[] projection = {ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME};
                Cursor cursor = this.activity.getContentResolver().query(uri, projection, null, null, null);
                cursor.moveToFirst();

                int numberColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                String number = cursor.getString(numberColumnIndex);

                int nameColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                String name = cursor.getString(nameColumnIndex);
                txtTitle.setText(name);
                txtContactNo.setText(number);
                txtTitle.requestFocus();

            }
        }
    }

    void updateDateButtons() {

        if (CreateFragment.startCallDate == 0) {
            datePickerStartDateButton.setText("Set Start Date");
        } else {
            Date startDate = new Date(this.startCallDate + this.startCallTime);
            datePickerStartDateButton.setText(dateFormat.format(startDate));
        }
        if (CreateFragment.endCallDate == 0) {
            datePickerEndDateButton.setText("Set End Date");
        } else {
            Date endDate = new Date(this.endCallDate + this.endCallTime);
            datePickerEndDateButton.setText(dateFormat.format(endDate));
        }
    }

    public void hideSoftKeyboard() {
        if (getActivity().getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
        }
    }

}