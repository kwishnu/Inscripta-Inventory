package com.baked.inscriptainventory

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.list_item.view.*

private val IMAGE_URI = arrayOf(
    "strip_tube_1",
    "strip_tube_4",
    "strip_tube_6",
    "tubes_w_caps",
    "cell_tube",
    "rotary_growth_vial",
    "blank"
    )
class InventoryAdapter(private val items: MutableList<MutableList<String>>, private val context: Context, val clickListener: (Int) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): RecyclerView.ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.list_item, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).clickableView.list_item.text = items[holder.adapterPosition][1]
//        var uri: Uri = Uri.parse("R.drawable.strip_tube_4")
        val uri =
            Uri.parse("android.resource://com.baked.inscriptainventory/drawable/" + IMAGE_URI[(items[holder.adapterPosition][0]).toInt()] )
        holder.image.setImageURI(uri)

        holder.clickableView.setOnClickListener {
            clickListener(position)
        }
    }
}

class ViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView) {
    val clickableView : RelativeLayout = itemView.list_element
    val image: ImageView = itemView.list_thumbnail
}