package reg.videoregistrator.views;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

import reg.videoregistrator.R;

public class VideosListAdapter extends RecyclerView.Adapter<VideosListAdapter.VideoHolder> {

    private static final String TAG = VideosListAdapter.class.getSimpleName();
    private Context mContext;
    private File[] mVideos;

    public VideosListAdapter(Context context, File[] files) {
        mContext = context;
        mVideos = files;
    }

    @Override
    public VideoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.video_list_item, parent, false);

        return new VideoHolder(view);
    }

    @Override
    public void onBindViewHolder(VideoHolder holder, int position) {
        File file = mVideos[position];
        Bitmap thumb = ThumbnailUtils.createVideoThumbnail(file.getPath(),
                MediaStore.Images.Thumbnails.MINI_KIND);
        holder.mVideoThumb.setImageBitmap(thumb);
        holder.mVideoName.setText(file.getName());
    }

    @Override
    public int getItemCount() {
        return mVideos.length;
    }

    class VideoHolder extends RecyclerView.ViewHolder {

        protected ImageView mVideoThumb;
        protected TextView mVideoName;

        public VideoHolder(View itemView) {
            super(itemView);

            mVideoThumb = (ImageView) itemView.findViewById(R.id.video_thumb);
            mVideoName = (TextView) itemView.findViewById(R.id.video_name);
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    showDialog(getLayoutPosition());
                    return false;
                }
            });
        }
    }

    private void showDialog(final int position) {
        CharSequence operations[] = new CharSequence[]{"Open", "Delete"};

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("Pick operation");
        builder.setItems(operations, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    ((IVideoLongClick) mContext).openFile(mVideos[position]);
                } else {
                    ((IVideoLongClick) mContext).deleteFile(mVideos[position]);
                }
            }
        });
        builder.show();
    }
}
