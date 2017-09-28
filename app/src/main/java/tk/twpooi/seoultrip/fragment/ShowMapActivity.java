package tk.twpooi.seoultrip.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import net.daum.mf.map.api.CameraUpdateFactory;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapPointBounds;
import net.daum.mf.map.api.MapPolyline;
import net.daum.mf.map.api.MapView;

import java.util.ArrayList;
import java.util.HashMap;

import tk.twpooi.seoultrip.Information;
import tk.twpooi.seoultrip.DBHelper;
import tk.twpooi.seoultrip.MainTabActivity;
import tk.twpooi.seoultrip.R;
import tk.twpooi.seoultrip.StartActivity;
import tk.twpooi.seoultrip.fragment.detailattraction.DetailActivity;

/**
 * Created by tw on 2016-08-15.
 */
public class ShowMapActivity extends AppCompatActivity implements MapView.POIItemEventListener{

    // Daum map
    private MapView mapView;
    private ViewGroup mapLayout;
    private boolean isSetPin = false;
    private boolean isPolyline = false;
    public static boolean isMapLoading = false;


    private DBHelper dbHelper;

    private Button showDaumMap;


    private ArrayList<HashMap<String, String>> attractionList;
//    private FitMapView t;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_map);

        initData();

        initUI();

        if (mapView == null) {
            createMap();
            setMap();
            if(MainTabActivity.isMapViewLoad)
                setMapPin();
        }

        if(!MainTabActivity.isMapViewLoad){

            FitMapView f = new FitMapView();
            f.start();

            MainTabActivity.setIsMapViewLoad(true);
        }

    }


    private void initData(){
        Intent intent = getIntent();

        dbHelper = new DBHelper(getApplicationContext(), "SeoulTrip.db", null, 1);

        isPolyline = intent.getBooleanExtra("isPolyline", false);
        attractionList = new ArrayList<HashMap<String, String>>();

        ArrayList<HashMap<String, String>> attractionTitleFromFavorite = (ArrayList<HashMap<String,String>>)intent.getSerializableExtra("attraction_favorite");
        if(attractionTitleFromFavorite == null) {
            ArrayList<String> attractionTitleList = (ArrayList<String>) intent.getSerializableExtra("attraction");
            String language = intent.getStringExtra("language");
            if (language == null || language.equals("")) {
                language = StartActivity.DATABASE_LANGUAGE;
            }

            for (String s : attractionTitleList) {
                HashMap<String, String> list = dbHelper.getResultSearchTitle(s, language).get(0);
                list.put("language", language);
                attractionList.add(list);
            }
        }else{

            for(HashMap<String, String> temp : attractionTitleFromFavorite){
                HashMap<String, String> list = dbHelper.getResultSearchTitle(temp.get("title"), temp.get("language")).get(0);
                list.put("language", temp.get("language"));
                attractionList.add(list);
            }

        }
    }

    private void initUI() {

        RelativeLayout root = (RelativeLayout)findViewById(R.id.root);
        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(MainTabActivity.isMapViewLoad)
                    onBackPressed();
            }
        });

        if(attractionList.size() == 1){
            showDaumMap = (Button)findViewById(R.id.show_daum);
            showDaumMap.setVisibility(View.VISIBLE);
            showDaumMap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String lat = attractionList.get(0).get("lat");
                    String lng = attractionList.get(0).get("lng");

                    String uri = "daummaps://look?p=" + lat + "," + lng;

                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    startActivity(intent);
                }
            });
        }

    }

    public void setMap(){
        mapLayout = (ViewGroup) findViewById(R.id.map_view);
        mapLayout.addView(mapView);
    }

    protected void setMapPin(){

        try {
            MapPOIItem[] markers = new MapPOIItem[attractionList.size()];
            MapPolyline polyline = new MapPolyline();
            polyline.setTag(1000);
            polyline.setLineColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));

            for (int i = 0; i < attractionList.size(); i++) {
                MapPOIItem marker = new MapPOIItem();

                HashMap<String, String> temp = attractionList.get(i);

                marker.setItemName(temp.get("title"));
                marker.setTag(i);
                marker.setMarkerType(MapPOIItem.MarkerType.RedPin);
                marker.setSelectedMarkerType(MapPOIItem.MarkerType.BluePin);
                if (temp.get("lat") != null && (!temp.get("lat").equals(""))) {
                    marker.setMapPoint(MapPoint.mapPointWithGeoCoord(
                            Double.parseDouble(temp.get("lat")),
                            Double.parseDouble(temp.get("lng"))
                    ));
                    polyline.addPoint(MapPoint.mapPointWithGeoCoord(
                            Double.parseDouble(temp.get("lat")),
                            Double.parseDouble(temp.get("lng"))
                    ));
                }

                markers[i] = marker;

            }

            mapView.removeAllPOIItems();
            mapView.addPOIItems(markers);
            mapView.setPOIItemEventListener(this);

            if(isPolyline) {
                mapView.addPolyline(polyline);
            }

            if(attractionList.size() > 0) {
                MapPointBounds mapPointBounds = new MapPointBounds(polyline.getMapPoints());
                int padding = 100;
                mapView.moveCamera(CameraUpdateFactory.newMapPointBounds(mapPointBounds, padding));
            }

            isSetPin = true;

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void onPOIItemSelected(MapView mapView, MapPOIItem mapPOIItem) {
    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem) {
    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem, MapPOIItem.CalloutBalloonButtonType calloutBalloonButtonType) {
        String title = mapPOIItem.getItemName();
        int index = mapPOIItem.getTag();
        Intent intent = new Intent(ShowMapActivity.this, DetailActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("title", title);
        intent.putExtra("language", attractionList.get(index).get("language"));
        intent.putExtra("noMapView", true);
        startActivity(intent);
    }

    @Override
    public void onDraggablePOIItemMoved(MapView mapView, MapPOIItem mapPOIItem, MapPoint mapPoint) {

    }

    private class FitMapView extends Thread{
        public void run(){

            try{
                sleep(500);
            }catch (Exception e){

            }

            setMapPin();

        }
    }

    public void createMap(){
        Information apiKey = new Information();
        mapView = new MapView(this);
        mapView.setDaumMapApiKey(Information.DAUM_MAP_APIKEY);
        isMapLoading = true;
    }

    public void deleteMapView(){
        if(mapView != null){
            ((ViewGroup)mapView.getParent()).removeView(mapView);
            mapView = null;
            isMapLoading = false;
        }
    }



    @Override
    public void onDestroy(){
        super.onDestroy();
        deleteMapView();
    }

}
