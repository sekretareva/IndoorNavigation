package com.example.mapview;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.util.Log;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textAnchor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textField;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textFont;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textSize;

public class MapData {
    Style style;
    ArrayList<List<Feature>> features;
    MapboxMap mapboxMap;
    Feature userLocationPoint;
    GeoJsonSource userLocation;
    ArrayList<GeoJsonSource> sources = new ArrayList<>();
    String[] sourceNames = {"lib", "mainWalls", "walls", "stairs", "400","401", "402", "404", "405", "403", "406", "WCF", "WCM", "Stairs1", "Stairs2", "400L", "401L", "402L", "403L", "404L", "405L", "406L", "WCML", "WCFL"};
    String[] roomsNames = {"400","401", "402", "403", "404", "405", "406", "WCF", "WCM", "Stairs1", "Stairs2"};
    String [] description= {"Кабинет № 400", "Лабаратория", "Кабинет № 402", "Кабинет № 403", "Кабинет № 404", "Кабинет № 405", "Кабинет № 406", "Женский туалет", "Мужской туалет", "Лестница № 1", "Лестница № 2"};


    MapData (Style style, Context context){
        this.style = style;

        try {
            //подгружаем данные по отдельным кабинетам (тип: полигон) и добавляем в стиль полученные данные
            GeoJsonSource source;
            for (String sourceName:sourceNames){
                sources.add( source = new GeoJsonSource(sourceName, new URI("asset://"+sourceName+".geojson")));
                style.addSource(source);
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        //устанавливаем стиль слоев
        style.addLayer(new FillLayer("lib", "lib").withProperties(PropertyFactory.fillColor(Color.WHITE),fillOpacity(1.0f)));
        style.addLayer(new LineLayer("mainWalls", "mainWalls").withProperties(PropertyFactory.lineGapWidth((float) 1.0),fillOpacity(1.0f)));
        style.addLayer(new LineLayer("walls", "walls").withProperties(fillOpacity(1.0f)));
        style.addLayer(new LineLayer("stairs", "stairs").withProperties(PropertyFactory.lineColor(Color.BLACK),fillOpacity(1.0f)));
        for (String room: roomsNames) {
            style.addLayer(new FillLayer(room, room).withProperties(PropertyFactory.fillColor(Color.BLUE),fillOpacity(0.0f)));
            style.addLayer(new SymbolLayer(room+"L", room+"L").withProperties(PropertyFactory.textColor(Color.BLACK),
                    textField(room),textSize(12f), textOpacity(0.7f)));
        }

        /*List<Feature> markerCoordinates = new ArrayList<>();
        markerCoordinates.add(Feature.fromGeometry(Point.fromLngLat(104.26015672462, 52.250868099897)));
        markerCoordinates.add(Feature.fromGeometry(Point.fromLngLat( 104.260241577899, 52.250755196808)));
        markerCoordinates.add(Feature.fromGeometry(Point.fromLngLat(104.260276132228,	52.2508298507214)));
        markerCoordinates.add(Feature.fromGeometry(Point.fromLngLat( 104.260522353784,52.2508988695676)));

        style.addImage("my-marker-image", BitmapFactory.decodeResource(context.getResources(), R.drawable.point));
        style.addSource(new GeoJsonSource("marker-source",FeatureCollection.fromFeatures(markerCoordinates)));
        style.addLayer( new SymbolLayer("marker-layer", "marker-source")
                .withProperties(iconImage("my-marker-image")));*/
    }

    public void getFeatures(MapboxMap mapboxMap, PointF finalPoint){
        this.mapboxMap = mapboxMap;
        features = new ArrayList<>();
        for (String room: roomsNames){
            features.add(this.mapboxMap.queryRenderedFeatures(finalPoint,room));
        }
    }

    public void setLocation(double longitude, double latitude){
        style.removeLayer("user-location");
        style.removeSource("user-location");

        userLocationPoint = Feature.fromGeometry(Point.fromLngLat(longitude,latitude));
        userLocation = new GeoJsonSource("user-location", userLocationPoint);
        style.addSource(userLocation);
        style.addLayer( new SymbolLayer("user-location", "user-location")
                .withProperties(iconImage("my-marker-image")));
    }
}
