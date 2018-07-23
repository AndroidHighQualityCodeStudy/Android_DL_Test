package com.xiaxl.dynamicloadhost;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

public class ProxyActivity extends Activity {

    private static final String TAG = "DL: ProxyActivity";

    public static final String FROM = "extra.from";
    public static final int FROM_EXTERNAL = 0;
    public static final int FROM_INTERNAL = 1;

    public static final String EXTRA_DEX_PATH = "extra.dex.path";
    public static final String EXTRA_CLASS = "extra.class";

    private String mClass;
    private String mSdcardApkPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSdcardApkPath = getIntent().getStringExtra(EXTRA_DEX_PATH);
        mClass = getIntent().getStringExtra(EXTRA_CLASS);

        Log.d(TAG, "mClass=" + mClass + " mDexPath=" + mSdcardApkPath);
        if (mClass == null) {
            launchTargetActivity();
        } else {
            launchTargetActivity(mClass);
        }
    }

    @SuppressLint("NewApi")
    protected void launchTargetActivity() {
        File file = new File(mSdcardApkPath);
        if (!file.exists()) {
            Log.d(TAG, "file.exists == false");
            return;
        }
        //
        PackageInfo packageInfo = getPackageManager().getPackageArchiveInfo(
                mSdcardApkPath, PackageManager.GET_ACTIVITIES);
        if ((packageInfo.activities != null)
                && (packageInfo.activities.length > 0)) {
            String activityName = packageInfo.activities[0].name;
            mClass = activityName;
            launchTargetActivity(mClass);
        }
    }

    /**
     * @param className 要启动的Activity类名称
     */
    @SuppressLint("NewApi")
    protected void launchTargetActivity(final String className) {
        Log.d(TAG, "start launchTargetActivity, className=" + className);
        //
        File dexOutputDir = this.getDir("dex", 0);
        final String dexOutputPath = dexOutputDir.getAbsolutePath();
        Log.d(TAG, "dexOutputPath: " + dexOutputPath);
        // 创建ClassLoader
        ClassLoader localClassLoader = ClassLoader.getSystemClassLoader();
        DexClassLoader dexClassLoader = new DexClassLoader(mSdcardApkPath,
                dexOutputPath, null, localClassLoader);
        try {
            // 加载class
            Class<?> localClass = dexClassLoader.loadClass(className);
            // 创建class的对象
            Constructor<?> localConstructor = localClass.getConstructor(new Class[]{});
            Object instance = localConstructor.newInstance(new Object[]{});
            Log.d(TAG, "instance = " + instance);
            // 调用setProxy方法  将当前的activity对象添加到 要唤起的activity中
            Method setProxy = localClass.getMethod("setProxy", new Class[]{Activity.class});
            setProxy.setAccessible(true);
            setProxy.invoke(instance, new Object[]{this});
            // 调用oncreate方法
            Method onCreate = localClass.getDeclaredMethod("onCreate",
                    new Class[]{Bundle.class});
            onCreate.setAccessible(true);
            Bundle bundle = new Bundle();
            bundle.putInt(FROM, FROM_EXTERNAL);
            onCreate.invoke(instance, new Object[]{bundle});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}