# FileProviderDemo
解决android.os.FileUriExposedException
主线思想就是FileProvider.getUriForFile 代替原来的Uri.fromFile来获取uri，赋予uri读写权限
AndroidManifest.xml中添加：

        <provider
            android:name="android.support.v4.content.FileProvider"
            android:authorities="包名.fileProvider"
            android:exported="false"
            android:grantUriPermissions="true">
            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/file_paths" />
        </provider>
        
其中@xml/file_paths需要我们在res中创建一个文件夹xml，在下面创建file_paths文件，文件名称和配置中一样就可以
file_paths的内容如下：

       <?xml version="1.0" encoding="utf-8"?>
       <paths>
           <!--Android/data/包名/-->
           <external-path path="Android/data/com.ai.cwf.fileproviderdemo/" name="files_root" />
           <external-path path="." name="external_storage_root" />
       </paths>

name可以随便写，只是个标识

当需要获取Uri时，就可以使用下面的方法，部分低版本不能使用getUriForFile，需要判断下版本，BuildConfig.APPLICATION_ID获取到的就是应用的包名

        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileProvider", file);
        } else {
            uri = Uri.fromFile(file);
        }
        
Demo里写了安装apk和打开图库的例子，需要的可以参考下

调用第三方应用的时候即设置action为Intent.ACTION_VIEW，startActivity会出现
ActivityManager: Permission Denial: opening provider ... (pid=31432, uid=10042) that is not exported from uid 10051
权限丢失，导致文件数据没有传到第三方应用
只需要给Intent  addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
给intent附上需要的读或写权限，就可以解决大部分Permission Denial
