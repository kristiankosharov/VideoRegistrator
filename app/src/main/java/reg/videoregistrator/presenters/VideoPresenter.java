package reg.videoregistrator.presenters;

import android.content.Context;

import reg.videoregistrator.models.Video;
import reg.videoregistrator.views.IMainView;

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
        mVideoModel.saveFile(filePath);
        mVideoModel.uploadFile((Context) mMainView, filePath);
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
