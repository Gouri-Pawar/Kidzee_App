package com.mgm.kidszee;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Message;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DrawingActivity extends AppCompatActivity {

    private final int REQUEST_EXTERNAL_STORAGE = 10;
    private PaintView paintView;
    private ImageButton currPaint, thumbNail1, thumbNail2, thumbNail3;
    private MediaPlayer fg_voice;

    // Animations
    private Animation rotate, moveRight, moveLeft, zoomIO, fadeIn, slideUD;

    private String saveName, savePath, prevFilePath[];
    private boolean paused = false;
    private int inActivityTime = 0;

    final Context context = this;
    final String albumName = "Kidzee";
    final static String TAG = "TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawing);

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

        // Buttons with lambdas
        findViewById(R.id.erase_btn).setOnClickListener(this::eraserClicked);
        findViewById(R.id.new_btn).setOnClickListener(this::newClicked);

        findViewById(R.id.undo_btn).setOnClickListener(v -> {
            v.startAnimation(moveLeft);
            if (paintView.canUndo()) {
                startVoice(R.raw.undo);
            } else {
                startVoice(R.raw.noundo);
            }
            paintView.onClickUndo();
        });

        findViewById(R.id.redo_btn).setOnClickListener(v -> {
            v.startAnimation(moveRight);
            if (paintView.canRedo()) {
                startVoice(R.raw.redo);
            } else {
                startVoice(R.raw.noredo);
            }
            paintView.onClickRedo();
        });

        thumbNail1 = findViewById(R.id.load1);
        thumbNail1.setOnClickListener(v -> loadClicked(v, 0));

        thumbNail2 = findViewById(R.id.load2);
        thumbNail2.setOnClickListener(v -> loadClicked(v, 1));

        thumbNail3 = findViewById(R.id.load3);
        thumbNail3.setOnClickListener(v -> loadClicked(v, 2));

        // Animations
        rotate = AnimationUtils.loadAnimation(this, R.anim.anim_rotate);
        moveLeft = AnimationUtils.loadAnimation(this, R.anim.move_left);
        moveRight = AnimationUtils.loadAnimation(this, R.anim.move_right);
        fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        zoomIO = AnimationUtils.loadAnimation(this, R.anim.zoom_in_and_out);
        slideUD = AnimationUtils.loadAnimation(this, R.anim.slide_up_and_down);

        setMusic(R.raw.m1);
        saveName = setSaveName();
        savePath = setSavePath();
        createThumbNail();

        Log.e(TAG, "onCreate: savePath - " + savePath + " saveName - " + saveName);
    }

    @Override
    protected void onPause() {
        super.onPause();
        paused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        paused = false;
        autoSave();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (paintView.drawingChanged) {
                saveDrawing();
                paintView.drawingChanged = false;
                autoSave();
                inActivityTime = 0;
            } else if (!paintView.drawingChanging) {
                if (inActivityTime >= 30) {
                    inActivityTime = 0;
                    startVoice(R.raw.inactivity);
                    final Dialog timeoutDialog = new Dialog(context);
                    timeoutDialog.setContentView(R.layout.custom_dialog);
                    TextView dialogText = timeoutDialog.findViewById(R.id.dialog_text);
                    dialogText.setText(R.string.timeout_msg);
                    ImageButton yesBtn = timeoutDialog.findViewById(R.id.btn_yes);
                    ImageButton noBtn = timeoutDialog.findViewById(R.id.btn_no);
                    yesBtn.setOnClickListener(v -> {
                        startVoice(R.raw.yes);
                        timeoutDialog.dismiss();
                        autoSave();
                    });
                    noBtn.setOnClickListener(v -> {
                        startVoice(R.raw.no);
                        timeoutDialog.dismiss();
                        paintView.startNew();
                        autoSave();
                    });
                    timeoutDialog.getWindow().getAttributes().windowAnimations = R.style.CustomDialogAnimation;
                    timeoutDialog.show();
                } else {
                    inActivityTime += 10;
                    autoSave();
                }
            } else {
                inActivityTime = 0;
                autoSave();
            }
        }
    };

    public void autoSave() {
        if (!paused) {
            Runnable runnable = () -> {
                long start = System.currentTimeMillis();
                while (System.currentTimeMillis() - start < 10000) {
                    // wait
                }
                handler.sendEmptyMessage(0);
            };
            new Thread(runnable).start();
        }
    }

    public void paintClicked(View view) {
        paintView.drawingChanging = true;
        if (view.getId() == R.id.rc) {
            startVoice(R.raw.red);
            setMusic(R.raw.m1);
            view.startAnimation(rotate);
        } else if (view.getId() == R.id.yc) {
            startVoice(R.raw.yellow);
            setMusic(R.raw.m2);
            view.startAnimation(rotate);
        } else if (view.getId() == R.id.gc) {
            startVoice(R.raw.green);
            setMusic(R.raw.m3);
            view.startAnimation(rotate);
        } else if (view.getId() == R.id.bc) {
            startVoice(R.raw.blue);
            setMusic(R.raw.m4);
            view.startAnimation(rotate);
        } else if (view.getId() == R.id.pc) {
            startVoice(R.raw.pink);
            setMusic(R.raw.m5);
            view.startAnimation(rotate);
        }

        ImageButton imgView = (ImageButton) view;
        String color = view.getTag().toString();
        paintView.setColor(color);
        if (view != currPaint) {
            imgView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.color_selected, null));
            currPaint.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.color_pallete, null));
            currPaint = (ImageButton) view;
        }
    }

    private void loadClicked(View view, final int i) {
        paintView.drawingChanging = true;
        view.startAnimation(zoomIO);
        if (prevFilePath[i] == null) {
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

    private void newClicked(View view) {
        startVoice(R.raw.clear);
        view.startAnimation(slideUD);
        paintView.drawingChanging = true;
        final Dialog newDialog = new Dialog(context);
        newDialog.setContentView(R.layout.custom_dialog);
        TextView dialogText = newDialog.findViewById(R.id.dialog_text);
        ImageButton yesBtn = newDialog.findViewById(R.id.btn_yes);
        ImageButton noBtn = newDialog.findViewById(R.id.btn_no);
        dialogText.setText(R.string.new_msg);
        yesBtn.setOnClickListener(v -> {
            startVoice(R.raw.yes);
            newDialog.dismiss();
            saveDrawing();
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

    void eraserClicked(View view) {
        startVoice(R.raw.erase);
        ImageButton imgView = (ImageButton) view;
        currPaint.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.color_pallete, null));
        imgView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.color_selected, null));
        currPaint = imgView;
        paintView.drawingChanging = true;
        view.startAnimation(fadeIn);
        setMusic(R.raw.erasor);
        paintView.setErase(true);
    }

    private void saveDrawing() {
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_EXTERNAL_STORAGE);
        } else {
            paintView.setDrawingCacheEnabled(true);
            paintView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
            Bitmap bitmap = paintView.getDrawingCache();
            File pictureFile = new File(savePath + File.separator + saveName);
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.close();
                Toast.makeText(getApplicationContext(),
                        "Drawing Saved! " + pictureFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(),
                        "Oops! Image could not be saved. " + pictureFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
            }
        }
        paintView.setDrawingCacheEnabled(false);
    }

    public void createThumbNail() {
        if (savePath != null) {
            File files[] = new File(savePath).listFiles();
            prevFilePath = new String[3];
            if (files != null && files.length > 0) {
                File file1 = files[0];
                File file2 = null;
                File file3 = null;

                for (int i = 1; i < files.length; i++) {
                    if (file1.lastModified() < files[i].lastModified()) {
                        file3 = file2;
                        file2 = file1;
                        file1 = files[i];
                    } else if ((file2 == null) || (file2.lastModified() < files[i].lastModified())) {
                        file3 = file2;
                        file2 = files[i];
                    } else if ((file3 == null) || (file3.lastModified() < files[i].lastModified())) {
                        file3 = files[i];
                    }
                }

                if (file1 != null) {
                    prevFilePath[0] = file1.getAbsolutePath();
                    Bitmap ThumbImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(prevFilePath[0]), 64, 64);
                    thumbNail1.setImageBitmap(ThumbImage);
                }
                if (file2 != null) {
                    prevFilePath[1] = file2.getAbsolutePath();
                    Bitmap ThumbImage2 = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(prevFilePath[1]), 64, 64);
                    thumbNail2.setImageBitmap(ThumbImage2);
                }
                if (file3 != null) {
                    prevFilePath[2] = file3.getAbsolutePath();
                    Bitmap ThumbImage3 = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(prevFilePath[2]), 64, 64);
                    thumbNail3.setImageBitmap(ThumbImage3);
                }
            }
        }
    }

    private String setSaveName() {
        String timeStamp = new SimpleDateFormat("ddMMyy_kkmmss", Locale.getDefault()).format(new Date());
        return "BD_" + timeStamp + ".png";
    }

    @Nullable
    private String setSavePath() {
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_EXTERNAL_STORAGE);
        } else {
            if (!isExternalStorageWritable()) {
                return null;
            }
            File mediaStorageDir = new File(context.getExternalFilesDir(
                    Environment.DIRECTORY_PICTURES).getAbsolutePath(), albumName);
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    return null;
                }
            }
            return mediaStorageDir.getAbsolutePath();
        }
        return null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveDrawing();
            } else {
                Toast.makeText(getApplicationContext(),
                        "Permission Denied! Image could not be saved.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    void startVoice(int voiceId) {
        if (fg_voice != null) {
            fg_voice.release();
        }
        fg_voice = MediaPlayer.create(context, voiceId);
        fg_voice.start();
    }

    void setMusic(int musicId) {
        if (paintView.bg_music != null) {
            paintView.bg_music.release();
        }
        paintView.bg_music = MediaPlayer.create(context, musicId);
    }
}
