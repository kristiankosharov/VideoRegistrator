package reg.videoregistrator.views;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import java.io.File;

import reg.videoregistrator.R;
import reg.videoregistrator.models.Video;

public class PreviewActivity extends Activity implements IVideoLongClick {

    private static final String TAG = PreviewActivity.class.getSimpleName();
    private VideosListAdapter mAdapter;
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preview_activity);

        mRecyclerView = (RecyclerView) findViewById(R.id.list_videos);
        File file = new File(Environment.getExternalStorageDirectory().getPath() + Video.VIDEO_DIRECTORY);
        if (file.listFiles() == null) {
            Toast.makeText(PreviewActivity.this, "", Toast.LENGTH_LONG).show();
            return;
        }
        mAdapter = new VideosListAdapter(this, file.listFiles());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                RecyclerView.VERTICAL);
        mRecyclerView.addItemDecoration(dividerItemDecoration);

    }

    @Override
    public void openFile(File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("file://" + file.getPath()));
        intent.setDataAndType(Uri.parse("file://" + file.getPath()), "video/*");
        PackageManager packageManager = getPackageManager();
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent);
        }
    }

    @Override
    public void deleteFile(final File file) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_title);
        builder.setMessage(R.string.dialog_message);
        builder.setPositiveButton(R.string.positive_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                file.delete();
                File file = new File(Environment.getExternalStorageDirectory().getPath() + Video.VIDEO_DIRECTORY);
                VideosListAdapter adapter = new VideosListAdapter(PreviewActivity.this, file.listFiles());
                if (file.listFiles() == null) {
                    return;
                }
                mRecyclerView.setAdapter(adapter);
            }
        });

        builder.setNegativeButton(R.string.negative_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
    }
}
