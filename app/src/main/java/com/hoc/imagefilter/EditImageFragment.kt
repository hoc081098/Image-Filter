package com.hoc.imagefilter

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import kotlinx.android.synthetic.main.fragment_main.*

class EditImageFragment : Fragment(), SeekBar.OnSeekBarChangeListener {
    var listener: EditImageListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.fragment_main, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        seekbar_brightness.run {
            // -100..100, default: 0
            max = 200
            progress = 100
            setOnSeekBarChangeListener(this@EditImageFragment)
        }
        seekbar_contrast.run {
            // 1.0..3.0, default: 1.0
            max = 20
            progress = 0
            setOnSeekBarChangeListener(this@EditImageFragment)
        }
        seekbar_saturation.run {
            // 0.0..3.0, default: 1.0
            max = 30
            progress = 10
            setOnSeekBarChangeListener(this@EditImageFragment)
        }
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        when (seekBar?.id) {
            R.id.seekbar_brightness -> listener?.onBrightnessChanged(progress - 100)
            R.id.seekbar_contrast -> listener?.onContrastChanged(0.1f * (progress + 10))
            R.id.seekbar_saturation -> listener?.onSaturationChanged(0.1f * progress)
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
        listener?.onEditStarted()
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        listener?.onEditCompleted()
    }

    fun resetSeekBar() {
        seekbar_brightness.progress = 100
        seekbar_contrast.progress = 0
        seekbar_saturation.progress = 10
    }

    interface EditImageListener {
        fun onBrightnessChanged(brightness: Int)

        fun onContrastChanged(contrast: Float)

        fun onSaturationChanged(saturation: Float)

        fun onEditStarted()

        fun onEditCompleted()
    }
}