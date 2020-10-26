package com.test.testalarm

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import java.util.*

class AlarmReceiver : BroadcastReceiver() {

    companion object{
        private const val CHANNEL_ID    = "_CHANNEL_ID"
        private const val CHANNEL_NAME  = "_CHANNEL_NAME"
        private const val ACTION_ALARM  = "_ACTION_ALARM"

        const val PREFERENCE_NAME       = "_PREFERENCE"
        const val ALARM_MESSAGE         = "_ALARM_MESSAGE"

        //알람 Intent 생성
        private fun createAlarmIntent(context: Context, msg: String?): PendingIntent {
            val intent = Intent(context,AlarmReceiver::class.java)
            intent.putExtra(ALARM_MESSAGE, msg ?: "")
            intent.action = ACTION_ALARM

            return PendingIntent.getBroadcast(context,0,intent,PendingIntent.FLAG_UPDATE_CURRENT)
        }

        //알람추가
        fun addAlarm(context: Context, msg: String?){

            //1. 등록된 알람이 있는 경우 삭제
            deleteAlarm(context)

            //2-1. 시간/분 설정값 가져오기
            val sharedPref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)

            //2-2. 현재시간 비교 알람 시간(년/월/일/시/분) 설정

            //2-2-1. 사용자 날자(시간) 설정
            val userCalendar = Calendar.getInstance()
            userCalendar.set(Calendar.HOUR_OF_DAY, sharedPref.getInt("_hour",0))
            userCalendar.set(Calendar.MINUTE, sharedPref.getInt("_minute",0))

            //2-2-2. 시스템 날자
            val sysCalendar = Calendar.getInstance()

            //2-2-3. 현재시간보다 작을 경우 내일로 변경
            if(sysCalendar.timeInMillis >= userCalendar.timeInMillis){
                Log.d("TAG", "addAlarm: 내일로 설정됨")
                userCalendar.set(Calendar.DAY_OF_MONTH, userCalendar.get(Calendar.DAY_OF_MONTH)+1)
            }

            val alarmTime = userCalendar.timeInMillis

            //3. 메세지 저장
            val sharedEdit = sharedPref.edit()
            sharedEdit.putString(ALARM_MESSAGE,msg ?: "")
            //로그
            sharedEdit.putLong("_alarmTime",alarmTime)
            sharedEdit.apply()

            //4. 알람 설정
            val alarmIntent = createAlarmIntent(context,msg)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            if(Build.VERSION.SDK_INT < 23){

                if(Build.VERSION.SDK_INT >= 19){
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime, alarmIntent)
                }else{
                    alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, alarmIntent)
                }
            }else{
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,alarmTime,alarmIntent)
            }

        }

        //알람취소
        fun deleteAlarm(context: Context){
            val alarmIntent = PendingIntent.getService(context, 0, Intent(context,AlarmReceiver::class.java), PendingIntent.FLAG_NO_CREATE)

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            if (alarmIntent != null && alarmManager != null) {
                alarmManager.cancel(alarmIntent)
            }
        }

    }

    override fun onReceive(context: Context, intent: Intent) {

        when(intent.action){

            AlarmReceiver.ACTION_ALARM -> {
                val message = intent.getStringExtra(ALARM_MESSAGE)

                val notificationIntent = Intent(context, MainActivity::class.java)

                val pendingIntent = PendingIntent.getActivity(context,0,notificationIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT)

                val builder = NotificationCompat.Builder(context,CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_alarm)
                    .setContentTitle("Test Alarm Title...")
                    .setContentText(message)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)

                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    val channel = NotificationChannel(CHANNEL_ID,CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
                    notificationManager.createNotificationChannel(channel)
                }

                notificationManager.notify(1,builder.build())

                //다시 호출 (다음날)
                addAlarm(context,message)
            }

            Intent.ACTION_BOOT_COMPLETED -> {

                val sharedPref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
                val msg = sharedPref.getString(ALARM_MESSAGE,"")
                addAlarm(context,msg)
            }


        }
    }
}