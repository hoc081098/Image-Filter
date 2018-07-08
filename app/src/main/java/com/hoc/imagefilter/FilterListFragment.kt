package com.hoc.imagefilter

import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.zomato.photofilters.FilterPack
import com.zomato.photofilters.imageprocessors.Filter
import com.zomato.photofilters.utils.ThumbnailItem
import com.zomato.photofilters.utils.ThumbnailsManager
import kotlinx.android.synthetic.main.fragment_filter_list.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch

class FilterListFragment : Fragment() {
    private lateinit var thumbnailAdapter: ThumbnailAdapter
    private val parentJob = Job()
    private val thumbnailList = mutableListOf<ThumbnailItem>()
    var listener: FilterSelectedListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.fragment_filter_list, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        thumbnailAdapter = ThumbnailAdapter(thumbnailList, ::onFilterSelected)

        recycler_thumbnail.run {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = thumbnailAdapter
            val space = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, resources.displayMetrics).toInt()
            addItemDecoration(SpaceItemDecoration(space))
        }

        prepareThumbnail(null)
    }

    private fun onFilterSelected(filter: Filter) =
            listener?.onFilterSelected(filter)

    interface FilterSelectedListener {
        fun onFilterSelected(filter: Filter)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        parentJob.cancel()
    }

    fun prepareThumbnail(bitmap: Bitmap?) {
        launch(UI, parent = parentJob) {
            val result = async(CommonPool, parent = parentJob) {
                val thumb = bitmap
                        ?.let { Bitmap.createScaledBitmap(it, 100, 100, false) }
                        ?: context?.getBitmapFromAsset(MainActivity.IMAGE_NAME, 100, 100)
                        ?: return@async emptyList<ThumbnailItem>()

                ThumbnailsManager.clearThumbs()
                FilterPack.getFilterPack(context)
                        .map {
                            ThumbnailItem().apply {
                                filterName = it.name
                                image = thumb
                                filter = it
                            }
                        }
                        .forEach(ThumbnailsManager::addThumb)

                mutableListOf(ThumbnailItem().apply {
                    filterName = "Normal"
                    image = thumb
                }).apply {
                    addAll(ThumbnailsManager.processThumbs(context))
                }
            }.await()

            thumbnailList.run {
                clear()
                addAll(result)
            }
            thumbnailAdapter.notifyDataSetChanged()
        }
    }
}
