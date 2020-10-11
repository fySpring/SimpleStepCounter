package com.fyspring.stepcounter.ui.activities

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.*
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.fyspring.stepcounter.R
import com.fyspring.stepcounter.base.BaseActivity
import com.fyspring.stepcounter.bean.StepEntity
import com.fyspring.stepcounter.constant.ConstantData
import com.fyspring.stepcounter.service.StepService
import com.fyspring.stepcounter.dao.StepDataDao
import com.fyspring.stepcounter.ui.view.BeforeOrAfterCalendarView
import com.fyspring.stepcounter.utils.StepCountCheckUtil
import com.fyspring.stepcounter.utils.TimeUtil
import kotlinx.android.synthetic.main.activity_main.*
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : BaseActivity(), Handler.Callback {
    private var calenderView: BeforeOrAfterCalendarView? = null
    private var curSelDate: String = ""
    private val df = DecimalFormat("#.##")
    private val stepEntityList: MutableList<StepEntity> = ArrayList()
    private var stepDataDao: StepDataDao? = null
    private var isBind = false
    private val mGetReplyMessenger = Messenger(Handler(this))
    private var messenger: Messenger? = null

    /**
     * 定时任务
     */
    private var timerTask: TimerTask? = null
    private var timer: Timer? = null

    override fun getLayoutId(): Int {
        return R.layout.activity_main
    }

    override fun initData() {
        curSelDate = TimeUtil.getCurrentDate()
        calenderView = BeforeOrAfterCalendarView(this)
        movement_records_calender_ll!!.addView(calenderView)
        requestPermission()
    }

    override fun initListener() {
        calenderView!!.setOnBoaCalenderClickListener(object :
            BeforeOrAfterCalendarView.BoaCalenderClickListener {
            override fun onClickToRefresh(position: Int, curDate: String) {
                //获取当前选中的时间
                curSelDate = curDate
                //根据日期去取数据
                setDatas()
            }
        })
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACTIVITY_RECOGNITION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                    1
                )
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.ACTIVITY_RECOGNITION
                    )
                ) {
                    //此处需要弹窗通知用户去设置权限
                    Toast.makeText(this, "请允许获取健身运动信息，不然不能为你计步哦~", Toast.LENGTH_SHORT).show()
                }
            } else {
                startStepService()
            }
        } else {
            startStepService()
        }
    }


    private fun startStepService() {
        /**
         * 这里判断当前设备是否支持计步
         */
        if (StepCountCheckUtil.isSupportStepCountSensor(this)) {
            getRecordList()
            is_support_tv.visibility = View.GONE
            setDatas()
            setupService()
        } else {
            movement_total_steps_tv.text = "0"
            is_support_tv!!.visibility = View.VISIBLE
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            for (i in permissions.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    startStepService()
                } else {
                    Toast.makeText(this, "请允许获取健身运动信息，不然不能为你计步哦~", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * 开启计步服务
     */
    private fun setupService() {
        val intent = Intent(this, StepService::class.java)
        isBind = bindService(intent, conn, Context.BIND_AUTO_CREATE)
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) startForegroundService(intent)
        else startService(intent)
    }

    /**
     * 用于查询应用服务（application Service）的状态的一种interface，
     * 更详细的信息可以参考Service 和 context.bindService()中的描述，
     * 和许多来自系统的回调方式一样，ServiceConnection的方法都是进程的主线程中调用的。
     */
    private val conn = object : ServiceConnection {
        /**
         * 在建立起于Service的连接时会调用该方法，目前Android是通过IBind机制实现与服务的连接。
         * @param name 实际所连接到的Service组件名称
         * @param service 服务的通信信道的IBind，可以通过Service访问对应服务
         */
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            timerTask = object : TimerTask() {
                override fun run() {
                    try {
                        messenger = Messenger(service)
                        val msg = Message.obtain(null, ConstantData.MSG_FROM_CLIENT)
                        msg.replyTo = mGetReplyMessenger
                        messenger!!.send(msg)
                    } catch (e: RemoteException) {
                        e.printStackTrace()
                    }
                }
            }
            timer = Timer()
            timer!!.schedule(timerTask, 0, 500)
        }

        /**
         * 当与Service之间的连接丢失的时候会调用该方法，
         * 这种情况经常发生在Service所在的进程崩溃或者被Kill的时候调用，
         * 此方法不会移除与Service的连接，当服务重新启动的时候仍然会调用 onServiceConnected()。
         * @param name 丢失连接的组件名称
         */
        override fun onServiceDisconnected(name: ComponentName) {

        }
    }

    /**
     * 设置记录数据
     */
    private fun setDatas() {
        val stepEntity = stepDataDao!!.getCurDataByDate(curSelDate)

        if (stepEntity != null) {
            val steps = stepEntity.steps?.let { Integer.parseInt(it) }
            //获取全局的步数
            movement_total_steps_tv.text = steps.toString()
            //计算总公里数
            movement_total_km_tv.text = steps?.let { countTotalKM(it) }
        } else {
            //获取全局的步数
            movement_total_steps_tv.text = "0"
            //计算总公里数
            movement_total_km_tv.text = "0"
        }

        //设置时间
        val time = TimeUtil.getWeekStr(curSelDate)
        movement_total_km_time_tv.text = time
        movement_total_steps_time_tv.text = time
    }

    /**
     * 简易计算公里数，假设一步大约有0.7米
     *
     * @param steps 用户当前步数
     * @return
     */
    private fun countTotalKM(steps: Int): String {
        val totalMeters = steps * 0.7
        //保留两位有效数字
        return df.format(totalMeters / 1000)
    }

    /**
     * 获取全部运动历史纪录
     */
    private fun getRecordList() {
        //获取数据库
        stepDataDao = StepDataDao(this)
        stepEntityList.clear()
        stepEntityList.addAll(stepDataDao!!.getAllDatas())
        if (stepEntityList.size > 7) {
            //在这里获取历史记录条数，当条数达到7条以上时，就开始删除第七天之前的数据
            for (entity in stepEntityList) {
                if (TimeUtil.isDateOutDate(entity.curDate!!)) {
                    stepDataDao?.deleteCurData(entity.curDate!!)
                }
            }
        }
    }

    override fun handleMessage(msg: Message): Boolean {
        when (msg.what) {
            //这里用来获取到Service发来的数据
            ConstantData.MSG_FROM_SERVER ->
                //如果是今天则更新数据
                if (curSelDate == TimeUtil.getCurrentDate()) {
                    //记录运动步数
                    val steps = msg.data.getInt("steps")
                    //设置的步数
                    movement_total_steps_tv.text = steps.toString()
                    //计算总公里数
                    movement_total_km_tv.text = countTotalKM(steps)
                }
        }
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        //记得解绑Service，不然多次绑定Service会异常
        if (isBind) this.unbindService(conn)
    }
}
