package com.hoc.imagefilter

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Intent
import android.content.Intent.ACTION_GET_CONTENT
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.zomato.photofilters.imageprocessors.Filter
import com.zomato.photofilters.imageprocessors.SubFilter
import com.zomato.photofilters.imageprocessors.subfilters.BrightnessSubFilter
import com.zomato.photofilters.imageprocessors.subfilters.ContrastSubFilter
import com.zomato.photofilters.imageprocessors.subfilters.SaturationSubfilter
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), FilterListFragment.FilterSelectedListener, EditImageFragment.EditImageListener {

    private var originalImage: Bitmap? = null
    private var filteredImage: Bitmap? = null
    private var finalImage: Bitmap? = null

    private var brightness = 0
    private var contrast = 1f
    private var saturation = 1f

    private lateinit var filterListFragment: FilterListFragment
    private lateinit var editImageFragment: EditImageFragment

    override fun onFilterSelected(filter: Filter) {
        resetControls()

        filteredImage = originalImage.copy()
        image_preview.setImageBitmap(filter.processFilter(filteredImage))
        finalImage = filteredImage.copy()
    }

    private fun resetControls() {
        editImageFragment.resetSeekBar()
        brightness = 0
        contrast = 1f
        saturation = 1f
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        supportActionBar?.run {
            setDisplayHomeAsUpEnabled(true)
            title = "Filter image"
        }

        view_pager.adapter = SectionsPagerAdapter(supportFragmentManager)
        tabs.setupWithViewPager(view_pager)

        loadImage()
    }

    private fun loadImage() {
        originalImage = getBitmapFromAsset(IMAGE_NAME, 300, 300)
        filteredImage = originalImage.copy()
        finalImage = originalImage.copy()
        image_preview.setImageBitmap(originalImage)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_open -> {
                openImageFromGallery()
                true
            }
            R.id.action_save -> {
                saveImageToGallery()
                true
            }
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun saveImageToGallery() {
        Dexter.withActivity(this)
                .withPermissions(READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE)
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                        when {
                            report.areAllPermissionsGranted() -> {
                                val res = contentResolver.insertImage(
                                        finalImage!!,
                                        "${System.currentTimeMillis()}_image.jpg",
                                        "Filtered image"
                                )
                                toast(if (res) "Image saved successfully!" else "Image saved failed")
                            }
                            else -> toast("Permissions are denied")
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?, token: PermissionToken?) {
                        token?.continuePermissionRequest()
                    }
                })
                .check()
    }

    private fun openImageFromGallery() {
        Dexter.withActivity(this)
                .withPermissions(READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE)
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                        if (report.areAllPermissionsGranted()) {
                            val intent = Intent(ACTION_GET_CONTENT).apply {
                                type = "image/*"
                            }
                            startActivityForResult(Intent.createChooser(intent, "Select image"), PICK_IMAGE_RC)
                        } else {
                            toast("Permissions are denied")
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?, token: PermissionToken?) {
                        token?.continuePermissionRequest()
                    }
                })
                .check()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            PICK_IMAGE_RC -> {
                if (resultCode == RESULT_OK) {
                    data?.data?.let { uri ->
                        val bitmap = getBitmapFromGallery(uri, 800, 800)

                        originalImage?.recycle()
                        filteredImage?.recycle()
                        finalImage?.recycle()

                        originalImage = bitmap.copy()
                        filteredImage = bitmap.copy()
                        finalImage = bitmap.copy()
                        bitmap?.recycle()

                        image_preview.setImageBitmap(originalImage)
                        filterListFragment.prepareThumbnail(originalImage)
                    }
                }
            }
        }
    }

    override fun onBrightnessChanged(brightness: Int) {
        this.brightness = brightness
        processFilter(BrightnessSubFilter(brightness))
    }

    override fun onContrastChanged(contrast: Float) {
        this.contrast = contrast
        processFilter(ContrastSubFilter(contrast))
    }

    override fun onSaturationChanged(saturation: Float) {
        this.saturation = saturation
        processFilter(SaturationSubfilter(saturation))
    }

    private fun processFilter(subFilter: SubFilter) {
        val bm = subFilter.process(finalImage.copy())
        image_preview.setImageBitmap(bm)
    }

    override fun onEditStarted() = Unit

    override fun onEditCompleted() {
        val bm = filteredImage.copy()
        finalImage = Filter().apply {
            addSubFilter(BrightnessSubFilter(brightness))
            addSubFilter(ContrastSubFilter(contrast))
            addSubFilter(SaturationSubfilter(saturation))
        }.processFilter(bm)
    }

    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
        override fun getItem(position: Int) = when (position) {
            0 -> FilterListFragment().apply { listener = this@MainActivity }.also {
                filterListFragment = it
            }
            1 -> EditImageFragment().apply { listener = this@MainActivity }.also {
                editImageFragment = it
            }
            else -> throw IllegalStateException()
        }

        override fun getPageTitle(position: Int) = when (position) {
            0 -> "Filters"
            1 -> "Edit"
            else -> null
        }

        override fun getCount() = 2
    }

    private fun Bitmap?.copy() = this?.copy(Bitmap.Config.ARGB_8888, true)

    private fun toast(charSequence: CharSequence) = Toast.makeText(this, charSequence, LENGTH_SHORT).show()

    companion object {
        const val PICK_IMAGE_RC = 1
        const val IMAGE_NAME = "image.jpeg"

        init {
            System.loadLibrary("NativeImageProcessor")
        }
    }
}
