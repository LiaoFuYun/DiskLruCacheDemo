package com.lfy.disklrucachedemo;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.util.LruCache;
import android.widget.ImageView;

import com.jakewharton.disklrucache.DiskLruCache;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;


public class PhotoWallAdapter extends CommonRecyclerAdapter<String> {
    private Context mContext;
    private RecyclerView mRecyclerView;
    private LruCache<String, Bitmap> mMemoryCache;
    private DiskLruCache mDiskCache;

    public PhotoWallAdapter(Context mContext, RecyclerView mRecyclerView, ArrayList<String> mDatas) {
        super(mContext, mDatas, R.layout.recycler_item);
        this.mContext = mContext;
        this.mRecyclerView = mRecyclerView;
        long maxMemory = Runtime.getRuntime().maxMemory();
        mMemoryCache = new LruCache<>((int) (maxMemory / 8));
        initDiskLruCache();
    }

    @Override
    public void convert(CommonRecycleHolder holder, String data, int position) {
        ImageView imageView = holder.findView(R.id.imageV);
        imageView.setTag(data);
        loadImage(imageView, data);
    }

    private void loadImage(ImageView imageView, String url) {
        String key = hashKeyForDisk(url);
        Bitmap bitmap;
        try {
            bitmap = getBitmapFromMemory(key);
            if (bitmap == null) {
                new BitmapWorker().execute(url);
            } else {
                System.out.println("从内存中加载~~~~~~~~~~~~~~~~~~~~~");
                imageView.setImageBitmap(bitmap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            return false;
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
    }

    private Bitmap getBitmapFromMemory(String key) {
        return mMemoryCache.get(key);
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

    private void initDiskLruCache() {
        File dir = getDiskLruCacheDir("photo");
        int appVersion = getAppVersion();
        long maxSize = getMaxCacheSize();
        try {
            mDiskCache = DiskLruCache.open(dir, appVersion, 1, maxSize);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private long getMaxCacheSize() {
        return 10 * 1024 * 1024;
    }

    private int getAppVersion() {
        try {
            PackageManager packageManager = mContext.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(mContext.getPackageName(), PackageManager.GET_ACTIVITIES);
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
            dirPath = mContext.getExternalCacheDir().getPath();
        } else {
            dirPath = mContext.getCacheDir().getPath();
        }
        File file = new File(dirPath + File.separator + fileName);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file;
    }

    private class BitmapWorker extends AsyncTask<String, Void, Bitmap> {
        private String mImageUrl;

        @Override
        protected Bitmap doInBackground(String... params) {
            mImageUrl = params[0];
            String key = hashKeyForDisk(params[0]);
            FileInputStream inputStream = null;
            FileDescriptor descriptor = null;
            DiskLruCache.Snapshot snapshot;
            try {
                snapshot = mDiskCache.get(key);
                if (snapshot == null) {
                    System.out.println("从网络上下载======================================");
                    DiskLruCache.Editor editor = mDiskCache.edit(key);
                    if (editor != null) {
                        OutputStream outputStream = editor.newOutputStream(0);
                        boolean b = downUrlToStream(params[0], outputStream);
                        if (b) {
                            editor.commit();
                        } else {
                            editor.abort();
                        }
                    }
                    snapshot = mDiskCache.get(key);
                }
                if (snapshot != null) {
                    inputStream = (FileInputStream) snapshot.getInputStream(0);
                    descriptor = inputStream.getFD();
                }
                Bitmap bitmap = null;
                if (descriptor != null) {
                    bitmap = BitmapFactory.decodeFileDescriptor(descriptor);
                }
                if (bitmap != null) {
                    if (mMemoryCache.get(key) == null) {
                        mMemoryCache.put(key, bitmap);
                    }
                }
                return bitmap;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (descriptor != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            ImageView mImageView = (ImageView) mRecyclerView.findViewWithTag(mImageUrl);
            if (mImageView != null && bitmap != null) {
                mImageView.setImageBitmap(bitmap);
                notifyDataSetChanged();
            }
        }
    }
}
