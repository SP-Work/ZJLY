package com.otitan.main.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.GeometryType;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.Polygon;
import com.esri.arcgisruntime.geometry.Polyline;
import com.esri.arcgisruntime.layers.ArcGISTiledLayer;
import com.esri.arcgisruntime.layers.OpenStreetMapLayer;
import com.esri.arcgisruntime.mapping.view.LocationDisplay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.mapping.view.SketchEditor;
import com.esri.arcgisruntime.mapping.view.SketchStyle;
import com.esri.arcgisruntime.symbology.SimpleFillSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.otitan.TitanApplication;
import com.otitan.base.ValueCallBack;
import com.otitan.data.DataRepository;
import com.otitan.data.Injection;
import com.otitan.main.listener.ArcgisLocation;
import com.otitan.main.listener.GeometryChangedListener;
import com.otitan.main.model.ActionModel;
import com.otitan.main.model.Location;
import com.otitan.main.model.MainModel;
import com.otitan.main.viewmodel.BootViewModel;
import com.otitan.main.viewmodel.CalloutViewModel;
import com.otitan.main.viewmodel.GeoViewModel;
import com.otitan.main.viewmodel.InitViewModel;
import com.otitan.main.viewmodel.SketchEditorViewModel;
import com.otitan.main.viewmodel.ToolViewModel;
import com.otitan.model.MyLayer;
import com.otitan.ui.mview.IMap;
import com.otitan.ui.view.ImgManagerView;
import com.otitan.ui.view.LayerManagerView;
import com.otitan.util.Constant;
import com.otitan.util.ConverterUtils;
import com.otitan.zjly.R;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MapCenterActivity extends AppCompatActivity implements ValueCallBack<Object>,
        ArcgisLocation, IMap {



    @BindView(R.id.mapview)
    MapView mapView;
    @BindView(R.id.share_isearch)
    ImageButton shareIsearch;

    @BindView(R.id.ib_distance)
    ImageButton ibDistance;
    @BindView(R.id.ib_sketch)
    ImageButton ibSketch;
    @BindView(R.id.ib_clean)
    ImageButton ibClean;
    @BindView(R.id.ib_location)
    ImageButton ibLocation;

    @BindView(R.id.tckz_imageview)
    TextView tckzImageview;
    @BindView(R.id.share_xtsz)
    TextView shareXtsz;
    @BindView(R.id.share_xcxxsb)
    TextView shareXcxxsb;
    @BindView(R.id.share_tcxr)
    TextView shareTcxr;
    @BindView(R.id.titleTextView)
    TextView titleTextView;
    @BindView(R.id.close_tuceng)
    AppCompatImageView closeTuceng;
    @BindView(R.id.cb_sl)
    CheckBox cbSl;
    @BindView(R.id.tile_extent)
    ImageView tileExtent;
    @BindView(R.id.cb_ys)
    CheckBox cbYs;
    @BindView(R.id.image_extent)
    ImageView imageExtent;
    @BindView(R.id.cb_dxt)
    CheckBox cbDxt;
    @BindView(R.id.dxt_extent)
    ImageView dxtExtent;
    @BindView(R.id.tckzExplv)
    ExpandableListView tckzExplv;
    @BindView(R.id.imgClose)
    AppCompatImageView imgClose;
    @BindView(R.id.rvImg)
    RecyclerView rvImg;
    @BindView(R.id.share_tckz)
    ImageButton shareTckz;

    /*include*/
    public View include_icTckz,include_img;

    private MainModel model;
    private SketchEditor sketchEditor;
    private ToolViewModel toolViewModel;
    private InitViewModel initViewModel;
    private CalloutViewModel calloutViewModel;
    private SketchEditorViewModel sketchEditorViewModel;
    private GeoViewModel geoViewModel;
    private BootViewModel bootViewModel;


    private OpenStreetMapLayer tiledLayer;
    private ArcGISTiledLayer gisTiledLayer;
    private ArrayList<MyLayer> layers = new ArrayList<>();

    private Location location;
    private ActionModel actionModel;
    private boolean isFirst = true;
    private DataRepository dataRepository;
    private String sbh;
    private LayerManagerView layerManagerView;
    public ImgManagerView imgManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_center);
        ButterKnife.bind(this);

        initView();
        initData();
    }

    void initView(){
        include_icTckz = findViewById(R.id.icTckz);
        include_img = findViewById(R.id.icImg);
    }

    void initData() {

        sbh = TitanApplication.Companion.getInstances().getSbh();
        mapView.setAttributionTextVisible(false);

        SimpleMarkerSymbol mPointSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.SQUARE, 0xFFFF0000, 20);
        SimpleLineSymbol mLineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, 0xFFFF8800, 4);
        SimpleFillSymbol mFillSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.CROSS, 0x40FFA9A9, mLineSymbol);

        sketchEditor = new SketchEditor();
        SketchStyle sketchStyle = new SketchStyle();
        sketchStyle.setFeedbackVertexSymbol(mPointSymbol);
        sketchStyle.setLineSymbol(mLineSymbol);
        sketchStyle.setFillSymbol(mFillSymbol);
        sketchEditor.setSketchStyle(sketchStyle);


        sketchEditor.addGeometryChangedListener(new GeometryChangedListener(mapView, this));
        mapView.setSketchEditor(sketchEditor);

        initViewModel = InitViewModel.getInstance(this);
        toolViewModel = ToolViewModel.getInstance(this);
        calloutViewModel = CalloutViewModel.getInstance(this);
        geoViewModel = GeoViewModel.getInstance(this);
        bootViewModel = BootViewModel.getInstance(this, this);
        sketchEditorViewModel = new SketchEditorViewModel();

        tiledLayer = initViewModel.addOpenStreetMapLayer(mapView);
        location = gisLocation(mapView);

        layerManagerView = new LayerManagerView(this, this);
        imgManager = new ImgManagerView(this, this);
        dataRepository = Injection.INSTANCE.provideDataRepository();
    }


    @OnClick({R.id.share_isearch, R.id.ib_location, R.id.ib_clean, R.id.ib_distance,
            R.id.ib_sketch, R.id.share_xcxxsb, R.id.tckz_imageview,R.id.close_tuceng})
    public void showInfo(View view) {
        switch (view.getId()) {
            case R.id.share_isearch:
                toolViewModel.cleanAllGraphics(mapView);
                toolViewModel.cleanSketch(mapView);
                actionModel = ActionModel.IQUERY;
                toolViewModel.showInfo(mapView);
                break;
            case R.id.ib_location:
                toolViewModel.myLocation(location);
                break;
            case R.id.ib_clean:
                toolViewModel.cleanAllGraphics(mapView);
                toolViewModel.cleanSketch(mapView);
                break;
            case R.id.ib_distance:
                toolViewModel.cleanAllGraphics(mapView);
                toolViewModel.cleanSketch(mapView);
                actionModel = ActionModel.DISTANCE;
                toolViewModel.distance(mapView);
                break;
            case R.id.ib_sketch:
                toolViewModel.cleanAllGraphics(mapView);
                toolViewModel.cleanSketch(mapView);
                actionModel = ActionModel.AREA;
                toolViewModel.area(mapView);
                break;
            case R.id.share_xcxxsb:
                startActivity(UpInfoActivity.class);
                break;
            case R.id.tckz_imageview:
                bootViewModel.layerManger();
                break;
            case R.id.close_tuceng:
                layerManagerView.close();
                break;
        }
    }

    @Override
    public void onSuccess(Object o) {
        toolViewModel.cleanAllGraphics(mapView);

        Geometry geometry = mapView.getSketchEditor().getGeometry();
        if (actionModel == ActionModel.DISTANCE) {

            if (GeometryType.POLYLINE == geometry.getGeometryType()) {
                Point point = geometry.getExtent().getCenter();
                double length = Math.abs(GeometryEngine.length((Polyline) geometry));

                calloutViewModel.showValueInmap(mapView, point, length, " 米");
            }
        }

        if (actionModel == ActionModel.AREA) {
            Point point = geometry.getExtent().getCenter();
            if (GeometryType.POLYGON == geometry.getGeometryType()) {
                double area = Math.abs(GeometryEngine.area((Polygon) geometry));
                calloutViewModel.showValueInmap(mapView, point, area, " 平方米");
            }
        }

        if (actionModel == ActionModel.IQUERY) {

            toolViewModel.iquery(mapView,geometry,calloutViewModel);

        }

        if(actionModel == ActionModel.ADDFEATURE){
            sketchEditorViewModel.addFeature(layers.get(0),geometry);
        }

    }

    @Override
    public void onFail(@NotNull String code) {

    }

    /*activity跳转*/
    private void startActivity(Class<?> clz) {
        Intent intent = new Intent(MapCenterActivity.this, clz);
        intent.putExtra("lon", ConverterUtils.toDouble(location.getGpspoint().getX()));
        intent.putExtra("lat", ConverterUtils.toDouble(location.getGpspoint().getY()));
        startActivity(intent);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public Location gisLocation(final MapView mapView) {
        final Location location = new Location();

        final LocationDisplay display = mapView.getLocationDisplay();
        display.addLocationChangedListener(new LocationDisplay.LocationChangedListener() {
            @Override
            public void onLocationChanged(LocationDisplay.LocationChangedEvent event) {
                Point point = event.getLocation().getPosition();
                if (point != null) {
                    location.setGpspoint(point);
                }

                if (isFirst) {
                    mapView.setViewpointCenterAsync(point, Constant.INSTANCE.getDefalutScale());
                    isFirst = false;
                }

                Point map = display.getMapLocation();
                if (map != null) {
                    location.setMappoint(map);
                }

                addPoint(location);
            }
        });
        display.startAsync();
        location.setMapView(mapView);
        return location;
    }

    @Override
    protected void onStart() {
        super.onStart();
        isFirst = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isFirst = true;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        isFirst = true;
    }

    void addPoint(Location location) {
        final String lon = ConverterUtils.toString(location.getGpspoint().getX());
        final String lat = ConverterUtils.toString(location.getGpspoint().getY());
        dataRepository.addPointToServer(lon, lat, sbh, new ValueCallBack<Object>() {
            @Override
            public void onSuccess(Object o) {
                dataRepository.addLocalPoint(lon, lat, sbh, "1");
            }

            @Override
            public void onFail(@NotNull String code) {
                dataRepository.addLocalPoint(lon, lat, sbh, "0");
            }
        });

    }


    @Override
    public void showTckz() {
        include_icTckz.setVisibility(View.VISIBLE);
        layerManagerView.initView();
    }

    @Nullable
    @Override
    public OpenStreetMapLayer getOpenStreetLayer() {
        return tiledLayer;
    }

    @NotNull
    @Override
    public ArrayList<MyLayer> getLayers() {
        return layers;
    }

    @Nullable
    @Override
    public ArcGISTiledLayer getTiledLayer() {
        return gisTiledLayer;
    }
}
