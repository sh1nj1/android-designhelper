package com.sh1nj1.android.designhelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.sh1nj1.android.designhelper.util.SystemUiHider;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class DesignScreenActivity extends Activity implements View.OnTouchListener, SeekBar.OnSeekBarChangeListener {

    private ImageView mContentView;
    private View mBottomControls;
    private View mTopControls;
    private TextView mText;
    private SeekBar mSeekBar;
    private TextView mPointer1;
    private TextView mPointer2;
    private Spinner mSpinner;
    private List<File> imageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int windowFlags =/* WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY
                |*/ WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        getWindow().setFlags(windowFlags, windowFlags);
        setContentView(R.layout.activity_design_screen);

        mTopControls = findViewById(R.id.topControls);
        mBottomControls = findViewById(R.id.bottomControls);
        mPointer1 = (TextView) findViewById(R.id.pointer1);
        mPointer2 = (TextView) findViewById(R.id.pointer2);
        mText = (TextView) findViewById(R.id.text);
        mSeekBar = (SeekBar) findViewById(R.id.seekbar);
        mContentView = (ImageView) findViewById(R.id.fullscreen_content);
        mSpinner = (Spinner) findViewById(R.id.menu);

        mDragView = mContentView;

        mPointer1.setOnTouchListener(this);
        mPointer2.setOnTouchListener(this);

        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 1: reloadImage(ImageView.ScaleType.FIT_START); break;
                    case 0:default: reloadImage(ImageView.ScaleType.CENTER); break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mContentView.setOnTouchListener(this);

        mSeekBar.setOnSeekBarChangeListener(this);
        mSeekBar.setProgress((int) mContentView.getAlpha() * 100);

        // loading files from /sdcard
        File f = Environment.getExternalStorageDirectory();
        File baseDir = new File(f, "DesignHelper_Assets");
        if (! baseDir.exists()) {
            Toast.makeText(this, String.format("%s directory is not found, you should make that directory and upload image files for design", baseDir.getAbsolutePath()), Toast.LENGTH_LONG).show();
            return;
        }
        String[] list = baseDir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                System.out.println(filename);
                if (filename.endsWith(".png") || filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
                    return true;
                }
                return false;
            }
        });
        imageList = new ArrayList<File>(list.length);
        for (int i = 0, l = list.length; i < l; ++i) {
            String filename = list[i];
            imageList.add(new File(baseDir, filename));
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        showNotification();
    }

    @Override
    protected void onStart() {
        super.onStart();
        removeNotification();
    }

    public void toggleVisible(View v) {
        if (mContentView.getVisibility() == View.VISIBLE) {
            mContentView.setVisibility(View.INVISIBLE);

            mBottomControls.setVisibility(View.GONE);

            View contentView = findViewById(android.R.id.content);
            WindowManager.LayoutParams p = (WindowManager.LayoutParams) ((FrameLayout) contentView.getParent().getParent()).getLayoutParams();
            p.x = 0; p.y = 0;
            p.width= px(100);
            p.height = px(60);
            getWindowManager().updateViewLayout((View) contentView.getParent().getParent(), p);
            //getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        } else {
            mContentView.setVisibility(View.VISIBLE);
            mBottomControls.setVisibility(View.VISIBLE);

            //getWindow().setFlags(0, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);

            View contentView = findViewById(android.R.id.content);
            WindowManager.LayoutParams p = (WindowManager.LayoutParams) ((FrameLayout) contentView.getParent().getParent()).getLayoutParams();
            p.width= WindowManager.LayoutParams.MATCH_PARENT;
            p.height = WindowManager.LayoutParams.MATCH_PARENT;
            getWindowManager().updateViewLayout((View) contentView.getParent().getParent(), p);

        }
    }

    public void toggleButtons(View v) {
        if (mBottomControls.getVisibility() == View.VISIBLE) {
            mTopControls.setVisibility(View.INVISIBLE);
            mBottomControls.setVisibility(View.INVISIBLE);
        } else {
            mTopControls.setVisibility(View.VISIBLE);
            mBottomControls.setVisibility(View.VISIBLE);
        }
    }

    private int mIndex = 0;

    public void previous(View v) {
        --mIndex;
        if (mIndex < 0) {
            mIndex = imageList.size() - 1;
        }
        setImage();
    }

    public void next(View v) {
        ++mIndex;
        if (mIndex >= imageList.size()) {
            mIndex = 0;
        }
        setImage();
    }

    private void setImage() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        String filename = imageList.get(mIndex).getAbsolutePath();
        Drawable imageDrawable = loadImageFromFilesystem(this, filename, displayMetrics.densityDpi);
        mContentView.setImageDrawable(imageDrawable);
    }


    private int mId = 0;
    public void showNotification() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("My notification")
                        .setContentText("Hello World!");
// Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, LinkActivity.class);

// The stack builder object will contain an artificial back stack for the
// started Activity.
// This ensures that navigating backward from the Activity leads out of
// your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
// Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
        mNotificationManager.notify(mId, mBuilder.build());
    }

    private void removeNotification() {
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(mId);
    }

    private void reloadImage(ImageView.ScaleType scaleType) {
        mContentView.getMeasuredWidth();
        mContentView.setScaleType(scaleType);
        mContentView.invalidate();
    }

    public void reset(View v) {
        setXY(mContentView, 0, 0);
    }

    public void left(View v) {
        setXY(mDragView, mDragView.getX() - 1, mDragView.getY());
    }

    public void up(View v) {
        setXY(mDragView, mDragView.getX(), mDragView.getY() - 1);
    }

    public void down(View v) {
        setXY(mDragView, mDragView.getX(), mDragView.getY() + 1);
    }

    public void right(View v) {
        setXY(mDragView, mDragView.getX() + 1, mDragView.getY());
    }

    private View mDragView;
    float dx=0,dy=0,x=0,y=0;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (mTopControls.getVisibility() != View.VISIBLE) {
            toggleButtons(mTopControls);
            return true;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                x = event.getX();
                y = event.getY();
                dx = x - v.getX();
                dy = y - v.getY();
            }
            break;
            case MotionEvent.ACTION_MOVE: {
                setXY(v, event.getX() - dx, event.getY() - dy);
            }
            break;
            case MotionEvent.ACTION_UP: {
                //your stuff
            }
        }
        return true;
    }

    private void setXY(View v, float x, float y) {
        mDragView = v;
        float dx, dy;
        int id = v.getId();
        if (id == R.id.pointer1 || id == R.id.pointer2) {
            dx = mPointer2.getX() - mPointer1.getX();
            dy = mPointer2.getY() - mPointer1.getY();
            if (dx < 0) dx *= -1f; if (dy < 0) dy *= -1f;
            if (v instanceof TextView) {
                TextView tv = (TextView) v;
                tv.setText(String.format("%.1f %.1f,\n %.1f %.1f", dp(x), dp(y), dp(dx), dp(dy)));
            }
        } else {
            mText.setText(String.format("%.1f, %.1f", dp(x), dp(y)));
        }
        v.setX(x);
        v.setY(y);
    }

    public int px(int dp) {
        Context ctx = this;
        DisplayMetrics displayMetrics = ctx.getResources().getDisplayMetrics();
        int px = Math.round(dp * displayMetrics.density);
        return px;
    }

    public float dp(float px) {
        Context ctx = this;
        DisplayMetrics displayMetrics = ctx.getResources().getDisplayMetrics();
        float dp = px / displayMetrics.density;
        return dp;
    }

    /**
     * Loads image from file system.
     *
     * @param context the application context
     * @param filename the filename of the image
     * @param originalDensity the density of the image, it will be automatically
     * resized to the device density
     * @return image drawable or null if the image is not found or IO error occurs
     */
    public static Drawable loadImageFromFilesystem(Context context, String filename, int originalDensity) {
        Drawable drawable = null;
        InputStream is = null;

        // set options to resize the image
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inDensity = originalDensity;

        try {
            is = new FileInputStream(filename);
            drawable = Drawable.createFromResourceStream(context.getResources(), null, is, filename, opts);
        } catch (Throwable e) {
            // handle
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Throwable e1) {
                    // ingore
                }
            }
        }
        return drawable;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mContentView.setAlpha((float) progress / 100f);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
