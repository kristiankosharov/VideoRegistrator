package reg.videoregistrator.utils;

import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Calendar;
import java.util.Locale;

public class FileUtils {
    private static final String TAG = FileUtils.class.getSimpleName();

    private static final String VIDEO_NAME = "video-";
    private static final String VIDEO_DIRECTORY = "/videos'";

    public static void saveFile(File input) throws IOException {
        if (isExistDir()) {
            File video = new File(getVideoPath());
            if (!video.exists()) {
                boolean createdFile = video.createNewFile();
                Log.d(TAG, "Save of file: " + input.getPath() + " is success: " + createdFile);
            }
            FileChannel outputChannel = null;
            FileChannel inputChannel = null;
            try {
                outputChannel = new FileOutputStream(video).getChannel();
                inputChannel = new FileInputStream(input).getChannel();
                inputChannel.transferTo(0, inputChannel.size(), outputChannel);
                inputChannel.close();
            } finally {
                if (inputChannel != null) inputChannel.close();
                if (outputChannel != null) outputChannel.close();
            }
        }
    }

    private static String getVideoPath() {
        return getVideoDirectory() + VIDEO_NAME + getVideoName();
    }

    public static String getVideoName() {
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(System.currentTimeMillis());
        String date = DateFormat.format("yyyy.MM.dd.HH.mm.ss", cal).toString();

        return VIDEO_NAME + date;
    }

    private static boolean isExistDir() {
        File videosDir = new File(getVideoDirectory());
        if (!videosDir.exists()) {
            return videosDir.mkdir();
        }

        return false;
    }

    private static String getVideoDirectory() {
        return Environment.getExternalStorageDirectory() + VIDEO_DIRECTORY;
    }
}
