package reg.videoregistrator.presenters;

import reg.videoregistrator.views.IMainView;

public interface IVideoPresenter {

    void saveFile(String filePath);
    void onResume(IMainView view);
    void onPause();
}
