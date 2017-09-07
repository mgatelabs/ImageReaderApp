package com.mgatelabs.imagereaderapp;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;

import com.mgatelabs.imagereaderapp.shared.Utils;

import java.io.IOException;

public class MainActivity extends Activity {

    private InfoServer infoServer;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView ipAddressView = (TextView) findViewById(R.id.ipAddress);

        ipAddressView.setText(Utils.getIPAddress(true));
    }

    @Override
    protected void onStart () {
        super.onStart();

        checkPermission("android.permission.READ_EXTERNAL_STORAGE");
        checkPermission("android.permission.INTERNET");

        try {
            infoServer = new InfoServer(8080);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkPermission(String permissionId) {
        switch (checkSelfPermission(permissionId)) {
            case PackageManager.PERMISSION_GRANTED: {

            }
            break;
            case PackageManager.PERMISSION_DENIED: {
                requestPermissions(new String[]{permissionId}, 1);
            }
            break;
        }
    }
}
