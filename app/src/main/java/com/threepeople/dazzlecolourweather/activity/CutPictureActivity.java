package com.threepeople.dazzlecolourweather.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.threepeople.dazzlecolourweather.R;
import com.threepeople.dazzlecolourweather.utils.Tools;
import com.threepeople.dazzlecolourweather.view.ImageCroppingView;

import java.io.FileNotFoundException;

public class CutPictureActivity extends AppCompatActivity {
    private final AppCompatActivity activity = this;

    private ImageCroppingView cropPicture;
    private View returnSetting;
    private View ok;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cut_picture);

        initView();
        myListener();
        Uri picUri = activity.getIntent().getParcelableExtra(Tools.picUriKey);
        if (picUri != null) {
            try {
                cropPicture.setPicture(picUri).refresh();
                return;
            } catch (FileNotFoundException e) {
                // ignore
            }
        }
        Toast.makeText(activity, "解析图片失败", Toast.LENGTH_SHORT).show();
        activity.setResult(RESULT_CANCELED);
        activity.finish();
    }

    private void initView() {
        cropPicture = findViewById(R.id.cropPicture);
        returnSetting = findViewById(R.id.returnSetting);
        ok = findViewById(R.id.ok);
    }

    private void myListener() {
        returnSetting.setOnClickListener(v -> onBackPressed());

        ok.setOnClickListener(v -> {
            Bitmap result = cropPicture.getCroppingResult();
            Intent back = new Intent();
            Bundle bundle = new Bundle();
            bundle.putBinder(Tools.cutResultKey, new Tools.BitmapBinder(result));
            back.putExtras(bundle);
            setResult(RESULT_OK, back);
            activity.finish();
        });
    }
}
