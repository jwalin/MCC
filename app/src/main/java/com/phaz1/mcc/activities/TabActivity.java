package com.phaz1.mcc.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.orm.query.Condition;
import com.orm.query.Select;
import com.phaz1.mcc.AppMsg;
import com.phaz1.mcc.R;
import com.phaz1.mcc.datalayer.CallTemplate;
import com.phaz1.mcc.datalayer.RandomAlarm;
import com.phaz1.mcc.fragments.CalendarListFragment;
import com.phaz1.mcc.fragments.CreateFragment;
import com.phaz1.mcc.helpers.CalendarHelper;

import java.util.Date;

public class TabActivity extends AppCompatActivity {
    public BottomNavigationView nav;
    private CallTemplate template;
    private CreateFragment createFragment;
    private CalendarListFragment calendarListFragment;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    addFragment(createFragment);
                    setTitle(R.string.title_home);
                    return true;
                case R.id.navigation_notifications:
                    if (Select.from(CallTemplate.class).count()<=0) {
                        AppMsg.
                                makeText(TabActivity.this, "Nothing to show", AppMsg.STYLE_ALERT).show();
                        return false;
                    } else {
                        addFragment(calendarListFragment);
                        setTitle(R.string.schedule);
                        return true;
                    }
            }
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        createFragment = new CreateFragment();
        calendarListFragment = new CalendarListFragment();
        addFragment(new CreateFragment());
        setTitle(R.string.title_home);

        nav = (BottomNavigationView) findViewById(R.id.navigation);
        notificationReceived();
    }

    private void playSound() {
        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.beep_beep);
        mediaPlayer.start();
    }


    void addFragment(Fragment fragment) {
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.content, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.tab, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        startActivity(new Intent(this, AboutActivity.class));
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        notificationReceived();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void notificationReceived() {
        try {
            String description = getIntent().getStringExtra("description");
            if (description != null) {
                playSound();
                String[] descs = description.split(":");
                String eventId = descs[1].split("-")[0].trim();
                template = CallTemplate.findById(CallTemplate.class, Long.valueOf(eventId.trim()));
                RandomAlarm randomAlarm = Select.from(RandomAlarm.class)
                        .where(Condition.prop("EVENT_ID").eq(Long.parseLong(eventId.trim()))).first();

                if (randomAlarm != null) {
                    startNewCall();
                    if (template.getEndDate() > new Date().getTime()) {
                        String desc = getString(R.string.app_name) + " : " + eventId + " - " + template.getTitle();
                        long randomTime = CalendarHelper.getRandomDate(randomAlarm.startTime, randomAlarm.endTime);
                        CalendarHelper.addToDeviceCalendar(this, randomTime, randomTime,
                                template.getTitle(), desc, null, 0);
                    }

                } else {
                    AlertDialog.Builder builder = CalendarHelper.showDialog(this, getString(R.string.app_name), template.getTitle() + "\n" + getString(R.string.want_to_call) + " " + template.getContactPhoneNumber() + " ?");
                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            startNewCall();
                        }
                    }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
                    builder.create().show();
                }
            }
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void startNewCall() {
        Intent in = new Intent(Intent.ACTION_CALL);
        in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        in.setData(Uri.parse("tel:" + template.getContactPhoneNumber()));
        if (ActivityCompat.checkSelfPermission(getBaseContext(),
                Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        startActivity(in);
        template.setComplete(1);
        template.save();
    }
}