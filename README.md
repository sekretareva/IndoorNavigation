# Создание план-схемы этажа

В проекте использована библиотека MapBox. Для ее использования необходимо в ```build.gradle``` добавить:
```
implementation ('com.mapbox.mapboxsdk:mapbox-android-sdk:8.1.0')
implementation 'com.mapbox.mapboxsdk:mapbox-sdk-turf:5.6.0'
```

Создание данного приложения было разделено на несколько этапов:
1. Создание датасетов для плана. 
2. Использование набора данных в стиле карты, куда они могут быть загружены либо через сервис MapBox Studio, либо вручную с помощью возможностей библиотеки.
3. Использование полученного стиля в самом приложении.
4. Добавление интерактива - обраьотка события onMapClick.

## Создание датасетов

Датасеты строятся на основе фалов в формате .geojson.

<em>Пример:</em>

```
{
	"type": "FeatureCollection",
	"features":
	[
		{ 
			"type": "Feature",
			"geometry": 
			{
				"type": "Polygon",
				"coordinates": 
				[[
					[104.260418570000,	52.2510366170000],
					[104.260172390447,	52.2509676076287],
					[104.2602761322280,	52.2508298507214],
					[104.2605223537840,	52.2508988695676],
					[104.260418570000,	52.2510366170000]
				]]
			},
			"properties": {}
		},
	]
}

```

FeatureCollection может состоять из нескольких элементов типа Feature, внутри Feature может быть элемент типа Point, LineString или Polygon. <br>
Создавать датасеты удобнее в MapBox Studio. Там можно расставить необходимые точки на карте и все данные соберутся в geojson-формате. 

## Создание стиля и использование его в приложении

MapBox предлагает разные стили карты (однотонные, цветные, светлые, темные и т.д.). <br>

Можно создать свой стиль в MapBox Studio на основе готового и добавить свои слои на основе созданных данных, стилизуя их по-своему:
```
mapboxMap.setStyle(new Style.Builder().fromUri("mapbox://styles/username/styleid"));
```

Можно загрузить уже готовый стиль, а создать и стилизовать слои в Android Studio:
```
mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
    @Override
	public void onStyleLoaded(@NonNull Style style) {
		try {
			GeoJsonSource source = new GeoJsonSource("geojson-source", new URI("asset://lib.geojson"));
			style.addSource(source);
			FillLayer lib = new FillLayer("lib", "geojson-source")
				.withProperties(
					PropertyFactory.fillColor(Color.parseColor("#000000")),
					PropertyFactory.opacity(0.0f));
		} catch (URISyntaxException exception) {}
	}
});

```
## Другие настройки
Чтобы карта открывалась в нужном месте и в нужном масштабе в блоке с картой в ```activity_main.xml``` необходимы следующие свойства:
```
mapbox:mapbox_cameraTargetLat="52.250755"
mapbox:mapbox_cameraTargetLng="104.260226"
mapbox:mapbox_cameraZoom="18"
```
