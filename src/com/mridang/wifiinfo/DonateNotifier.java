package com.mridang.wifiinfo;

import java.util.Date;
import java.util.Random;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.RingtoneManager;
import android.net.Uri;

/*
 * This class is the donation class that requests a donation
 */
public class DonateNotifier {

	/* The name of the launch count preference */
	private static String LAUNCH_COUNT = "launch_count";
	/* The name of the launch count preference */
	private static String PROMPT_DATE = "prompt_date";
	/* The location of the donation application */
	private static String APP_TITLE = "com.mridang.donate";

	/*
	 * Display the rate dialog if needed.
	 */
	public static void init(Context ctxContext) {

		try {

			//Let's see if the user has already purchased the donation application
			try {

				ctxContext.getPackageManager().getPackageInfo(APP_TITLE, PackageManager.GET_META_DATA);

			} catch (NameNotFoundException e) {

				//Apparently not, so let's run all the donation logic
				SharedPreferences prePreferences = ctxContext.getSharedPreferences("launchdata", Context.MODE_PRIVATE);
				Editor ediEditor = prePreferences.edit();

				//Get the number of launches and increment the value
				Integer intLaunches = prePreferences.getInt(LAUNCH_COUNT, 0) + 1;
				ediEditor.putInt(LAUNCH_COUNT, intLaunches).commit();

				if (intLaunches == (intLaunches > 10 ? (intLaunches > 250 ? (intLaunches > 1000 ? Integer.MAX_VALUE : 1000) : 250) : 10)) {

					//Create the intent to open up the Play store
					Intent ittMarket = new Intent(Intent.ACTION_VIEW);
					ittMarket.setData(Uri.parse("market://details?id=" + APP_TITLE));

					//Build the notification that will be shown
					Notification notNotification = new Notification.BigTextStyle(
							new Notification.Builder(ctxContext)
							.setSmallIcon(R.drawable.ic_notification)
							.setAutoCancel(true)
							.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
							.setContentTitle(ctxContext.getString(R.string.application_name))
							.setContentText("Would you consider a tiny donation for my work?")
							.setContentIntent(PendingIntent.getActivity(ctxContext, 0, ittMarket, 0)))
					.bigText("Thank you for using so many of my extensions for Dashclock. After publishing over twenty of them on Play store, I'd like to continue building more for the community. They're all free and open-source and if you've found any of them useful, you could click this notification to donate and buy me a coffee.")
					.setBigContentTitle(ctxContext.getString(R.string.application_name))
					.build();

					//Let's show the notification to the user
					NotificationManager nmrNotifier = (NotificationManager) ctxContext.getSystemService(Context.NOTIFICATION_SERVICE);
					nmrNotifier.notify(new Random().nextInt(), notNotification);

					//Set the date when the user has been prompted
					Long lngDatetime = new Date(System.currentTimeMillis()).getTime();
					ediEditor.putLong(PROMPT_DATE, lngDatetime).commit();

				}

			} 

		} catch (Exception e) {

			e.printStackTrace();

		}

	}

}