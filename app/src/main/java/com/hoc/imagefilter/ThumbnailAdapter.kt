package com.hoc.imagefilter

import android.graphics.Bitmap
import android.support.v4.content.ContextCompat.getColor
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.zomato.photofilters.imageprocessors.Filter
import com.zomato.photofilters.utils.ThumbnailItem
import kotlinx.android.synthetic.main.thumbnail_item_layout.view.*

class ThumbnailAdapter(
        private val thumbnailItems: List<ThumbnailItem>,
        private val filterSelectedListener: (Filter) -> Unit?
) : RecyclerView.Adapter<ThumbnailAdapter.ViewHolder>() {
    private var selectedIndex = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            LayoutInflater.from(parent.context)
                    .inflate(R.layout.thumbnail_item_layout, parent, false)
                    .let(::ViewHolder)

    override fun getItemCount() = thumbnailItems.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val thumbnailItem = thumbnailItems[position]
        holder.run {
            thumbnailImage.setImageBitmap(thumbnailItem.image)
            thumbnailImage.setOnClickListener {
                notifyItemChanged(selectedIndex)
                notifyItemChanged(adapterPosition)
                selectedIndex = adapterPosition
                filterSelectedListener(thumbnailItem.filter ?: return@setOnClickListener)
            }

            nameText.run {
                text = thumbnailItem.filterName
                val color = if (adapterPosition == selectedIndex) R.color.colorSelectedFilter else R.color.colorNonSelectedFilter
                setTextColor(getColor(context, color))
            }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameText = itemView.name!!
        val thumbnailImage = itemView.thumbnail!!
    }
}