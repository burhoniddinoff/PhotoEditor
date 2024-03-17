package com.example.photoeditor.presenter.screen

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PointF
import android.media.MediaScannerConnection
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.get
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.photoeditor.R
import com.example.photoeditor.data.EmojiData
import com.example.photoeditor.databinding.ScreenEditorBinding
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.UUID
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.sqrt

class EditorScreen : Fragment(R.layout.screen_editor) {

    private val binding by viewBinding(ScreenEditorBinding::bind)
    private val list = ArrayList<ViewGroup>()
    private val data = EmojiData(0, R.drawable.ic_glasses)

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.container.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) addEmoji(event.x, event.y)
            return@setOnTouchListener true
        }

        binding.saveButton.setOnClickListener {
            saveImageToGallery()
        }

    }

    private fun addEmoji(x: Float, y: Float) {

        clearSelectedState()

        val emojiContainer = LayoutInflater.from(requireContext()).inflate(R.layout.emoji_container, binding.container, false) as ViewGroup

        emojiContainer[0].isSelected = true
        (emojiContainer[1] as ImageView).setImageResource(data.imageRes)
        emojiContainer[2].setOnClickListener {
            list.remove(emojiContainer)
            binding.container.removeView(emojiContainer)
        }

        emojiContainer.x = x
        emojiContainer.y = y

        binding.container.addView(emojiContainer)
        list.add(emojiContainer)
        onTouchListener(emojiContainer)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun onTouchListener(viewGroup: ViewGroup) {
        var initTouchX = 0f
        var initTouchY = 0f
        var isInitState = false
        var initDistance = 0f
        var initAngle = 0f

        viewGroup.setOnTouchListener { v, event ->
            when (event.action) {

                MotionEvent.ACTION_DOWN -> {
                    clearSelectedState()

                    viewGroup[0].isSelected = true
                    viewGroup[2].visibility = View.VISIBLE

                    initTouchX = event.x
                    initTouchY = event.y

                }

                MotionEvent.ACTION_MOVE -> {
                    if (event.pointerCount == 1) {
                        viewGroup.x += event.x - initTouchX
                        viewGroup.y += event.y - initTouchY
                    }

                    if (event.pointerCount == 2) {

                        if (!isInitState) {
                            val firstPointF = PointF(event.getX(0), event.getY(0))
                            val secondPointF = PointF(event.getX(1), event.getY(1))

                            initDistance = firstPointF distance secondPointF
                            initAngle = atan((event.getY(1) - event.getY(0)) / (event.getX(1) - event.getX(0)))
                            isInitState = true
                        }

                        val nextFirstPointF = PointF(event.getX(0), event.getY(0))
                        val nextSecondPointF = PointF(event.getX(1), event.getY(1))
                        val newDistance = nextFirstPointF distance nextSecondPointF
                        viewGroup.scaleX *= newDistance / initDistance
                        viewGroup.scaleY *= newDistance / initDistance


                        val newAngle = atan((event.getY(1) - event.getY(0)) / (event.getX(1) - event.getX(0)))
                        val diffAngle = (newAngle - initAngle) * 180 / PI
                        viewGroup.rotation += diffAngle.toFloat()

                    } else isInitState = false

                }
            }

            return@setOnTouchListener true
        }
    }

    private fun clearSelectedState() {
        list.forEach {
            it[0].isSelected = false
            it[2].visibility = View.GONE
        }
    }


    fun saveImageToGallery() {
        val context: Context = requireContext()
        val frameLayout = binding.container
        val bitmap =
            Bitmap.createBitmap(frameLayout.width, frameLayout.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        frameLayout.draw(canvas)
        val filename = "${UUID.randomUUID()}.jpg"
        val saveDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)

        if (!saveDir.exists()) {
            saveDir.mkdirs()
        }

        val file = File(saveDir, filename)

        try {
            val outputStream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()

            // Tell the media scanner to scan the new image, so it will be visible in the gallery
            MediaScannerConnection.scanFile(
                context,
                arrayOf(file.absolutePath),
                null,
                null
            )

            Toast.makeText(context, "Image saved to gallery", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to save image", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }


}


infix fun PointF.distance(pointF: PointF): Float =
    sqrt((this.x - pointF.x) * (this.x - pointF.x) + (this.y - pointF.y) * (this.y - pointF.y))