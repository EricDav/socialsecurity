package com.example.davidalienyi.socialsecurity;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;




/**
 * Created by David on 01/07/2018.
 */

public class Helper {

    /**
     *
     * @param monthInNum Month in number
     *
     * @return {String} month in string
     */
    public static String getMonth(Integer monthInNum) {
        Map<Integer, String> dictionary = new HashMap<Integer, String>();
        dictionary.put(0, "Jan");
        dictionary.put(1, "Feb");
        dictionary.put(2, "Mar");
        dictionary.put(3, "Apr");
        dictionary.put(4, "May");
        dictionary.put(5, "Jun");
        dictionary.put(6, "Jul");
        dictionary.put(7, "Aug");
        dictionary.put(8, "Sep");
        dictionary.put(9, "Oct");
        dictionary.put(10, "Nov");
        dictionary.put(11, "Dec");

        return dictionary.get(monthInNum);
    }

    /**
     *
     * Get social handle mapping.
     *
     * @return Hashmap
     */
    public static Map<Integer, String> getSocialHandlesMapping() {
        Map<Integer, String> dictionary = new HashMap<Integer, String>();
        dictionary.put(0, "Facebook");
        dictionary.put(1, "Twitter");
        dictionary.put(2, "Snapchat");
        dictionary.put(3, "Wechat");
        dictionary.put(4, "Slack");
        dictionary.put(5, "Skype");
        dictionary.put(6, "Youtube");
        dictionary.put(7, "Instagram");

        return dictionary;
    }

    public static Map<Integer, String> getSecurityQuestions() {
        Map<Integer, String> dictionary = new HashMap<Integer, String>();

        dictionary.put(0, "What is your favourite food?");
        dictionary.put(1, "What is the name of your best friend?");
        dictionary.put(2, "What is your favourite color?");
        dictionary.put(3, "What is the name of your first child?");
        dictionary.put(4, "What is your favourite movie?");
        dictionary.put(5, "What is the name of your first child?");

        return dictionary;
    }

    /**
     *It gets the formatted date an entry was created.
     *
     * @param dateTime The date to be formated
     *
     * @return String A formatted date.
     */
    public static String getFormatedDate(String dateTime) {
        Calendar currentDate = Calendar.getInstance();
        String[] date  = dateTime.split(",");

        Integer year = Integer.parseInt(date[0]);
        Integer month = Integer.parseInt(date[1]);
        Integer day = Integer.parseInt(date[2]);

        if (currentDate.get(Calendar.YEAR) == year && currentDate.get(Calendar.MONTH) == month && currentDate.get(Calendar.DAY_OF_MONTH) == day) {
            return "Today";
        } else if (currentDate.get(Calendar.YEAR) == year && currentDate.get(Calendar.MONTH) == month && currentDate.get(Calendar.DAY_OF_MONTH) == day - 1) {
            return "Yesterday";
        } else {
            return year == currentDate.get(Calendar.YEAR) ? getMonth(month) + " " + day.toString() : year.toString() + " " + getMonth(month) + " " + day.toString();
        }
    }

    public static String getFullDate(String dateTime) {
        String[] date  = dateTime.split(",");

        Integer year = Integer.parseInt(date[0]);
        Integer month = Integer.parseInt(date[1]);
        Integer day = Integer.parseInt(date[2]);
        Integer hour = Integer.parseInt(date[3]);
        Integer minute = Integer.parseInt(date[4]);

        Calendar calender = Calendar.getInstance();
        calender.set(year, month, day, hour, minute);

        String[] monthsOfTheYear = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug","Sep", "Oct", "Nov", "Dec"};
        String[] daysOfTheWeek = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

        return daysOfTheWeek[calender.get(Calendar.DAY_OF_WEEK) - 1] + " " + monthsOfTheYear[month] +
                " " + day.toString() + " " + year.toString() + " at" + " " + hour.toString() + ":" + minute.toString();
    }

    /**
     * Checks if device is connected to the internet.
     *
     * @param context Context
     * @return boolean true if device is connected to the internet else false.
     */
    public static boolean isConnected(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnected();
    }

    /**
     * Displays a progress loader.
     *
     * @param progress The the progress instance to be opened
     * @param title The title to be displayed on the dialog
     */
    public static void openProgress(ProgressDialog progress, String title) {
        String capitalizeTitle = title.substring(0, 1).toUpperCase() + title.substring(1);
        progress.setMessage(capitalizeTitle + "...");
        //progress.setMessage("Wait while " + title + "...");
        if (title.equals("logging out")) {
            progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        } else {
            progress.setCancelable(true); // disable dismiss by tapping outside of the dialog
        }
        progress.show();
    }


    /**
     * Cancel an open progress loader.
     *
     * @param progress the progress instance to be closed
     */
    public static void cancelProgress(ProgressDialog progress) {
        progress.dismiss();
    }

}
