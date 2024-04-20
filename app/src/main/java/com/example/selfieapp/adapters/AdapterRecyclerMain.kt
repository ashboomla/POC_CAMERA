package com.example.selfieapp.adapters

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.selfieapp.models.Image
import com.example.selfieapp.R
import kotlinx.android.synthetic.main.row_image_adapter_main.view.*

class AdapterRecyclerMain(var mContext: Context): RecyclerView.Adapter<AdapterRecyclerMain.MyViewHolder>() {

    private var imageList : ArrayList<Image> = ArrayList();
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        var view = LayoutInflater.from(mContext).inflate(R.layout.row_image_adapter_main,parent,false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    override fun onBindViewHolder(holder: AdapterRecyclerMain.MyViewHolder, position: Int) {
        var image = imageList[position]
        holder.bind(image)
    }

     fun setData(imageList : ArrayList<Image>){
        this.imageList = imageList
        notifyDataSetChanged()
    }


    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(image : Image) {

            itemView.image_view.setImageURI(Uri.parse(image.path))

        }
    }
}
