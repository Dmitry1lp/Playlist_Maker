package com.practicum.playlistmaker.player.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF
import android.view.MotionEvent
import androidx.core.graphics.drawable.toBitmap
import android.graphics.drawable.Drawable
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import com.practicum.playlistmaker.R


class PlaybackButtonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0,
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private var playDrawable: Drawable? = null
    private var pauseDrawable: Drawable? = null
    private var isPlaying = false
    var onButtonClick: (() -> Unit)? = null

    private var imageRect = RectF(0f, 0f, 0f, 0f)

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.PlaybackButtonView,
            defStyleAttr,
            defStyleRes
        ).apply {
            try {
                playDrawable = getDrawable(R.styleable.PlaybackButtonView_playView)
                pauseDrawable = getDrawable(R.styleable.PlaybackButtonView_pauseView)
            } finally {
                recycle()
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                return true
            }
            MotionEvent.ACTION_UP -> {
                onButtonClick?.invoke()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredSize = playDrawable?.intrinsicWidth ?: 120

        val width = resolveSize(desiredSize, widthMeasureSpec)
        val height = resolveSize(desiredSize, heightMeasureSpec)

        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        imageRect = RectF(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat())
    }

    override fun onDraw(canvas: Canvas) {
        val image = if (isPlaying) pauseDrawable else playDrawable
        image?.let {
            it.setBounds(0,0,width,height)
            it.draw(canvas)
        }
    }

    fun setPlaying(playing: Boolean) {
        if(isPlaying != playing) {
            isPlaying = playing
            invalidate()
        }
    }
}