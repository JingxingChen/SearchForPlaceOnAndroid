package com.edu.usc.example.placessearch;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.Places;

public  class PhotoTask extends AsyncTask<String,Void,Bitmap> {
    private  GoogleApiClient mGoogleApiClient;
    public PhotoTask(GoogleApiClient mGoogleApiClient){
        this.mGoogleApiClient=mGoogleApiClient;
    }

    @Override
    protected Bitmap doInBackground(String... strings) {
        PlacePhotoMetadataResult result = Places.GeoDataApi
                .getPlacePhotos(mGoogleApiClient,strings[0]).await();
        if(result.getStatus().isSuccess()){
            PlacePhotoMetadataBuffer photoMetadataBuffer = result.getPhotoMetadata();
            if(photoMetadataBuffer.getCount()>0){
                Bitmap[] bitmaps = new Bitmap[photoMetadataBuffer.getCount()];
                for(int i=0;i<photoMetadataBuffer.getCount();i++){
                    PlacePhotoMetadata photo = photoMetadataBuffer.get(i);
                    Bitmap image = photo.getScaledPhoto(mGoogleApiClient,380,400).await().getBitmap();
                    bitmaps[i]=image;

                }
                photoMetadataBuffer.release();
                return bitmaps[0];
            }
        }
        return null;
    }

}