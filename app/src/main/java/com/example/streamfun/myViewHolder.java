package com.example.streamfun;

import android.app.Application;
import android.app.Application;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class myViewHolder extends RecyclerView.ViewHolder
{
    SimpleExoPlayerView simpleExoPlayerView;
    SimpleExoPlayer simpleExoPlayer;
    TextView vtitleview, like_text;
    ImageView like_btn;
    DatabaseReference likereference;
    ImageView cmnt_ibtn;
    TextView cmnt_text;

    public myViewHolder(@NonNull View itemView)
    {
        super(itemView);
        vtitleview=itemView.findViewById(R.id.vtitle);
        simpleExoPlayerView=itemView.findViewById(R.id.exoplayer);
        like_btn = itemView.findViewById(R.id.like_image);
        like_text = itemView.findViewById(R.id.like_count);
        cmnt_ibtn = itemView.findViewById(R.id.cmnt_ibtn);
        cmnt_text = itemView.findViewById(R.id.cmnt_text);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mClickListener.onItemClick(v, getAdapterPosition());
            }
        });

        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mClickListener.onItemLongClick(v,getAdapterPosition());
                return false;
            }
        });
    }

    public void getlikebuttonstatus(final String postkey, final String userid)
    {
        likereference= FirebaseDatabase.getInstance().getReference("likes");
        likereference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(postkey).hasChild(userid))
                {
                    int likecount=(int)snapshot.child(postkey).getChildrenCount();
                    like_text.setText(likecount+" likes");
                    like_btn.setImageResource(R.drawable.ic_favorite_red_24dp);
                }
                else
                {
                    int likecount=(int)snapshot.child(postkey).getChildrenCount();
                    like_text.setText(likecount+" likes");
                    like_btn.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void releasePlayer(){
        if(simpleExoPlayer!=null){
            simpleExoPlayer.release();
            simpleExoPlayer.clearVideoSurface();
            simpleExoPlayerView.getPlayer().release();;


            simpleExoPlayer = null;
            simpleExoPlayerView =null;
        }
    }

    void prepareExoplayer(Application application, String videotitle, String videourl)
    {
        try
        {
            vtitleview.setText(videotitle);
            BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
            TrackSelector trackSelector = new DefaultTrackSelector(new AdaptiveTrackSelection.Factory(bandwidthMeter));
            simpleExoPlayer =(SimpleExoPlayer) ExoPlayerFactory.newSimpleInstance(application,trackSelector);

            Uri videoURI = Uri.parse(videourl);

            DefaultHttpDataSourceFactory dataSourceFactory = new DefaultHttpDataSourceFactory("exoplayer_video");
            ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
            MediaSource mediaSource = new ExtractorMediaSource(videoURI, dataSourceFactory, extractorsFactory, null, null);

            simpleExoPlayerView.setPlayer(simpleExoPlayer);
            simpleExoPlayer.prepare(mediaSource);
            simpleExoPlayer.setPlayWhenReady(false);
        }catch (Exception ex)
        {
            Log.d("Explayer Creshed", ex.getMessage().toString());
        }
    }
    private myViewHolder.ClickListener mClickListener;
    public interface ClickListener{
        void onItemClick(View view, int position);
        void onItemLongClick(View view, int position);

    }
    public void setOnClickListen(myViewHolder.ClickListener clickListener){
        mClickListener = clickListener;
    }
}
