//********************************************************************************
//
// net.phaz1.buddDialer.activities.MainActivity.java
//
// Copyright (c) 2017 Phaz1 Inc.
//
// All Rights Reserved
//
//  This program artifact may not be copied, reproduced or distributed in any
//  manner, way or form without prior written authorization from an authorized
//  representative of Phaz1 Inc.
//
//********************************************************************************

package com.phaz1.mcc.activities;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.phaz1.mcc.R;

import java.util.List;

/**
 * Main app class once app is instanctiated
 *
 * @author James Earle
 * @version 1.0
 * @see com.phaz1.mcc
 */

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.CAMERA, Manifest.permission.READ_CONTACTS, Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR, Manifest.permission.CALL_PHONE, Manifest.permission.READ_PHONE_STATE, Manifest.permission.PROCESS_OUTGOING_CALLS, Manifest.permission.INTERNET)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {/* ... */}

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {/* ... */}
                }).check();
        setContentView(R.layout.activity_main);
    }
}
