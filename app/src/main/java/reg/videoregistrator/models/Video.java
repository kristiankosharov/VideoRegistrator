package reg.videoregistrator.models;

import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;

import java.io.File;
import java.util.Calendar;
import java.util.Locale;

import rx.Observable;

public class Video {
    private static final String TAG = Video.class.getSimpleName();

    public static final String VIDEO_DIRECTORY = "/videos";
    private static final String VIDEO_NAME = "video-";
    private static final String VIDEO_EXTENSION = ".mp4";

    public void saveFile(String filePath) {
        if (isExistDir()) {
            File oldFile = new File(filePath);
            File video = new File(getVideoDirectory(), getVideoName());
            boolean isRename = oldFile.renameTo(video);
            Log.d(TAG, "IS RENAME FILE: " + isRename);
        }
    }

    public Observable uploadFile(String filePath) {
        return Observable.just(filePath);
    }

    public static String getVideoName() {
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(System.currentTimeMillis());
        String date = DateFormat.format("yyyy-MM-dd-HH:mm:ss", cal).toString();

        return VIDEO_NAME + date + VIDEO_EXTENSION;
    }

    private boolean isExistDir() {
        File videosDir = new File(getVideoDirectory());
        if (!videosDir.exists()) {
            return videosDir.mkdir();
        }

        return true;
    }

    private String getVideoDirectory() {
        return Environment.getExternalStorageDirectory() + VIDEO_DIRECTORY;
    }
}
