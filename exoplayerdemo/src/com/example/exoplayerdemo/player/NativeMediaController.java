package com.example.exoplayerdemo.player;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.Formatter;
import java.util.Locale;

/**
 * 自定义MediaController，用于原生的MediaPlayer，具有支持自定义UI的特性。
 * 使用方法：
 * 实现MediaControllerGenerator接口，该接口包含一个方法：
 * {@link MediaControllerGenerator#generateMediaController()}
 * 该方法从自定义的xml布局文件生成控制面板的布局，并且生成控制面板中的控件集合。
 * 控制控件集合对应的类为{ BaseMediaControllerHolder }，包含用于控制MediaPlayer的各种操作按钮和选项。
 */
public class NativeMediaController extends FrameLayout {
    private static final String TAG = "VideoControllerView";

    public static final int VIEW_MODE_PORTRAIT = 0;
    public static final int VIEW_MODE_LANDSCAPE = 1;

    private static final int sDefaultTimeout = 3000;
    private static final int FADE_OUT = 1;
    private static final int SHOW_PROGRESS = 2;

    private MediaController.MediaPlayerControl mPlayerControl;
    private Context mContext;
    private ViewGroup mAnchor;
    private View mRoot;
    private ProgressBar mProgress;
    private TextView mEndTime, mCurrentTime;
    private boolean mShowing;
    private boolean mDragging;
    private StringBuilder mFormatBuilder;
    private Formatter mFormatter;
    private ImageButton mPauseButton;
    private ImageButton mFullscreenButton;
    private View mParent;
    private View mHeaderView;
    private ImageButton mHeaderBack;
    private TextView mHeaderTitle;

    private int mPauseBtnResId, mStartBtnResId, mFullScreenResId, mUnFullScreenResId;

    private Handler mHandler = new MessageHandler(this);

    /**
     * 公共接口：自定义控制条布局生成
     */
    public interface MediaControllerGenerator {
        /**
         * 从布局文件生成一个控制条的自定义布局
         *
         * @return BaseMediaControllerHolder对象，控制条控件的集合
         */
        BaseMediaControllerHolder generateMediaController();
    }

    private MediaControllerGenerator mUIGenerator;

    public void setUIGenerator(MediaControllerGenerator generator) {
        this.mUIGenerator = generator;
    }

    public NativeMediaController(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        Log.i(TAG, TAG);
    }

    public NativeMediaController(Context context) {
        super(context);
        mContext = context;

        Log.i(TAG, TAG);
    }


    @Override
    public void onFinishInflate() {
        Log.d("0-0", TAG + " onFinishInflate");
        super.onFinishInflate();
    }

    public void setMediaPlayer(MediaController.MediaPlayerControl playerContral) {
        mPlayerControl = playerContral;
        updatePausePlay();
        updateFullScreen();
    }

    /**
     * Set the view that acts as the anchor for the control view.
     * This can for example be a VideoView, or your Activity's main view.
     *
     * @param view The view to which to anchor the controller when it is visible.
     */
    public void setAnchorView(ViewGroup view) {
        Log.d("0-0", TAG + " setAnchorView");
        mAnchor = view;

        LayoutParams frameParams = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );

        removeAllViews();
        View v = makeControllerView();
        addView(v, frameParams);
    }

    /**
     * Create the view that holds the widgets that control playback.
     * Derived classes can override this to create their own.
     *
     * @return The controller view.
     */
    protected View makeControllerView() {
        if (mUIGenerator != null) {
            initControllerView(mUIGenerator.generateMediaController());
        }

        return mRoot;
    }

    private void initControllerView(BaseMediaControllerHolder holder) {
        mRoot = holder.parentLayout;
        mPauseBtnResId = holder.pauseResId;
        mStartBtnResId = holder.startResId;
        mFullScreenResId = holder.fullscreenResId;
        mUnFullScreenResId = holder.unfullscreenResId;

        mHeaderView = holder.headerView;

        mHeaderBack = holder.headerBack;
        if (mHeaderBack != null) {
            mHeaderBack.requestFocus();
            mHeaderBack.setOnClickListener(mBackListener);
        }

        mHeaderTitle = holder.headerTitle;

        mPauseButton = holder.pauseButton;
        if (mPauseButton != null) {
            mPauseButton.requestFocus();
            mPauseButton.setOnClickListener(mPauseListener);
        }

        mFullscreenButton = holder.fullScreenButton;
        if (mFullscreenButton != null) {
            mFullscreenButton.requestFocus();
            mFullscreenButton.setOnClickListener(mFullscreenListener);
        }

        mParent = holder.parentLayout;
        if (mParent != null) {
            mParent.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    hide();
                }
            });
        }

        mProgress = holder.seekbar;
        if (mProgress != null) {
            if (mProgress instanceof SeekBar) {
                SeekBar seeker = (SeekBar) mProgress;
                seeker.setOnSeekBarChangeListener(mSeekListener);
            }
            mProgress.setMax(1000);
        }

        mEndTime = holder.totalTimeView;
        mCurrentTime = holder.currentTimeView;
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());

    }

    /**
     * Show the controller on screen. It will go away
     * automatically after 3 seconds of inactivity.
     */
    public void show() {
        show(sDefaultTimeout);
    }

    /**
     * Disable pause or seek buttons if the stream cannot be paused or seeked.
     * This requires the control interface to be a MediaPlayerControlExt
     */
    private void disableUnsupportedButtons() {
        if (mPlayerControl == null) {
            return;
        }

        try {
            if (mPauseButton != null && !mPlayerControl.canPause()) {
                mPauseButton.setEnabled(false);
            }
        } catch (IncompatibleClassChangeError ex) {
            // We were given an old version of the interface, that doesn't have
            // the canPause/canSeekXYZ methods. This is OK, it just means we
            // assume the media can be paused and seeked, and so we don't disable
            // the buttons.
        }
    }

    /**
     * Show the controller on screen. It will go away
     * automatically after 'timeout' milliseconds of inactivity.
     *
     * @param timeout The timeout in milliseconds. Use 0 to show
     *                the controller until hide() is called.
     */
    public void show(int timeout) {
        if (!mShowing && mAnchor != null) {
            setProgress();
            if (mPauseButton != null) {
                mPauseButton.requestFocus();
            }
            disableUnsupportedButtons();

            LayoutParams tlp = new LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.BOTTOM
            );

            mAnchor.addView(this, tlp);
            mShowing = true;
        }
        updatePausePlay();
        updateFullScreen();

        // cause the progress bar to be updated even if mShowing
        // was already true.  This happens, for example, if we're
        // paused with the progress bar showing the user hits play.
        mHandler.sendEmptyMessage(SHOW_PROGRESS);

        Message msg = mHandler.obtainMessage(FADE_OUT);
        if (timeout != 0) {
            mHandler.removeMessages(FADE_OUT);
            mHandler.sendMessageDelayed(msg, timeout);
        }
    }

    public boolean isShowing() {
        return mShowing;
    }

    /**
     * Remove the controller from the screen.
     */
    public void hide() {
        if (mAnchor == null) {
            return;
        }

        try {
            mAnchor.removeView(this);
            mHandler.removeMessages(SHOW_PROGRESS);
        } catch (IllegalArgumentException ex) {
            Log.w("MediaController", "already removed");
        }
        mShowing = false;
    }

    private String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    private int setProgress() {
        if (mPlayerControl == null || mDragging) {
            return 0;
        }

        int position = mPlayerControl.getCurrentPosition();
        int duration = mPlayerControl.getDuration();
        if (mProgress != null) {
            if (duration > 0) {
                // use long to avoid overflow
                long pos = 1000L * position / duration;
                mProgress.setProgress((int) pos);
            }
            int percent = mPlayerControl.getBufferPercentage();
            mProgress.setSecondaryProgress(percent * 10);
        }

        if (mEndTime != null)
            mEndTime.setText("/" + stringForTime(duration));
        if (mCurrentTime != null)
            mCurrentTime.setText(stringForTime(position));

        return position;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        show(sDefaultTimeout);
        return true;
    }

    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        show(sDefaultTimeout);
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (mPlayerControl == null) {
            return true;
        }

        int keyCode = event.getKeyCode();
        final boolean uniqueDown = event.getRepeatCount() == 0
                && event.getAction() == KeyEvent.ACTION_DOWN;
        if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK
                || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                || keyCode == KeyEvent.KEYCODE_SPACE) {
            if (uniqueDown) {
                doPauseResume();
                show(sDefaultTimeout);
                if (mPauseButton != null) {
                    mPauseButton.requestFocus();
                }
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
            if (uniqueDown && !mPlayerControl.isPlaying()) {
                mPlayerControl.start();
                updatePausePlay();
                show(sDefaultTimeout);
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
                || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
            if (uniqueDown && mPlayerControl.isPlaying()) {
                mPlayerControl.pause();
                updatePausePlay();
                show(sDefaultTimeout);
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
                || keyCode == KeyEvent.KEYCODE_VOLUME_UP
                || keyCode == KeyEvent.KEYCODE_VOLUME_MUTE) {
            // don't show the controls for volume adjustment
            return super.dispatchKeyEvent(event);
        } else if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_MENU) {
            if (uniqueDown) {
                hide();
            }
            return true;
        }

        show(sDefaultTimeout);
        return super.dispatchKeyEvent(event);
    }

    private OnClickListener mBackListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            ((Activity) mContext).finish();
        }
    };

    private OnClickListener mPauseListener = new OnClickListener() {
        public void onClick(View v) {
            doPauseResume();
            show(sDefaultTimeout);
        }
    };

    private OnClickListener mFullscreenListener = new OnClickListener() {
        public void onClick(View v) {
            doToggleFullscreen();
        }
    };

    public void updatePausePlay() {
        if (mRoot == null || mPauseButton == null || mPlayerControl == null) {
            return;
        }

        if (mPlayerControl.isPlaying()) {
            mPauseButton.setImageResource(mPauseBtnResId);
        } else {
            mPauseButton.setImageResource(mStartBtnResId);
        }
    }

    public void updateFullScreen() {
        if (mRoot == null || mFullscreenButton == null || mPlayerControl == null) {
            return;
        }

        if (mControllerOtherOperation.isFullscreen()) {
            mFullscreenButton.setImageResource(mUnFullScreenResId);
        } else {
            mFullscreenButton.setImageResource(mFullScreenResId);
        }
    }

    private void doPauseResume() {
        if (mPlayerControl == null) {
            return;
        }

        if (mPlayerControl.isPlaying()) {
            mPlayerControl.pause();
        } else {
            mPlayerControl.start();
        }
        updatePausePlay();
    }

    private void doToggleFullscreen() {
        if (mPlayerControl == null) {
            return;
        }

        mControllerOtherOperation.doFullscreen();

    }

    @Override
    public void setEnabled(boolean enabled) {
        if (mPauseButton != null) {
            mPauseButton.setEnabled(enabled);
        }
        if (mProgress != null) {
            mProgress.setEnabled(enabled);
        }
        disableUnsupportedButtons();
        super.setEnabled(enabled);
    }

    public interface MediaControllerOtherOperation{
        void doFullscreen();

        boolean isFullscreen();
    }

    private MediaControllerOtherOperation mControllerOtherOperation;

    public void setControllerOtherOperation(MediaControllerOtherOperation controllerOtherOperation) {
        mControllerOtherOperation = controllerOtherOperation;
    }

    // There are two scenarios that can trigger the seekbar listener to trigger:
    //
    // The first is the user using the touchpad to adjust the posititon of the
    // seekbar's thumb. In this case onStartTrackingTouch is called followed by
    // a number of onProgressChanged notifications, concluded by onStopTrackingTouch.
    // We're setting the field "mDragging" to true for the duration of the dragging
    // session to avoid jumps in the position in case of ongoing playback.
    //
    // The second scenario involves the user operating the scroll ball, in this
    // case there WON'T BE onStartTrackingTouch/onStopTrackingTouch notifications,
    // we will simply apply the updated position without suspending regular updates.
    private OnSeekBarChangeListener mSeekListener = new OnSeekBarChangeListener() {
        public void onStartTrackingTouch(SeekBar bar) {
            show(3600000);

            mDragging = true;

            // By removing these pending progress messages we make sure
            // that a) we won't update the progress while the user adjusts
            // the seekbar and b) once the user is done dragging the thumb
            // we will post one of these messages to the queue again and
            // this ensures that there will be exactly one message queued up.
            mHandler.removeMessages(SHOW_PROGRESS);
        }

        public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
            if (mPlayerControl == null) {
                return;
            }

            if (!fromuser) {
                // We're not interested in programmatically generated changes to
                // the progress bar's position.
                return;
            }

            long duration = mPlayerControl.getDuration();
            long newposition = (duration * progress) / 1000L;
            mPlayerControl.seekTo((int) newposition);
            if (mCurrentTime != null)
                mCurrentTime.setText(stringForTime((int) newposition));
        }

        public void onStopTrackingTouch(SeekBar bar) {
            mDragging = false;
            setProgress();
            updatePausePlay();
            show(sDefaultTimeout);

            // Ensure that progress is properly updated in the future,
            // the call to show() does not guarantee this because it is a
            // no-op if we are already showing.
            mHandler.sendEmptyMessage(SHOW_PROGRESS);
        }
    };


    private static class MessageHandler extends Handler {
        private final WeakReference<NativeMediaController> mView;

        MessageHandler(NativeMediaController view) {
            mView = new WeakReference<NativeMediaController>(view);
        }

        @Override
        public void handleMessage(Message msg) {
            NativeMediaController view = mView.get();
            if (view == null || view.mPlayerControl == null) {
                return;
            }

            int pos;
            switch (msg.what) {
                case FADE_OUT:
                    view.hide();
                    break;
                case SHOW_PROGRESS:
                    pos = view.setProgress();
                    if (!view.mDragging && view.mShowing && view.mPlayerControl.isPlaying()) {
                        msg = obtainMessage(SHOW_PROGRESS);
                        sendMessageDelayed(msg, 1000 - (pos % 1000));
                    }
                    break;
            }
        }
    }
}
