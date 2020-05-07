package e.aman.transcoderdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import e.aman.transcoderdemo.databinding.ActivityFinalBinding;
import e.aman.transcoderdemo.utils.Constants;

public class FinalActivity extends AppCompatActivity {

    private ActivityFinalBinding binding;
    private SimpleExoPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFinalBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);


        String inputVideoPath = getIntent().getExtras().getString(Constants.FINAL_URL);

        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(getApplicationContext(), Util.getUserAgent(this, "Exo player Rotate"));


        // This is the MediaSource representing the media to be played.
        MediaSource videoSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.parse(inputVideoPath));

        // SimpleExoPlayer
        player = ExoPlayerFactory.newSimpleInstance(this);
        // Prepare the player with the source.
        player.prepare(videoSource);
        player.setPlayWhenReady(true);

        binding.playerview.setPlayer(player);


    }


}
