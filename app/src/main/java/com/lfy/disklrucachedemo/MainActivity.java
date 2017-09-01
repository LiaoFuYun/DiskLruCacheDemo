package com.lfy.disklrucachedemo;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class MainActivity extends BaseActivity {
    private static final String TAG = "MainActivity";
    private ArrayList<String> mListDatas = new ArrayList<>();
    private RecyclerAdapter mAdapter;
    private StaggeredGridLayoutManager mLayoutManager;

    private ProgressDialog mProgressDialog;

    // 临时的辅助类，用于防止同一个文件夹的多次扫描
    private HashSet<String> mDirPaths = new HashSet<>();
    // 存储临时文件夹图片数量
    private int mTempFileSize = 0;
    // 存储临时文件夹路径
    private File mTempFileDir;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0x11:
                    showImage();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mAdapter = new RecyclerAdapter(this, mListDatas);
        mLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(mLayoutManager);
        getImages();
    }

    // 获取图片
    private void getImages() {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            showToast("暂无外部存储");
            return;
        }
        mProgressDialog = ProgressDialog.show(this, null, "正在加载中", true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Uri contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                ContentResolver contentResolver = MainActivity.this.getContentResolver();
                Cursor cursor = contentResolver.query(contentUri,
                        null,
                        MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=?",
                        new String[]{"image/jpeg", "image/png"},
                        MediaStore.Images.Media.DATE_MODIFIED
                );
                if (cursor == null) {
                    showToast("手机中暂无图片");
                    return;
                }
                while (cursor.moveToNext()) {
                    // 图片路径
                    String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    File parentFile = new File(path).getParentFile();
                    String dir = parentFile.getAbsolutePath();
                    if (mDirPaths.contains(dir)) {
                        continue;
                    } else {
                        mDirPaths.add(dir);
                    }
                    int fileSize = parentFile.listFiles(new FilenameFilter() {
                        @Override
                        public boolean accept(File dir, String name) {
                            return name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".bmp") || name.endsWith(".jpeg");
                        }
                    }).length;

                    if (fileSize >= mTempFileSize) {
                        mTempFileSize = fileSize;
                        mTempFileDir = parentFile;
                    }
                }
                cursor.close();
                mDirPaths = null;
                mHandler.sendEmptyMessage(0x11);
            }
        }).start();
    }

    private void showImage() {
        mProgressDialog.dismiss();
        if (mTempFileDir == null) {
            showToast("手机中暂无图片");
            return;
        }
        String[] paths = mTempFileDir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".bmp") || name.endsWith(".jpeg");
            }
        });
        List<String> list = Arrays.asList(paths);
        mListDatas.addAll(list);
        mAdapter.setDirPath(mTempFileDir.getAbsolutePath());
    }
}
