package com.test.testalarm

import android.content.Context
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.time.hours

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        init()
    }

    private fun init() {
        
        timePicker1.setIs24HourView(true)

        val sharedPref = applicationContext.getSharedPreferences(AlarmReceiver.PREFERENCE_NAME, Context.MODE_PRIVATE)
        var hour = sharedPref.getInt("_hour", -1)
        var minute = sharedPref.getInt("_minute", -1)
        var alarmTime = sharedPref.getLong("_alarmTime", -1)


        if(hour > -1){
            textView1.text = "알람 설정 (시:분)\n${String.format("%02d:%02d",hour,minute)}"
            alarmLog(alarmTime)
        }

        /**
         * 버튼 클릭 -> 설정
         */
        button1.setOnClickListener {
            val sharedEdit = sharedPref.edit()

            hour = timePicker1.hour
            minute = timePicker1.minute

            sharedEdit.putInt("_hour",hour)
            sharedEdit.putInt("_minute",minute)
            sharedEdit.apply()

            textView1.text = "알람 설정 (시:분)\n${String.format("%02d:%02d",hour,minute)}"

            /**
             * Alarm 설정
             */
            AlarmReceiver.addAlarm(this,"테스트 알람 메세지입니다.")

            alarmTime = sharedPref.getLong("_alarmTime", -1)

            alarmLog(alarmTime)

            Toast.makeText(this, "설정되었습니다.", Toast.LENGTH_SHORT).show()
        }
    }


    //예약알림 로그 확인용
    private fun alarmLog(alarmTime: Long){
        if(alarmTime > -1) {
            val logCalendar = Calendar.getInstance()
            logCalendar.timeInMillis = alarmTime
            textLog.text = String.format("%02d월 %02d일 %02d시 %02d분 알림 예약",logCalendar.get(Calendar.MONTH)+1,logCalendar.get(Calendar.DAY_OF_MONTH),logCalendar.get(Calendar.HOUR_OF_DAY),logCalendar.get(Calendar.MINUTE))
        }
    }
}