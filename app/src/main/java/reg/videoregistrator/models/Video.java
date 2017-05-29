package reg.videoregistrator.models;

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

import rx.Observable;
import rx.functions.Func1;

public class Video {
    private static final String TAG = Video.class.getSimpleName();

    private static final String VIDEO_NAME = "video-";
    private static final String VIDEO_DIRECTORY = "/videos'";

    public void saveFile(String filePath) {
        Observable
                .just(filePath)
                .map(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String filePath) {
                        if (isExistDir()) {
                            File video = new File(getVideoPath());
                            if (!video.exists()) {
                                boolean createdFile = false;
                                try {
                                    createdFile = video.createNewFile();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                Log.d(TAG, "Save of file: " + filePath + " is success: " + createdFile);
                            }
                            FileChannel outputChannel = null;
                            FileChannel inputChannel = null;
                            try {
                                outputChannel = new FileOutputStream(video).getChannel();
                                inputChannel = new FileInputStream(new File(filePath)).getChannel();
                                inputChannel.transferTo(0, inputChannel.size(), outputChannel);
                                inputChannel.close();
                            } catch (IOException exception) {
                                exception.printStackTrace();
                            } finally {


                                try {
                                    if (inputChannel != null) inputChannel.close();
                                    if (outputChannel != null) outputChannel.close();
                                } catch (IOException exception) {
                                    exception.printStackTrace();
                                }
                            }
                        }

                        return null;
                    }
                });
    }

    private String getVideoPath() {
        return getVideoDirectory() + VIDEO_NAME + getVideoName();
    }

    public static String getVideoName() {
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(System.currentTimeMillis());
        String date = DateFormat.format("yyyy.MM.dd.HH.mm.ss", cal).toString();

        return VIDEO_NAME + date;
    }

    private boolean isExistDir() {
        File videosDir = new File(getVideoDirectory());
        if (!videosDir.exists()) {
            return videosDir.mkdir();
        }

        return false;
    }

    private String getVideoDirectory() {
        return Environment.getExternalStorageDirectory() + VIDEO_DIRECTORY;
    }
}
