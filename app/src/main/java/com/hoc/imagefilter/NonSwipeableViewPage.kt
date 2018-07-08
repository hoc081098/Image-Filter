package com.hoc.imagefilter

import android.annotation.SuppressLint
import android.content.Context
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.animation.DecelerateInterpolator
import android.widget.Scroller

class NonSwipeableViewPage(context: Context, attrs: AttributeSet?) : ViewPager(context, attrs) {
    init {
        setMyScroller()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent?) = false

    override fun onInterceptTouchEvent(ev: MotionEvent?) = false

    private fun setMyScroller() {
        ViewPager::class.java.getDeclaredField("mScroller")
                .apply {
                    isAccessible = true
                    set(this@NonSwipeableViewPage, MyScroller(context))
                }
    }

    private class MyScroller(context: Context?) : Scroller(context, DecelerateInterpolator()) {
        override fun startScroll(startX: Int, startY: Int, dx: Int, dy: Int, duration: Int) {
            super.startScroll(startX, startY, dx, dy, 350)
        }
    }
}