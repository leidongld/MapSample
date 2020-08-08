package com.openld.mapsample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int REQUEST_CODE_FOR_AMAP = 1 << 10;
    private Button mBtnMapSelectPoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initWidgets();
    }

    private void initWidgets() {
        mBtnMapSelectPoint = findViewById(R.id.btn_map_select_point);
        mBtnMapSelectPoint.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        Intent intent;
        if (id == R.id.btn_map_select_point) {
            intent = new Intent(this, AMapActivity.class);
            startActivityForResult(intent, REQUEST_CODE_FOR_AMAP);
        }
    }
}