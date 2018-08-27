package com.edu.usc.example.placessearch;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.RunnableFuture;

public class Main2Activity extends AppCompatActivity {
private TextView textInput;

private ArrayList<Place> myplace;
private Button buttonNext;
private Button buttonPrevious;

private ProgressDialog pd;

private ProgressDialog pd1;

private MyArrayAdapter myArrayAdapter1 = null;
private MyArrayAdapter myArrayAdapter2 = null;

private MyArrayAdapter myArrayAdapter3 = null;

private String nextPageTocken;

private String nextPageTocken1;

private String relInput;

private String input;

private String status;

private ListView mListView;

private Handler uiHandler = new Handler();

public static MyArrayAdapter myArrayAdapter;

public static int position;
private int count;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        count = 1;
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_main2);
        ImageView returnBack = findViewById(R.id.returnBackFor2);
        returnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        textInput = findViewById(R.id.textView4);
        Intent i = getIntent();
        input = i.getStringExtra("input");
        JSONObject jsonObject=null;
        try {
            jsonObject = new JSONObject(input);
            relInput = jsonObject.getString("results");
            nextPageTocken = jsonObject.getString("next_page_token");
            status = jsonObject.getString("status");
        }catch (Exception e){
            e.printStackTrace();
        }
        if(status!=null&&status.equals("OK")) {
            getContent(relInput);
        }else{
            ArrayList<String> list = new ArrayList <String>();
            list.add("No results");
            ArrayAdapter arrayAdapter = new ArrayAdapter(getApplicationContext(),R.layout.noreview,R.id.noReview,list);

            mListView = findViewById(R.id.myView);

            mListView.setDivider(null);
            mListView.setAdapter(arrayAdapter);
        }
        buttonNext = findViewById(R.id.buttonNext);
        buttonPrevious = findViewById(R.id.buttonPrevious);
        buttonPrevious.setEnabled(false);
        if(nextPageTocken==null){
            buttonNext.setEnabled(false);
        }
        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    pd = ProgressDialog.show(Main2Activity.this,"","Fetching next page");
                    pd.setCancelable(false);
                    count++;
                    if(count==2) {
                        buttonPrevious.setEnabled(true);
                        if (myArrayAdapter2 != null) {
                            if(nextPageTocken1==null){
                                buttonNext.setEnabled(false);
                            }
                            mListView.setAdapter(myArrayAdapter2);
                            myArrayAdapter = myArrayAdapter2;
                            handler.sendEmptyMessage(0);
                            return;
                        }
                    }else if(count ==3){
                        buttonNext.setEnabled(false);
                        if (myArrayAdapter3 != null) {
                            mListView.setAdapter(myArrayAdapter3);
                            myArrayAdapter = myArrayAdapter3;
                            handler.sendEmptyMessage(0);
                            return;
                        }
                    }
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                                String url = "http://hw8map-env.us-west-1.elasticbeanstalk.com?nextPage=" + nextPageTocken;

                                JSONObject jsObj = getJson(url);


                                try {
                                    relInput = jsObj.getString("results");

                                    if(count==2) {

                                        nextPageTocken = jsObj.getString("next_page_token");
                                    }else if(count==3){
                                        nextPageTocken1 = jsObj.getString("next_page_token");
                                    }
                                    Log.i("relInput",relInput);
                                    Log.i("nextPageToken",nextPageTocken);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }


                                Runnable runnable =new Runnable() {
                                    @Override
                                    public void run() {
                                        if(count==2&&nextPageTocken1==null){
                                            buttonNext.setEnabled(false);
                                        }

                                        getContent(relInput);
                                    }
                                };
                                uiHandler.post(runnable);

                            handler.sendEmptyMessage(0);
                        }
                    }).start();


            }
        });
        buttonPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                count--;
                if(count==2){
                    buttonNext.setEnabled(true);
                    mListView.setAdapter(myArrayAdapter2);
                    myArrayAdapter = myArrayAdapter2;
                }else if(count==1){
                    buttonNext.setEnabled(true);
                    buttonPrevious.setEnabled(false);
                    mListView.setAdapter(myArrayAdapter1);
                    myArrayAdapter = myArrayAdapter1;
                }
            }
        });


    }

    private void getContent(String input){
        MyArrayAdapter arrayAdapter = null;
        try {
            JSONArray arr = new JSONArray(input);
            myplace = new ArrayList<Place>();
            for(int j=0;j<arr.length();j++){
                Place place = new Place();
                JSONObject jsonPart = arr.getJSONObject(j);
                place.setIcon(jsonPart.getString("icon"));
                place.setPlaceName(jsonPart.getString("name"));
                place.setLocationName(jsonPart.getString("vicinity"));
                place.setPlaceId(jsonPart.getString("place_id"));
                myplace.add(place);
            }
            mListView = findViewById(R.id.myView);
            arrayAdapter = new MyArrayAdapter(this,R.layout.element,myplace);
            if(count ==1){
                myArrayAdapter1 = arrayAdapter;
            }else if(count ==2){
                myArrayAdapter2 = arrayAdapter;
            }else if(count ==3){
                myArrayAdapter3 = arrayAdapter;
            }
            myArrayAdapter = arrayAdapter;
            mListView.setAdapter(arrayAdapter);
            mListView.setDivider(null);
            mListView.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    MainActivity.curPlace=(Place) parent.getItemAtPosition(position);
                    Main2Activity.position=position;
                    pd1 = ProgressDialog.show(Main2Activity.this,"","Fetching details");
                    pd1.setCancelable(false);

                    final String plaId = ((Place)(parent.getItemAtPosition(position))).getPlaceId();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {

                            Intent i = new Intent(Main2Activity.this,Main3Activity.class);
                            i.putExtra("placeId",plaId);
                            startActivity(i);
                            spandTimeMethod();

                            handler1.sendEmptyMessage(0);
                        }

                    }).start();

                }
            });
        }catch (Exception e){
            e.printStackTrace();
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
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            pd.dismiss();
        }
    };

    Handler handler1 = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            pd1.dismiss();
        }
    };

    private void spandTimeMethod() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
