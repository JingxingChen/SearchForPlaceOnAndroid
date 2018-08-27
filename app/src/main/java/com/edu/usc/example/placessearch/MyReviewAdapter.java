package com.edu.usc.example.placessearch;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MyReviewAdapter extends ArrayAdapter<ReviewRes>{
    private int resourceId;
    private String placeId;

    public String getPlaceId() {
        return placeId;
    }

    public MyReviewAdapter(Context context, int resource, ReviewRes[] objects){
        super(context, resource, objects);
        this.resourceId = resource;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final ReviewRes reviewRes = getItem(position);
        LinearLayout userListItem = new LinearLayout(getContext());
        String inflater = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater vi = (LayoutInflater)getContext().getSystemService(inflater);
        vi.inflate(resourceId,userListItem,true);
        ImageView icon = userListItem.findViewById(R.id.revImage);

        ImageDownloader task = new ImageDownloader();
        Bitmap myImage = null;

        try {
            myImage = task.execute(reviewRes.getIcon()).get();
            int width = myImage.getWidth();
            int height = myImage.getHeight();
            int newWidth = 60;
            int newHeight = 60;
            float scaleWidth = ((float)newWidth)/width;
            float scaleHeight = ((float)newHeight)/height;
            Matrix matrix = new Matrix();
            matrix.postScale(scaleWidth,scaleHeight);

            myImage = Bitmap.createBitmap(myImage,0,0,width,height,matrix,true);
        } catch (Exception e){
            e.printStackTrace();
        }
        icon.setImageBitmap(myImage);
        TextView custName = userListItem.findViewById(R.id.revName);
        custName.setText(reviewRes.getName());
        TextView reviewTime = userListItem.findViewById(R.id.revTime);
        reviewTime.setText(reviewRes.getTime());
        RatingBar ratingBar = userListItem.findViewById(R.id.revRate);
        ratingBar.setRating(reviewRes.getRate());
        TextView reviewText = userListItem.findViewById(R.id.revText);
        reviewText.setText(reviewRes.getText());
        return userListItem;
    }
}

