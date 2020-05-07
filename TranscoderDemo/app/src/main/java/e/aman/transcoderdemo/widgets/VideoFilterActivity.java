package e.aman.transcoderdemo.widgets;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.daasuu.epf.filter.GlBilateralFilter;
import com.daasuu.epf.filter.GlFilterGroup;
import com.daasuu.epf.filter.GlGrayScaleFilter;
import com.daasuu.epf.filter.GlInvertFilter;
import com.daasuu.epf.filter.GlMonochromeFilter;
import com.daasuu.gpuv.composer.GPUMp4Composer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.io.File;

import e.aman.transcoderdemo.R;
import e.aman.transcoderdemo.databinding.ActivityVideoFilterBinding;
import e.aman.transcoderdemo.utils.Constants;
import e.aman.transcoderdemo.views.MainActivity;

public class VideoFilterActivity extends AppCompatActivity {

    private ActivityVideoFilterBinding binding;
    private String inputVideoPath="";
    private SimpleExoPlayer player;
    private String filter="";
    private File dest;
    SharedPreferences timePreferences;
    SharedPreferences.Editor timeEditor;
    private com.daasuu.gpuv.egl.filter.GlFilterGroup glFilterGroup=null;

    SharedPreferences filePref;
    SharedPreferences.Editor fileEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVideoFilterBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        inputVideoPath = getIntent().getExtras().getString(Constants.CONVERT_VIDEO_URI);
        Log.e("filter input : " , inputVideoPath);

        makeDir();

        setUpExoPlayer();
        applyFilter();

        timePreferences = getSharedPreferences(Constants.TIME , Context.MODE_PRIVATE);
        timeEditor = timePreferences.edit();

        filePref = getSharedPreferences(Constants.FILE , Context.MODE_PRIVATE);
        fileEditor = filePref.edit();

    }

    private void makeDir()
    {
        String root = Environment.getExternalStorageDirectory().toString();
        File outputDirA = new File(root +"/outputs/Filter/");
        outputDirA.mkdir();
        File outputDirAA = new File(root +"/outputs/Filter/a/");
        outputDirAA.mkdir();

        File outputDirB = new File(root +"/outputs/Filter/");
        outputDirB.mkdir();
        File outputDirBB = new File(root +"/outputs/Filter/b/");
        outputDirBB.mkdir();
    }

    private void applyFilter()
    {
        binding.grayscaleImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter = "grayscale";
                glFilterGroup = new com.daasuu.gpuv.egl.filter.GlFilterGroup(new com.daasuu.gpuv.egl.filter.GlGrayScaleFilter());
                binding.ePlayer.setGlFilter(new GlGrayScaleFilter());
            }
        });

        binding.watermarkImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter = "watermark";
                glFilterGroup = new com.daasuu.gpuv.egl.filter.GlFilterGroup(new com.daasuu.gpuv.egl.filter.GlBilateralFilter());
                binding.ePlayer.setGlFilter(new GlBilateralFilter());
            }
        });

        binding.monochromeImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter = "monochrome";
                glFilterGroup = new com.daasuu.gpuv.egl.filter.GlFilterGroup(new com.daasuu.gpuv.egl.filter.GlMonochromeFilter());
                binding.ePlayer.setGlFilter(new GlMonochromeFilter());
            }
        });

        binding.invertImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter = "invert";
                glFilterGroup = new com.daasuu.gpuv.egl.filter.GlFilterGroup(new com.daasuu.gpuv.egl.filter.GlInvertFilter());
                binding.ePlayer.setGlFilter(new GlInvertFilter());
            }
        });

        binding.videoFilterCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(VideoFilterActivity.this , MainActivity.class);
                i.putExtra(Constants.INPUT_VIDEO_URL , inputVideoPath);
                startActivity(i);
                finish();
            }
        });

        binding.videoFilterDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.progressLayout.setVisibility(View.VISIBLE);
                saveVideo();

            }
        });


    }



    private void saveVideo()
    {

        File outputDir=null;
        String filePrefix="";
        String root = Environment.getExternalStorageDirectory().toString();


        final String file = filePref.getString(Constants.FILE_NUMBER , Constants.FILE_NUMBER_TWO);
        if (file.equals(Constants.FILE_NUMBER_TWO))
        {
            outputDir = new File(root +"/outputs/Filter/a/");
            filePrefix =  "myfiltervideo1";
            fileEditor.putString(Constants.FILE_NUMBER , Constants.FILE_NUMBER_ONE);
            fileEditor.apply();
        }
        else
        {
            outputDir = new File(root +"/outputs/Filter/b/");
            filePrefix =  "myfiltervideo2";
            fileEditor.putString(Constants.FILE_NUMBER , Constants.FILE_NUMBER_TWO);
            fileEditor.apply();
        }


        outputDir.mkdir();

        String fileExtension = ".mp4";
        dest = new File(outputDir , filePrefix + fileExtension);


        int time = timePreferences.getInt(Constants.HOW_MANY , Constants.FIRST);
        if (time == 1)
        {
            new GPUMp4Composer(getRealPathFromUri(this , Uri.parse(inputVideoPath)), dest.getAbsolutePath()).
                    filter(glFilterGroup)
                    .listener(new GPUMp4Composer.Listener() {
                        @Override
                        public void onProgress(final double progress) {
                            Log.e("progress : " , progress * 100 + "");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    binding.progressBar.setProgress((int)(progress*100));
                                    binding.progressTextView.setText((int)(progress*100)+"/"+binding.progressBar.getMax());

                                }
                            });
                        }

                        @Override
                        public void onCompleted() {

                     runOnUiThread(new Runnable() {
                         @Override
                         public void run() {
                             binding.progressLayout.setVisibility(View.GONE);
                         }
                     });

                            timeEditor.putInt(Constants.HOW_MANY , Constants.SECOND);
                            timeEditor.apply();


                            if (file.equals(Constants.FILE_NUMBER_TWO))
                            {
                                deleteTempDirB();
                            }
                            else
                            {
                                deleteTempDirA();
                            }

                            Intent i = new Intent(VideoFilterActivity.this , MainActivity.class);
                            i.putExtra(Constants.INPUT_VIDEO_URL , dest.getAbsolutePath());
                            startActivity(i);
                            finish();

                        }

                        @Override
                        public void onCanceled() {

                        }

                        @Override
                        public void onFailed(final Exception exception)
                        {

                           runOnUiThread(new Runnable() {
                               @Override
                               public void run() {
                                   Log.e("failed" , exception.getMessage());
                                   binding.progressLayout.setVisibility(View.GONE);
                               }
                           });
                        }
                    }).start();
        }
        else
        {
            new GPUMp4Composer(inputVideoPath, dest.getAbsolutePath()).
                    filter(glFilterGroup)
                    .listener(new GPUMp4Composer.Listener() {
                        @Override
                        public void onProgress(final double progress) {
                            Log.e("progress : " , progress * 100 + "");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    binding.progressBar.setProgress((int)(progress*100));
                                    binding.progressTextView.setText((int)(progress*100)+"/"+binding.progressBar.getMax());
                                }
                            });
                        }

                        @Override
                        public void onCompleted() {


                            timeEditor.putInt(Constants.HOW_MANY , Constants.SECOND);
                            timeEditor.apply();


                            Intent i = new Intent(VideoFilterActivity.this , MainActivity.class);
                            i.putExtra(Constants.INPUT_VIDEO_URL , dest.getAbsolutePath());
                            startActivity(i);

                            finish();

                        }

                        @Override
                        public void onCanceled() {

                        }

                        @Override
                        public void onFailed(final Exception exception)
                        {
                           runOnUiThread(new Runnable() {
                               @Override
                               public void run() {
                                   Log.e("failed" , exception.getMessage());
                                   binding.progressLayout.setVisibility(View.GONE);
                               }
                           });
                        }
                    }).start();
        }


    }

    private void setUpExoPlayer()
    {
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(getApplicationContext(), Util.getUserAgent(this, "Exo player Rotate"));


        // This is the MediaSource representing the media to be played.
        MediaSource videoSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.parse(inputVideoPath));

        // SimpleExoPlayer
        player = ExoPlayerFactory.newSimpleInstance(this);
        // Prepare the player with the source.
        player.prepare(videoSource);
        player.setPlayWhenReady(true);

        binding.ePlayer.setSimpleExoPlayer(player);
    }

    private static String getRealPathFromUri(Context context, Uri contentUri)
    {
        Cursor cursor = null;

        try {

            String proj[] = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri , proj , null , null , null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);

        }
        catch (Exception e)
        {
           Log.e("error" , "here");
            return "";
        }
        finally {
            if(cursor!=null)
            {
                cursor.close();
            }
        }



    }


    private void deleteTempDirA()
    {
        String root = Environment.getExternalStorageDirectory().toString();
        File dir = new File(root +"/outputs/Filter/a/");
        if (dir.isDirectory())
        {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++)
            {
                new File(dir, children[i]).delete();
            }
        }
    }

    private void deleteTempDirB()
    {
        String root = Environment.getExternalStorageDirectory().toString();
        File dir = new File(root +"/outputs/Filter/b/");
        if (dir.isDirectory())
        {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++)
            {
                new File(dir, children[i]).delete();
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        player.stop();
    }
}
