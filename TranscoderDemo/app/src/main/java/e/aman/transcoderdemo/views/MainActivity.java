package e.aman.transcoderdemo.views;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;

import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Toast;

import com.otaliastudios.transcoder.Transcoder;
import com.otaliastudios.transcoder.TranscoderListener;
import com.otaliastudios.transcoder.engine.TrackType;
import com.otaliastudios.transcoder.source.DataSource;
import com.otaliastudios.transcoder.source.TrimDataSource;
import com.otaliastudios.transcoder.source.UriDataSource;
import com.otaliastudios.transcoder.strategy.DefaultAudioStrategy;
import com.otaliastudios.transcoder.strategy.DefaultVideoStrategy;
import com.otaliastudios.transcoder.strategy.TrackStrategy;
import com.otaliastudios.transcoder.strategy.size.AspectRatioResizer;

import org.florescu.android.rangeseekbar.RangeSeekBar;

import java.io.File;

import e.aman.transcoderdemo.FinalActivity;
import e.aman.transcoderdemo.R;
import e.aman.transcoderdemo.databinding.ActivityMainBinding;
import e.aman.transcoderdemo.utils.Constants;
import e.aman.transcoderdemo.widgets.VideoFilterActivity;


public class MainActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener
{
    private ActivityMainBinding binding;
    private int duration;
    boolean isPlaying = false;
    String path;
    SharedPreferences preferences ;
    SharedPreferences.Editor editor;
    boolean isRotate = false , isCropped = false;
    MediaPlayer mediaPlayer;
    int currentAngleRotation = 0;
    float aspect_ratio = 0F;
    int textureview_height , textureview_width;
    File dest = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);


        path = getIntent().getExtras().getString(Constants.INPUT_VIDEO_URL);
        Log.e("path : " , path);

        makeDir();

        binding.mainScreenVideoview.setSurfaceTextureListener(this);
        preferences = getSharedPreferences(Constants.DATA , Context.MODE_PRIVATE);
        editor = preferences.edit();


       //pause video button
        binding.pauseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlaying) {
                    binding.pauseImage.setImageResource(R.drawable.play_icon);
                    mediaPlayer.pause();
                    isPlaying = false;
                } else {
                    mediaPlayer.start();
                    binding.pauseImage.setImageResource(R.drawable.pause_icon);
                    isPlaying = true;
                }
            }
        });

        setListeners();
        setParams();                                    //To set rotation and other parameters

    }   //onCreate end

    private void setParams()
    {
        String  rotationAngle = preferences.getString(Constants.ROTATION_ANGLE , "0");
        float aspectRatio = preferences.getFloat(Constants.ASPECT_RATIO ,0);

        if (!rotationAngle.equals("0"))
        {
            isRotate = true;
            currentAngleRotation = Integer.parseInt(rotationAngle);
            binding.mainScreenVideoview.setRotation(Integer.parseInt(rotationAngle));
        }
        if (aspectRatio!=0)
        {
            isCropped = true;
            aspect_ratio = aspectRatio;
            if (aspectRatio == (float)1.3333334)
            {
                adjustAspectRatio(4 , 3);
            }
            else if (aspectRatio == (float)1.7777778)
            {
                adjustAspectRatio(16 , 9);
            }
            else if (aspectRatio == (float)1.0)
            {
                adjustAspectRatio(1,1);
            }
        }

    }


    private void resizeVideoView()
    {
        float crop_size = preferences.getFloat(Constants.ASPECT_RATIO , 0);
        String rotate_angle = preferences.getString(Constants.ROTATION_ANGLE , "0");

        Log.e("crop size : " , crop_size+  "abx");

        if (crop_size != 0)
        {
            if (crop_size == (float)1.3333334)
            {
                adjustAspectRatio(4 , 3);
            }
            else if (crop_size == (float)1.7777778)
            {
                adjustAspectRatio(16 , 9);
            }
            else if (crop_size == (float)1.0)
            {
                adjustAspectRatio(1,1);
            }

        }


        if (!rotate_angle.equals("0"))
        {
            binding.mainScreenVideoview.setRotation(Integer.parseInt(rotate_angle));
        }



    }


    private void setListeners()
    {
        binding.rotateLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentAngleRotation == 270)
                {
                    currentAngleRotation = 0;
                }
                else
                {
                    currentAngleRotation  = currentAngleRotation + 90;
                }

                editor.putString(Constants.ROTATION_ANGLE , "" + currentAngleRotation);
                editor.apply();
                binding.mainScreenVideoview.setRotation(currentAngleRotation);
                isRotate=true;

            }
        });

        binding.showLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save_video();
                Toast.makeText(getApplicationContext() , "Wait....processing!!!",Toast.LENGTH_LONG).show();
            }
        });

        binding.aspectRatioLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.cropControllerView.setVisibility(View.VISIBLE);
            }
        });

        binding.textviewFourThree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adjustAspectRatio(4 , 3);
                aspect_ratio = 4F/3F;
            }
        });

        binding.textviewSixteenNine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adjustAspectRatio(16 , 9);
                aspect_ratio =  16F/9F;
            }
        });

        binding.textviewSquare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adjustAspectRatio(1 , 1);
                aspect_ratio = 1F;
            }
        });


        binding.cropDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isCropped=true;
                editor.putFloat(Constants.ASPECT_RATIO, aspect_ratio);
                editor.putString(Constants.ASPECT_RATIO_STRING , aspect_ratio + "");
                Log.e("aspect ratio" , aspect_ratio + "abx");
                editor.apply();
                binding.cropControllerView.setVisibility(View.GONE);
//
            }
        });

        binding.cropCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isCropped=false;
                binding.cropControllerView.setVisibility(View.GONE);
                adjustAspectRatio(textureview_width , textureview_height);
            }
        });

        binding.videoFilterLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(MainActivity.this , VideoFilterActivity.class);
                i.putExtra(Constants.CONVERT_VIDEO_URI , path);
                startActivity(i);
                finish();
            }
        });


    }


    private String getTime(int seconds)
    {
        int hr = seconds/3600;
        int rem = seconds % 3600;
        int mn = rem/60;
        int sec = rem%60;
        return  String.format("%02d",hr) + ":" + String.format("%02d",mn) + ":" + String.format("%02d",sec);
    }


    private void makeDir()
    {
        String root = Environment.getExternalStorageDirectory().toString();
        File outputDir = new File(root +"/outputs/");
        outputDir.mkdir();

    }



    private void save_video()
    {
        binding.progressLayout.setVisibility(View.VISIBLE);

        String root = Environment.getExternalStorageDirectory().toString();
        File outputDir = new File(root +"/outputs");
        outputDir.mkdir();
        String filePrefix = "mysavedvideo";
        String fileExtension = ".mp4";
        dest = new File(outputDir , filePrefix + fileExtension);

        DataSource source = new UriDataSource(MainActivity.this, Uri.parse(path));
        TrackStrategy audioStrategy = DefaultAudioStrategy.builder().channels(DefaultAudioStrategy.CHANNELS_AS_INPUT).sampleRate(DefaultAudioStrategy.SAMPLE_RATE_AS_INPUT).build();
        String angle = preferences.getString(Constants.ROTATION_ANGLE , "0");
        Float ratio = preferences.getFloat(Constants.ASPECT_RATIO , 0F);



        if (isCropped && !isRotate)
        {
            DefaultVideoStrategy mTranscodeVideoStrategy = new DefaultVideoStrategy.Builder().addResizer(new AspectRatioResizer(ratio)).build();
            Transcoder.into(dest.getAbsolutePath())
                .addDataSource(TrackType.VIDEO, source)
                .addDataSource(TrackType.AUDIO , source)
                      .setVideoTrackStrategy(mTranscodeVideoStrategy)
                    .setAudioTrackStrategy(audioStrategy)
                    .setListener(new TranscoderListener() {
                        public void onTranscodeProgress(final double progress) {
                            Log.e("progress : " , String.valueOf((int)progress));
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    binding.progressBar.setProgress((int)(progress*100));
                                    binding.progressTextView.setText((int)(progress*100)+"/"+binding.progressBar.getMax());
                                }
                            });
                        }
                        public void onTranscodeCompleted(int successCode) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext() , "successful" , Toast.LENGTH_SHORT).show();
                                    binding.progressLayout.setVisibility(View.GONE);

                                    sendToFinalActivity(dest.getAbsolutePath());

                                }
                            });
                        }
                        public void onTranscodeCanceled() {}
                        public void onTranscodeFailed(@NonNull Throwable exception) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext() , "failed" , Toast.LENGTH_SHORT).show();
                                    binding.progressLayout.setVisibility(View.GONE);
                                }
                            });
                        }
                    }).
                    transcode();
        }
        else if (isRotate && !isCropped)
        {
            int rotate_angle = Integer.parseInt(angle);
            Transcoder.into(dest.getAbsolutePath())
                    .setAudioTrackStrategy(audioStrategy)
                .addDataSource(TrackType.VIDEO, source)
                    .addDataSource(TrackType.AUDIO , source)
                .setListener(new TranscoderListener() {
                    public void onTranscodeProgress(final double progress) {
                        Log.e("progress : " , String.valueOf((int)progress));
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                binding.progressBar.setProgress((int)(progress*100));
                                binding.progressTextView.setText((int)(progress*100)+"/"+binding.progressBar.getMax());
                            }
                        });


                    }
                    public void onTranscodeCompleted(int successCode) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext() , "successful" , Toast.LENGTH_SHORT).show();
                                binding.progressLayout.setVisibility(View.GONE);

                                sendToFinalActivity(dest.getAbsolutePath());
                            }
                        });
                    }
                    public void onTranscodeCanceled() {}
                    public void onTranscodeFailed(@NonNull Throwable exception) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext() , "failed" , Toast.LENGTH_SHORT).show();
                                binding.progressLayout.setVisibility(View.GONE);
                            }
                        });
                    }
                }).
                    setVideoRotation(rotate_angle)
                    .transcode();
        }

        else if (isCropped && isRotate)
        {

            int rotate_angle = Integer.parseInt(angle);

            DefaultVideoStrategy mTranscodeVideoStrategy = new DefaultVideoStrategy.Builder().addResizer(new AspectRatioResizer(ratio)).build();
            Transcoder.into(dest.getAbsolutePath())
                    .addDataSource(TrackType.VIDEO, source)
                .addDataSource(TrackType.AUDIO , source)
                    .setVideoTrackStrategy(mTranscodeVideoStrategy)
                    .setAudioTrackStrategy(audioStrategy)
                    .setListener(new TranscoderListener() {
                        public void onTranscodeProgress(final double progress) {

                            Log.e("progress : " , String.valueOf((int)progress));
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    binding.progressBar.setProgress((int)(progress*100));
                                    binding.progressTextView.setText((int)(progress*100)+"/"+binding.progressBar.getMax());
                                }
                            });

                        }
                        public void onTranscodeCompleted(int successCode) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext() , "successful" , Toast.LENGTH_SHORT).show();
                                    binding.progressLayout.setVisibility(View.GONE);

                                    sendToFinalActivity(dest.getAbsolutePath());

                                }
                            });
                        }
                        public void onTranscodeCanceled() {}
                        public void onTranscodeFailed(@NonNull Throwable exception) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext() , "failed" , Toast.LENGTH_SHORT).show();
                                    binding.progressLayout.setVisibility(View.GONE);
                                }
                            });
                        }
                    }).
                    setVideoRotation(rotate_angle).
                    transcode();

        }
        else
        {
            DefaultVideoStrategy mTranscodeVideoStrategy = new DefaultVideoStrategy.Builder().build();
            Transcoder.into(dest.getAbsolutePath())
                    .addDataSource(TrackType.VIDEO, source)
                    .addDataSource(TrackType.AUDIO , source)
                    .setVideoTrackStrategy(mTranscodeVideoStrategy)
                    .setAudioTrackStrategy(audioStrategy)
                    .setListener(new TranscoderListener() {
                        public void onTranscodeProgress(final double progress) {

                            Log.e("progress : " , String.valueOf((int)progress));
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    binding.progressBar.setProgress((int)(progress*100));
                                    binding.progressTextView.setText((int)(progress*100)+"/"+binding.progressBar.getMax());
                                }
                            });

                        }
                        public void onTranscodeCompleted(int successCode) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext() , "successful" , Toast.LENGTH_SHORT).show();
                                    binding.progressLayout.setVisibility(View.GONE);

                                    sendToFinalActivity(dest.getAbsolutePath());

                                }
                            });
                        }
                        public void onTranscodeCanceled() {}
                        public void onTranscodeFailed(@NonNull Throwable exception) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext() , "failed" , Toast.LENGTH_SHORT).show();
                                    binding.progressLayout.setVisibility(View.GONE);
                                }
                            });
                        }
                    }).
                    transcode();

        }



    }

    private void sendToFinalActivity(String absolutePath)
    {
        Intent i =new Intent(MainActivity.this , FinalActivity.class);
        i.putExtra(Constants.FINAL_URL , absolutePath);
        startActivity(i);
        finish();
    }


    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {

        textureview_height = height;
        textureview_width = width;

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setSurface(new Surface(surface));
        try {
            Uri video = Uri.parse(path);
            mediaPlayer.setDataSource(this, video);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        duration = mediaPlayer.getDuration()/1000;

                binding.tvvleft.setText("00.00.00");
                binding.tvvright.setText(getTime(mediaPlayer.getDuration()/1000));
                mediaPlayer.setLooping(true);
                binding.seekbar.setRangeValues(0,duration);
                binding.seekbar.setSelectedMaxValue(duration);
                binding.seekbar.setSelectedMinValue(0);
                binding.seekbar.setEnabled(true);
                binding.seekbar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener() {
                    @Override
                    public void onRangeSeekBarValuesChanged(RangeSeekBar bar, Object minValue, Object maxValue) {
                        mediaPlayer.seekTo((int)minValue*1000);
                        binding.tvvleft.setText(getTime((int)bar.getSelectedMinValue()));
                        binding.tvvright.setText(getTime((int)bar.getSelectedMaxValue()));
                    }
                });




        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(mediaPlayer.getCurrentPosition() >= binding.seekbar.getSelectedMaxValue().intValue() * 1000)
                    mediaPlayer.seekTo(binding.seekbar.getSelectedMinValue().intValue() * 1000);

            }
        },1000);


        resizeVideoView();




    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {


    }


    private void adjustAspectRatio(int videoWidth, int videoHeight) {
        int viewWidth = binding.mainScreenVideoview.getWidth();
        int viewHeight = binding.mainScreenVideoview.getHeight();
        double aspectRatio = (double) videoHeight / videoWidth;

        int newWidth, newHeight;
        if (viewHeight > (int) (viewWidth * aspectRatio)) {
            // limited by narrow width; restrict height
            newWidth = viewWidth;
            newHeight = (int) (viewWidth * aspectRatio);
        } else {
            // limited by short height; restrict width
            newWidth = (int) (viewHeight / aspectRatio);
            newHeight = viewHeight;
        }
        int xoff = (viewWidth - newWidth) / 2;
        int yoff = (viewHeight - newHeight) / 2;

        Matrix txform = new Matrix();
        binding.mainScreenVideoview.getTransform(txform);
        txform.setScale((float) newWidth / viewWidth, (float) newHeight / viewHeight);
        //txform.postRotate(10);          // just for fun
        txform.postTranslate(xoff, yoff);
        binding.mainScreenVideoview.setTransform(txform);
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isRotate = false;
        isCropped = false;
       if(mediaPlayer.isPlaying())
           mediaPlayer.stop();
    }




}
