package e.aman.transcoderdemo.views;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;


import java.io.File;

import e.aman.transcoderdemo.databinding.ActivityPickVideoBinding;
import e.aman.transcoderdemo.utils.Constants;


public class PickVideoActivity extends AppCompatActivity
{

    private ActivityPickVideoBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding  = ActivityPickVideoBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

       isStoragePermissionGranted();
        binding.addVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickVideo();
            }
        });

        SharedPreferences preferences = getSharedPreferences(Constants.DATA , Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat(Constants.ASPECT_RATIO , 0);
        editor.putString(Constants.ROTATION_ANGLE,  "0");
        editor.apply();


        SharedPreferences timePreferences = getSharedPreferences(Constants.TIME , Context.MODE_PRIVATE);
        SharedPreferences.Editor timeEditor = timePreferences.edit();
        timeEditor.putInt(Constants.HOW_MANY , Constants.FIRST);
        timeEditor.apply();



    }



    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE ,Manifest.permission.READ_EXTERNAL_STORAGE  }, 1);
                return false;
            }
        }
        else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void pickVideo()
    {
        Intent i = new Intent(Intent.ACTION_PICK , MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        i.setType("video/*");
        startActivityForResult(i , 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 100  && resultCode == RESULT_OK)
        {
           Uri uriMain = data.getData();
           Intent i = new Intent(PickVideoActivity.this , MainActivity.class);
           i.putExtra(Constants.INPUT_VIDEO_URL , uriMain.toString());
            Log.e("path video" , uriMain.toString());
           startActivity(i);
        }
    }



    }
