package com.example.mapview;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.RemoteException;
import android.util.Log;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class BeaconData implements BeaconConsumer {
    BeaconManager beaconManager;
    String[] beaconsNames = {"abeacon_49D7", "abeacon_D0F9"};
    ArrayList<Beacon> analizedBeacons = new ArrayList<>();
    ArrayList<Coordinates> beaconsCoord = new ArrayList<>();
    double lenLongitude = 85972.2942636587;
    double lenLatitude = 88287.1313095034;

    BeaconData(Context context)  {
        beaconManager = org.altbeacon.beacon.BeaconManager.getInstanceForApplication(context);
        beaconManager.getBeaconParsers().clear();
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.bind(this);

        beaconsCoord.add(new Coordinates(104.2601723904, 52.250967607));
        beaconsCoord.add(new Coordinates(104.2604282868, 52.250627807));

        /*File file = new File("C:/Users/user/Android/MapView/beaconsCoordinates.txt");
        Scanner sc = null;
        try {
            sc = new Scanner(file);
            double x = sc.nextDouble();
            double y = sc.nextDouble();
            Log.i("FILE", x+ " " + y);
            beaconsCoord.add(new Coordinates(x,y));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }*/
    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.removeAllRangeNotifiers();
        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, org.altbeacon.beacon.Region region) {
                if (beacons.size() > 0) {
                    for (org.altbeacon.beacon.Beacon beacon: beacons){
                       if (Arrays.asList(beaconsNames).contains(beacon.getBluetoothName()) && beacon.getRssi()>-100)
                           analizedBeacons.add(beacon);
                    }
                    getCoordinates(analizedBeacons);
                    analizedBeacons.clear();
                }
            }
        });

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch ( RemoteException e) {    }
    }

    public void getCoordinates(ArrayList<Beacon> beacons){
        for (Beacon b: beacons)
            Log.i("BEACONS", b.getDistance()+"");
        if (beacons.size() == 2){
            double r1 = beacons.get(0).getDistance();
            double r2 = beacons.get(1).getDistance();
            double d_long = beaconsCoord.get(0).longitude-beaconsCoord.get(1).longitude;
            double d_lat = beaconsCoord.get(0).latitude-beaconsCoord.get(1).latitude;
            double d = Math.sqrt(d_lat*d_lat+d_long*d_long);
            double a = (r1*r1 - r2*r2 + d*d)/(2*d);
            double h = Math.sqrt(r1*r1 - a*a);

            double x = beaconsCoord.get(0).longitude + a/d*(beaconsCoord.get(1).longitude-beaconsCoord.get(0).longitude);
            double y = beaconsCoord.get(0).latitude + a/d*(beaconsCoord.get(1).latitude-beaconsCoord.get(0).latitude);

            Log.d("COORDS", x + " " + y);
        }
    }

    @Override
    public Context getApplicationContext() {
        return null;
    }

    @Override
    public void unbindService(ServiceConnection connection) {

    }

    @Override
    public boolean bindService(Intent intent, ServiceConnection connection, int mode) {
        return false;
    }
}
