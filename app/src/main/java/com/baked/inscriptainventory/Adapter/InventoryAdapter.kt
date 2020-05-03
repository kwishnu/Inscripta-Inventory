package com.baked.inscriptainventory.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.baked.inscriptainventory.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.list_item.view.*
private const val TAG = "InscriptaInventory_IA"

class InventoryAdapter(
    private val items: MutableList<MutableList<String>>,
    private val context: Context,
    val clickListener: (Int) -> Unit,
    val imageListener: (Int) -> Unit,
    val numberListener: (Int) -> Unit,
    val thumbnailListener: (Int) -> Unit,
    val longClickListener: (Int, View) -> Unit,
    val images: MutableList<String>
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
        val partNumberStr = if ((items[holder.adapterPosition][1]).isBlank() || items[holder.adapterPosition][1] == "null") "None" else items[holder.adapterPosition][1]
        holder.clickableView.list_subtitle.text = "Cat. No.: $partNumberStr"
        val minStockLevel =  if ((items[holder.adapterPosition][3]).isEmpty() || items[holder.adapterPosition][3] == "null") ("0") else (items[holder.adapterPosition][3])
        val numInStockStr =  if ((items[holder.adapterPosition][4]).isEmpty() || items[holder.adapterPosition][4] == "null") ("0") else (items[holder.adapterPosition][4])
        val lowQuantityAlarm = numInStockStr.toInt() <= minStockLevel.toInt()//true if count is less than or equal to Min Stock Level
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
        val path = if ((items[holder.adapterPosition][0]).isBlank() || items[holder.adapterPosition][0] == "null") images[0] else images[(items[holder.adapterPosition][0]).toInt()]
        Picasso.get()
            .load(path)
            .centerCrop()
            .resize(50, 0)
            .into(holder.image)

        val commentsImageStr = if (items[holder.adapterPosition][5].isEmpty() || items[holder.adapterPosition][5] == "null") "blank" else "comments"
        val commentsUri = Uri.parse("android.resource://com.baked.inscriptainventory/drawable/$commentsImageStr")
        holder.clickableImage.setImageURI(commentsUri)
        holder.clickableView.setOnClickListener {
            clickListener(position)
        }
        holder.clickableImage.setOnClickListener {
            imageListener(position)
        }
        holder.detail.setOnClickListener {
            numberListener(position)
        }
        holder.clickableThumbnail.setOnClickListener {
            thumbnailListener(position)
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
    val clickableThumbnail : ImageView = itemView.list_thumbnail
    val detail: TextView = itemView.list_detail
    val image: ImageView = itemView.list_thumbnail
}
