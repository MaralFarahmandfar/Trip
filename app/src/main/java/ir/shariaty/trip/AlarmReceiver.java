package ir.shariaty.trip;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {

    private static MediaPlayer mediaPlayer;
    private static final String CHANNEL_ID = "alarm_channel";
    private static final int NOTIFICATION_ID = 1001;

    @Override
    public void onReceive(Context context, Intent intent) {
        String alarmName = intent.getStringExtra("alarm_name");

        // پخش صدای آلارم
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context, R.raw.alarm_sound); // فایل رو در res/raw قرار بده
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
        }

        // ایجاد نوتیفیکیشن کانال (برای اندروید 8 به بالا)
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "آلارم سفر",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("نوتیفیکیشن آلارم سفر");
            notificationManager.createNotificationChannel(channel);
        }

        // Intent برای توقف آلارم
        Intent stopIntent = new Intent(context, StopAlarmReceiver.class);
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // ساخت نوتیفیکیشن
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_alarm) // آیکون نوتیف
                .setContentTitle("آلارم فعال شد!")
                .setContentText(alarmName)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(true) // تا وقتی قطع نشه، نوتیف بمونه
                .addAction(R.drawable.ic_stop, "توقف", stopPendingIntent); // دکمه توقف

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    public static void stopAlarm() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
