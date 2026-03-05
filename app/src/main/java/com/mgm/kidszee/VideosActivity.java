package com.mgm.kidszee;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import android.widget.MediaController;

public class VideosActivity extends AppCompatActivity {

    private Button englishLetters, englishNumbers;
    private VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videos);

        englishLetters = findViewById(R.id.videos_english_letters_btn);
        englishNumbers = findViewById(R.id.videos_english_numbers_btn);
        videoView = findViewById(R.id.videos_video_view);

        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);

        // Play default video
        playVideo(R.raw.english_alphabet_song);

        // English Letters Button
        englishLetters.setOnClickListener(view -> {
            englishLetters.setBackgroundResource(R.drawable.transparent_background);
            englishNumbers.setBackgroundResource(R.drawable.button_style);
            playVideo(R.raw.english_alphabet_song);
        });

        // English Numbers Button
        englishNumbers.setOnClickListener(view -> {
            englishNumbers.setBackgroundResource(R.drawable.transparent_background);
            englishLetters.setBackgroundResource(R.drawable.button_style);
            playVideo(R.raw.english_numbers_song);
        });
    }

    /**
     * Method to play video from raw folder
     */
    private void playVideo(int videoRes) {
        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + videoRes);
        videoView.setVideoURI(uri);
        videoView.setOnPreparedListener(mp -> mp.setLooping(true));
        videoView.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (videoView != null && videoView.isPlaying()) {
            videoView.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (videoView != null) {
            videoView.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (videoView != null) {
            videoView.stopPlayback();
        }
    }
}