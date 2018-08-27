package com.edu.usc.example.placessearch;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.ContactsContract;
import android.service.carrier.CarrierMessagingService;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.GeoDataApi;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.PlacePhotoResult;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Main3Activity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener, GestureDetector.OnGestureListener,OnMapReadyCallback {
    private TextView mSelectTv;

    private ListView mTypeLv;

    private TextView mSelectTv1;

    private TextView mSelectTv2;

    private ListView mTypeLv1;

    private ListView mTypeLv2;

    private PopupWindow typeSelectPopup;

    private PopupWindow typeSelectPopup1;

    private PopupWindow typeSelectPopup2;

    private List <String> testData;

    private List <String> testData1;

    private List <String> testData2;

    private ArrayAdapter <String> testDataAdapter;

    private ArrayAdapter <String> testDataAdapter1;

    private ArrayAdapter <String> testDataAdapter2;

    private Place myPlace;

    private GoogleApiClient mGoogleApiClient;

    private String twitterUrl;

    private JSONObject placeDetail;

    private JSONArray placeReview;

    private ViewFlipper viewFlipper = null;

    private GestureDetector gestureDetector = null;

    private PlacePhotoMetadataBuffer photoMetadataBuffer;

    private Handler UIhander = new Handler();

    private PlaceAutoCompleteAdapter mPlaceAutocompleteAdapter;

    private String latitude="";

    private String longtitude= "";

    private String OrgLatitude="";

    private String OrgLongtitude = "";

    private GoogleMap mGoogleMap;

    ArrayList<LatLng> pointList;

    private ReviewRes[] reviewStore;

    private ReviewRes[] yelpStore;

    private MyReviewAdapter myReviewAdapter1=null;

    private MyReviewAdapter myReviewAdapter2=null;

    ListView mListView;

    /** A class to parse the Google Places in JSON format */
    private class ParserTask extends
            AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(
                String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
                System.out.println("do in background:" + routes);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(15);


                // Changing the color polyline according to the mode
                lineOptions.color(Color.BLUE);
                lineOptions.geodesic(true);
            }
            if(lineOptions!=null) {

                // Drawing polyline in the Google Map for the i-th route
                mGoogleMap.addPolyline(lineOptions);
            }else{
                Toast.makeText(getApplicationContext(),"Direction not found",Toast.LENGTH_SHORT).show();
            }
        }
    }

    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(
            new LatLng(-40,-168),new LatLng(71,136)
    );

    private ResultCallback <PlacePhotoResult> mDisplayPhotoResultCallback
            = new ResultCallback <PlacePhotoResult>() {
        @Override
        public void onResult(PlacePhotoResult placePhotoResult) {
            if (!placePhotoResult.getStatus().isSuccess()) {
                return;
            }

            ImageView mImageView = new ImageView(getApplicationContext());
            mImageView.setPadding(20, 20, 20, 20);
            Bitmap image = placePhotoResult.getBitmap();
            int width  = image.getWidth();
            Log.i("width",String.valueOf(width));

            int height = image.getHeight();
            Log.i("height",String.valueOf(height));

            int newWidth = 1080;

            float ratio = height/width;

            float newHeight = newWidth*ratio;
            Log.i("newHeight",String.valueOf(newHeight));

            float scale = ((float) newWidth/width);

            Matrix matrix = new Matrix();

            matrix.postScale(scale,scale);

            image = Bitmap.createBitmap(image,0,0,width,height,matrix,true);

            Log.i("bitMapWidth",String.valueOf(image.getWidth()));

            Log.i("bitMapHeight",String.valueOf(image.getHeight()));


            mImageView.setLayoutParams(new LinearLayout.LayoutParams((int) image.getWidth(), (int) image.getHeight()));
            mImageView.setImageBitmap(image);
            LinearLayout photoLayout = findViewById(R.id.layOutPhoto);
            photoLayout.addView(mImageView);
        }
    };

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void adjustTvTextSize(TextView tv, int maxWidth, String text) {
        int avaiWidth = maxWidth - tv.getPaddingLeft() - tv.getPaddingRight() - 10;

        if (avaiWidth <= 0) {
            return;
        }

        TextPaint textPaintClone = new TextPaint(tv.getPaint());
        // note that Paint text size works in px not sp
        float trySize = textPaintClone.getTextSize();

        while (textPaintClone.measureText(text) > avaiWidth) {
            trySize--;
            textPaintClone.setTextSize(trySize);
        }

        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, trySize);
    }

    private void call(String phone) {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void initUI() {
        mSelectTv =  findViewById(R.id.tv_select_input1);
        mSelectTv1 = findViewById(R.id.selectiveReview);
        mSelectTv2 = findViewById(R.id.selectiveButton);
    }

    private void initListener() {
        mSelectTv.setOnClickListener(this);
        mSelectTv1.setOnClickListener(this);
        mSelectTv2.setOnClickListener(this);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_select_input1:

                initSelectPopup();

                if (typeSelectPopup != null && !typeSelectPopup.isShowing()) {
                    typeSelectPopup.showAsDropDown(mSelectTv, 0, 10);
                }
                break;
            case R.id.selectiveReview:
                initSelectPopup1();

                if (typeSelectPopup1 != null && !typeSelectPopup1.isShowing()) {
                    typeSelectPopup1.showAsDropDown(mSelectTv1, 0, 10);
                }
                break;
            case R.id.selectiveButton:
                initSelectPopup2();

                if (typeSelectPopup2 != null && !typeSelectPopup2.isShowing()) {
                    typeSelectPopup2.showAsDropDown(mSelectTv2, 0, 10);
                }
                break;
        }
    }

    private void initSelectPopup() {
        mTypeLv = new ListView(this);
        TestData();
        // 设置适配器
        testDataAdapter = new ArrayAdapter <String>(this, R.layout.popup_text_item, testData);
        mTypeLv.setAdapter(testDataAdapter);

        // 设置ListView点击事件监听
        mTypeLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView <?> parent, View view, int position, long id) {
                // 在这里获取item数据
                String value = testData.get(position);


                if(OrgLatitude.trim()!=""&&OrgLongtitude.trim()!=""){
                    if(pointList.size()>=2){
                        pointList.clear();
                        mGoogleMap.clear();
                    }
                    LatLng org = new LatLng(Double.valueOf(latitude),Double.valueOf(longtitude));
                    LatLng dest = new LatLng(Double.valueOf(OrgLatitude),Double.valueOf(OrgLongtitude));
                    pointList.add(org);
                    pointList.add(dest);
                    double centerX = (Double.valueOf(latitude)+Double.valueOf(OrgLatitude))/2;
                    double centerY = (Double.valueOf(longtitude)+Double.valueOf(OrgLongtitude))/2;
                    LatLng center = new LatLng(centerX,centerY);
                    String url = getDirectionsUrl(org,dest,value.toLowerCase());
                    JSONObject jsonObject = getJson(url);
                    ParserTask parserTask = new ParserTask();
                    parserTask.execute(jsonObject.toString());
                    mGoogleMap.addMarker(new MarkerOptions().position(org).title("Here is destination"));
                    mGoogleMap.addMarker(new MarkerOptions().position(dest).title("Here is where you from"));
                    int size = getMapSize(org,dest);
                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center,size));

                }


                mSelectTv.setText(value);
                // 选择完后关闭popup窗口
                typeSelectPopup.dismiss();
            }
        });
        typeSelectPopup = new PopupWindow(mTypeLv, mSelectTv.getWidth(), ActionBar.LayoutParams.WRAP_CONTENT, true);
        // 取得popup窗口的背景图片
        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.bg_corner);
        typeSelectPopup.setBackgroundDrawable(drawable);
        typeSelectPopup.setFocusable(true);
        typeSelectPopup.setOutsideTouchable(true);
        typeSelectPopup.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                // 关闭popup窗口
                typeSelectPopup.dismiss();
            }
        });
    }
    private void initSelectPopup1() {
        mTypeLv1 = new ListView(this);
        TestData1();
        // 设置适配器
        testDataAdapter1 = new ArrayAdapter <String>(this, R.layout.popup_text_item, testData1);
        mTypeLv1.setAdapter(testDataAdapter1);

        // 设置ListView点击事件监听
        mTypeLv1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView <?> parent, View view, int position, long id) {
                // 在这里获取item数据
                String value = testData1.get(position);

                mSelectTv1.setText(value);

                ListView listView = findViewById(R.id.myReviewList);

                if(value.equals("Google reviews")){
                    if(myReviewAdapter1!=null) {
                        listView.setAdapter(myReviewAdapter1);
                    }
                }
                if(value.equals("Yelp reviews")){
                    if(myReviewAdapter2!=null)
                    listView.setAdapter(myReviewAdapter2);
                    else {
                        ArrayList<String> list =new ArrayList <String>();
                        list.add("No reviews");
                        ArrayAdapter arrayAdapter = new ArrayAdapter(getApplicationContext(),R.layout.noreview,R.id.noReview,list);
                        listView.setAdapter(arrayAdapter);


                    }
                }

                // 选择完后关闭popup窗口
                typeSelectPopup1.dismiss();
            }
        });
        typeSelectPopup1 = new PopupWindow(mTypeLv1, mSelectTv1.getWidth(), ActionBar.LayoutParams.WRAP_CONTENT, true);
        // 取得popup窗口的背景图片
        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.bg_corner);
        typeSelectPopup1.setBackgroundDrawable(drawable);
        typeSelectPopup1.setFocusable(true);
        typeSelectPopup1.setOutsideTouchable(true);
        typeSelectPopup1.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                // 关闭popup窗口
                typeSelectPopup1.dismiss();
            }
        });
    }
    private void initSelectPopup2() {
        mTypeLv2 = new ListView(this);
        TestData2();
        // 设置适配器
        testDataAdapter2 = new ArrayAdapter <String>(this, R.layout.popup_text_item, testData2);
        mTypeLv2.setAdapter(testDataAdapter2);

        // 设置ListView点击事件监听
        mTypeLv2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView <?> parent, View view, int position, long id) {
                // 在这里获取item数据
                String value = testData2.get(position);

                mSelectTv2.setText(value);
                if(value.equals("Highest rating")){

                    if(myReviewAdapter1!=null) {
                        myReviewAdapter1.sort(new CompareByRateD());
                    }
                    if(myReviewAdapter2!=null) {
                        myReviewAdapter2.sort(new CompareByRateD());
                    }
                }else if(value.equals("Lowest rating")){
                    if(myReviewAdapter1!=null) {
                        myReviewAdapter1.sort(new CompareByRateI());
                    }
                    if(myReviewAdapter2!=null) {
                        myReviewAdapter2.sort(new CompareByRateI());
                    }
                }else if(value.equals("Most recent")){

                    if(myReviewAdapter1!=null) {
                        myReviewAdapter1.sort(new CompareByTimeD());
                    }
                    if(myReviewAdapter2!=null) {
                        myReviewAdapter2.sort(new CompareByTimeD());
                    }
                }else if(value.equals("Least recent")){
                    if(myReviewAdapter1!=null) {
                        myReviewAdapter1.sort(new CompareByTimeI());
                    }
                    if(myReviewAdapter2!=null) {
                        myReviewAdapter2.sort(new CompareByTimeI());
                    }
                }else if(value.equals("Default order")){
                    if(myReviewAdapter1!=null) {
                        myReviewAdapter1.sort(new CompareByDefault());
                    }
                    if(myReviewAdapter2!=null) {
                        myReviewAdapter2.sort(new CompareByDefault());
                    }
                }
                // 选择完后关闭popup窗口
                typeSelectPopup2.dismiss();
            }
        });
        typeSelectPopup2 = new PopupWindow(mTypeLv2, mSelectTv2.getWidth(), ActionBar.LayoutParams.WRAP_CONTENT, true);
        // 取得popup窗口的背景图片
        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.bg_corner);
        typeSelectPopup2.setBackgroundDrawable(drawable);
        typeSelectPopup2.setFocusable(true);
        typeSelectPopup2.setOutsideTouchable(true);
        typeSelectPopup2.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                // 关闭popup窗口
                typeSelectPopup2.dismiss();
            }
        });
    }

    private void TestData() {
        testData = new ArrayList <String>();
        testData.add("Driving");
        testData.add("Bicycling");
        testData.add("Transit");
        testData.add("Walking");
    }

    private void TestData1(){
        testData1 = new ArrayList <String>();
        testData1.add("Google reviews");
        testData1.add("Yelp reviews");
    }

    private void TestData2(){
        testData2 = new ArrayList <String>();
        testData2.add("Default order");
        testData2.add("Highest rating");
        testData2.add("Lowest rating");
        testData2.add("Most recent");
        testData2.add("Least recent");
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest,String TravelMode) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + ","
                + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Travelling Mode
        String mode = "mode="+TravelMode;


        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&"
                + mode;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/"
                + output + "?" + parameters;
        System.out.println("getDerectionsURL--->: " + url);
        return url;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        mListView = findViewById(R.id.myReviewList);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView <?> parent, View view, int position, long id) {
                TextView reviewGroup = findViewById(R.id.selectiveReview);
                String selectContent = reviewGroup.getText().toString();
                if(selectContent.equals("Google reviews")){
                    String url = reviewStore[position].getUrl();
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.addCategory(Intent.CATEGORY_BROWSABLE);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                }else if(selectContent.equals("Yelp reviews")){
                    String url = yelpStore[position].getUrl();
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.addCategory(Intent.CATEGORY_BROWSABLE);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                }
            }
        });
        final ImageView heartView = findViewById(R.id.imageView7);
        if(MainActivity.curPlace.getId()==-1){
            heartView.setImageResource(R.drawable.heart_whiteborder);
        }if(MainActivity.curPlace.getId()==1){
            heartView.setImageResource(R.drawable.ic_favorite_black_24dp);
        }
        ImageView returnBack = findViewById(R.id.returnBackFrom3);
        returnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        final LinearLayout infoButton = findViewById(R.id.buttonInfo);
        LinearLayout photoButton = findViewById(R.id.buttonPhoto);
        LinearLayout mapButton = findViewById(R.id.buttonMap);
        LinearLayout reviewButton = findViewById(R.id.buttonReview);
        final LinearLayout infoContent = findViewById(R.id.layOutInfo);
        final ScrollView photoContent = findViewById(R.id.scrollPhoto);
        final LinearLayout mapContent = findViewById(R.id.layOutMap);
        final LinearLayout reviewContent = findViewById(R.id.layOutReview);
        final TextView bottomInfo = findViewById(R.id.bottomInfo);
        final TextView bottomPhoto =findViewById(R.id.bottomPhoto);
        final TextView bottomMap = findViewById(R.id.bottomMap);
        final TextView bottomReview = findViewById(R.id.bottomReview);

        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                infoContent.setVisibility(View.VISIBLE);
                photoContent.setVisibility(View.GONE);
                mapContent.setVisibility(View.GONE);
                reviewContent.setVisibility(View.GONE);
                bottomInfo.setVisibility(View.VISIBLE);
                bottomPhoto.setVisibility(View.INVISIBLE);
                bottomMap.setVisibility(View.INVISIBLE);
                bottomReview.setVisibility(View.INVISIBLE);
            }
        });

        photoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                infoContent.setVisibility(View.GONE);
                photoContent.setVisibility(View.VISIBLE);
                mapContent.setVisibility(View.GONE);
                reviewContent.setVisibility(View.GONE);
                bottomInfo.setVisibility(View.INVISIBLE);
                bottomPhoto.setVisibility(View.VISIBLE);
                bottomMap.setVisibility(View.INVISIBLE);
                bottomReview.setVisibility(View.INVISIBLE);
            }
        });

        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                infoContent.setVisibility(View.GONE);
                photoContent.setVisibility(View.GONE);
                mapContent.setVisibility(View.VISIBLE);
                reviewContent.setVisibility(View.GONE);
                bottomInfo.setVisibility(View.INVISIBLE);
                bottomPhoto.setVisibility(View.INVISIBLE);
                bottomMap.setVisibility(View.VISIBLE);
                bottomReview.setVisibility(View.INVISIBLE);
            }
        });

        reviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                infoContent.setVisibility(View.GONE);
                photoContent.setVisibility(View.GONE);
                mapContent.setVisibility(View.GONE);
                reviewContent.setVisibility(View.VISIBLE);
                bottomInfo.setVisibility(View.INVISIBLE);
                bottomPhoto.setVisibility(View.INVISIBLE);
                bottomMap.setVisibility(View.INVISIBLE);
                bottomReview.setVisibility(View.VISIBLE);
            }
        });

        pointList = new ArrayList <>();

        initUI();

        initListener();

        viewFlipper = findViewById(R.id.viewflipper);

        gestureDetector = new GestureDetector(this);

        heartView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(MainActivity.curPlace.getId()==-1){
                    heartView.setImageResource(R.drawable.ic_favorite_black_24dp);
                    MainActivity.curPlace.setId(1);
                    MainActivity.myArrayAdapter.add(MainActivity.curPlace);
                    Main2Activity.myArrayAdapter.remove(MainActivity.curPlace);
                    Main2Activity.myArrayAdapter.insert(MainActivity.curPlace,Main2Activity.position);

                    Toast.makeText(getApplicationContext(), MainActivity.curPlace.getPlaceName()+"was added to favorites", Toast.LENGTH_LONG).show();
                }else if(MainActivity.curPlace.getId()==1){
                    heartView.setImageResource(R.drawable.heart_whiteborder);
                    MainActivity.curPlace.setId(-1);
                    MainActivity.myArrayAdapter.remove(MainActivity.curPlace);
                    Main2Activity.myArrayAdapter.remove(MainActivity.curPlace);
                    Main2Activity.myArrayAdapter.insert(MainActivity.curPlace,Main2Activity.position);

                    Toast.makeText(getApplicationContext(), MainActivity.curPlace.getPlaceName()+"was removed from favorites", Toast.LENGTH_LONG).show();
                }
            }
        });

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();
        mPlaceAutocompleteAdapter = new PlaceAutoCompleteAdapter(this,Places.getGeoDataClient(this,null),LAT_LNG_BOUNDS,null);
        AutoCompleteTextView autoText = findViewById(R.id.AutoText);
        autoText.setAdapter(mPlaceAutocompleteAdapter);
        autoText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.i("count",String.valueOf(count));
                Log.i("start",String.valueOf(start));
                Log.i("before",String.valueOf(before));
                Log.i("seq",s.toString());
                if(count-before>=2){
                    String placeValue = s.toString().replace(' ','+');
                    String placeUrl = "https://maps.googleapis.com/maps/api/geocode/json" +
                            "?address="+placeValue+"&key=AIzaSyD0jMKsDzqfp7k6Pa6Hw8GO-DmTK-38TgM";
                    JSONObject placeJson = getJson(placeUrl);
                    try{
                        OrgLatitude=placeJson.getJSONArray("results").getJSONObject(0).
                                getJSONObject("geometry").getJSONObject("location").getString("lat");

                        OrgLongtitude = placeJson.getJSONArray("results").getJSONObject(0).
                                getJSONObject("geometry").getJSONObject("location").getString("lng");
                        Log.i("lat",OrgLatitude);
                        Log.i("lon",OrgLongtitude);
                        LatLng org = new LatLng(Double.valueOf(latitude),Double.valueOf(longtitude));
                        LatLng dest = new LatLng(Double.valueOf(OrgLatitude),Double.valueOf(OrgLongtitude));
                        if(pointList.size()>=2){
                            pointList.clear();
                            mGoogleMap.clear();
                        }
                        pointList.add(org);
                        pointList.add(dest);
                        double centerX = (Double.valueOf(latitude)+Double.valueOf(OrgLatitude))/2;
                        double centerY = (Double.valueOf(longtitude)+Double.valueOf(OrgLongtitude))/2;
                        LatLng center = new LatLng(centerX,centerY);
                        TextView et = findViewById(R.id.tv_select_input1);
                        String url = getDirectionsUrl(org,dest,et.getText().toString().toLowerCase());
                        JSONObject jsonObject = getJson(url);
                        ParserTask parserTask = new ParserTask();
                        parserTask.execute(jsonObject.toString());
                        mGoogleMap.addMarker(new MarkerOptions().position(dest).title("Here is where you from"));
                        mGoogleMap.addMarker(new MarkerOptions().position(org).title("Here is destination"));
                        int size = getMapSize(org,dest);
                        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center,size));
                    }catch (Exception e){

                    }
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        Intent intent = getIntent();
        final String placeId = intent.getStringExtra("placeId");

        Toast.makeText(Main3Activity.this, placeId, Toast.LENGTH_SHORT).show();

        Places.GeoDataApi.getPlacePhotos(mGoogleApiClient, placeId).setResultCallback(new ResultCallback <PlacePhotoMetadataResult>() {
            @Override
            public void onResult(@NonNull PlacePhotoMetadataResult photos) {
                if (!photos.getStatus().isSuccess()) {
                    LinearLayout photoLayOut = findViewById(R.id.layOutPhoto);
                    TextView tv = new TextView(getApplicationContext());
                    tv.setText("No photos");
                    tv.setGravity(Gravity.CENTER);
                    tv.setWidth(380);
                    tv.setHeight(470);
                    tv.setPadding(0,150,0,0);
                    photoLayOut.addView(tv);
                    return;
                }

                photoMetadataBuffer = photos.getPhotoMetadata();

                for (int i = 0; i < photoMetadataBuffer.getCount(); i++) {
                    photoMetadataBuffer.get(i).getPhoto(mGoogleApiClient).setResultCallback(mDisplayPhotoResultCallback);
                }

//                        for (int i = 0;i<photoMetadataBuffer.getCount();i++){
//                            PlacePhotoMetadata photo = photoMetadataBuffer.get(i);
//                            Bitmap image = photo.getScaledPhoto(mGoogleApiClient,380,2000).await().getBitmap();
//
//                            ImageView imageView = new ImageView(this);
//
//                            imageView.setLayoutParams(new ViewGroup.LayoutParams(image.getWidth(),image.getHeight()));
//
//                            LinearLayout photoLayout = findViewById(R.id.layOutPhoto);
//
//                            photoLayout.addView(imageView);
//
//                        }
                photoMetadataBuffer.release();

            }
        });

        String placeUrl = "https://maps.googleapis.com/maps/api/place/details/json?placeid=" + placeId + "&key=AIzaSyD0jMKsDzqfp7k6Pa6Hw8GO-DmTK-38TgM";
        String placeName="";
        String address1 = "";
        String city="";
        String state = "";
        String country = "";
        String phoneNumber = "";
        try {
            placeDetail = getJson(placeUrl).getJSONObject("result");
            latitude = placeDetail.getJSONObject("geometry").getJSONObject("location").getString("lat");
            longtitude = placeDetail.getJSONObject("geometry").getJSONObject("location").getString("lng");
            placeReview = placeDetail.getJSONArray("reviews");

            placeName = placeDetail.getString("name");

            placeName = URLEncoder.encode(placeName);

            address1 = placeDetail.getString("formatted_address");

            address1 = URLEncoder.encode(address1);

            phoneNumber = placeDetail.getString("formatted_phone_number");

            phoneNumber = URLEncoder.encode(phoneNumber);
            JSONArray address_components = placeDetail.getJSONArray("address_components");
            for(int i=0;i<address_components.length();i++){
                String type = address_components.getJSONObject(i).getJSONArray("types").getString(0);
                if(type.equals("administrative_area_level_2")){
                    city=address_components.getJSONObject(i).getString("short_name");
                    city = URLEncoder.encode(city);
                }
                if(type.equals("administrative_area_level_1")){
                    state = address_components.getJSONObject(i).getString("short_name");
                    state = URLEncoder.encode(state);
                }
                if(type.equals("country")){
                    country = address_components.getJSONObject(i).getString("short_name");
                    country = URLEncoder.encode(country);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        String yelpUrl = "http://hw8map-env.us-west-1.elasticbeanstalk.com?yelp=true&name="
                +placeName+"&address1="+address1+"&city="+city+"&state="+state+"&country="+country
                +"&phone="+phoneNumber;


        Log.i("yelpUrl",yelpUrl);

        DownloadTask task1 = new DownloadTask();
        String yelpResult = null;
        try {
            yelpResult = task1.execute(yelpUrl).get();

            yelpResult = yelpResult.substring(0,yelpResult.length()-1);

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        JSONObject yelpJSON = null;
        String busId = null;
        try {
            yelpJSON = new JSONObject(yelpResult);
            if(yelpJSON!=null){
                busId = yelpJSON.getJSONArray("businesses").getJSONObject(0).getString("id");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String yelpBusUrl = "http://hw8map-env.us-west-1.elasticbeanstalk.com?busId="+busId;
        String yelpFinal = null;
        JSONObject yelpBusRes = null;
        DownloadTask task2 = new DownloadTask();
        try {
            yelpFinal = task2.execute(yelpBusUrl).get();
            yelpFinal = yelpFinal.substring(0,yelpFinal.length()-1);
            Log.i("yelpFinal",yelpFinal);
            yelpBusRes = new JSONObject(yelpFinal);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(yelpBusRes!=null){
            try {
                JSONArray placeYelp = yelpBusRes.getJSONArray("reviews");
                yelpStore = new ReviewRes[placeYelp.length()];
                for(int i=0;i<placeYelp.length();i++){
                    ReviewRes reviewRes = new ReviewRes();
                    String text = placeYelp.getJSONObject(i).getString("text");
                    int rate = placeYelp.getJSONObject(i).getInt("rating");
                    String time = placeYelp.getJSONObject(i).getString("time_created");

                    String icon = placeYelp.getJSONObject(i).getJSONObject("user").getString("image_url");
                    String name = placeYelp.getJSONObject(i).getJSONObject("user").getString("name");
                    String url =  placeYelp.getJSONObject(i).getString("url");
                    reviewRes.setId(i);
                    reviewRes.setTime(time);
                    reviewRes.setText(text);
                    reviewRes.setRate(rate);
                    reviewRes.setName(name);
                    reviewRes.setIcon(icon);
                    reviewRes.setUrl(url);
                    yelpStore[i]=reviewRes;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if(yelpStore!=null) {
                myReviewAdapter2 = new MyReviewAdapter(this, R.layout.review, yelpStore);
            }
        }
        if(placeReview!=null) {
            reviewStore = new ReviewRes[placeReview.length()];
            SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            for (int i = 0; i < placeReview.length(); i++) {
                ReviewRes reviewRes = new ReviewRes();
                try {
                    String url = placeReview.getJSONObject(i).getString("author_url");
                    String name = placeReview.getJSONObject(i).getString("author_name");
                    int rating = placeReview.getJSONObject(i).getInt("rating");
                    String text = placeReview.getJSONObject(i).getString("text");
                    String icon = placeReview.getJSONObject(i).getString("profile_photo_url");
                    Date date = new Date((placeReview.getJSONObject(i).getLong("time")) * 1000);
                    String time = ft.format(date);
                    reviewRes.setUrl(url);
                    reviewRes.setIcon(icon);
                    reviewRes.setName(name);
                    reviewRes.setRate(rating);
                    reviewRes.setText(text);
                    reviewRes.setTime(time);
                    reviewRes.setId(i);
                    reviewStore[i] = reviewRes;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            myReviewAdapter1 = new MyReviewAdapter(this, R.layout.review, reviewStore);
            mListView.setAdapter(myReviewAdapter1);
            mListView.setDivider(null);

        }

        final TextView reviewMethod = findViewById(R.id.selectiveReview);

        TextView orderMethod = findViewById(R.id.selectiveButton);


        Places.GeoDataApi.getPlaceById(mGoogleApiClient, placeId)
                .setResultCallback(new ResultCallback <PlaceBuffer>() {
                    @Override
                    public void onResult(PlaceBuffer places) {
                        if (places.getStatus().isSuccess() && places.getCount() > 0) {
                            myPlace = places.get(0);
                            Log.i("found", "Place found: " + myPlace.getName());
                            TextView headText = findViewById(R.id.textView5);
                            headText.setText(myPlace.getName());
                            TextView address = findViewById(R.id.address_bar);
                            final TextView phone = findViewById(R.id.phoneNumber_bar);
                            TextView price = findViewById(R.id.priceLevel_Bar);
                            RatingBar rating = findViewById(R.id.RatingStar_Bar);
                            TextView google = findViewById(R.id.Google_Bar);
                            String googleUrl = "";
                            TextView website = findViewById(R.id.Website_Bar);
                            try {
                                googleUrl = placeDetail.getString("url");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            googleUrl = "<a href = " + googleUrl + ">" + googleUrl + "</a>";
                            google.setText(Html.fromHtml(googleUrl));
                            google.setMovementMethod(LinkMovementMethod.getInstance());
                            address.setText(myPlace.getAddress());
                            String webUrl = "<a href = " + myPlace.getWebsiteUri() + ">" + myPlace.getWebsiteUri() + "</a>";
                            website.setText(Html.fromHtml(webUrl));
                            website.setMovementMethod(LinkMovementMethod.getInstance());
                            phone.setText(Html.fromHtml(myPlace.getPhoneNumber().toString()));
                            rating.setRating(myPlace.getRating());
                            String priceLevel = "$";
                            for (int i = 1; i < myPlace.getPriceLevel(); i++) {
                                priceLevel += "$";
                            }
                            String twitterName = "";
                            String twitterAddress = "";
                            String twitterweb = "";
                            if (myPlace.getName() != null) {
                                twitterName = "Check+out+" + (myPlace.getName().toString()).replace(' ', '+');
                            }
                            if (myPlace.getAddress() != null) {
                                twitterAddress = "+located+at+" + (myPlace.getAddress().toString()).replace(' ', '+');
                            }
                            if (myPlace.getWebsiteUri() != null) {
                                twitterweb = "+pWebsite:+" + Uri.encode(myPlace.getWebsiteUri().toString());
                            }
                            String tweetContent = twitterName
                                    + twitterAddress
                                    + twitterweb;
                            price.setText(priceLevel);
                            twitterUrl = "https://twitter.com/intent/tweet?tweet_id=987982322438885376&text=" + tweetContent;
                            ImageView twitterBut = findViewById(R.id.twitterButton);
                            twitterBut.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent();
                                    intent.setAction(Intent.ACTION_VIEW);
                                    intent.addCategory(Intent.CATEGORY_BROWSABLE);
                                    intent.setData(Uri.parse(twitterUrl));
                                    startActivity(intent);
                                }
                            });
                            phone.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    call(phone.getText().toString());
                                }
                            });


                        } else {
                            Log.e("failed", "Place not found");
                        }
                        places.release();
                    }

                });
    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
            .findFragmentById(R.id.theRealMap);
    mapFragment.getMapAsync(this);


    }

    public JSONObject getJson(String url) {
        DownloadTask task = new DownloadTask();
        String result = null;
        JSONObject jsonObject = null;
        try {
            result = task.execute(url).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            jsonObject = new JSONObject(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }


    @Override
    public boolean onDown(MotionEvent e) {

        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {


    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {

        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (e1.getX() - e2.getX() > 120) {
            this.viewFlipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.push_left_in));
            this.viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this,
                    R.anim.push_left_out));
            this.viewFlipper.showNext();
            return true;
        } else if (e1.getX() - e2.getX() < -120) {
            this.viewFlipper.setInAnimation(AnimationUtils.loadAnimation(this,
                    R.anim.push_right_in));
            this.viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this,
                    R.anim.push_right_out));
            this.viewFlipper.showPrevious();
            return true;
        }
        return true;
    }

    private void spandTimeMethod() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    private int getMapSize(LatLng org,LatLng dest){
        int count = 0;
        double xDiff = (org.latitude-dest.latitude)>0?(org.latitude-dest.latitude):(dest.latitude-org.latitude);
        double yDiff = (org.longitude-dest.longitude)>0?(org.longitude-dest.longitude):(dest.longitude-org.longitude);
        double maxDiff = Math.max(2*xDiff,yDiff);
        while (maxDiff<360){
            count++;
            maxDiff*=2;
        }
        return count-1;

    }

    @Override
    public void onMapReady(GoogleMap map) {
            mGoogleMap = map;
            LatLng yourPlace = new LatLng(Double.valueOf(latitude),Double.valueOf(longtitude));
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(yourPlace, 13));
            map.addMarker(new MarkerOptions().position(yourPlace).title("Here is destination"));
    }
}




