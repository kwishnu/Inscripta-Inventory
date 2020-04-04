package com.baked.inscriptainventory

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.list_item.view.*
private const val TAG = "InscriptaInventory_IA"

class InventoryAdapter(private val items: MutableList<MutableList<String>>, private val context: Context, val clickListener: (Int) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): RecyclerView.ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.list_item, parent, false))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).clickableView.list_item.text = items[holder.adapterPosition][3]
        val partNumberStr = items[holder.adapterPosition][2]
        holder.clickableView.list_subtitle.text = "Part No.: $partNumberStr"
        val numInStockStr =  if (items[holder.adapterPosition][5] == "null") ("0") else (items[holder.adapterPosition][5])
        val lowQuantityAlarm = numInStockStr.toInt() <= (items[holder.adapterPosition][4]).toInt()//true if count is less than or equal to Min Stock Level
        holder.clickableView.list_detail.text = numInStockStr
        if (lowQuantityAlarm) {
            holder.clickableView.list_detail.background =
                ContextCompat.getDrawable(
                    context,
                    R.drawable.details_rect_red
                )
        } else {
            holder.clickableView.list_detail.background =
                ContextCompat.getDrawable(
                    context,
                    R.drawable.details_rect_green
                )
        }
        val itemImageStr = ImagesArray().IMAGE_URI[(items[holder.adapterPosition][1]).toInt()]
        val uri = Uri.parse("android.resource://com.baked.inscriptainventory/drawable/$itemImageStr")
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