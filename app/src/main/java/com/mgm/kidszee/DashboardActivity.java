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
    public static MediaPlayer mediaPlayer, music;

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

        // Initialize and start looping music
        music = MediaPlayer.create(this, R.raw.looped_music);
        music.setLooping(true);
        music.start();

        // Initialize video
        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.background_video);
        video.setVideoURI(uri);
        video.start();
        songs.animate().scaleX(0.8f).setDuration(3000);

        // Set button listeners
        englishLetters.setOnClickListener(v -> {
            if (music != null && music.isPlaying()) {
                music.pause();
            }
            if (video != null && video.isPlaying()) {
                video.pause();
            }
            startActivity(new Intent(DashboardActivity.this, EnglishLettersActivity.class));
        });
        englishNumbers.setOnClickListener(v -> {
            if (music != null && music.isPlaying()) {
                music.pause();
            }
            if (video != null && video.isPlaying()) {
                video.pause();
            }
            startActivity(new Intent(DashboardActivity.this, EnglishNumbersActivity.class));
        });
        drawing.setOnClickListener(v -> {
            if (music != null && music.isPlaying()) {
                music.pause();
            }
            if (video != null && video.isPlaying()) {
                video.pause();
            }
            startActivity(new Intent(DashboardActivity.this, DrawingActivity.class));
        });
        songs.setOnClickListener(v -> {
            if (music != null && music.isPlaying()) {
                music.pause();
            }
            if (video != null && video.isPlaying()) {
                video.pause();
            }
            startActivity(new Intent(DashboardActivity.this, VideosActivity.class));
        });

        video.setOnPreparedListener(media -> {
            releaseVideo();
            mediaPlayer = media;
            mediaPlayer.setLooping(true);
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

    private void pauseDashboardMusic() {
        if (music != null && music.isPlaying()) {
            music.pause();
        }
        if (video != null && video.isPlaying()) {
            video.pause();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        pauseDashboardMusic();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume music and video safely
        if (music != null) {
            music.start();
            music.setLooping(true);
        }
        if (video != null) {
            video.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseVideo();
        releaseMusic();
    }
}