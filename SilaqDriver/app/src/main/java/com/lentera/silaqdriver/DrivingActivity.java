package com.lentera.silaqdriver;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.gesture.GestureLibraries;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.collection.LLRBNode;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.lentera.silaqdriver.common.Common;
import com.lentera.silaqdriver.common.LatLngInterpolator;
import com.lentera.silaqdriver.common.MarkerAnimation;
import com.lentera.silaqdriver.model.DrivingOrderModel;
import com.lentera.silaqdriver.model.OrderModel;
import com.lentera.silaqdriver.remote.IGoogleAPI;
import com.lentera.silaqdriver.remote.RetrofitClient;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.paperdb.Paper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;

public class DrivingActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private Marker driverMarker;
    private DrivingOrderModel drivingOrderModel;

    //animasi
    private Handler handler;
    private int index, next;
    private LatLng start, end;
    private float v;
    private double lat, lng;
    private Polyline blackPolyline, greyPolyline;
    private PolylineOptions polylineOptions, blackPolylineOptions;
    private List<LatLng> polylineList;
    private IGoogleAPI iGoogleAPI;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @BindView(R.id.txt_order_number)
    TextView txt_order_number;
    @BindView(R.id.txt_name)
    TextView txt_name;
    @BindView(R.id.txt_address)
    TextView txt_addres;
    @BindView(R.id.txt_date)
    TextView txt_date;
    @BindView(R.id.txt_total)
    TextView txt_total;


    @BindView(R.id.btn_start)
    MaterialButton btn_start;
    @BindView(R.id.btn_call)
    MaterialButton btn_call;
    @BindView(R.id.btn_done)
    MaterialButton btn_done;

    @BindView(R.id.img_food_image)
    ImageView img_food_image;

    AutocompleteSupportFragment places_fragment;
    PlacesClient placesClient;
    List<Place.Field> placeFields = Arrays.asList(Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG);
    private Polyline bluePolyline;

    @OnClick(R.id.btn_call)
    void onCallClick() {
        if (drivingOrderModel != null) {
             if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                 Dexter.withActivity(this)
                         .withPermission(Manifest.permission.CALL_PHONE)
                         .withListener(new PermissionListener() {
                             @Override
                             public void onPermissionGranted(PermissionGrantedResponse response) {

                             }

                             @Override
                             public void onPermissionDenied(PermissionDeniedResponse response) {
                                 Toast.makeText(DrivingActivity.this, "Izinkan aplikasi melakukan panggilan", Toast.LENGTH_SHORT).show();
                             }

                             @Override
                             public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                             }
                         }).check();
                  return;
            }
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse(new StringBuilder("tel:")
                    .append(drivingOrderModel.getOrderModel().getUserPhone()).toString()));

            startActivity(intent);
        }
    }


    @OnClick(R.id.btn_start)
    void onStartTripClick(){
        String data = Paper.book().read(Common.DRIVING_ORDER_DATA);
        Paper.book().write(Common.TRIP_START,data);
        btn_start.setEnabled(false);

        drawRoutes(data);
    }

    private boolean isInit = false;
    private Location previousLocation =null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driving);

        iGoogleAPI = RetrofitClient.getInstance().create(IGoogleAPI.class);

        initPlaces();
        setAutoCompletePlaces();

        ButterKnife.bind(this);
        buildLocationRequest();
        buildLocationCallback();


        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                                .findFragmentById(R.id.map);
                        mapFragment.getMapAsync(DrivingActivity.this::onMapReady);

                        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(DrivingActivity.this);
                        fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper());
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(DrivingActivity.this, "Anda harus menghidupkan GPS untuk menggunakan aplikasi ini !", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();


    }

    private void setAutoCompletePlaces() {
       places_fragment = (AutocompleteSupportFragment)getSupportFragmentManager()
       .findFragmentById(R.id.places_autocomplete_fragment);
       places_fragment.setPlaceFields(placeFields);
       places_fragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
           @Override
           public void onPlaceSelected(@NonNull Place place) {
               Toast.makeText(DrivingActivity.this, new StringBuilder(place.getName())
                       .append("-")
                       .append(place.getLatLng().toString()), Toast.LENGTH_SHORT).show();

           }

           @Override
           public void onError(@NonNull Status status) {
               Toast.makeText(DrivingActivity.this, ""+status.getStatusMessage(), Toast.LENGTH_SHORT).show();
           }
       });
    }

    private void initPlaces() {
        Places.initialize(this, getString(R.string.google_api_key));
        placesClient = Places.createClient(this);
    }

    private void setDrivingOrder() {
        Paper.init(this);
        String data;

        if (TextUtils.isEmpty(Paper.book().read(Common.TRIP_START))){
            btn_start.setEnabled(true);
            data = Paper.book().read(Common.DRIVING_ORDER_DATA);
        }else {
            btn_start.setEnabled(false);
            data = Paper.book().read(Common.TRIP_START);
        }

        if (!TextUtils.isEmpty(data)){
            drawRoutes(data);
            drivingOrderModel = new Gson()
                    .fromJson(data, new TypeToken<DrivingOrderModel>(){}.getType());
            if (drivingOrderModel != null){
                Common.setSpanStringColor("Nama: ",
                drivingOrderModel.getOrderModel().getUserName(),txt_name,
                        Color.parseColor("#1B1C1E"));

                txt_date.setText(new StringBuilder()
                        .append("Tanggal: ")
                .append(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
                .format(drivingOrderModel.getOrderModel().getCreateDate())));

                Common.setSpanStringColor("No. : ",
                        drivingOrderModel.getOrderModel().getKey(),txt_order_number,
                        Color.parseColor("#1B1C1E"));

                Common.setSpanStringColor("Alamat: ",
                        drivingOrderModel.getOrderModel().getShippingAddress(),txt_addres,
                        Color.parseColor("#1B1C1E"));

                Common.setSpanStringColor("Total: ",
                        String.valueOf(drivingOrderModel.getOrderModel().getTotalPayment()),txt_total,Color.parseColor("#1B1C1E"));

                Glide.with(this).load(drivingOrderModel.getOrderModel().getCartItemList().get(0)
                .getFoodImage())
                        .into(img_food_image);
            }
        }
        else {
            Toast.makeText(this, "Pesanan diantar kosong", Toast.LENGTH_SHORT).show();
        }
    }

    private void drawRoutes(String data) {
        DrivingOrderModel drivingOrderModel  = new Gson()
                .fromJson(data, new  TypeToken<DrivingOrderModel>(){}.getType());

        //add box
        mMap.addMarker(new MarkerOptions()
        .icon(BitmapDescriptorFactory.fromResource(R.drawable.box))
        .title(drivingOrderModel.getOrderModel().getUserName())
        .snippet(drivingOrderModel.getOrderModel().getShippingAddress())
        .position(new LatLng(drivingOrderModel.getOrderModel().getLat()
                ,drivingOrderModel.getOrderModel().getLng())));

        fusedLocationProviderClient.getLastLocation()
                .addOnFailureListener(e -> Toast.makeText(DrivingActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show())
                .addOnSuccessListener(location -> {
                    String to = new StringBuilder()
                            .append(drivingOrderModel.getOrderModel().getLat())
                            .append(",")
                            .append(drivingOrderModel.getOrderModel().getLng())
                            .toString();
                    String from = new StringBuilder()
                            .append(location.getLatitude())
                            .append(",")
                            .append(location.getLongitude())
                            .toString();


                    compositeDisposable.add(iGoogleAPI.getDirections("driving",
                            "less_driving",
                            from,to,
                            getString(R.string.google_maps_key))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(s -> {

                        try {
                            JSONObject jsonObject = new JSONObject(s);
                            JSONArray jsonArray = jsonObject.getJSONArray("routes");
                            for (int i=0;i<jsonArray.length();i++){
                                JSONObject route= jsonArray.getJSONObject(i);
                                JSONObject poly = route.getJSONObject("overview_polyline");
                                String polyline = poly.getString("points");
                                polylineList =Common.decodePoly(polyline);
                            }

                            polylineOptions = new PolylineOptions();
                            polylineOptions.color(Color.BLUE);
                            polylineOptions.width(12);
                            polylineOptions.startCap(new SquareCap());
                            polylineOptions.jointType(JointType.ROUND);
                            polylineOptions.addAll(polylineList);
                            bluePolyline = mMap.addPolyline(polylineOptions);
                        }
                        catch (Exception e){
                            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }, throwable -> Toast.makeText(DrivingActivity.this, ""+throwable.getMessage(), Toast.LENGTH_SHORT).show())) ;
                });
    }



    private void buildLocationCallback() {
        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                LatLng locationDriver = new LatLng(locationResult.getLastLocation().getLatitude(),
                        locationResult.getLastLocation().getLongitude());

                if (driverMarker == null){
                    int height,width;
                    height = width =150;
                    BitmapDrawable bitmapDrawable = (BitmapDrawable) ContextCompat
                            .getDrawable(DrivingActivity.this, R.drawable.driver);
                    Bitmap reseized = Bitmap.createScaledBitmap(bitmapDrawable.getBitmap(),width,height,false);

                    driverMarker =   mMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromBitmap(reseized))
                            .position(locationDriver).title("Anda"));

                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationDriver,18));

                }


                if (isInit && previousLocation != null){
                    String from = new StringBuilder()
                            .append(previousLocation.getLatitude())
                            .append(",")
                            .append(previousLocation.getLongitude())
                            .toString();
                    String to = new StringBuilder()
                            .append(locationDriver.latitude)
                            .append(",")
                            .append(locationDriver.longitude)
                            .toString();

                    moveMarkerAnimation(driverMarker,from,to);
                     previousLocation= locationResult.getLastLocation();
                }
                if (!isInit){
                    isInit = true;
                    previousLocation = locationResult.getLastLocation();
                }

//                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationDriver,15));
          }
       };

    }

//    private void showAddress(){
//
//    }

    private void moveMarkerAnimation(Marker marker, String from, String to) {
        compositeDisposable.add(iGoogleAPI.getDirections("driving",
                "less_driving",
                from,to,
                getString(R.string.google_maps_key))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(returnResult -> {
                    try {
                        JSONObject jsonObject = new JSONObject(returnResult);
                        JSONArray jsonArray = jsonObject.getJSONArray("routes");

                        for (int i= 0; i<jsonArray.length();i++){
                            JSONObject route = jsonArray.getJSONObject(i);
                            JSONObject poly = route.getJSONObject("overview_polyline");
                            String polyline = poly.getString("points");
                            polylineList = Common.decodePoly(polyline);
                        }

                        polylineOptions = new PolylineOptions();
                        polylineOptions.color(Color.GRAY);
                        polylineOptions.width(5);
                        polylineOptions.startCap(new SquareCap());
                        polylineOptions.jointType(JointType.ROUND);
                        polylineOptions.addAll(polylineList);
                        greyPolyline = mMap.addPolyline(polylineOptions);

                        blackPolylineOptions = new PolylineOptions();
                        blackPolylineOptions.color(Color.BLACK);
                        blackPolylineOptions.width(5);
                        blackPolylineOptions.startCap(new SquareCap());
                        blackPolylineOptions.jointType(JointType.ROUND);
                        blackPolylineOptions.addAll(polylineList);
                        blackPolyline = mMap.addPolyline(blackPolylineOptions);

                        //Animator
                        ValueAnimator polylineAnimator = ValueAnimator.ofInt(0,100);
                        polylineAnimator.setDuration(2000);
                        polylineAnimator.setInterpolator(new LinearInterpolator());
                        polylineAnimator.addUpdateListener(animation -> {
                            List<LatLng> points = greyPolyline.getPoints();
                            int percentValue = (int)animation.getAnimatedValue();
                            int size = points.size();
                            int newPoints = (int)(size*(percentValue/100.0f));
                            List<LatLng> p = points.subList(0, newPoints);
                            blackPolyline.setPoints(p);
                        });
                        polylineAnimator.start();

                        handler = new Handler();
                        index = -1;
                        next = 1;
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (index <polylineList.size()-1){
                                    index++;
                                    next= index+1;
                                    start = polylineList.get(index);
                                    end = polylineList.get(next);
                                }
                                ValueAnimator valueAnimator = ValueAnimator.ofInt(0,1);
                                valueAnimator.setDuration(1500);
                                valueAnimator.setInterpolator(new LinearInterpolator());
                                valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                    @Override
                                    public void onAnimationUpdate(ValueAnimator animation) {
                                        v = valueAnimator.getAnimatedFraction();
                                        lng = v*end.longitude+(1-v)
                                                *start.longitude;
                                        lat = v*end.latitude+(1-v)
                                                *start.latitude;
                                        LatLng newPos = new LatLng(lat,lng);
                                        marker.setPosition(newPos);
                                        marker.setAnchor(0.5f,0.5f);
                                        marker.setRotation(Common.getBearing(start, newPos));

                                        mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
                                    }
                                });

                                valueAnimator.start();
                                if (index<polylineList.size()-2)
                                    handler.postDelayed(this, 1500);
                            }
                        },1500);

                    }catch (Exception e){
                        Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }, throwable -> {
                    if (throwable != null)
                        Toast.makeText(DrivingActivity.this, ""+throwable, Toast.LENGTH_SHORT).show();
                }));
    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(15000); //15 detik
        locationRequest.setFastestInterval(10000);
        locationRequest.setSmallestDisplacement(20f);

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        setDrivingOrder();
        mMap.getUiSettings().setZoomControlsEnabled(true);

        try {
            boolean success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,
                    R.raw.uber_light_with_label));
            if (!success)
                Log.e("Silaq", "Style gagal memuat");
        }catch (Resources.NotFoundException ex){
            Log.e("Silaq","Sumber tidak ada");
        }
    }

    @Override
    protected void onDestroy() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        compositeDisposable.clear();
        super.onDestroy();
    }
}
