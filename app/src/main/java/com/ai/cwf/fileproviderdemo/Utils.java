package com.ai.cwf.fileproviderdemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created at 陈 on 2017/4/26.
 *
 * @author chenwanfeng
 * @email 237142681@qq.com
 */

public class Utils {
    /*unInstall app*/
    public static void unInstallApp(@NonNull Context context, @NonNull String packageName) {
        Uri packageURI = Uri.parse("package:" + packageName);
        Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
        context.startActivity(uninstallIntent);
    }


    /*check the app is installed*/
    public static boolean checkAppInstalled(@NonNull Context context, @NonNull String packageName) {
        PackageInfo packageInfo;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            packageInfo = null;
            e.printStackTrace();
        }
        if (packageInfo == null) {
            //System.out.println("没有安装");
            return false;
        } else {
            //System.out.println("已经安装");
            return true;
        }
    }

    //安装本地apk文件
    public static void installApk(@NonNull Context context, @NonNull String apkFilePath) {
        File file = new File(apkFilePath);
        if (!file.exists()) {
            Toast.makeText(context, "没有找到安装包", Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        intent.setDataAndType(getUriForFile(context, file),
                "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

    /**
     * 打开相机
     *
     * @param fragment    Fragment
     * @param activity    Activity
     * @param file        File
     * @param requestCode result requestCode
     */
    public static void openCamera(Fragment fragment, Activity activity, @NonNull File file, @NonNull int requestCode) {
        if (fragment == null && activity == null) {
            return;
        }
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (fragment != null) {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, getUriForFile(fragment.getActivity(), file));
            fragment.startActivityForResult(intent, requestCode);
        } else {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, getUriForFile(activity, file));
            activity.startActivityForResult(intent, requestCode);
        }
    }

    /**
     * 使用系统裁剪工具
     *
     * @param activity    Activity
     * @param outputX     输出图片宽度
     * @param outputY     输出图片高度
     * @param uri        uri
     * @param requestCode result requestCode
     */
    public static void startPhotoZoom(Activity activity, int outputX, int outputY, @NonNull Uri uri, @NonNull int requestCode) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }
        // 下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", outputX);
        intent.putExtra("aspectY", outputY);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", outputX);
        intent.putExtra("outputY", outputX);
        //image type
        intent.putExtra("outputFormat", "JPEG");
        intent.putExtra("noFaceDetection", true);
        //true - don't return uri |  false - return uri
        intent.putExtra("return-data", true);
        activity.startActivityForResult(intent, requestCode);
    }

    /*打开系统相册*/
    public static void openSystemGallery(Activity activity, @NonNull int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        activity.startActivityForResult(intent, requestCode);
    }


    //7.0及以上，对Uir授予读写权限
    public static Uri getUriForFile(@NonNull Context context, @NonNull File file) {
        if (context == null || file == null) {
            throw new NullPointerException();
        }
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileProvider", file);
        } else {
            uri = Uri.fromFile(file);
        }
        return uri;
    }


    public static void copyFileFromAsset(String targetFilePath, String assetResName, AssetManager assetManager) throws IOException {
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;

        try {
            File e = new File(targetFilePath);
            if (!e.getParentFile().exists()) {
                e.getParentFile().mkdirs();
            }

            if (e.exists()) {
                e.delete();
            }

            inputStream = assetManager.open(assetResName);
            fileOutputStream = new FileOutputStream(targetFilePath);
            byte[] b = new byte[1024];

            int l;
            while ((l = inputStream.read(b)) != -1) {
                fileOutputStream.write(b, 0, l);
            }

            inputStream.close();
            fileOutputStream.close();
            inputStream = null;
            fileOutputStream = null;
        } catch (IOException var11) {
            var11.printStackTrace();
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }

            if (fileOutputStream != null) {
                fileOutputStream.close();
            }

        }

    }
}
