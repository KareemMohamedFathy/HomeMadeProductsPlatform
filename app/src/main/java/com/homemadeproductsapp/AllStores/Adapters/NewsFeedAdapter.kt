package com.homemadeproductsapp.AllStores.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.homemadeproductsapp.DB.Feed
import com.homemadeproductsapp.MyStore.Listeners.NewsFeedClickListener
import com.homemadeproductsapp.R

class NewsFeedAdapter(private val timeLinePhotos: ArrayList<Feed>, private val newsFeedClickListener: NewsFeedClickListener) : RecyclerView.Adapter<NewsFeedAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsFeedAdapter.ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.news_feed_layout, parent, false)
        return NewsFeedAdapter.ViewHolder(view)


    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        if (position * 2 < timeLinePhotos.size) {
            Glide.with(holder.itemView).load(timeLinePhotos[position].imagePathProduct)
                .into(holder.imageView1)
            holder.imageView1.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View?) {
                    newsFeedClickListener.onClick(timeLinePhotos[position])
                }

            })
        }
       /* if (position * 2 + 1 < timeLinePhotos.size) {
            Glide.with(holder.itemView).load(timeLinePhotos[(position * 2) + 1].imagePathProduct)
                .into(holder.imageView2)
            holder.imageView2.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View?) {
                    newsFeedClickListener.onClick(timeLinePhotos[(position * 2) + 1])
                }

            })

        }*/

    }


    override fun getItemCount(): Int {
            return timeLinePhotos.size


    }

    class ViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView){
        var imageView1: ImageView =itemView.findViewById(R.id.imageView1)
      //  var imageView2: ImageView =itemView.findViewById(R.id.imageView2)



    }

}

