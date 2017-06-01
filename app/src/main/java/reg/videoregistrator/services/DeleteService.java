package reg.videoregistrator.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;

import reg.videoregistrator.models.Video;

public class DeleteService extends IntentService {
    private static final String TAG = DeleteService.class.getSimpleName();
    private static final long DIR_LIMIT = 1000000000; // 1GB in bytes
    private File videoDir = new File(Environment.getExternalStorageDirectory().getPath() + Video.VIDEO_DIRECTORY);

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
        // Check if there are files in dir
        if (videoDir.listFiles() == null) return;
        File[] childs = videoDir.listFiles();
        sortFileArray(childs);
        deleteOldVideos(childs);
    }

    /**
     * Check if dir's size is above 1GB
     *
     * @return true or false
     */
    private boolean reachedLimit() {
        if (videoDir.isDirectory()) {
            if (videoDir.length() >= DIR_LIMIT) {
                return true;
            }
        }

        return false;
    }

    /**
     * Delete oldest videos while dir's size is under 1GB
     *
     * @param files array with files
     */
    private void deleteOldVideos(File[] files) {
        for (File child : files) {
            if (reachedLimit()) {
                child.delete();
            } else {
                return;
            }
        }
    }

    /**
     * Method for sorting files by last modified time.
     *
     * @param array with files
     */
    private void sortFileArray(File[] array) {
        Arrays.sort(array, new Comparator<File>() {
            @Override
            public int compare(File lhs, File rhs) {
                long result = lhs.lastModified() - rhs.lastModified();
                if (result < 0) {
                    return 1;
                } else if (result > 0) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });
    }
}
