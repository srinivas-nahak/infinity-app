package community.infinity.network_related;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface FileUploadService {
    @Multipart
    @POST("upload")
    Call<ResponseBody> uploadMultipleFilesDynamic(
            @Part("description") RequestBody description,
            @Part List<MultipartBody.Part> files);
    @Multipart
    @POST("profile_pic")
    Call<ResponseBody> uploadProfilePic(
            @Part("description") RequestBody description,
            @Part MultipartBody.Part file);
    @Multipart
    @POST("wall_pic")
    Call<ResponseBody> uploadWallPic(
            @Part("description") RequestBody description,
            @Part MultipartBody.Part file);
    @Multipart
    @POST("msg_pic")
    Call<ResponseBody> uploadMsgPic(
            @Part("description") RequestBody description,
            @Part MultipartBody.Part file);
}
