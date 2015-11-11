package com.example.exoplayerdemo.player;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * VideoView的控制操作栏控件集合，可以根据自定义的UI布局对该对象进行赋值。
 * 该控件集合继承了如下内容：
 * {@link #pauseButton} 开始/暂停按钮
 * {@link #startResId}  开始ICON资源ID
 * {@link #pauseButton} 暂停ICON资源ID
 * {@link #totalTimeView}   视频总时间
 * {@link #currentTimeView} 当前播放时间
 * {@link #seekbar}     进度条
 * {@link #fullScreenButton}    全屏按钮
 * {@link #fullscreenResId}     全屏ICON资源ID
 * {@link #unfullscreenResId}   取消全屏ICON资源ID
 *
 */
public class BaseMediaControllerHolder {
    public View parentLayout;          //父控件
    public ImageButton pauseButton;     //开始/暂停按钮
    public TextView totalTimeView;      //视频总长度
    public TextView currentTimeView;    //当前播放时间
    public SeekBar seekbar;             //进度条
    public ImageButton fullScreenButton;    //全屏按钮

    public View headerView;
    public ImageButton headerBack;
    public TextView headerTitle;

    public int startResId;          //开始按钮图片资源ID
    public int pauseResId;          //暂停按钮图片资源ID
    public int fullscreenResId;     //全屏按钮图片资源ID
    public int unfullscreenResId;   //取消全屏按钮图片资源ID

}
