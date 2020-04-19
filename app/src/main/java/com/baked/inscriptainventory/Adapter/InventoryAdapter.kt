package com.baked.inscriptainventory.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.baked.inscriptainventory.Resource.ImagesArray
import com.baked.inscriptainventory.R
import kotlinx.android.synthetic.main.list_item.view.*
private const val TAG = "InscriptaInventory_IA"

class InventoryAdapter(private val items: MutableList<MutableList<String>>,
                       private val context: Context,
                       val clickListener: (Int) -> Unit,
                       val imageListener: (Int) -> Unit,
                       val longClickListener: (Int, View) -> Unit
):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): RecyclerView.ViewHolder {
        return ViewHolder(
            LayoutInflater.from(
                context
            ).inflate(
                R.layout.list_item,
                parent,
                false
            )
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).clickableView.list_item.text = items[holder.adapterPosition][2]
        val partNumberStr = items[holder.adapterPosition][1]
        holder.clickableView.list_subtitle.text = "Part No.: $partNumberStr"
        val numInStockStr =  if (items[holder.adapterPosition][4] == "null") ("0") else (items[holder.adapterPosition][4])
        val lowQuantityAlarm = numInStockStr.toInt() <= (items[holder.adapterPosition][3]).toInt()//true if count is less than or equal to Min Stock Level
        holder.detail.text = numInStockStr

        if (lowQuantityAlarm) {
            holder.detail.background =
                ContextCompat.getDrawable(
                    context,
                    R.drawable.details_rect_red
                )
        } else {
            holder.detail.background =
                ContextCompat.getDrawable(
                    context,
                    R.drawable.details_rect_green
                )
        }
        val commentsImageStr = if (items[holder.adapterPosition][5].isEmpty() || items[holder.adapterPosition][5] == "null") "blank" else "comments"
        val itemImageStr = ImagesArray().IMAGE_URI[(items[holder.adapterPosition][0]).toInt()]
        val uri = Uri.parse("android.resource://com.baked.inscriptainventory/drawable/$itemImageStr")
        val commentsUri = Uri.parse("android.resource://com.baked.inscriptainventory/drawable/$commentsImageStr")
        holder.image.setImageURI(uri)
        holder.clickableImage.setImageURI(commentsUri)
        holder.clickableView.setOnClickListener {
            clickListener(position)
        }
        holder.clickableImage.setOnClickListener {
            imageListener(position)
        }
        holder.clickableView.setOnLongClickListener {
            val view = holder.itemView
            longClickListener(position, view)
            true
        }
    }
}


class ViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView) {
    val clickableView : RelativeLayout = itemView.list_text_layout
    val clickableImage : ImageView = itemView.comment_image
    val detail: TextView = itemView.list_detail
    val image: ImageView = itemView.list_thumbnail
}
