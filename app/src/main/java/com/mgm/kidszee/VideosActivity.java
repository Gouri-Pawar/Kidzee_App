package com.mgm.kidszee;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

public class VideosActivity extends AppCompatActivity {

    private Button englishLetters, englishNumbers;
    private VideoView videoView;
    private MediaPlayer mediaPlayer;
    private Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videos);

        englishLetters = findViewById(R.id.videos_english_letters_btn);
        englishNumbers = findViewById(R.id.videos_english_numbers_btn);

        videoView = findViewById(R.id.videos_video_view);

        uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.english_alphabet_song);
        videoView.setVideoURI(uri);
        videoView.start();

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer media) {
                releaseMedia();
                mediaPlayer = media;
            }
        });

        englishLetters.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                englishLetters.setBackgroundResource(R.drawable.transparent_background);
                englishNumbers.setBackgroundResource(R.drawable.button_style);

                uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.english_alphabet_song);
                videoView.setVideoURI(uri);
                videoView.start();
            }
        });
        englishNumbers.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                englishNumbers.setBackgroundResource(R.drawable.transparent_background);
                englishLetters.setBackgroundResource(R.drawable.button_style);

                uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.english_numbers_song);
                videoView.setVideoURI(uri);
                videoView.start();
            }
        });

    }

    private void releaseMedia(){
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        videoView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        videoView.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        releaseMedia();
    }
}
