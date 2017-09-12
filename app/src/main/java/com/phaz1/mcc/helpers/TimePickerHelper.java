package com.phaz1.mcc.helpers;

import android.support.v4.app.FragmentManager;
import android.view.View;

import com.fastaccess.datetimepicker.TimePickerFragmentDialog;

/**
 * Created by altaf on 18/05/2017.
 */

public class TimePickerHelper {
    public static void addTimePicker(final FragmentManager manager, View btn){
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerFragmentDialog.newInstance(false).show(manager, "TimePickerFragmentDialog");
            }
        });
    }
}