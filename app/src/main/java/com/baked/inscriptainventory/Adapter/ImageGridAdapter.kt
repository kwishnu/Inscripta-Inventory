package com.baked.inscriptainventory.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.baked.inscriptainventory.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_grid.view.*
private const val TAG = "InscriptaInventory_IGA"
private var lastClicked = 0

class ImageGridAdapter(private val context: Context, private val images: Array<String>, val imageListener: (Int) -> Unit) :
    RecyclerView.Adapter<ImageGridAdapter.ViewHolder>() {
    companion object {
        var setIGAtoZero = 0
    }

    override fun getItemCount(): Int {
        return images.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_grid, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        lastClicked = setIGAtoZero
        val path = images[position]
        Picasso.get()
            .load(path)
            .centerCrop()
            .resize(50, 0)
            .into(holder.iv)
        if (position == lastClicked) {
            holder.iv.background =
                ContextCompat.getDrawable(
                    context,
                    R.drawable.radio_flat_selected
                )
        } else {
            holder.iv.background =
                ContextCompat.getDrawable(
                    context,
                    R.drawable.radio_flat_regular
                )
        }

        holder.iv.setOnClickListener{v: View ->
            v.background =
                ContextCompat.getDrawable(
                    context,
                    R.drawable.radio_flat_selected
                )
            lastClicked = position
            setIGAtoZero = position
            imageListener(position)
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val iv = view.iv as ImageView
    }
}