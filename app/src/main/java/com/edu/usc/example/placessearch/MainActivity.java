package com.edu.usc.example.placessearch;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.PopupWindow;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.GeoDataApi;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,RadioGroup.OnCheckedChangeListener{
    private LocationManager locationManager;

    private  LocationListener locationListener;
    /** TextView选择框 */
    private TextView mSelectTv;

    /** popup窗口里的ListView */
    private ListView mTypeLv;

    /** popup窗口 */
    private PopupWindow typeSelectPopup;

    /** 模拟的假数据 */
    private List<String> testData;

    /** 数据适配器 */
    private ProgressDialog pd;

    private ArrayAdapter<String> testDataAdapter;

    private RadioGroup radioGroup;

    private String keyWord="";

    private String category="";

    private String latitude="";

    private String longtitude="";

    private double radius=16093;

    private String location="";

    private Handler UIhandler = new Handler();

    private ProgressDialog pd1;

    private ArrayAdapter noReview;

    private PlaceAutoCompleteAdapter mPlaceAutocompleteAdapter;

    private  static  final  LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(
            new LatLng(-40,-168),new LatLng(71,136)
    );
    public static MyArrayAdapter myArrayAdapter;

    public static Place curPlace;


    TextView tv1;
    TextView tv2;


//        protected void onPostExcute(String result){
//            super.onPostExecute(result);
//            try{
//                JSONObject jsonObject = new JSONObject(result);
//                String objInfo = jsonObject.getString("results");
//                Log.i("information",objInfo);
//                JSONArray arr = new JSONArray(objInfo);
//                for(int i=0;i<arr.length();i++){
//                    JSONObject jsonPart = arr.getJSONObject(i);
//
//                }
//            }catch (JSONException e){
//                e.printStackTrace();
//            }
//            Log.i("website Content",result);
//        }


    public void onRequestPermissionResult(int requestCode,String[] permissions,int[] grantResults){
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);

        if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
            startListening();
        }

    }

    public void startListening(){
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        }

    }
    public void updateLocationInfo(Location location){

        latitude = String.valueOf(location.getLatitude());
        longtitude = String.valueOf(location.getLongitude());

    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setContentView(R.layout.activity_main);
        Button clearButton = findViewById(R.id.button4);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.curPlace=null;
                MainActivity.myArrayAdapter=new MyArrayAdapter(getApplicationContext(),R.layout.element,new ArrayList <Place>());
                EditText keyword = findViewById(R.id.editTextKeyword);
                TextView category = findViewById(R.id.tv_select_input);
                EditText distance = findViewById(R.id.editText3Distance);
                EditText location = findViewById(R.id.editText2Location);
                keyword.setText("",TextView.BufferType.EDITABLE);
                category.setText("Default");
                distance.setText("",TextView.BufferType.EDITABLE);
                location.setText("",TextView.BufferType.EDITABLE);
                location.setFocusable(false);
                location.setFocusableInTouchMode(false);
                RadioGroup radioGroup = findViewById(R.id.radioGroup);
                radioGroup.check(R.id.yourPlace);
                ((LinearLayout)findViewById(R.id.linearlay_1)).removeView(tv1);
                ((LinearLayout)findViewById(R.id.linearlay_2)).removeView(tv2);
            }
        });


        String[] noContent = new String[1];
        noContent[0]="No favorites";

        noReview = new ArrayAdapter(this,R.layout.noreview,R.id.noReview,noContent);

        myArrayAdapter = new MyArrayAdapter(this,R.layout.element,new ArrayList <Place>());

        final ListView favoriteListView = findViewById(R.id.favoriteListView);

        favoriteListView.setAdapter(myArrayAdapter);


        Button searchButton = findViewById(R.id.buttonForSearch);

        Button favorButton = findViewById(R.id.buttonForFavorite);

        final ImageView underSearch = findViewById(R.id.underSearch);

        final ImageView underFavor = findViewById(R.id.underFavorite);

        final LinearLayout searchLayOut = findViewById(R.id.layOutSearch);

        final LinearLayout favorLayOut = findViewById(R.id.layOutFavorite);

        favorLayOut.setVisibility(View.GONE);
        searchLayOut.setVisibility(View.VISIBLE);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchLayOut.setVisibility(View.VISIBLE);
                favorLayOut.setVisibility(View.GONE);
                underSearch.setVisibility(View.VISIBLE);
                underFavor.setVisibility(View.GONE);
            }
        });

        favorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchLayOut.setVisibility(View.GONE);
                favorLayOut.setVisibility(View.VISIBLE);
                underSearch.setVisibility(View.GONE);
                underFavor.setVisibility(View.VISIBLE);
                if(myArrayAdapter.getCount()==0){
                    favoriteListView.setAdapter(noReview);
                }else{
                    favoriteListView.setAdapter(myArrayAdapter);
                }

            }
        });

        favoriteListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                                    @Override
                                                    public void onItemClick(AdapterView <?> parent, View view, int position, long id) {
                                                        MainActivity.curPlace = myArrayAdapter.getItem(position);
                                                        pd1 = ProgressDialog.show(MainActivity.this, "", "Fetching details");
                                                        pd1.setCancelable(false);
                                                        Log.i("placeId", myArrayAdapter.getItem(position).getPlaceId());
                                                        final String plaId = myArrayAdapter.getItem(position).getPlaceId();
                                                        new Thread(new Runnable() {
                                                            @Override
                                                            public void run() {

                                                                Intent i = new Intent(MainActivity.this, Main3Activity.class);
                                                                i.putExtra("placeId", plaId);
                                                                startActivity(i);
                                                                spandTimeMethod();

                                                                handler1.sendEmptyMessage(0);
                                                            }

                                                        }).start();
                                                    }
                                                });


                mPlaceAutocompleteAdapter = new PlaceAutoCompleteAdapter(this,  Places.getGeoDataClient(this,null),LAT_LNG_BOUNDS,null);
        AutoCompleteTextView acTextView = findViewById(R.id.editText2Location);
        acTextView.setAdapter(mPlaceAutocompleteAdapter);
        locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                updateLocationInfo(location);

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        if(Build.VERSION.SDK_INT<23){

            startListening();

        }else{

            if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){

                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);

            }else {

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);

                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                if(location!=null) {

                    updateLocationInfo(location);

                }

            }

        }
        initUI();
        initListener();
        radioGroup = findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener(this);

        final Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                        pd = ProgressDialog.show(MainActivity.this,"","Fetching results");
                        pd.setCancelable(false);
                        Log.i("i am areadly","begin");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        spandTimeMethod();
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                clickFunction(button);
                            }
                        };
                        UIhandler.post(runnable);
                        handler.sendEmptyMessage(0);
                    }

                }).start();

            }
        });


    }

    /**
     * 初始化UI
     */
    private void initUI() {
        mSelectTv = findViewById(R.id.tv_select_input);
    }

    /**
     * 初始化监听
     */
    private void initListener() {
        mSelectTv.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_select_input:
                // 点击控件后显示popup窗口
                initSelectPopup();
                // 使用isShowing()检查popup窗口是否在显示状态
                if (typeSelectPopup != null && !typeSelectPopup.isShowing()) {
                    typeSelectPopup.showAsDropDown(mSelectTv, 0, 10);
                }
                break;
        }
    }

    /**
     * 初始化popup窗口
     */
    private void initSelectPopup() {
        mTypeLv = new ListView(this);
        TestData();
        // 设置适配器
        testDataAdapter = new ArrayAdapter<String>(this, R.layout.popup_text_item, testData);
        mTypeLv.setAdapter(testDataAdapter);

        // 设置ListView点击事件监听
        mTypeLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 在这里获取item数据
                String value = testData.get(position);
                // 把选择的数据展示对应的TextView上
                mSelectTv.setText(value);
                // 选择完后关闭popup窗口
                typeSelectPopup.dismiss();
            }
        });
        View tview = findViewById(R.id.tv_select_input);
        typeSelectPopup = new PopupWindow(mTypeLv, mSelectTv.getWidth()-10, ActionBar.LayoutParams.WRAP_CONTENT, true);
        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.bg_corner);
        typeSelectPopup.setBackgroundDrawable(drawable);
        typeSelectPopup.setFocusable(true);
        typeSelectPopup.setOutsideTouchable(true);
        typeSelectPopup.showAsDropDown(mSelectTv,-70,-480);

        typeSelectPopup.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                // 关闭popup窗口
                typeSelectPopup.dismiss();
            }
        });
    }

    private void TestData() {
        testData = new ArrayList<>();
        testData.add("Default");
        testData.add("Airport");
        testData.add("Amusement Park");
        testData.add("Aquarium");
        testData.add("Art Gallery");
        testData.add("Bakery");
        testData.add("Bar");
        testData.add("Beauty Salon");
        testData.add("Bowling Alley");
        testData.add("Bus Station");
        testData.add("Cafe");
        testData.add("Campground");
        testData.add("Car Rental");
        testData.add("Casino");
        testData.add("Lodging");
        testData.add("Movie Theater");
        testData.add("Museum");
        testData.add("Night Club");
        testData.add("Park");
        testData.add("Parking");
        testData.add("Restaurant");
        testData.add("Shopping Mall");
        testData.add("Stadium");
        testData.add("Subway Station");
        testData.add("Taxi Stand");
        testData.add("Train Station");
        testData.add("Transit Station");
        testData.add("Travel Agency");
        testData.add("Zoo");
    }
    public void onCheckedChanged(RadioGroup group, int checkedId){
        switch (checkedId){
            case R.id.yourPlace:
                findViewById(R.id.editText2Location).setFocusable(false);
                findViewById(R.id.editText2Location).setFocusableInTouchMode(false);

                break;
            case R.id.OtherPlace:
                findViewById(R.id.editText2Location).setFocusable(true);
                findViewById(R.id.editText2Location).setFocusableInTouchMode(true);
                findViewById(R.id.editText2Location).requestFocus();

                break;
        }
    }
    public JSONObject getJson(String url){
        DownloadTask task = new DownloadTask();
        String result =null;
        JSONObject jsonObject = null;
        try {
            result = task.execute(url).get();
        }catch(Exception e){
            e.printStackTrace();
        }
        try {
            jsonObject = new JSONObject(result);
        }catch (Exception e){
            e.printStackTrace();
        }
        return jsonObject;
    }
    public void clickFunction(View view){
        if((((EditText)findViewById(R.id.editTextKeyword)).getText().toString().trim().length()==0)||
                (((EditText)findViewById(R.id.editText2Location)).getText().toString().trim().length()==0&&
                        ((RadioButton)findViewById(R.id.OtherPlace)).isChecked())) {
            if(((EditText)findViewById(R.id.editTextKeyword)).getText().toString().trim().length()==0) {
                ((LinearLayout) findViewById(R.id.linearlay_1)).removeView(tv1);
                tv1 = new TextView(this);
                tv1.setText("Please enter mandatory field");
                tv1.setGravity(Gravity.LEFT);
                tv1.setTextColor(Color.RED);
                ((LinearLayout) findViewById(R.id.linearlay_1)).addView(tv1);
            }
            if(((EditText)findViewById(R.id.editText2Location)).getText().toString().trim().length()==0&&((RadioButton)findViewById(R.id.OtherPlace)).isChecked()){
                ((LinearLayout)findViewById(R.id.linearlay_2)).removeView(tv2);
                tv2 = new TextView(this);
                tv2.setText("Please enter mandatory field");
                tv2.setGravity(Gravity.LEFT);
                tv2.setTextColor(Color.RED);
                ((LinearLayout)findViewById(R.id.linearlay_2)).addView(tv2);
            }
            Toast.makeText(MainActivity.this, "Please fix all fields with errors", Toast.LENGTH_LONG).show();
            return;
        }


        if(((EditText) findViewById(R.id.editTextKeyword)).getText().toString().trim().length()!=0){
            keyWord = ((EditText) findViewById(R.id.editTextKeyword)).getText().toString();
            keyWord = keyWord.replace(' ','+');
        }

        if(((TextView)findViewById(R.id.tv_select_input)).getText().toString().trim().length()!=0) {
            category = ((TextView) findViewById(R.id.tv_select_input)).getText().toString();
            category = category.replace(' ','+');
        }
        if(((EditText) findViewById(R.id.editText3Distance)).getText().toString().trim().length()!=0){
            radius = Integer.parseInt(((EditText) findViewById(R.id.editText3Distance)).getText().toString().trim())*1609.34;
        }

        if(((EditText) findViewById(R.id.editText2Location)).getText().toString().trim().length()!=0){
            location = ((EditText) findViewById(R.id.editText2Location)).getText().toString();
            location = location.replace(' ','+');
        }

        if(((RadioButton)findViewById(R.id.OtherPlace)).isChecked()) {
            String url = "https://maps.googleapis.com/maps/api/geocode/json?address="+location+"&key=AIzaSyD0l5EmYEXZaT4AdEhnSXX9niPy9t65asg";
            Log.i("url",url);
            JSONObject jobj = getJson(url);
            try {
                JSONObject jobjr = jobj.getJSONArray("results").getJSONObject(0).getJSONObject("geometry").getJSONObject("location");
                latitude = jobjr.getString("lat");
                longtitude = jobjr.getString("lng");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        String serveUrl = "http://hw8map-env.us-west-1.elasticbeanstalk.com?keyword="+keyWord+"&category="+category+"&radius="+radius+"&latitude="+latitude+"&longtitude="+longtitude;
        Log.i("Url",serveUrl);
        JSONObject Jsonobj = getJson(serveUrl);
        String show=null;
        try {
            show = Jsonobj.toString();
            Log.i("show",show);

        }catch (Exception e){
            e.printStackTrace();
        }
        Intent i = new Intent(MainActivity.this,Main2Activity.class);
            i.putExtra("input",show);
            startActivity(i);
        }



    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            pd.dismiss();
        }
    };
    private void spandTimeMethod() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
            Handler handler1 = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    pd1.dismiss();
                }
            };

}

