package com.openld.mapsample

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

/**
 * author: lllddd
 * created on: 2020/8/16 17:04
 * description:
 */

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private val REQUEST_CODE_FOR_AMAP: Int = 1000
    private lateinit var mBtnMapSelectPoint: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initWidgets();
    }

    private fun initWidgets() {
        mBtnMapSelectPoint = findViewById(R.id.btn_map_select_point)
        mBtnMapSelectPoint.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        val id = v!!.id
        val intent :Intent
        if (id == R.id.btn_map_select_point) {
            intent = Intent(this, AMapActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_FOR_AMAP);
        }
    }
}