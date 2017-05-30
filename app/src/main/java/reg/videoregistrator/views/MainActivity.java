package reg.videoregistrator.views;

import android.content.Intent;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.IOException;
import java.util.List;

import reg.videoregistrator.R;
import reg.videoregistrator.models.Video;
import reg.videoregistrator.presenters.IVideoPresenter;
import reg.videoregistrator.presenters.VideoPresenter;
import reg.videoregistrator.services.DeleteService;
import reg.videoregistrator.utils.CameraUtils;
import reg.videoregistrator.utils.PreferencesUtils;

public class MainActivity extends AppCompatActivity implements IMainView, View.OnClickListener, SurfaceHolder.Callback, MediaRecorder.OnInfoListener, View.OnLongClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private MediaRecorder mMediaRecorder;
    private SurfaceView mVideoView;
    private SurfaceHolder mVideoHolder;
    private Button mBtnStart;
    private Button mBtnStop;
    private File mOutputFile;
    private Camera mCamera;
    private IVideoPresenter mVideoPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mVideoView = (SurfaceView) findViewById(R.id.camera_view);
        mBtnStart = (Button) findViewById(R.id.btn_start);
        mBtnStop = (Button) findViewById(R.id.btn_stop);
        mBtnStart.setOnClickListener(this);
        mBtnStop.setOnClickListener(this);
        mVideoHolder = mVideoView.getHolder();
        mVideoHolder.addCallback(this);
        mVideoPresenter = new VideoPresenter(this);
        mVideoView.setOnLongClickListener(this);


        ((Button) findViewById(R.id.settings)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PreferencesActivity.class);
                startActivity(intent);
            }
        });

        ((Button) findViewById(R.id.preview)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PreviewActivity.class);
                startActivity(intent);
            }
        });

        if (PreferencesUtils.removeOldVideos(this)) {
            Log.d(TAG, "Start service");
            startService(new Intent(this, DeleteService.class));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaRecorder();
        releaseCamera();
        if (mVideoPresenter != null) {
            mVideoPresenter.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mVideoPresenter != null) {
            mVideoPresenter.onResume(this);
        }
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        switch (viewId) {
            case R.id.btn_start:
                startRecording();
                break;
            case R.id.btn_stop:
                mVideoPresenter.saveFile(mOutputFile.getPath());
                stopRecording();
                break;
        }
    }

    private void startRecording() {
        if (prepareVideoRecorder()) {
            mMediaRecorder.start();
        } else {
            releaseMediaRecorder();
        }
    }

    private void stopRecording() {
        try {
            mMediaRecorder.stop();  // stop the recording
        } catch (RuntimeException e) {
            // RuntimeException is thrown when stop() is called immediately after start().
            // In this case the output file is not properly constructed ans should be deleted.
            Log.d(TAG, "RuntimeException: stop() is called immediately after start()");
            //noinspection ResultOfMethodCallIgnored
            mOutputFile.delete();
        }
        releaseMediaRecorder();
        mCamera.lock();
        releaseCamera();
    }

    private boolean prepareCamera() {
        mCamera = CameraUtils.getDefaultCameraInstance();

        Camera.Parameters parameters = mCamera.getParameters();
        List<Camera.Size> mSupportedPreviewSizes = parameters.getSupportedPreviewSizes();
        List<Camera.Size> mSupportedVideoSizes = parameters.getSupportedVideoSizes();
        Camera.Size optimalSize = CameraUtils.getOptimalVideoSize(mSupportedVideoSizes,
                mSupportedPreviewSizes, mVideoView.getWidth(), mVideoView.getHeight());

        CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        profile.videoFrameWidth = optimalSize.width;
        profile.videoFrameHeight = optimalSize.height;

        parameters.setPreviewSize(profile.videoFrameWidth, profile.videoFrameHeight);
        mCamera.setParameters(parameters);
        try {
            mCamera.setPreviewDisplay(mVideoHolder);
        } catch (IOException e) {
            Log.e(TAG, "Surface texture is unavailable or unsuitable" + e.getMessage());
            return false;
        }

        mCamera.unlock();
        return true;
    }

    private boolean prepareVideoRecorder() {
        if (!prepareCamera()) {
            Log.d(TAG, "Camera is not prepared");
            return false;
        }

        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setCamera(mCamera);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        if (PreferencesUtils.getAudio(this)) {
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
            Pair<Integer, Integer> videoSize = PreferencesUtils.getResolution(this);
            int quality = PreferencesUtils.getQuality(this);
            mMediaRecorder.setProfile(CamcorderProfile.get(quality));
            mMediaRecorder.setVideoSize(videoSize.first, videoSize.second);
        } else {
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            Pair<Integer, Integer> videoSize = PreferencesUtils.getResolution(this);
            mMediaRecorder.setVideoSize(videoSize.first, videoSize.second);
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
        }

        mMediaRecorder.setMaxDuration(PreferencesUtils.getMaxDuration(this));
        mMediaRecorder.setMaxFileSize(PreferencesUtils.getSizeLimit(this));
        mMediaRecorder.setOnInfoListener(this);

        mOutputFile = new File(this.getExternalCacheDir() + Video.getVideoName());

        if (!mOutputFile.exists()) {
            try {
                boolean isCreated = mOutputFile.createNewFile();
                Log.d(TAG, "Is created temp file: " + isCreated);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        mMediaRecorder.setOutputFile(mOutputFile.getPath());
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }

        return true;
    }

    private void releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
            mCamera.lock();
        }
    }

    private void releaseCamera() {
        if (mCamera != null) {
            // release the camera for other applications
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void saveVideo() {

    }

    @Override
    public void onInfo(MediaRecorder mr, int what, int extra) {
        if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED ||
                what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED) {
            stopRecording();
            mOutputFile.delete();
            startRecording();
        }
    }

    @Override
    public boolean onLongClick(View v) {
        int viewId = v.getId();
        if (viewId == R.id.camera_view) {
            stopRecording();
            mVideoPresenter.saveFile(mOutputFile.getPath());
            startRecording();
        }

        return false;
    }
}
