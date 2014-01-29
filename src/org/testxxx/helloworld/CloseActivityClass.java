package org.testxxx.helloworld;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

public class CloseActivityClass {
	public static List<Activity> activityList = new ArrayList<Activity>();
	 
    public static void exitClient(Context context)
    {
        Log.d("sdfas", "----- exitClient -----");
        // πÿ±’À˘”–Activity
        for (int i = 0; i < activityList.size(); i++)
        {
            if (null != activityList.get(i))
            {
                activityList.get(i).finish();
            }
        }
   
        ActivityManager activityMgr= (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE );
        activityMgr.restartPackage(context.getPackageName());
 
        System.exit(0);
    }
}
