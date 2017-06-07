package reg.videoregistrator.presenters;

import android.util.Log;
import reg.videoregistrator.models.Video;
import reg.videoregistrator.views.IMainView;
import rx.Subscriber;

public class VideoPresenter implements IVideoPresenter {

    private static final String TAG = VideoPresenter.class.getSimpleName();
    private IMainView mMainView;
    private Video mVideoModel;

    public VideoPresenter(IMainView mainView) {
        mMainView = mainView;
        mVideoModel = new Video();
    }

    @Override
    public void saveFile(String filePath) {
        mVideoModel.saveFile(filePath)
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "Error: " + e.getMessage());
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(String o) {
                        Log.d(TAG, "onNext: " + o);
                    }
                });
    }

    @Override
    public void onResume(IMainView view) {
        this.mMainView = view;
    }

    @Override
    public void onPause() {
        this.mMainView = null;
    }
}
