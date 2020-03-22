package com.baked.inscriptainventory

import android.app.Activity
import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        holder.clickableView.setOnClickListener {
            clickListener(position)
        }
    }
}

class ViewHolder (view: View) : RecyclerView.ViewHolder(view) {
    // Holds the TextView
    val clickableView = view.list_item
}