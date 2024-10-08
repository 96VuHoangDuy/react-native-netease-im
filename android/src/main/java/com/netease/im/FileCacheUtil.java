package com.netease.im;

import android.content.Context;
//import android.content.pm.IPackageDataObserver;
//import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.netease.im.uikit.common.util.file.FileUtil;
import com.netease.im.uikit.common.util.log.LogUtil;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by dowin on 2017/7/6.
 * <p>
 * log: SDK日志
 * file: 文件消息文件
 * image: 图片消息文件
 * audio：语音消息文件
 * video：视频消息文件
 * thumb：图片/视频缩略图文件
 */

public class FileCacheUtil {

    final static String TAG = "FileCacheUtil";

    interface OnObserverGet {
        void onGetCacheSize(String size);
    }

    interface OnObserverClean {
        void onCleanCache(boolean succeeded);
    }

    public static long getDirSize(File dir){
        long size = 0;
        for (File file : dir.listFiles()) {
            if (file != null && file.isDirectory()) {
                size += getDirSize(file);
            } else if (file != null && file.isFile()) {
                size += file.length();
            }
        }
        return size;
    }

    public static WritableMap getSessionsCacheSie(ArrayList<String> sessionIds) {
        // Create a list to store session data
        List<WritableMap> sessionDataList = new ArrayList<>();
        long totalSize = 0;

        for (String sessionId: sessionIds) {
            long folderSize = getSessionCacheSie(sessionId);
            totalSize += folderSize;
            Log.d("FOLDER_SIZE", String.valueOf(folderSize));
            WritableMap result = Arguments.createMap();
            result.putString("sessionId", sessionId);
            result.putString("size", FileUtil.formatFileSize(folderSize));
            result.putDouble("sizeNumber",folderSize);
            // Add the result map to the list
            sessionDataList.add(result);
        }

        // Sort the list in descending order of size
        Collections.sort(sessionDataList, new Comparator<WritableMap>() {
            @Override
            public int compare(WritableMap o1, WritableMap o2) {
                return Double.compare(o2.getDouble("sizeNumber"), o1.getDouble("sizeNumber"));
            }
        });

        // Create the result array and add the sorted data
        WritableArray arrResult = Arguments.createArray();
        for (WritableMap data : sessionDataList) {
            arrResult.pushMap(data);
        }

        WritableMap finalResult = Arguments.createMap();
        finalResult.putArray("data", arrResult);
        finalResult.putString("totalSize", FileUtil.formatFileSize(totalSize));

        return finalResult;
    }

    public static String cleanSessionCache(ArrayList<String> sessionIds) {
        for (String sessionId: sessionIds) {
            cleanSessionCache(sessionId);
        }

        return "deleteSuccess";
    }


    public static long getSessionCacheSie(String sessionId) {
        Set<String> pathList = getCacheDir(sessionId);
        Log.d("pathList", pathList + "");
        long allLength = 0;
        for (String s : pathList) {
            File dirFile = new File(s);
            if (dirFile.exists()) {
                long t = getDirSize(dirFile);
                Log.d(TAG, s + ":" + FileUtil.formatFileSize(t));
                allLength += t;
            }
        }
        Log.d("allLengthallLength", FileUtil.formatFileSize(allLength) + "");
        return allLength;
    }

    public static String cleanSessionCache(String sessionId) {
        Set<String> pathList = getCacheDir(sessionId);
        Log.d("pathList", pathList + "");

        for (String s : pathList) {
            File dirFile = new File(s);
            if (dirFile.exists()) {
                deleteDir(dirFile);
            }
        }

        return "deleteSuccess";
    }

    private static void deleteDir(File file) {
        if (file == null || !file.exists()) {
            return;
        }
        if (file.isFile()) {
            file.delete();
        }
        File[] list = file.listFiles();
        if (list != null && list.length > 0) {
            for (File f : list) {
                if (f.isDirectory()) {
                    deleteDir(f);
                } else {
                    f.delete();
                }
            }
        }
    }

    private static long makeDirSize(File file) {

        if (file == null || !file.exists()) {
            return 0L;
        }
        if (file.isFile()) {
            return file.length();
        }
        long all = 0L;
        File[] list = file.listFiles();
        if (list != null && list.length > 0) {
            for (File f : list) {
                if (f.isDirectory()) {
                    all += makeDirSize(f);
                } else {
                    all += f.length();
                }
            }
        }
        return all;
    }

//    private static void getCacheSize(IPackageStatsObserver.Stub observer) {
//        Context context = IMApplication.getContext();
//        String pkg = context.getPackageName();
//        PackageManager pm = context.getPackageManager();
//        try {
//            LogUtil.w(TAG, "name:" + pm.getClass().getName());
//            Method getPackageSizeInfo = pm.getClass().getMethod("getPackageSizeInfo", String.class, IPackageStatsObserver.class);
//            getPackageSizeInfo.invoke(pm, pkg, observer);
//        } catch (Exception ex) {
//            LogUtil.e("", "NoSuchMethodException");
//            ex.printStackTrace();
//        }
//    }

    private static Set<String> getCacheDir(String sessionId) {
//        StorageType[] storageTypes = StorageType.values();
        String[] sdkFileName = {"file/", "image/", "audio/", "video/", "thumb/"};
        Set<String> path = new HashSet<>();
        Context context = IMApplication.getContext();
//        path.add(context.getCacheDir().getAbsolutePath());
//        path.add(context.getExternalCacheDir().getAbsolutePath());

//        for (StorageType type : storageTypes) {
//            path.add(StorageUtil.getDirectoryByDirType(type));
//        }
        for (String sdk : sdkFileName) {
            String pathDir = context.getCacheDir().getAbsolutePath() + "/nim/" + sdk + sessionId;
            path.add(pathDir);
        }
//        File imageCacheDir = IMApplication.getImageLoaderKit().getChacheDir();
//        if (imageCacheDir.exists()) {
//            path.add(imageCacheDir.getAbsolutePath());
//        }


        Log.d("getCacheDir", context.getCacheDir().getAbsolutePath());
        Log.d("getExternalCacheDir", context.getExternalCacheDir().getAbsolutePath());
        return path;
    }


    private static long getEnvironmentSize() {
        File localFile = Environment.getDataDirectory();
        if (localFile == null)
            return 0L;

        StatFs statFs = new StatFs(localFile.getPath());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return statFs.getBlockCountLong() * statFs.getBlockSizeLong();
        }
        return statFs.getBlockCount() * statFs.getBlockSize();
    }

//    private static void freeStorageAndNotify(IPackageDataObserver.Stub observer) {
//
//        try {
//            Context context = IMApplication.getContext();
//            PackageManager pm = context.getPackageManager();
//            LogUtil.w(TAG, "name:" + pm.getClass().getName());
//            Method localMethod = pm.getClass().getMethod("freeStorageAndNotify", Long.TYPE,
//                    IPackageDataObserver.class);
//            long localLong = Long.valueOf(getEnvironmentSize() - 1L);
//
//            localMethod.invoke(pm, localLong, observer);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
