package com.openld.mapsample.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.openld.mapsample.R
import com.openld.mapsample.bean.POIBean
import com.openld.mapsample.interfaces.OnPoiItemClickListener

class POIAdapter() : RecyclerView.Adapter<POIAdapter.ViewHolder>() {
    private lateinit var mContext : Context
    private lateinit var mPoiBeanList : List<POIBean>
    private lateinit var mListener : OnPoiItemClickListener

    constructor(context : Context, poiBeanList : List<POIBean>) : this() {
        this.mContext = context
        this.mPoiBeanList = mPoiBeanList
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view : View = LayoutInflater.from(mContext).inflate(R.layout.item_poi, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mPoiBeanList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }

    /**
     * 设置POI条目点击的监听器
     */
    public fun setPoiItemClickListener(listener : OnPoiItemClickListener) {
        this.mListener = listener
    }
}
