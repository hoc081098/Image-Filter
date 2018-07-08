package com.hoc.imagefilter

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View

class SpaceItemDecoration(private val space: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View?, parent: RecyclerView, state: RecyclerView.State) {
        outRect.run {
            when (parent.getChildAdapterPosition(view)) {
                state.itemCount - 1 -> {
                    left = space
                    right = 0
                }
                else -> {
                    left = 0
                    right = space
                }
            }
        }
    }
}