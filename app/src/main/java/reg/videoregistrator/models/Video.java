package reg.videoregistrator.models;

import android.net.Uri;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import rx.Observable;
import rx.functions.Func1;
import okhttp3.ResponseBody;
import reg.videoregistrator.utils.ServiceGenerator;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class Video {
    private static final String TAG = Video.class.getSimpleName();

    public static final String VIDEO_DIRECTORY = "/videos";
    private static final String VIDEO_NAME = "video-";
    private static final String VIDEO_EXTENSION = ".mp4";

    private VideoUploadService videoUploadService;

    public Video() {
        // create upload service client
        videoUploadService = ServiceGenerator.createService(VideoUploadService.class);
    }

    public Observable<String> saveFile(String filePath) {
        if (isExistDir()) {
            File oldFile = new File(filePath);
            File video = new File(getVideoDirectory(), getVideoName());
            boolean isRename = oldFile.renameTo(video);
            Log.d(TAG, "IS RENAME FILE: " + isRename);
            if (isRename) {
                return uploadFile(video.getPath());
            }
        }

        return Observable.error(new Throwable("File is not renamed"));
    }

    /**
     * Upload video to server
     *
     * @param filePath path ot file which will upload
     * @return {@link Observable<String>}
     */
    public Observable<String> uploadFile(String filePath) {
        return Observable.just(filePath)
                .flatMap(new Func1<String, Observable<ResponseBody>>() {
                    @Override
                    public Observable<ResponseBody> call(String s) {
                        Log.d(TAG, s);
                        File file = new File(s);
                        Uri fileUri = Uri.parse("content://" + file);
                        Log.d(TAG, "File uri: " + fileUri.getPath());
                        // create RequestBody instance from file
                        RequestBody requestFile =
                                RequestBody.create(
                                        MediaType.parse(URLConnection.guessContentTypeFromName(file.getName())),
                                        file
                                );
                        // MultipartBody.Part is used to send also the actual file name
                        MultipartBody.Part body =
                                MultipartBody.Part.createFormData("video", file.getName(), requestFile);

                        return videoUploadService.upload(body);
                    }
                })
                .flatMap(new Func1<ResponseBody, Observable<String>>() {
                    @Override
                    public Observable<String> call(ResponseBody s) {
                        // TODO parse string
                        try {
                            return Observable.just(s.string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        return null;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Get right video name in data format
     *
     * @return patch of video file
     */
    public static String getVideoName() {
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(System.currentTimeMillis());
        String date = DateFormat.format("yyyy-MM-dd-HH:mm:ss", cal).toString();

        return VIDEO_NAME + date + VIDEO_EXTENSION;
    }

    /**
     * Check if video's dir exist. If it don't exist,
     * created it.
     *
     * @return true if exist or result from creation
     */
    private boolean isExistDir() {
        File videosDir = new File(getVideoDirectory());
        if (!videosDir.exists()) {
            return videosDir.mkdir();
        }

        return true;
    }

    /**
     * Get path of video's dir
     *
     * @return path of dir
     */
    private String getVideoDirectory() {
        return Environment.getExternalStorageDirectory() + VIDEO_DIRECTORY;
    }
}
