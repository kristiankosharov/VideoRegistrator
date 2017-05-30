package reg.videoregistrator.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.CameraProfile;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Pair;

import reg.videoregistrator.R;

public class PreferencesUtils {

    private static final int BYTES_IN_MB = 1000000;
    private static final int MS_IN_MINUTE = 60000;

    public static int getMaxDuration(Context context) {
        String prefKey = context.getString(R.string.pref_key_duration);
        String duration = getPreferences(context).getString(prefKey, "");
        if (TextUtils.isEmpty(duration)) {
            return MS_IN_MINUTE;
        } else {
            return replaceText(duration) * MS_IN_MINUTE;
        }
    }

    public static long getSizeLimit(Context context) {
        String prefKey = context.getString(R.string.pref_key_video_size);
        String size = getPreferences(context).getString(prefKey, "");
        if (TextUtils.isEmpty(size)) {
            return 3 * BYTES_IN_MB;
        } else {
            return replaceText(size) * BYTES_IN_MB;
        }
    }

    public static Pair<Integer, Integer> getResolution(Context context) {
        String prefKey = context.getString(R.string.pref_key_resolution_list);
        String size = getPreferences(context).getString(prefKey, "");
        Pair<Integer, Integer> result = new Pair<>(720, 480);
        if (TextUtils.isEmpty(size)) {
            return result;
        } else {
            String[] resolutions = size.split(" x ");
            int width = Integer.parseInt(resolutions[0]);
            int heigh = Integer.parseInt(resolutions[1]);
            return new Pair<>(width, heigh);
        }
    }

    public static int getQuality(Context context) {
        String prefKey = context.getString(R.string.pref_key_quality);
        String result = getPreferences(context).getString(prefKey, "");
        if (TextUtils.isEmpty(result)) {
            return CameraProfile.QUALITY_LOW;
        }

        return Integer.parseInt(result);
    }

    public static boolean getAudio(Context context) {
        String prefKey = context.getString(R.string.pref_key_audio);
        return getPreferences(context).getBoolean(prefKey, false);
    }

    public static int getGLimit(Context context) {
        String prefKey = context.getString(R.string.pref_key_g);
        String gLimit = getPreferences(context).getString(prefKey, "");
        if (TextUtils.isEmpty(gLimit)) {
            return 1;
        } else {
            return replaceText(gLimit);
        }
    }

    public static boolean removeOldVideos(Context context) {
        String prefKey = context.getString(R.string.pref_key_old_videos);
        return getPreferences(context).getBoolean(prefKey, true);
    }

    public static void setSaveFile(Context context, boolean saved) {
        getPreferences(context).edit().putBoolean(context.getString(R.string.pref_saving_video), saved).commit();
    }

    public static boolean getSaveFile(Context context) {
        return getPreferences(context).getBoolean(context.getString(R.string.pref_saving_video), false);
    }

    private static int replaceText(String text) {
        return Integer.parseInt(text.replaceAll("[A-Za-z ]", ""));
    }

    private static SharedPreferences getPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }
}
