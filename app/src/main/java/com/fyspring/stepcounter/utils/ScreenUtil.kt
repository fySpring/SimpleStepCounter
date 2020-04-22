package com.fyspring.stepcounter.utils

import android.content.Context


/**
 * Created by fySpring
 * Date: 2020/4/21
 * To do:
 */
class ScreenUtil {
    companion object {
        fun getScreenWidth(mContext: Context): Int {

            val displayMetrics = mContext.resources.displayMetrics
            //获取屏幕宽高，单位是像素
            val widthPixels = displayMetrics.widthPixels
            val heightPixels = displayMetrics.heightPixels
            //获取屏幕密度倍数
            val density = displayMetrics.density

            return widthPixels
        }
    }
}