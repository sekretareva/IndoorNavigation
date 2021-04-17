package com.example.mapview;

import android.graphics.Color;
import android.graphics.PointF;

import com.mapbox.geojson.Feature;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillOpacity;

public class MapData {
    Style style;
    ArrayList<List<Feature>> features;
    MapboxMap mapboxMap;
    //GeoJsonSource source_401, source_403, library, mainWalls, walls, stairs;
    ArrayList<GeoJsonSource> sources = new ArrayList<>();
    String[] sourceNames = {"lib", "mainWalls", "walls", "stairs", "401", "403"};
    String[] roomsNames = {"401", "403"};

    MapData (Style style){
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
        style.addLayer(new LineLayer("stairs", "stairs").withProperties(PropertyFactory.lineColor(Color.BLUE),fillOpacity(1.0f)));
        style.addLayer(new FillLayer("401", "401").withProperties(PropertyFactory.fillColor(Color.BLUE),fillOpacity(0.0f)));
        style.addLayer(new FillLayer("403", "403").withProperties(PropertyFactory.fillColor(Color.BLUE),fillOpacity(0.0f)));

    }

    public void getFeatures(MapboxMap mapboxMap, PointF finalPoint){
        this.mapboxMap = mapboxMap;
        features = new ArrayList<>();
        for (String room: roomsNames){
            features.add(this.mapboxMap.queryRenderedFeatures(finalPoint,room));
        }
    }
}
