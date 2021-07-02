package com.example.contcasa;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import static android.app.NotificationManager.IMPORTANCE_HIGH;
import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.O;


public final class AlarmReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "alarm_channel";
    @Override
    public void onReceive(Context context, Intent intent) {
        //Comparamos la accion que se envia para la configuracion de la alarma.
        if ("com.service.RunAlarma".equals(intent.getAction())){
            final NotificationManager manager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            createNotificationChannel(context);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID);
            builder.setSmallIcon(R.drawable.ic_launcher_background);//icono
            builder.setColor(ContextCompat.getColor(context, R.color.colorAccent));//Color de la notificacion
            builder.setContentTitle(context.getString(R.string.app_name));//Titulo
            builder.setContentText("Alarma de Casa");//Subtitulo
            builder.setTicker("Es tiempo de poner atencion en actividades para tu hogar.");//Sub mensaje
            builder.setVibrate(new long[] {1000,500,1000,500,1000,500});
            builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
            Intent intentEntra = new Intent(context,MainActivity.class);
            builder.setContentIntent(PendingIntent.getActivity(context,1,intentEntra,FLAG_UPDATE_CURRENT));
            builder.setAutoCancel(true);
            builder.setPriority(Notification.PRIORITY_HIGH);
            manager.notify(1, builder.build());
        }
    }

    /**
     * Se crea el Canal para la creacion de la notificacion
     * @param ctx
     */
    private static void createNotificationChannel(Context ctx) {
        if(SDK_INT < O) return;
        final NotificationManager mgr = ctx.getSystemService(NotificationManager.class);
        if(mgr == null) return;
        final String name = "AlarmReceiver";
        if(mgr.getNotificationChannel(name) == null) {
            final NotificationChannel channel =
                    new NotificationChannel(CHANNEL_ID, name, IMPORTANCE_HIGH);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[] {1000,500,1000,500,1000,500});
            channel.setBypassDnd(true);
            mgr.createNotificationChannel(channel);
        }
    }



}
