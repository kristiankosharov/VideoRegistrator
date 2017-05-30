package reg.videoregistrator.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import reg.videoregistrator.models.Video;

public class DeleteService extends IntentService {
    private static final String TAG = DeleteService.class.getSimpleName();

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public DeleteService(String name) {
        super(name);
    }

    public DeleteService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "Handle intent");
        File file = new File(Environment.getExternalStorageDirectory().getPath() + Video.VIDEO_DIRECTORY);
        File[] childs = file.listFiles();
        if (childs != null) {
            for (File childFile : childs) {
                Log.d(TAG, childFile.getName());
                if (shouldDelete(childFile.lastModified())) {
                    childFile.delete();
                }
            }
        }
    }

    private boolean shouldDelete(long timestamp) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(timestamp);
        Date fileDate = calendar.getTime();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(GregorianCalendar.DAY_OF_YEAR, -14);
        Date beforeWeeks = calendar.getTime();

        Log.d(TAG, fileDate.toString());
        Log.d(TAG, beforeWeeks.toString());

        if (fileDate.before(beforeWeeks)) {
            return true;
        }

        return false;
    }
}
