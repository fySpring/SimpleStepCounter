package com.fyspring.stepcounter.ui.view

import android.widget.RelativeLayout

import android.content.Context
import android.widget.LinearLayout
import android.view.LayoutInflater
import com.fyspring.stepcounter.R
import com.fyspring.stepcounter.utils.TimeUtil


/**
 * Created by fySpring
 * Date: 2020/4/21
 * To do:
 */
class BeforeOrAfterCalendarView(mContext: Context) : RelativeLayout(mContext) {
    private var dayList: MutableList<Int> = ArrayList()
    private var dateList: MutableList<String> = ArrayList()
    private var itemViewList: MutableList<RecordsCalenderItemView> = ArrayList()
    private var calenderViewLl: LinearLayout? = null
    private var curPosition: Int = 0

    init {
        val view =
            LayoutInflater.from(mContext).inflate(R.layout.before_or_after_calendar_layout, this)

        calenderViewLl = view.findViewById(R.id.boa_calender_view_ll)
        setBeforeDateViews()
        initItemViews()
    }


    /**
     * 设置之前的日期显示
     */
    private fun setBeforeDateViews() {
        //获取日期列表
        dateList.addAll(TimeUtil.getBeforeDateListByNow())
        dayList.addAll(TimeUtil.dateListToDayList(dateList))
    }

    private fun initItemViews() {
        for (i in dateList.indices) {
            val day = dayList[i]
            val curItemDate = dateList[i]
            val itemView: RecordsCalenderItemView
            itemView = if (day == TimeUtil.getCurrentDay()) {
                RecordsCalenderItemView(context , "今天", day.toString(), i, curItemDate)
            } else {
                RecordsCalenderItemView(
                    context,
                    TimeUtil.getCurWeekDay(curItemDate),
                    day.toString(),
                    i,
                    curItemDate
                )
            }

            itemViewList.add(itemView)
            calenderViewLl?.addView(itemView)

            itemView.setOnCalenderItemClick(object : RecordsCalenderItemView.OnCalenderItemClick {
                override fun onCalenderItemClick() {
                    curPosition = itemView.position
                    switchPositionView(curPosition)

                    if (calenderClickListener != null) {
                        calenderClickListener!!.onClickToRefresh(
                            curPosition,
                            dateList[curPosition]
                        )
                    }
                }
            })
        }
        switchPositionView(6)
    }

    private fun switchPositionView(position: Int) {
        for (i in itemViewList.indices) {
            if (position == i) {
                itemViewList[i].setChecked(true)
            } else {
                itemViewList[i].setChecked(false)
            }
        }
    }

    private var calenderClickListener: BoaCalenderClickListener? = null

    interface BoaCalenderClickListener {
        fun onClickToRefresh(position: Int, curDate: String)
    }

    fun setOnBoaCalenderClickListener(calenderClickListener: BoaCalenderClickListener) {
        this.calenderClickListener = calenderClickListener
    }

}