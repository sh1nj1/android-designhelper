package com.sh1nj1.android.designhelper;

import java.util.List;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBarActivity;


public class LinkActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent target = new Intent(Intent.ACTION_MAIN, null);
        target.addCategory(Intent.CATEGORY_LAUNCHER);

        ActivityManager am = (ActivityManager) this
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> alltasks = am
                .getRunningTasks(2);
        for (ActivityManager.RunningTaskInfo task : alltasks) {
            System.out.println("tasks="+task);
            System.out.println("topact="+task.topActivity);
            if (! task.topActivity.getClassName().equals(LinkActivity.class.getName())) {
                target.setPackage(task.topActivity.getPackageName());
                break;
            }
        }
        System.out.println("intent="+target);

        finish();

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(target);
        stackBuilder.addNextIntent(new Intent(this, DesignScreenActivity.class));
        stackBuilder.startActivities();
    }

}
