package com.edu.usc.example.placessearch;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class MyArrayAdapter extends ArrayAdapter<Place>implements Serializable{
    private static final long serialVersionUID=1L;
    private int resourceId;
    private String placeId;

    public String getPlaceId() {
        return placeId;
    }

    public MyArrayAdapter(Context context, int resource, Place[] objects){
        super(context, resource, objects);
        this.resourceId = resource;
    }
    public MyArrayAdapter(Context context, int resource, List<Place> objects){
        super(context,resource,objects);
        this.resourceId = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final Place place = getItem(position);
        LinearLayout userListItem = new LinearLayout(getContext());
        String inflater = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater vi = (LayoutInflater)getContext().getSystemService(inflater);
        vi.inflate(resourceId,userListItem,true);
        ImageView icon = userListItem.findViewById(R.id.imageView3);

        ImageDownloader task = new ImageDownloader();
        Bitmap myImage = null;

        try {
            myImage = task.execute(place.getIcon()).get();
        } catch (Exception e){
            e.printStackTrace();
        }
        icon.setImageBitmap(myImage);
        TextView placeName = (TextView) userListItem.findViewById(R.id.placeName);
        placeName.setText(place.getPlaceName());
        TextView locationName = userListItem.findViewById(R.id.locationName);
        locationName.setText(place.getLocationName());
        final ImageView imageRight = userListItem.findViewById(R.id.heartImage);
        if(place.getId()==-1){
            imageRight.setImageResource(R.drawable.heart_blackborder);
        }else if(place.getId()==1){
            imageRight.setImageResource(R.drawable.redheart);
        }

        imageRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(place.getId()==-1) {
                    imageRight.setImageResource(R.drawable.redheart);
                    MainActivity.myArrayAdapter.add(place);
                    place.setId(1);
                    Toast.makeText(getContext(), place.getPlaceName()+"was added to favorites", Toast.LENGTH_LONG).show();
                }else if(place.getId()==1){
                    imageRight.setImageResource(R.drawable.heart_blackborder);
                    MainActivity.myArrayAdapter.remove(place);
                    place.setId(-1);
                    Toast.makeText(getContext(), place.getPlaceName()+"was removed from favorites", Toast.LENGTH_LONG).show();
                }
            }
        });
        return userListItem;
    }
}


