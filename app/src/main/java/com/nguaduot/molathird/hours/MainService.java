package com.nguaduot.molathird.hours;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainService extends AccessibilityService {
    private Pattern patternDate = Pattern.compile("\\d{4}-(\\d{2}-\\d{2})");
    private Pattern patternTime = Pattern.compile("(\\d{2}:\\d{2})-(\\d{2}:\\d{2})");

    private static final String EVENT_CLASS_MOLA_RECORD = "android.support.v7.widget.RecyclerView";
    private static final String CLASS_TARGET_VIEWGROUP = LinearLayout.class.getName();
    private static final String CLASS_TARGET_WIDGET = TextView.class.getName();

    private static final int NOTIFICATION_ID = 5;
    private static final String CHANNEL_ID = "molahours";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        log(String.format("onAccessibilityEvent eventType: %s className: %s",
                event.getEventType(), event.getClassName()));
//        sendNotification("0000-00-00", getString(R.string.toast_stats, 0, "00:00"));
        if (!EVENT_CLASS_MOLA_RECORD.contentEquals(event.getClassName())) {
            return;
        }
        AccessibilityNodeInfo source = event.getSource();
        if (source == null) {
            return;
        }
        int count_order = 0;
        int sum_minutes = 0;
        for (int i = 0; i < source.getChildCount(); i++) {
            AccessibilityNodeInfo ani1 = source.getChild(i);
            if (CLASS_TARGET_VIEWGROUP.contentEquals(ani1.getClassName())) {
                for (int j = 0; j < ani1.getChildCount(); j++) {
                    AccessibilityNodeInfo ani2 = ani1.getChild(j);
                    log(String.format("child child className: %s text: %s",
                            ani2.getClassName(), ani2.getText()));
                    if (!CLASS_TARGET_WIDGET.contentEquals(ani2.getClassName())) {
                        continue;
                    }
                    Matcher matcher = patternTime.matcher(ani2.getText());
                    if (!matcher.matches()) {
                        continue;
                    }
                    int minutes = calculateMinutes(matcher.group(1), matcher.group(2));
                    if (minutes >= 0) {
                        count_order++;
                        sum_minutes += minutes;
                    }
                }
            }
        }
        if (count_order > 0) {
            log(String.format(Locale.US, "count_order: %d sum_minutes: %d",
                    count_order, sum_minutes));
            String time = minutes2str(sum_minutes);
            sendToast(getString(R.string.toast_stats, count_order, time));
            sendNotification(getString(R.string.notification_title, count_order),
                    getString(R.string.notification_content, time));
        }
    }

    @Override
    public void onInterrupt() {

    }

    private int calculateMinutes(String start, String end) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.US);
        try {
            Date dateStart = sdf.parse(start);
            Date dateEnd = sdf.parse(end);
            if (dateEnd != null && dateStart != null) {
                return (int) (dateEnd.getTime() - dateStart.getTime()) / (1000 * 60);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private String minutes2str(int minutes) {
        return String.format(Locale.US, "%02d:%02d", minutes / 60, minutes % 60);
    }

    private void sendToast(String content) {
        Toast.makeText(this, content, Toast.LENGTH_SHORT).show();
    }

    private void sendNotification(String title, String content) {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_LOW);
            channel.setDescription(getString(R.string.notification_channel_description));
            manager.createNotificationChannel(channel);
        }

        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification_task)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setCategory(Notification.CATEGORY_PROMO)
                    .setVisibility(Notification.VISIBILITY_PUBLIC)
                    .setAutoCancel(true);
        } else {
            builder = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.ic_notification_task)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setPriority(Notification.PRIORITY_LOW)
                    .setCategory(Notification.CATEGORY_PROMO)
                    .setVisibility(Notification.VISIBILITY_PUBLIC)
                    .setAutoCancel(true);
        }

        manager.notify(NOTIFICATION_ID, builder.build());
    }

    private void log(String content) {
        Log.d("molahours", content);
    }
}
