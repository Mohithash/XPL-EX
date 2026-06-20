package eu.faircode.xlua;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.util.Log;

import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.hooks.XReport;

public class XNotify {
    private static final String TAG = "XLua.XNotify";
    private static final String cChannelName = "xlua";

    public static Notification.Builder buildExceptionNotification(
            Context context,
            XReport report,
            PackageManager pm,
            Resources resources) {

        try {
            Notification.Builder builder = new Notification.Builder(context);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                builder.setChannelId(cChannelName);
            builder.setSmallIcon(android.R.drawable.ic_dialog_alert);
            builder.setContentTitle(resources.getString(R.string.msg_exception, report.hookId));
            builder.setContentText(pm.getApplicationLabel(pm.getApplicationInfo(report.packageName, 0)));

            builder.setPriority(Notification.PRIORITY_HIGH);
            builder.setCategory(Notification.CATEGORY_STATUS);
            builder.setVisibility(Notification.VISIBILITY_SECRET);

            // Main
            Intent main = context.getPackageManager().getLaunchIntentForPackage(BuildConfig.APPLICATION_ID);
            if (main != null) {
                main.putExtra(ActivityMain.EXTRA_SEARCH_PACKAGE, report.packageName);
                PendingIntent pi = PendingIntent.getActivity(context, report.uid, main, PendingIntent.FLAG_IMMUTABLE);
                builder.setContentIntent(pi);
            }

            builder.setAutoCancel(true);
            return builder;
        }catch (Throwable e) {
            Log.e(TAG, "Error creating Notification for Exception! \n" + e + "\n" + Log.getStackTraceString(e));
            return null;
        }
    }

    @SuppressLint("MissingPermission")
    public static Notification.Builder buildUsageNotification(
            Context context,
            XLuaHook hook,
            XReport report,
            PackageManager pm,
            Resources resources) {
        try {

            // Get group name
            String name = hook.getGroup().toLowerCase().replaceAll("[^a-z]", "_");
            int resId = resources.getIdentifier("group_" + name, "string", BuildConfig.APPLICATION_ID);
            String group = (resId == 0 ? report.hookId : resources.getString(resId));

            // Build notification
            Notification.Builder builder = new Notification.Builder(context);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                builder.setChannelId(cChannelName);
            builder.setSmallIcon(android.R.drawable.ic_dialog_info);
            builder.setContentTitle(resources.getString(R.string.msg_usage, group));
            builder.setContentText(pm.getApplicationLabel(pm.getApplicationInfo(report.packageName, 0)));
            if (BuildConfig.DEBUG)
                builder.setSubText(report.hookId);

            builder.setPriority(Notification.PRIORITY_DEFAULT);
            builder.setCategory(Notification.CATEGORY_STATUS);
            builder.setVisibility(Notification.VISIBILITY_SECRET);

            // Main
            Intent main = context.getPackageManager().getLaunchIntentForPackage(BuildConfig.APPLICATION_ID);
            if (main != null) {
                main.putExtra(ActivityMain.EXTRA_SEARCH_PACKAGE, report.packageName);
                PendingIntent pi = PendingIntent.getActivity(context, report.uid, main, PendingIntent.FLAG_IMMUTABLE);
                builder.setContentIntent(pi);
            }

            builder.setAutoCancel(true);
            return builder;
        }catch (Throwable e) {
            Log.e(TAG, "Error creating Notification for Usage! \n" + e + "\n" + Log.getStackTraceString(e));
            return null;
        }
    }
}
