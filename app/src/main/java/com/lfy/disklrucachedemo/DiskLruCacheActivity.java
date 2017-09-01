package com.lfy.disklrucachedemo;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.jakewharton.disklrucache.DiskLruCache;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DiskLruCacheActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "DiskLruCacheActivity";
    private ImageView imageView;

    private DiskLruCache diskLruCache;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disklrucache);
        imageView = (ImageView) findViewById(R.id.imageView);
        Button getBtn = (Button) findViewById(R.id.getBtn);
        Button resetBtn = (Button) findViewById(R.id.resetBtn);
        Button clearBtn = (Button) findViewById(R.id.clearBtn);
        getBtn.setOnClickListener(this);
        resetBtn.setOnClickListener(this);
        clearBtn.setOnClickListener(this);
    }

    private void getImage() {
        final String url = "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1504180943740&di=783b001829759c16b0f21717dcf99fb8&imgtype=0&src=http%3A%2F%2Fimg5.duitang.com%2Fuploads%2Fitem%2F201411%2F13%2F20141113232344_HyJ4s.jpeg";
        final String key = hashKeyForDisk(url);
        if (!getImageFromCache(key)) {
            getImageFromNet(url, key);
        }
    }

    private boolean getImageFromCache(String key) {
        try {
            DiskLruCache.Snapshot snapshot = diskLruCache.get(key);
            if (snapshot != null) {
                InputStream inputStream = snapshot.getInputStream(0);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                imageView.setImageBitmap(bitmap);
                inputStream.close();
                snapshot.close();
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void getImageFromNet(final String url, final String key) {
        Log.e(TAG, "从网上下载、、、、、、、、、、、、");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DiskLruCache.Editor editor = diskLruCache.edit(key);
                    if (editor != null) {
                        OutputStream outputStream = editor.newOutputStream(0);
                        boolean b = downUrlToStream(url, outputStream);
                        if (b) {
                            editor.commit();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    getImageFromCache(key);
                                }
                            });
                        } else {
                            editor.abort();
                        }
                    }
                    diskLruCache.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void initDiskLruCache() {
        File dir = getDiskLruCacheDir("bitmap");
        int appVersion = getAppVersion();
        long maxSize = getMaxCacheSize();
        try {
            diskLruCache = DiskLruCache.open(dir, appVersion, 1, maxSize);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private long getMaxCacheSize() {
        return 10 * 1024 * 1024;
    }

    private int getAppVersion() {
        try {
            PackageManager packageManager = getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), PackageManager.GET_ACTIVITIES);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 1;
    }

    private File getDiskLruCacheDir(String fileName) {
        String dirPath;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)
                || !Environment.isExternalStorageRemovable()) {
            dirPath = getExternalCacheDir().getPath();
        } else {
            dirPath = getCacheDir().getPath();
        }
        File file = new File(dirPath + File.separator + fileName);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file;
    }

    private boolean downUrlToStream(String urlString, OutputStream outputStream) {
        HttpURLConnection connection = null;
        BufferedInputStream bufferedInputStream = null;
        BufferedOutputStream bufferedOutputStream = null;
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();

            InputStream inputStream = connection.getInputStream();
            bufferedInputStream = new BufferedInputStream(inputStream);
            bufferedOutputStream = new BufferedOutputStream(outputStream);
            int b;
            while ((b = bufferedInputStream.read()) != -1) {
                bufferedOutputStream.write(b);
            }
            bufferedOutputStream.flush();
            return true;
        } catch (Exception e) {
            Looper.prepare();
            showToast("下载图片失败请检查网络连接！");
            Looper.loop();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            try {
                if (bufferedOutputStream != null) {
                    bufferedOutputStream.close();
                }
                if (bufferedInputStream != null) {
                    bufferedInputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private String hashKeyForDisk(String key) {
        String cacheKey = "";
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(key.getBytes());
            cacheKey = bytesToHexString(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return cacheKey;
    }

    private String bytesToHexString(byte[] digest) {
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            String hexString = Integer.toHexString(0xFF & b);
            if (hexString.length() == 1) {
                sb.append('0');
            }
            sb.append(hexString);
        }
        return sb.toString();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.getBtn:
                initDiskLruCache();
                getImage();
                break;
            case R.id.resetBtn:
                imageView.setImageResource(R.mipmap.ic_launcher);
                break;
            case R.id.clearBtn:
                if (diskLruCache != null) {
                    try {
                        diskLruCache.delete();
                        imageView.setImageResource(R.mipmap.ic_launcher);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }
}
