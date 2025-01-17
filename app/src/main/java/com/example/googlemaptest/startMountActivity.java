package com.example.googlemaptest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Chronometer;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class startMountActivity extends AppCompatActivity implements OnMapReadyCallback {
    private String MountName;
    private DatabaseReference mData, mRef;
    private String path;
    private GoogleMap mMap;
    private TextView mname;
    private Chronometer stopWatch;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_mount);

        Intent getMainIntent = getIntent();
        MountName = getMainIntent.getStringExtra("MountName");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mname = findViewById(R.id.start);
        mname.setText(MountName);
        Chronometer stopWatch  = (Chronometer) findViewById(R.id.chronometer);
        stopWatch.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener(){
            @Override
            public void onChronometerTick(Chronometer cArg) {
                long time = SystemClock.elapsedRealtime() - cArg.getBase();

                cArg.setText(timeHandler(time,stopWatch));
            }
        });
        long currentTime =  SystemClock.elapsedRealtime() - stopWatch.getBase();
        String time1 = timeHandler(currentTime, stopWatch);
        stopWatch.setBase(SystemClock.elapsedRealtime());
        stopWatch.start();
    }

    public String timeHandler(long time, Chronometer stopWatch)
    {
        long currentTime =  SystemClock.elapsedRealtime() - stopWatch.getBase();

        int h = (int)(currentTime / 3600000);
        int m = (int)(currentTime - h * 3600000) / 60000;
        int s = (int)(currentTime - h * 3600000 - m * 60000) / 1000 ;

        String hh = h < 10 ? "0"+h: h+"";
        String mm = m < 10 ? "0"+m: m+"";
        String ss = s < 10 ? "0"+s: s+"";
        return hh+":"+mm+":"+ss;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        Intent getMainIntent = getIntent();
        MountName = getMainIntent.getStringExtra("MountName");
        Log.d("startMountAct", "getIntent"+MountName);
        mData = FirebaseDatabase.getInstance().getReference("MountainList");
        mRef = mData.child(MountName);
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                MountElement mountInfo = snapshot.getValue(MountElement.class);
                String startPoint = mountInfo.starting;
                String endPoint = mountInfo.end;
                String[] startPointSplit = startPoint.split(" ");
                String[] endPointSplit = endPoint.split(" ");
                Log.d("MainActivity", "ValueEventListener : " + endPointSplit[0]);
                Log.d("MainActivity", "ValueEventListener : " + endPointSplit[1]);
                Double lag = Double.parseDouble(endPointSplit[0]);
                Double log = Double.parseDouble(endPointSplit[1]);
                Double slag = Double.parseDouble(startPointSplit[0]);
                Double slog = Double.parseDouble(startPointSplit[1]);
                LatLng latLng = new LatLng(log, lag);
                LatLng latLng1 = new LatLng(slog, slag);
                mMap.addMarker(new MarkerOptions().position(latLng).title(mountInfo.mname).snippet(String.valueOf(mountInfo.maxHeight)));
                mMap.addMarker(new MarkerOptions().position(latLng1).title(mountInfo.mname).snippet(String.valueOf(mountInfo.maxHeight)));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                path = mountInfo.path;
                Log.d("startpath", "getIntent1 "+ path);
                drawLine(mMap, path);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }


    public void drawLine(GoogleMap Map , String path){
        PolylineOptions polylineOptions = new PolylineOptions();
        try {
            String[] path_list = path.split(",");
            for (int i = 0; i < path_list.length; i++) {
                path_list[i] = path_list[i].trim();
                Double lat = Double.parseDouble(path_list[i].split(" ")[0]);
                Double lng = Double.parseDouble(path_list[i].split(" ")[1]);
                polylineOptions.add(new LatLng(lng, lat));
                polylineOptions.width(10);
                polylineOptions.color(Color.BLUE);
            }
            Polyline polyline = Map.addPolyline(polylineOptions);

        }
        catch (Exception e){}
    }
}