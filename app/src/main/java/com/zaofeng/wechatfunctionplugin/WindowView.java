package com.zaofeng.wechatfunctionplugin;

import android.content.Context;
import android.graphics.PixelFormat;
import android.support.annotation.IntDef;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by 李可乐 on 2017/2/13 0013.
 */

public class WindowView implements CompoundButton.OnCheckedChangeListener {

    public static final int IndexRelease = 0;
    public static final int IndexBack = 1;
    public static final int IndexComment = 2;

    @IntDef({IndexRelease, IndexBack, IndexComment})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Index {
    }

    private Context mContext;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mWLayoutParams;

    private View viewRoot;
    private TextView btnMain;
    private CheckBox checkMenu;
    private View layoutMenu;

    private CheckBox checkRelease;
    private CheckBox checkBack;
    private CheckBox checkComment;

    private int mStartX;
    private int mStartY;
    private int mEndX;
    private int mEndY;

    private OnWindowViewCheckChangeListener onWindowViewCheckChangeListener;

    public WindowView(Context context) {
        this.mContext = context;
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        initView();
        initWindowLayout();
        initTouchListener();
        initClickListener();
        addView();
    }

    private void initView() {
        viewRoot = LayoutInflater.from(mContext).inflate(R.layout.layout_window, null);
        btnMain = (TextView) viewRoot.findViewById(R.id.txt_window_main);
        checkMenu = (CheckBox) viewRoot.findViewById(R.id.check_window_menu);
        layoutMenu = viewRoot.findViewById(R.id.layout_window_more);

        checkRelease = (CheckBox) viewRoot.findViewById(R.id.check_window_release_copy);
        checkBack = (CheckBox) viewRoot.findViewById(R.id.check_window_release_back);
        checkComment = (CheckBox) viewRoot.findViewById(R.id.check_window_comment_copy);

    }

    public void addView() {
        mWindowManager.addView(viewRoot, mWLayoutParams);
    }

    public void removeView() {
        mWindowManager.removeViewImmediate(viewRoot);
    }

    private void initClickListener() {
        checkMenu.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checkMenu.setText(isChecked ? "收起更多" : "打开更多");
                layoutMenu.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            }
        });

        checkRelease.setOnCheckedChangeListener(this);
        checkBack.setOnCheckedChangeListener(this);
        checkComment.setOnCheckedChangeListener(this);
    }



    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int id = buttonView.getId();
        if (onWindowViewCheckChangeListener == null) {
            return;
        }
        switch (id) {
            case R.id.check_window_release_copy:
                onWindowViewCheckChangeListener.onChange(IndexRelease,isChecked);
                break;
            case R.id.check_window_release_back:
                onWindowViewCheckChangeListener.onChange(IndexBack,isChecked);
                break;
            case R.id.check_window_comment_copy:
                onWindowViewCheckChangeListener.onChange(IndexComment,isChecked);
                break;
        }
    }

    private void initWindowLayout() {
        mWLayoutParams = new WindowManager.LayoutParams();
        mWLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        mWLayoutParams.format = PixelFormat.TRANSLUCENT;
        mWLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mWLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        mWLayoutParams.x = 100;
        mWLayoutParams.y = 300;
        mWLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mWLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
    }


    private void initTouchListener() {
        viewRoot.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mStartX = (int) event.getRawX();
                        mStartY = (int) event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        mEndX = (int) event.getRawX();
                        mEndY = (int) event.getRawY();
                        if (needIntercept()) {
                            mWLayoutParams.x = (int) (event.getRawX() - getViewRoot().getMeasuredWidth() / 2);
                            mWLayoutParams.y = (int) (event.getRawY() - getViewRoot().getMeasuredHeight() / 2);
                            mWindowManager.updateViewLayout(getViewRoot(), mWLayoutParams);
                            return true;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if (needIntercept()) {
                            return true;
                        }
                        break;
                    default:
                        break;
                }

                return false;
            }

            private boolean needIntercept() {
                return Math.abs(mStartX - mEndX) > 30 || Math.abs(mStartY - mEndY) > 30;
            }
        });
    }

    public void setMainTitle(String text) {
        btnMain.setText(text);
    }

    public View getViewRoot() {
        return viewRoot;
    }

    public void setViewCheckList(boolean isRelease,boolean isBack,boolean isComment){
        if (checkRelease!=null&&checkBack!=null&&checkComment!=null){
            checkRelease.setChecked(isRelease);
            checkBack.setChecked(isBack);
            checkComment.setChecked(isComment);
        }
    }

    public void setOnViewRootClick(final OnWindowViewClickListener onWindowViewClickListener) {
        viewRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onWindowViewClickListener != null) {
                    onWindowViewClickListener.onWindowClick(v);
                }
            }
        });
    }

    public void setOnWindowViewCheckChangeListener(OnWindowViewCheckChangeListener onWindowViewCheckChangeListener) {
        this.onWindowViewCheckChangeListener = onWindowViewCheckChangeListener;
    }

    public interface OnWindowViewClickListener {
        void onWindowClick(View view);
    }

    public interface OnWindowViewCheckChangeListener {
        void onChange(@Index int index,boolean isChecked);
    }

}
