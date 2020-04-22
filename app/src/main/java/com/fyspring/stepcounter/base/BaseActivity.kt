package com.fyspring.stepcounter.base

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity

/**
 * Created by fySpring
 * Date: 2020/4/22
 * To do:
 */
abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutId())
    }

    override fun setContentView(@LayoutRes layoutResID: Int) {
        super.setContentView(layoutResID)
        init()
    }

    override fun setContentView(view: View) {
        super.setContentView(view)
        init()
    }

    override fun setContentView(view: View, params: ViewGroup.LayoutParams) {
        super.setContentView(view, params)
        init()
    }

    private fun init() {
        initData()
        initListener()
    }

    abstract fun getLayoutId(): Int
    abstract fun initData()
    abstract fun initListener()
}