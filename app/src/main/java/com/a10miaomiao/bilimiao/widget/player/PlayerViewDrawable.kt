package com.a10miaomiao.bilimiao.widget.player

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ClipDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.view.Gravity
import android.view.ViewGroup
import com.a10miaomiao.bilimiao.comm.utils.dip
import com.a10miaomiao.bilimiao.comm.utils.dipFloat

object PlayerViewDrawable {

    fun progressBarDrawable(
        context: Context,
        themeColor: Int
    ): Drawable {
        // 背景层
        // EN: Background layer
        val backgroundDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = context.dipFloat(3f)
            setColor(Color.parseColor("#ECF0F1")) // 背景颜色
            // EN: Background color
        }

        // 次要进度层
        // EN: Secondary progress layer
        val secondaryProgressDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = context.dipFloat(3f)
            setColor(Color.parseColor("#C6CACE")) // 次要进度颜色
            // EN: Secondary progress color
        }
        val secondaryProgressClip = ClipDrawable(secondaryProgressDrawable, Gravity.START, ClipDrawable.HORIZONTAL)

        // 主要进度层
        // EN: Primary progress layer
        val progressDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = context.dipFloat(3f)
            setColor(themeColor) // 主要进度颜色
            // EN: Primary progress color
        }
        val progressClip = ClipDrawable(progressDrawable, Gravity.START, ClipDrawable.HORIZONTAL)

        return LayerDrawable(
            arrayOf(backgroundDrawable, secondaryProgressClip, progressClip)
        ).apply {
            setId(0, android.R.id.background) // 背景层
            // EN: Background layer
            setId(1, android.R.id.secondaryProgress) // 次要进度层
            // EN: Secondary progress layer
            setId(2, android.R.id.progress) // 主要进度层
            // EN: Primary progress layer
        }
    }

    fun bottomProgressBarDrawable(
        context: Context,
        themeColor: Int
    ): Drawable {
        // 背景层
        // EN: Background layer
        val backgroundDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = context.dipFloat(1f)
            setColor(Color.parseColor("#4c000000")) // 背景颜色（半透明黑色）
            // EN: Background color (semi-transparent black)
            setSize(ViewGroup.LayoutParams.MATCH_PARENT, context.dip(4))
        }

        // 次要进度层
        // EN: Secondary progress layer
        val secondaryProgressDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = context.dipFloat(1f)
            setColor(Color.parseColor("#ffe0e0e0")) // 次要进度颜色
            // EN: Secondary progress color
            setSize(ViewGroup.LayoutParams.MATCH_PARENT, context.dip(4))
        }
        val secondaryProgressClip = ClipDrawable(secondaryProgressDrawable, Gravity.START, ClipDrawable.HORIZONTAL)

        // 主要进度层
        // EN: Primary progress layer
        val progressDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = context.dipFloat(1f)
            setColor(themeColor) // 主要进度颜色（从主题中获取 colorAccent）
            // EN: Primary progress color (get colorAccent from theme)
            setSize(ViewGroup.LayoutParams.MATCH_PARENT, context.dip(4))
        }
        val progressClip = ClipDrawable(progressDrawable, Gravity.START, ClipDrawable.HORIZONTAL)

        // 创建 LayerDrawable
        // EN: Create LayerDrawable
        return LayerDrawable(arrayOf(backgroundDrawable, secondaryProgressClip, progressClip)).apply {
            setId(0, android.R.id.background) // 背景层
            // EN: Background layer
            setId(1, android.R.id.secondaryProgress) // 次要进度层
            // EN: Secondary progress layer
            setId(2, android.R.id.progress) // 主要进度层
            // EN: Primary progress layer
        }
    }

    fun videoVolumeProgress(
        context: Context,
        themeColor: Int
    ): Drawable {
        // 背景 shape
        // EN: Background shape
        val backgroundDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(Color.WHITE) // #ffffffff
            cornerRadius = context.resources.displayMetrics.density * 2f // 2dp
        }

        // 前景 progress shape
        // EN: Foreground progress shape
        val progressDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(themeColor)
            cornerRadius = context.resources.displayMetrics.density * 2f // 2dp
        }

        // Clip 包裹 progress
        // EN: Clip wrapping progress
        val clipProgressDrawable = ClipDrawable(
            progressDrawable,
            Gravity.BOTTOM,
            ClipDrawable.VERTICAL
        )

        // layer-list 拼装
        // EN: layer-list assembly
        return LayerDrawable(
            arrayOf(backgroundDrawable, clipProgressDrawable)
        ).apply {
            setId(0, android.R.id.background)
            setId(1, android.R.id.progress)
        }
    }

    fun dialogProgressBar(
        context: Context,
        themeColor: Int
    ): Drawable {
        // 背景 shape
        // EN: Background shape
        val backgroundDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(Color.WHITE) // #ffffffff
            cornerRadius = context.resources.displayMetrics.density * 2f // 2dp
        }

        // 前景 progress shape
        // EN: Foreground progress shape
        val progressDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(themeColor)
            cornerRadius = context.resources.displayMetrics.density * 2f // 2dp
        }

        // Clip 包裹 progress
        // EN: Clip wrapping progress
        val clipProgressDrawable = ClipDrawable(
            progressDrawable,
            Gravity.START,
            ClipDrawable.HORIZONTAL
        )

        // layer-list 拼装
        // EN: layer-list assembly
        return LayerDrawable(
            arrayOf(backgroundDrawable, clipProgressDrawable)
        ).apply {
            setId(0, android.R.id.background)
            setId(1, android.R.id.progress)
        }
    }

}