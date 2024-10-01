package com.netease.im;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;

public class GalleryObserver extends ContentObserver {

    private Context context;

    public GalleryObserver(Handler handler, Context context) {
        super(handler);
        this.context = context;
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        super.onChange(selfChange, uri);
        // Check if the change is related to the image media content
        if (uri.toString().startsWith(MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString())) {
            WritableMap writableMap = Arguments.createMap();
            writableMap.putBoolean("photoLibraryDidChange", true);
            ReactCache.emit("observePhotoLibraryDidChange", writableMap);

            Log.d("GalleryObserver", "New image detected: " + uri.toString());
            // You can handle the event of a new image being added here
        }
    }
}
