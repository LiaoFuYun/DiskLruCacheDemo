package com.lfy.disklrucachedemo;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CrashHandler implements Thread.UncaughtExceptionHandler {
    private static CrashHandler instance;

    private Context mContext;
    // 系统默认的handler
    private Thread.UncaughtExceptionHandler defaultHandler;
    // 格式化时间
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.CHINA);

    private Map<String, String> infos = new HashMap<>();

    private CrashHandler() {
    }

    public void init(Context context) {
        this.mContext = context.getApplicationContext();
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    public static CrashHandler getInstance() {
        if (instance == null) {
            synchronized (CrashHandler.class) {
                if (instance == null) {
                    instance = new CrashHandler();
                }
            }
        }
        return instance;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        if (!handleException(e) && defaultHandler != null) {
            defaultHandler.uncaughtException(t, e);
            return;
        }
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        AppManager.getAppManager().AppExit(mContext);
        Process.killProcess(Process.myPid());
    }

    private boolean handleException(Throwable e) {
        if (e == null) {
            return false;
        }
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                Toast.makeText(mContext, "程序奔溃了,即将退出", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        }.start();
        collectDeviceInfo();
        String fileName = saveCrash2File(e);
        Log.e("CrashHandler", fileName + "");
        return true;
    }

    private String saveCrash2File(Throwable e) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : infos.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key)
                    .append("=")
                    .append(value)
                    .append("\n");
        }
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        e.printStackTrace(printWriter);
        Throwable cause = e.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        sb.append("\n").append(writer.toString());

        String time = dateFormat.format(new Date());
        String fileName = "crash-" + time + ".log";

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String path = Environment.getExternalStorageDirectory().getPath() + "/crash/";
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            try {
                FileOutputStream fileOut = new FileOutputStream(path + fileName);
                fileOut.write(sb.toString().getBytes());
                fileOut.close();
                return path + fileName;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    // 收集异常版本信息
    private void collectDeviceInfo() {
        PackageManager packageManager = mContext.getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(mContext.getPackageName(), PackageManager.GET_ACTIVITIES);
            int versionCode = packageInfo.versionCode;
            String versionName = packageInfo.versionName == null ? "null" : packageInfo.versionName;
            infos.put("versionCode", versionCode + "");
            infos.put("versionName", versionName);
        } catch (PackageManager.NameNotFoundException e) {
            infos.put("exception1", e.getMessage());
        }
        String brand = Build.BRAND;
        String model = Build.MODEL;
        String device = Build.DEVICE;

        infos.put("brand", brand);
        infos.put("model", model);
        infos.put("device", device);
    }
}
