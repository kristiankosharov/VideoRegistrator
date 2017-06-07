package reg.videoregistrator.models;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import rx.Observable;

public interface VideoUploadService {
    @Multipart
    @POST("/")
    Observable<ResponseBody> upload(
            @Part MultipartBody.Part file
    );
}
