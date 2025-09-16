package com.mgm.kidszee;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

public class DashboardActivity extends AppCompatActivity {

    private VideoView video;
    private MediaPlayer mediaPlayer, music;

    private Button englishLetters, englishNumbers, drawing, songs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        video = findViewById(R.id.main_video);

        englishLetters = findViewById(R.id.main_english_letters_btn);
        englishNumbers = findViewById(R.id.main_english_numbers_btn);
        drawing = findViewById(R.id.main_drawing_btn);
        songs = findViewById(R.id.main_songs_btn);

        initSpinner();

        music = music.create(this, R.raw.looped_music);
        music.setLooping(true);
        music.start();

        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.background_video);
        video.setVideoURI(uri);
        video.start();
        songs.animate().scaleX(0.8f).setDuration(3000);

        englishLetters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DashboardActivity.this, EnglishLettersActivity.class));
            }
        });
        englishNumbers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DashboardActivity.this, EnglishNumbersActivity.class));
            }
        });
        drawing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DashboardActivity.this, DrawingActivity.class));
            }
        });
        songs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DashboardActivity.this, VideosActivity.class));
            }
        });

        video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer media) {
                releaseVideo();
                mediaPlayer = media;
                mediaPlayer.setLooping(true);
            }
        });
    }

    private void initSpinner() {
        englishLetters.setText("English Letters");
        englishNumbers.setText("English Numbers");
        songs.setText("Music Videos");
    }


    private void releaseVideo() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void releaseMusic() {
        if (music != null) {
            music.release();
            music = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        video.pause();
        music.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // EnglishLettersActivity.VISIBLE_BACKGROUND=false;
        video.start();
        music.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseVideo();
        releaseMusic();
    }

}