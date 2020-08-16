package com.openld.mapsample.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.openld.mapsample.R
import com.openld.mapsample.bean.POIBean
import com.openld.mapsample.interfaces.OnPoiItemClickListener

class POIAdapter() : RecyclerView.Adapter<POIAdapter.ViewHolder>() {
    private lateinit var mContext: Context
    private lateinit var mPoiBeanList: List<POIBean>
    private lateinit var mListener: OnPoiItemClickListener

    constructor(context: Context, poiBeanList: List<POIBean>) : this() {
        this.mContext = context
        this.mPoiBeanList = poiBeanList
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(mContext).inflate(R.layout.item_poi, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mPoiBeanList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (itemCount == 0) {
            return
        }
        val poiBean = mPoiBeanList[position]

        holder.txtPoi.text = poiBean.poiName
        holder.txtPoiDetail.text = poiBean.address

        holder.poiItem.setOnClickListener(View.OnClickListener {
            mListener.onClickPoiItem(position, poiBean)
        })
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var poiItem : ConstraintLayout = itemView.findViewById(R.id.poi_item)
        var txtPoi: TextView = itemView.findViewById(R.id.txt_poi)
        var txtPoiDetail: TextView = itemView.findViewById(R.id.txt_poi_detail)
    }

    /**
     * 设置POI条目点击的监听器
     */
    public fun setPoiItemClickListener(listener: OnPoiItemClickListener) {
        this.mListener = listener
    }
}
