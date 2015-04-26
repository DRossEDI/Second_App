package uk.co.derekross.second_app.Utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by derek_000 on 26/04/2015.
 */
public class SharedPrefUtil {

    // utils for saving and loading pain entries array in shared pref
    public static boolean savePicIdArray(String newEntry, Context context) {

        return saveEntryInArraySP(newEntry, context, "PicArray", "PicId");
    }

    public static String[] loadPicIdArray(Context context) {
        return loadArrayFromSP(context, "PicArray", "PicId");
    }

    public static boolean nullOutPicIdArray(Context context) {
        return nullOutEntryArraySP(context, "PicArray", "PicId");
    }

    // helper method to save string arrays into shared pref
    public static boolean saveEntryInArraySP(String newEntry, Context context,
                                             String sharedprefpart, String sharedprefname) {
        SharedPreferences prefs = context.getSharedPreferences(sharedprefpart,
                context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        int size = prefs.getInt(sharedprefname + "_size", 0);
        editor.putInt(sharedprefname + "_size", size + 1);
        editor.putString(sharedprefname + "_" + size, newEntry);
        return editor.commit();
    }

    public static String[] loadArrayFromSP(Context context,
                                           String sharedprefpart, String sharedprefname) {
        SharedPreferences prefs = context.getSharedPreferences(sharedprefpart,
                context.MODE_PRIVATE);
        int size = prefs.getInt(sharedprefname + "_size", 0);
        String array[] = new String[size];
        for (int i = 0; i < size; i++) {
            array[i] = prefs.getString(sharedprefname + "_" + i, null);
        }
        return array;
    }

    public static boolean nullOutEntryArraySP(Context context,
                                              String sharedprefpart, String sharedprefname) {
        SharedPreferences prefs = context.getSharedPreferences(sharedprefpart,
                context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        int size = prefs.getInt(sharedprefname + "_size", 0);
        if (size == 0) {
            // already null
            return true;
        } else {
            for (int i = 0; i < size; i++) {
                editor.putString(sharedprefname + "_" + i, null);

            }
            editor.putInt(sharedprefname + "_size", 0);
            editor.commit();
        }

        return true;
    }




}
