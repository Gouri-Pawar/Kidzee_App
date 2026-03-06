package com.mgm.kidszee;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class DrawingActivity extends AppCompatActivity {

    private PaintView paintView;
    private ImageButton currPaint, thumbNail1, thumbNail2, thumbNail3;
    private MediaPlayer fg_voice;
    private Animation rotate, moveRight, moveLeft, zoomIO, fadeIn, slideUD;
    private String saveName, savePath, prevFilePath[];
    private boolean paused = false;
    private int inActivityTime = 0;
    private int pendingMusic = -1; // store music to play when drawing starts

    final Context context = this;
    final String albumName = "Kidzee";
    final static String TAG = "TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawing);

        // Pause Dashboard music if playing
        if (DashboardActivity.music != null && DashboardActivity.music.isPlaying()) {
            DashboardActivity.music.pause();
        }

        paintView = findViewById(R.id.drawing);

        // Color palette
        LinearLayout paintLayout = findViewById(R.id.paint_colors);
        for (int i = 0; i < paintLayout.getChildCount(); ++i) {
            ImageButton c = (ImageButton) paintLayout.getChildAt(i);
            if (i == 0) {
                c.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.color_selected, null));
                currPaint = c;
            } else {
                c.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.color_pallete, null));
            }
        }

        findViewById(R.id.erase_btn).setOnClickListener(this::eraserClicked);
        findViewById(R.id.new_btn).setOnClickListener(this::newClicked);
        findViewById(R.id.undo_btn).setOnClickListener(v -> {
            v.startAnimation(moveLeft);
            if (paintView.canUndo()) startVoice(R.raw.undo);
            else startVoice(R.raw.noundo);
            paintView.onClickUndo();
        });
        findViewById(R.id.redo_btn).setOnClickListener(v -> {
            v.startAnimation(moveRight);
            if (paintView.canRedo()) startVoice(R.raw.redo);
            else startVoice(R.raw.noredo);
            paintView.onClickRedo();
        });

        thumbNail1 = findViewById(R.id.load1);
        thumbNail2 = findViewById(R.id.load2);
        thumbNail3 = findViewById(R.id.load3);

        thumbNail1.setOnClickListener(v -> loadClicked(v, 0));
        thumbNail2.setOnClickListener(v -> loadClicked(v, 1));
        thumbNail3.setOnClickListener(v -> loadClicked(v, 2));

        rotate = AnimationUtils.loadAnimation(this, R.anim.anim_rotate);
        moveLeft = AnimationUtils.loadAnimation(this, R.anim.move_left);
        moveRight = AnimationUtils.loadAnimation(this, R.anim.move_right);
        fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        zoomIO = AnimationUtils.loadAnimation(this, R.anim.zoom_in_and_out);
        slideUD = AnimationUtils.loadAnimation(this, R.anim.slide_up_and_down);

        setMusic(R.raw.m1); // start DrawingActivity music

        // ✅ Set app-specific storage path
        savePath = getSavePath();
        saveName = setSaveName();
        createThumbNail();

        Log.e(TAG, "onCreate: savePath - " + savePath + " saveName - " + saveName);
    }

    // ------------------------ SAVE PATH ------------------------
    private String getSavePath() {
        File mediaStorageDir = new File(getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES), albumName);
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Toast.makeText(this, "Unable to access storage!", Toast.LENGTH_SHORT).show();
                return null;
            }
        }
        return mediaStorageDir.getAbsolutePath();
    }

    private String setSaveName() {
        String timeStamp = new SimpleDateFormat("ddMMyy_kkmmss", Locale.getDefault()).format(new Date());
        return "BD_" + timeStamp + ".png";
    }

    // ------------------------ SAVE DRAWING ------------------------
    private void saveDrawing() {
        if (savePath == null) return; // storage unavailable

        try {
            Bitmap bitmap = Bitmap.createBitmap(paintView.getWidth(), paintView.getHeight(), Bitmap.Config.ARGB_8888);
            android.graphics.Canvas canvas = new android.graphics.Canvas(bitmap);
            paintView.draw(canvas);

            File pictureFile = new File(savePath + File.separator + saveName);
            FileOutputStream fos = new FileOutputStream(pictureFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();

            saveName = setSaveName();
            createThumbNail();
            Log.e(TAG, "Drawing saved: " + pictureFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving drawing!", Toast.LENGTH_SHORT).show();
        }
    }

    // ------------------------ THUMBNAILS ------------------------
    private void createThumbNail() {
        if (savePath == null) return;

        File files[] = new File(savePath).listFiles();
        prevFilePath = new String[3]; // store latest 3 drawings

        if (files == null || files.length == 0) {
            thumbNail1.setImageResource(R.drawable.ic_placeholder);
            thumbNail2.setImageResource(R.drawable.ic_placeholder);
            thumbNail3.setImageResource(R.drawable.ic_placeholder);
            return;
        }

        // Sort files by lastModified descending (newest first)
        Arrays.sort(files, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));

        for (int i = 0; i < Math.min(3, files.length); i++) {
            prevFilePath[i] = files[i].getAbsolutePath();
            Bitmap thumbImage = ThumbnailUtils.extractThumbnail(
                    BitmapFactory.decodeFile(prevFilePath[i]), 64, 64
            );

            switch (i) {
                case 0: thumbNail1.setImageBitmap(thumbImage); break;
                case 1: thumbNail2.setImageBitmap(thumbImage); break;
                case 2: thumbNail3.setImageBitmap(thumbImage); break;
            }
        }

        if (files.length < 3) {
            if (files.length < 2) thumbNail2.setImageResource(R.drawable.ic_placeholder);
            if (files.length < 3) thumbNail3.setImageResource(R.drawable.ic_placeholder);
        }
    }

    // ------------------------ COLOR CLICK ------------------------
    public void paintClicked(View view) {
        paintView.drawingChanging = true;

        // Assign music ID instead of playing immediately
        if (view.getId() == R.id.rc) pendingMusic = R.raw.m1;
        else if (view.getId() == R.id.yc) pendingMusic = R.raw.m2;
        else if (view.getId() == R.id.gc) pendingMusic = R.raw.m3;
        else if (view.getId() == R.id.bc) pendingMusic = R.raw.m4;
        else if (view.getId() == R.id.pc) pendingMusic = R.raw.m5;

        startVoiceForColor(view.getId()); // play voice immediately

        // Set paint color
        String color = view.getTag().toString();
        paintView.setColor(color);

        if (view != currPaint) {
            ((ImageButton) view).setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.color_selected, null));
            currPaint.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.color_pallete, null));
            currPaint = (ImageButton) view;
        }
    }

    private void startVoiceForColor(int viewId) {
        if (fg_voice != null) fg_voice.release();

        if (viewId == R.id.rc) fg_voice = MediaPlayer.create(context, R.raw.red);
        else if (viewId == R.id.yc) fg_voice = MediaPlayer.create(context, R.raw.yellow);
        else if (viewId == R.id.gc) fg_voice = MediaPlayer.create(context, R.raw.green);
        else if (viewId == R.id.bc) fg_voice = MediaPlayer.create(context, R.raw.blue);
        else if (viewId == R.id.pc) fg_voice = MediaPlayer.create(context, R.raw.pink);

        if (fg_voice != null) fg_voice.start();
    }

    // ------------------------ NEW / LOAD / ERASER ------------------------
    private void newClicked(View view) {
        startVoice(R.raw.clear);
        view.startAnimation(slideUD);
        paintView.drawingChanging = true;

        final Dialog newDialog = new Dialog(context);
        newDialog.setContentView(R.layout.custom_dialog);
        TextView dialogText = newDialog.findViewById(R.id.dialog_text);
        dialogText.setText(R.string.new_msg);

        ImageButton yesBtn = newDialog.findViewById(R.id.btn_yes);
        ImageButton noBtn = newDialog.findViewById(R.id.btn_no);

        yesBtn.setOnClickListener(v -> {
            startVoice(R.raw.yes);
            newDialog.dismiss();
            saveDrawing();      // save current before clearing
            paintView.startNew();
            saveName = setSaveName();
            createThumbNail();
        });

        noBtn.setOnClickListener(v -> {
            startVoice(R.raw.no);
            newDialog.dismiss();
        });

        newDialog.getWindow().getAttributes().windowAnimations = R.style.CustomDialogAnimation;
        newDialog.show();
    }

    private void loadClicked(View view, final int i) {
        paintView.drawingChanging = true;
        view.startAnimation(zoomIO);

        if (prevFilePath == null || prevFilePath[i] == null) {
            startVoice(R.raw.noprev);
            Toast.makeText(this, "No previous drawings", Toast.LENGTH_SHORT).show();
            return;
        }

        startVoice(R.raw.load);
        final Dialog loadDialog = new Dialog(context);
        loadDialog.setContentView(R.layout.custom_dialog);
        TextView dialogText = loadDialog.findViewById(R.id.dialog_text);
        dialogText.setText(R.string.load_msg);

        ImageButton yesBtn = loadDialog.findViewById(R.id.btn_yes);
        ImageButton noBtn = loadDialog.findViewById(R.id.btn_no);

        yesBtn.setOnClickListener(v -> {
            startVoice(R.raw.yes);
            loadDialog.dismiss();
            paintView.setBitMap(prevFilePath[i]);
        });

        noBtn.setOnClickListener(v -> {
            startVoice(R.raw.no);
            loadDialog.dismiss();
        });

        loadDialog.getWindow().getAttributes().windowAnimations = R.style.CustomDialogAnimation;
        loadDialog.show();
    }

    void eraserClicked(View view) {
        startVoice(R.raw.erase);
        ((ImageButton) currPaint).setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.color_pallete, null));
        ((ImageButton) view).setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.color_selected, null));
        currPaint = (ImageButton) view;
        paintView.drawingChanging = true;
        view.startAnimation(fadeIn);
        setMusic(R.raw.erasor);
        paintView.setErase(true);
    }

    // ------------------------ VOICE & MUSIC ------------------------
    void startVoice(int voiceId) {
        if (fg_voice != null) fg_voice.release();
        fg_voice = MediaPlayer.create(context, voiceId);
        fg_voice.start();
    }

    void setMusic(int musicId) {
        if (paintView.bg_music != null) paintView.bg_music.release();
        paintView.bg_music = MediaPlayer.create(context, musicId);
        if (paintView.bg_music != null) {
            paintView.bg_music.setLooping(true);
            paintView.bg_music.start();
        }
    }

    // ------------------------ LIFECYCLE ------------------------
    @Override
    protected void onPause() {
        super.onPause();
        paused = true;
        if (paintView.bg_music != null && paintView.bg_music.isPlaying()) paintView.bg_music.pause();
        if (fg_voice != null && fg_voice.isPlaying()) fg_voice.pause();
        saveDrawing(); // auto save on pause
    }

    @Override
    protected void onResume() {
        super.onResume();
        paused = false;
        if (paintView.bg_music != null) paintView.bg_music.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (paintView.bg_music != null) {
            paintView.bg_music.release();
            paintView.bg_music = null;
        }
        if (fg_voice != null) {
            fg_voice.release();
            fg_voice = null;
        }
        // Resume Dashboard music
        if (DashboardActivity.music != null) DashboardActivity.music.start();
    }
}