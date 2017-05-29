package reg.videoregistrator.views;

import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.IOException;
import java.util.List;

import reg.videoregistrator.R;
import reg.videoregistrator.utils.CameraUtils;
import reg.videoregistrator.utils.FileUtils;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SurfaceHolder.Callback {

    private static final String TAG = MainActivity.class.getSimpleName();
    private MediaRecorder mMediaRecorder;
    private SurfaceView mVideoView;
    private SurfaceHolder mVideoHolder;
    private Button mBtnStart;
    private Button mBtnStop;
    private File mOutputFile;
    private Camera mCamera;

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

    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaRecorder();
        releaseCamera();
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        switch (viewId) {
            case R.id.btn_start:
                startRecording();
                break;
            case R.id.btn_stop:
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
        if(!prepareCamera()) {
            Log.d(TAG, "Camera is not prepared");
            return false;
        }

        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setCamera(mCamera);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

        mOutputFile = new File(this.getExternalCacheDir() + FileUtils.getVideoName());

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

    private void releaseCamera(){
        if (mCamera != null){
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
}
