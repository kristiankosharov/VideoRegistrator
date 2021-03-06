package reg.videoregistrator.utils;


import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

public class PermissionsUtils {

    public static final int PERMISSION_GRANT = 1;

    /**
     * Check self if there are needed permissions. If they aren't granted
     * check if should show dialog. Showing dialog for grant permissions {@link Manifest.permission#RECORD_AUDIO} ,
     * {@link Manifest.permission#CAMERA} , {@link Manifest.permission#WRITE_EXTERNAL_STORAGE}
     *
     * @param context calling activity
     */
    public static void checkPermissionsIfNeeded(final Activity context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(context, Manifest.permission.RECORD_AUDIO) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(context, Manifest.permission.CAMERA) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                ActivityCompat.requestPermissions(context,
                        new String[]{
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.CAMERA,
                                Manifest.permission.RECORD_AUDIO
                        }, PERMISSION_GRANT);
            }
        }
    }
}
