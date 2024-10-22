package com.example.imageapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, PERMISSION_REQUEST_CODE);
            } else {
                setContentView(new MyGraphicView(this));
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            } else {
                setContentView(new MyGraphicView(this));
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setContentView(new MyGraphicView(this));
            } else {
                setContentView(R.layout.activity_main);
                Toast.makeText(this, "권한이 거부되었습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private static class MyGraphicView extends View {
        private Bitmap picture;

        public MyGraphicView(Context context) {
            super(context);
            Uri imageUri = getImageUri(context);
            if (imageUri != null) {
                picture = loadBitmapFromUri(context, imageUri);
            } else {
                picture = null;
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (picture != null) {
                int cenX = this.getWidth() / 2;
                int cenY = this.getHeight() / 2;
                int picX = (this.getWidth() - picture.getWidth()) / 2;
                int picY = (this.getHeight() - picture.getHeight()) / 2;
                canvas.scale(0.5f, 0.5f, cenX, cenY);
                canvas.drawBitmap(picture, picX, picY, null);
            }
        }

        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            if (picture != null && !picture.isRecycled()) {
                picture.recycle();
                picture = null;
            }
        }

        private Uri getImageUri(Context context) {
            Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            String[] projection = { MediaStore.Images.Media._ID };
            String selection = MediaStore.Images.Media.DISPLAY_NAME + "=?";
            String[] selectionArgs = new String[]{ "zebra.jpg" };

            Cursor cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                    cursor.close();
                    return Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, String.valueOf(id));
                }
                cursor.close();
            }
            return null;
        }

        private Bitmap loadBitmapFromUri(Context context, Uri uri) {
            try {
                ContentResolver resolver = context.getContentResolver();
                InputStream inputStream = resolver.openInputStream(uri);
                return BitmapFactory.decodeStream(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}