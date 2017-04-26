package com.ai.cwf.fileproviderdemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private final int PERMISSION_REQUEST_CODE = 0;
    private final int OPEN_CAMERA_REQUEST_CODE = 1;
    private final int OPEN_CAMERA_CROP_REQUEST_CODE = 2;
    private final int OPEN_GALLERY_REQUEST_CODE = 3;
    private final int OPEN_GALLERY_CROP_REQUEST_CODE = 4;
    private final int CROP_REQUEST_CODE = 5;


    private AppCompatImageView imageView;
    private File apkFile;
    private File tempFile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = (AppCompatImageView) findViewById(R.id.image);
        apkFile = new File(getExternalCacheDir(), "temp.apk");
        findViewById(R.id.install).setOnClickListener(this);
        findViewById(R.id.open_camera).setOnClickListener(this);
        findViewById(R.id.open_camera_crop).setOnClickListener(this);
        findViewById(R.id.open_gallery).setOnClickListener(this);
        findViewById(R.id.open_gallery_crop).setOnClickListener(this);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS, Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.install:
                if (!Utils.checkAppInstalled(this, "com.ai.cwf.changeicon"))
                    Utils.installApk(this, apkFile.getAbsolutePath());
                else {
                    Toast.makeText(this, "应用已安装", Toast.LENGTH_SHORT).show();
                    Utils.unInstallApp(this, "com.ai.cwf.changeicon");
                }
                break;
            case R.id.open_camera:
                tempFile = new File(getExternalCacheDir(), System.currentTimeMillis() + ".jpg");
                Utils.openCamera(null, this, tempFile, OPEN_CAMERA_REQUEST_CODE);
                break;
            case R.id.open_camera_crop:
                tempFile = new File(getExternalCacheDir(), System.currentTimeMillis() + ".jpg");
                Utils.openCamera(null, this, tempFile, OPEN_CAMERA_CROP_REQUEST_CODE);
                break;
            case R.id.open_gallery:
                Utils.openSystemGallery(this, OPEN_GALLERY_REQUEST_CODE);
                break;
            case R.id.open_gallery_crop:
                Utils.openSystemGallery(this, OPEN_GALLERY_CROP_REQUEST_CODE);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case OPEN_CAMERA_REQUEST_CODE:
                    imageView.setImageBitmap(BitmapFactory.decodeFile(tempFile.getAbsolutePath()));
                    break;
                case OPEN_CAMERA_CROP_REQUEST_CODE:
                    Utils.startPhotoZoom(this, 240, 300, Utils.getUriForFile(this, tempFile), CROP_REQUEST_CODE);
                    break;
                case OPEN_GALLERY_REQUEST_CODE:
                    imageView.setImageURI(data.getData());
                    break;
                case OPEN_GALLERY_CROP_REQUEST_CODE:
                    Utils.startPhotoZoom(this, 240, 300, data.getData(), CROP_REQUEST_CODE);
                    break;
                case CROP_REQUEST_CODE:
                    Bundle extra = data.getExtras();
                    if (extra != null) {
                        File mPhoto = null;
                        Bitmap photo = extra.getParcelable("data");
                        Bitmap.CompressFormat format = Bitmap.CompressFormat.JPEG;
                        int quality = 100;
                        OutputStream stream = null;
                        try {
                            mPhoto = new File(getExternalCacheDir(), System.currentTimeMillis() + ".jpg");
                            if (!mPhoto.exists())
                                mPhoto.createNewFile();
                            stream = new FileOutputStream(mPhoto);
                            photo.compress(format, quality, stream);
                            imageView.setImageBitmap(photo);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                        }
                    }
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    tempFile = getExternalCacheDir();
                    if (!tempFile.exists())
                        tempFile.mkdirs();
                    if (!apkFile.exists()) {
                        try {
                            Utils.copyFileFromAsset(apkFile.getAbsolutePath(), apkFile.getName(), getAssets());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
