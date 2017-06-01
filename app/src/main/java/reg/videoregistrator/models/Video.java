package reg.videoregistrator.models;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;

import java.io.File;
import java.util.Calendar;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import rx.Observable;
import rx.functions.Func1;

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

    public Observable<ResponseBody> uploadFile(Context context, String filePath) {
        // create upload service client
        VideoUploadService service =
                ServiceGenerator.createService(VideoUploadService.class);

        // https://github.com/iPaulPro/aFileChooser/blob/master/aFileChooser/src/com/ipaulpro/afilechooser/utils/FileUtils.java
        // use the FileUtils to get the actual file by uri
        File file = new File(filePath);

        // create RequestBody instance from file
        RequestBody requestFile =
                RequestBody.create(
                        MediaType.parse(context.getContentResolver().getType(Uri.parse(filePath))),
                        file
                );

        // MultipartBody.Part is used to send also the actual file name
        MultipartBody.Part body =
                MultipartBody.Part.createFormData("picture", file.getName(), requestFile);

        // add another part within the multipart request
        String descriptionString = "hello, this is description speaking";
        RequestBody description =
                RequestBody.create(
                        okhttp3.MultipartBody.FORM, descriptionString);
        Observable<ResponseBody> call = service.upload(description, body);

        return call;
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
