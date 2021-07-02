package com.example.contcasa;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.Calendar;

public class Alarma extends AppCompatActivity {

    private TextView notificationsTime;
    private Button changeConfigTemepratura;
    private Button changeNotificationOn;
    private Button changeNotificationOff;
    private TextView txtBaseTemperatura;
    private int alarmID = 1;
    private SharedPreferences settings;
    private Context context;
    boolean isAlarmStatus;
    private String TAG;
    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarma);
        context = this;
        TAG=Alarma.class.getName();
        changeConfigTemepratura = (Button) findViewById(R.id.change_config_temepratura);
        changeNotificationOn= (Button) findViewById(R.id.change_notification_on);
        changeNotificationOff= (Button) findViewById(R.id.change_notification_off);
        notificationsTime = (TextView) findViewById(R.id.notifications_time);
        txtBaseTemperatura = (TextView) findViewById(R.id.txt_base_temperatura);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        settings = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
        String hour, minute,baseTemperatura;

        hour = settings.getString("hour","00");
        minute = settings.getString("minute","00");
        isAlarmStatus = settings.getBoolean("isAlarmStatus",false);
        baseTemperatura = settings.getString("baseTemperatura","25");
        notificationsTime.setText(hour+":"+minute);
        txtBaseTemperatura.setText(baseTemperatura);
        activarDesactivarAlarma();
        changeConfigTemepratura.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String datoTemperatura = txtBaseTemperatura.getText().toString();
                if (datoTemperatura.isEmpty()){
                    Toast.makeText(context, "El campo de temperatura esta vacio.", Toast.LENGTH_LONG).show();
                    return;
                }
                SharedPreferences.Editor edit = settings.edit();
                edit.putString("baseTemperatura", datoTemperatura);
                edit.commit();
                Toast.makeText(context, "Se a configurado el encendido del ventilador cuando la temperatura sea igual o mayor a "+ datoTemperatura, Toast.LENGTH_LONG).show();
            }
        });

        notificationsTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        String finalHour, finalMinute;

                        finalHour = "" + selectedHour;
                        finalMinute = "" + selectedMinute;
                        if (selectedHour < 10) finalHour = "0" + selectedHour;
                        if (selectedMinute < 10) finalMinute = "0" + selectedMinute;
                        notificationsTime.setText(finalHour + ":" + finalMinute);

                        Calendar today = Calendar.getInstance();

                        today.set(Calendar.HOUR_OF_DAY, selectedHour);
                        today.set(Calendar.MINUTE, selectedMinute);
                        today.set(Calendar.SECOND, 0);

                        SharedPreferences.Editor edit = settings.edit();
                        edit.putString("hour", finalHour);
                        edit.putString("minute", finalMinute);

                        //SAVE ALARM TIME TO USE IT IN CASE OF REBOOT
                        edit.putInt("alarmID", alarmID);
                        edit.putLong("alarmTime", today.getTimeInMillis());

                        edit.commit();

                        //Toast.makeText(Alarma.this, getString(R.string.changed_to, finalHour + ":" + finalMinute), Toast.LENGTH_LONG).show();

                       // Utils.setAlarm(alarmID, today.getTimeInMillis(), Alarma.this);
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle(getString(R.string.select_time));
                mTimePicker.show();
            }
        });

        changeNotificationOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isAlarmStatus=true;
                comitStatusAlarma(isAlarmStatus);
            }
        });

        changeNotificationOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isAlarmStatus=false;
                comitStatusAlarma(isAlarmStatus);
            }
        });
    }

    public void comitStatusAlarma(boolean data){
        activarDesactivarAlarma();
        SharedPreferences.Editor edit = settings.edit();
        edit.putBoolean("isAlarmStatus", data);
        edit.commit();
    }

    public void activarDesactivarAlarma(){
        if (!isAlarmStatus){
            cancelarAlarma();
            notificationsTime.setBackgroundResource(R.color.colorOff);
        }else{
            notificationsTime.setBackgroundResource(R.color.colorOn);
            activarAlarma();
        }
    }

    /**
     * Activa la alarma enviando al reciber del app una alerta para que comience a escuchar
     * Este modulo esta desarrollado para que una vez llegue la hora configurada este comience
     * a enviar alertas al usuario esto esta configurado cada 2 minutos despues de el encendido
     * la alarma.
     */
    public void activarAlarma(){
        alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction("com.service.RunAlarma");
        alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        int hora =Integer.parseInt(notificationsTime.getText().toString().split(":")[0]);
        int minutos = Integer.parseInt(notificationsTime.getText().toString().split(":")[1]);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY,hora);
        calendar.set(Calendar.MINUTE, minutos);
        Log.e(TAG, "activarAlarma: "+calendar.getTime().toString() );
        //Se repite la alarma cada 2 minutos.
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                1000 * 60 * 2, alarmIntent);
    }

    /**
     * Desactiva la alarma para que no este enviando avisos.
     */
    public void cancelarAlarma(){
        Log.e(TAG, "activarAlarma: Se cancelo la alarma" );
        if (alarmMgr!= null) {
            alarmMgr.cancel(alarmIntent);
        }
    }
}