package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
public class MainActivity extends AppCompatActivity {

    private final String TAG = "MA_TAG";
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int LOCATION_PERMISSION_CODE = 101;
    private static final int NOTIFICATION_PERMISSION_CODE = 102;
    private static final int PERMISSION_CODE = 1024;

    private static final String CHANNEL_ID = "my_channel";
//    private NotificationManager notificationManager;

    /*private boolean isCamPermissionGranted = false;
    private boolean isLocPermissionGranted = false;
    private boolean isNofyPermissionGranted = false;*/

    Button locPerm;

    FusedLocationProviderClient fusedLocationProviderClient;
    TextView rLocation;

    String[] appPermission = {Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.POST_NOTIFICATIONS};

    /*int[] n = {CAMERA_PERMISSION_CODE,
            LOCATION_PERMISSION_CODE,
            NOTIFICATION_PERMISSION_CODE};*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);

        Button camPerm = findViewById(R.id.CamPerBt);
        super.onCreate(savedInstanceState);
        locPerm = findViewById(R.id.LocPermBt);
        Button nofPerm = findViewById(R.id.NofPermBt);

        rLocation = findViewById(R.id.textView2);

        /*if(checkAndRequestPermission()){
            toast("Requests done");
        }*/

        camPerm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE);


            }
        });

        locPerm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, LOCATION_PERMISSION_CODE);
            }
        });

        nofPerm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {    //TIRAMISU code name for Android 13
                    checkPermission(Manifest.permission.POST_NOTIFICATIONS, NOTIFICATION_PERMISSION_CODE);
                }
            }
        });
    }

    /*public boolean checkAndRequestPermission() {
        List<String> listOfPermissions = new ArrayList<>();
        for (String perm : appPermission) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                listOfPermissions.add(perm);
            }
        }
        if (!listOfPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(MainActivity.this, listOfPermissions.toArray(new String[listOfPermissions.size()]), PERMISSION_CODE);
            return false;
        }
        return true;
    }*/

    public void checkPermission(String permission, int requestCode) {

        if(ActivityCompat.checkSelfPermission(MainActivity.this,permission) == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == CAMERA_PERMISSION_CODE) {
                Intent intent = new Intent(MainActivity.this, MainActivity2.class);
                startActivities(new Intent[]{intent});
            } else if (requestCode == LOCATION_PERMISSION_CODE) {
                getLocation();
            } else if (requestCode == NOTIFICATION_PERMISSION_CODE) {
                addNotification();
            }
        }else if(ActivityCompat.shouldShowRequestPermissionRationale(this, permission)){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Please provide permission")
                    .setTitle("Need Permission")
                    .setPositiveButton("Ok",((dialog, which) -> {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission},requestCode);
                        dialog.dismiss();
                    }))
                    .setNegativeButton("Cancel",((dialog, which) -> dialog.dismiss()));
            builder.show();
        }else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission},requestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.e(TAG, "onRequestPermissionsResult > " + requestCode + " / " + grantResults.length);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(MainActivity.this, MainActivity2.class);
                startActivities(new Intent[]{intent});
            } else if(!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                Log.e(TAG,"cam per");
                Redirct();
            }else {
                Log.e(TAG,"cam toast");
                toast("Camera Permission denied");
            }
        } else if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            } else if(!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Redirct();
            }else {
                toast("Location permission denied");
            }
        } else if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                addNotification();
            } else if(!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.POST_NOTIFICATIONS)) {
                Redirct();
            }else {
                toast("Notification Permission denied");
            }

        }
    }

    private void addNotification() {

        Intent intent = new Intent(this, MainActivity3.class);
        PendingIntent pendingIntent = PendingIntent.getActivities(this, 0, new Intent[]{intent}, PendingIntent.FLAG_IMMUTABLE);

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Notification")
                .setContentText("This is Notification")
                .setContentIntent(pendingIntent);

        nm.createNotificationChannel(new NotificationChannel(CHANNEL_ID, "New Channel", NotificationManager.IMPORTANCE_HIGH));
        nm.notify(NOTIFICATION_PERMISSION_CODE, notification.build());
    }

    private void getLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
        } else {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                        try {
                            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            assert addresses != null;
                            String s1 = "Address: " + addresses.get(0).getAddressLine(0);
                            String s2 = s1 + addresses.get(0).getLocality();
                            String s3 = s2 + addresses.get(0).getCountryName();
                            rLocation.setText(s3);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                    }
                }
            });
        }
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void Redirct() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Alert!");
        alertDialog.setMessage("Please provide permissions");
        alertDialog.setButton(Dialog.BUTTON_POSITIVE, "Setting", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", getPackageName(), null));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        alertDialog.show();
    }
}