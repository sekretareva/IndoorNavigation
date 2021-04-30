package com.example.mapview;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.RemoteException;
import android.util.Log;

import com.mapbox.mapboxsdk.maps.MapboxMap;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.TreeMap;

public class BeaconData implements BeaconConsumer {
    BeaconManager beaconManager;
    MapboxMap mapboxMap;
    MapData mapData;
    String[] beaconsNames = {"abeacon_49D7", "abeacon_D0F9", "abeacon_79B8", "abeacon_8259"};
    ArrayList<Beacon> analizedBeacons = new ArrayList<>();
    TreeMap<String, Coordinates> beaconsCoord = new TreeMap<>();
    TreeMap<String, ArrayList<Double>> beaconsDist = new TreeMap<>();
    double lenLongitude = 85972.2942636587;
    double lenLatitude = 88287.1313095034;
    double user_x=0, user_y=0;

    BeaconData(MapboxMap mapboxMap, Context context, MapData mapData)  {
        this.mapboxMap = mapboxMap;
        this.mapData = mapData;
        beaconManager = org.altbeacon.beacon.BeaconManager.getInstanceForApplication(context);
        beaconManager.getBeaconParsers().clear();
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.bind(this);

        Coordinates c36 = new Coordinates(104.26015672462, 52.250868099897);
        Coordinates c34 = new Coordinates(104.260241577899,52.250755196808);
        Coordinates c22 = new Coordinates(104.260276132228,	52.2508298507214);
        Coordinates c23 = new Coordinates(104.260522353784,52.2508988695676);
        beaconsCoord.put("abeacon_49D7", c36);
        beaconsCoord.put("abeacon_D0F9", c34);
        beaconsCoord.put("abeacon_79B8", c22);
        beaconsCoord.put("abeacon_8259", c23);

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
                    for (Beacon beacon: beacons){
                       if (Arrays.asList(beaconsNames).contains(beacon.getBluetoothName()) && beacon.getRssi()>-100){
                           ArrayList<Double> beaconDist = new ArrayList<>();
                           if (beaconsDist.containsKey(beacon.getBluetoothName())){
                               beaconDist = beaconsDist.get(beacon.getBluetoothName());
                               if (beaconDist.size()==10)
                                   beaconDist.remove(0);
                               beaconDist.add(beacon.getDistance());
                           }
                           else {
                               beaconDist.add(beacon.getDistance());
                               beaconsDist.put(beacon.getBluetoothName(), beaconDist);
                           }
                           analizedBeacons.add(beacon);
                       }
                    }
                    getCoordinates(analizedBeacons);
                    analizedBeacons.clear();
                }
            }
        });

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch ( RemoteException ignored) {    }


    }

    public void getCoordinates(ArrayList<Beacon> beacons){
        //Log.i("BEACONS", "There are " + beacons.size() + " beacons");
        if (beacons.size()<3)
            return;
        if (beacons.size() > 3){
            beacons = closestBeacons(beacons);
        }

        double[] R = new double[beacons.size()];

        for (int i = 0; i< beacons.size(); i++){
            ArrayList<Double> beaconDist = new ArrayList<>();
            beaconDist.addAll(beaconsDist.get(beacons.get(i).getBluetoothName()));
            Collections.sort(beaconDist);
            R[i] = beaconDist.get(beaconDist.size()/2);
            //Log.d("DIST", beacons.get(i).getBluetoothName() + " "+R[i]);

            /*if (beacons.get(i).getBluetoothName().equals(beaconsNames[1]))
                Log.d("D0F9", beacons.get(i).getDistance()+"");
            if (beacons.get(i).getBluetoothName().equals(beaconsNames[0]))
                Log.d("49D7", beacons.get(i).getDistance()+"");
            if (beacons.get(i).getBluetoothName().equals(beaconsNames[2]))
                Log.d("79B8", beacons.get(i).getDistance()+"");*/
        }

        Coordinates[] dif = new Coordinates[beacons.size()];
        for (int i = 0; i< beacons.size(); i++){
            String beaconName1 = beacons.get(i).getBluetoothName();
            String beaconName2 = beacons.get((i+1)%beacons.size()).getBluetoothName();
            dif[i] = new Coordinates(0,0);
            dif[i].longitude = (beaconsCoord.get(beaconName1).longitude - beaconsCoord.get(beaconName2).longitude)*lenLongitude;
            dif[i].latitude = (beaconsCoord.get(beaconName1).latitude - beaconsCoord.get(beaconName2).latitude)*lenLatitude;
        }

        double K1, K2, K3;
        K1 = dif[0].longitude*dif[0].longitude - dif[2].longitude*dif[2].longitude+ dif[0].latitude*dif[0].latitude - dif[2].latitude*dif[2].latitude - R[1]*R[1]+R[2]*R[2];
        K2 = K1/(2*dif[1].longitude);
        K3 = dif[1].latitude/dif[1].longitude;

        double D_sqrt = 2* Math.sqrt(K2*K2*K3*K3 - (K3*K3+1)*(K2*K2 - R[0]*R[0]));
        double y11, y12, x11, x12, y21, y22, x21, x22;

        if (D_sqrt>=0){
            y11 = (2*K2*K3 + D_sqrt)/(2*(K3*K3 + 1));
            y12 = (2*K2*K3 - D_sqrt)/(2*(K3*K3 + 1));
            x11 = K2 - K3*y11;
            x12 = K2 - K3*y12;
            x11 = beaconsCoord.get(beacons.get(0).getBluetoothName()).longitude + x11/lenLongitude;
            y11 = beaconsCoord.get(beacons.get(0).getBluetoothName()).latitude + y11/lenLatitude;
            //Log.d("BEACONS",beacons.get(0).getBluetoothName() + " x = " + x11 + " y = " + y11);

            x12 = beaconsCoord.get(beacons.get(0).getBluetoothName()).longitude + x12/lenLongitude;
            y12 = beaconsCoord.get(beacons.get(0).getBluetoothName()).latitude + y12/lenLatitude;
            //Log.d("BEACONS",beacons.get(0).getBluetoothName() + " x = " + x12 + " y = " + y12);

            x21 = x11 + dif[0].longitude;
            x22 = x12 + dif[0].longitude;
            y21 = y11 + dif[0].latitude;
            y22 = y12 + dif[0].latitude;

            if (Math.abs(x21*x21 + y21*y21 - R[1]*R[1]) < Math.abs(x22*x22 + y22*y22 - R[1]*R[1])){
                //Log.d("COORDS_1",beacons.get(0).getBluetoothName() + " x = " + x11 + " y = " + y11);
                user_x = x11;
                user_y = y11;
            }
            else {
                //Log.d("COORDS_2",beacons.get(0).getBluetoothName() + " x = " + x12 + " y = " + y12);
                user_x = x12;
                user_y = y12;
            }
        }
    }

    public ArrayList<Beacon> closestBeacons(ArrayList<Beacon> beacons){
        Beacon farthest = new Beacon.Builder().setRssi(-100).build();
        beacons.add(farthest);
        int[] indexes = {beacons.size()-1,beacons.size()-1,beacons.size()-1};
        int leadersNumber = 3;

        for (int b = 0; b < beacons.size(); b++){
            for (int i = 0; i < leadersNumber; i++)
                if (beacons.get(b).getDistance() < beacons.get(indexes[i]).getDistance() || beacons.get(indexes[i]).getRssi()==-100){
                    for (int j = leadersNumber - 1; j>i; j--)
                        indexes[j] = indexes[j-1];
                    indexes[i] = b;
                    break;
                }
        }

        ArrayList<Beacon> closest = new ArrayList<>();
        for (int i = leadersNumber-1; i>=0; i--){
            closest.add(beacons.get(indexes[i]));
        }
        return  closest;
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
