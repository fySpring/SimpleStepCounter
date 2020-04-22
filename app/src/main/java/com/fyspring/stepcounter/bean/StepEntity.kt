package com.fyspring.stepcounter.bean

/**
 * Created by fySpring
 * Date: 2020/4/22
 * To do:
 */
class StepEntity() {
    var curDate: String? = null //当天的日期
    var steps: String? = null   //当天的步数

    constructor(curDate: String, steps: String) : this() {
        this.curDate = curDate
        this.steps = steps
    }

    override fun toString(): String {
        return "StepEntity{" +
                "curDate='" + curDate + '\'' +
                ", steps=" + steps +
                '}'
    }
}