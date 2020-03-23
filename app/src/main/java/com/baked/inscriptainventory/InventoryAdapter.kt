package com.baked.inscriptainventory

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.list_item.view.*

//removed: , val clickListener: (Int) -> Unit
class InventoryAdapter(private val items: ArrayList<String>, val context : Context, val clickListener: (Int) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): RecyclerView.ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.list_item, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).clickableView?.text = items[holder.adapterPosition]
//        var uri: Uri = Uri.parse("R.drawable.strip_tube_4")
        val uri =
            Uri.parse("android.resource://com.baked.inscriptainventory/drawable/strip_tube_4")
        holder.image.setImageURI(uri)

        holder.clickableView.setOnClickListener {
            clickListener(position)
        }
    }
}

class ViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView) {
    // Holds the TextView
    val clickableView = itemView.list_item
    val image: ImageView = itemView.list_thumbnail
}