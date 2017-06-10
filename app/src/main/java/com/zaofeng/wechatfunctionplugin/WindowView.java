package com.zaofeng.wechatfunctionplugin;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.support.annotation.IntDef;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import com.zaofeng.wechatfunctionplugin.model.Constant;
import com.zaofeng.wechatfunctionplugin.utils.Logger;
import com.zaofeng.wechatfunctionplugin.utils.SPUtils;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by 李可乐 on 2017/2/13 0013.
 */

public class WindowView implements CompoundButton.OnCheckedChangeListener {

  public static final int IndexRelease = 0;
  public static final int IndexBack = 1;
  public static final int IndexComment = 2;
  private Context mContext;
  private WindowManager mWindowManager;
  private WindowManager.LayoutParams mWLayoutParams;
  private View viewRoot;

  private ImageButton txtActionMain;
  private CheckBox checkMenu;
  private View layoutMenu;
  private TextView txtPaste;
  private CheckBox checkComment;

  private OnWindowViewCheckChangeListener onWindowViewCheckChangeListener;
  private ActionListener actionListener;
  private ActionLongListener actionLongListener;

  final GestureDetector gestureDetector =
      new GestureDetector(mContext, new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
          updateViewLayoutByMotionEvent(e2);
          return true;
        }

        @Override public boolean onSingleTapConfirmed(MotionEvent e) {
          if (actionListener != null) {
            actionListener.onAction(txtActionMain);
          }
          return true;
        }

        @Override public void onLongPress(MotionEvent e) {
          if (actionLongListener!=null){
            actionLongListener.onAction(txtActionMain);
          }
        }

        @Override public boolean onDoubleTap(MotionEvent e) {
          if (actionLongListener!=null){
            actionLongListener.onAction(txtActionMain);
          }
          return true;
        }

        @Override public boolean onDown(MotionEvent e) {
          return true;
        }
      });

  public WindowView(Context context) {
    this.mContext = context;
    mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

    initView();
    initWindowLayout();
    initClickListener();
    initViewDate();
    addView();
  }

  private void initViewDate() {
    setOnChangeViewData(SPUtils.getSharedPreference(mContext));
  }

  private void initView() {
    viewRoot = LayoutInflater.from(mContext).inflate(R.layout.layout_window, null);
    txtActionMain = (ImageButton) viewRoot.findViewById(R.id.txt_window_action_main);
    checkMenu = (CheckBox) viewRoot.findViewById(R.id.check_window_menu);
    layoutMenu = viewRoot.findViewById(R.id.layout_window_more);

    checkComment = (CheckBox) viewRoot.findViewById(R.id.check_window_comment_copy);
    txtPaste = (TextView) viewRoot.findViewById(R.id.txt_window_comment_paste);
  }

  public void addView() {
    mWindowManager.addView(viewRoot, mWLayoutParams);
  }

  public void removeView() {
    mWindowManager.removeViewImmediate(viewRoot);
  }

  private void initClickListener() {
    checkMenu.setOnCheckedChangeListener((buttonView, isChecked) -> {
      checkMenu.setText(isChecked ? "收起更多" : "打开更多");
      layoutMenu.setVisibility(isChecked ? View.VISIBLE : View.GONE);
    });

    checkComment.setOnCheckedChangeListener(this);

    txtActionMain.setOnTouchListener(new OnTouchListener() {
      @Override public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
      }
    });
  }

  @Override
  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    int id = buttonView.getId();
    if (onWindowViewCheckChangeListener == null) {
      return;
    }
    switch (id) {
      case R.id.check_window_comment_copy:
        onWindowViewCheckChangeListener.onChange(IndexComment, isChecked);
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

  public View getViewRoot() {
    return viewRoot;
  }

  public void setOnViewMainActionListener(final ActionListener listener) {
    this.actionListener = listener;
  }

  public void setOnViewMainActionLongListener(final  ActionLongListener listener){
    this.actionLongListener=listener;
  }

  public void setTxtPasteClickListener(final OnClickListener onClickListener) {
    this.txtPaste.setOnClickListener(onClickListener);
  }

  private void updateViewLayoutByMotionEvent(MotionEvent e2) {
    mWLayoutParams.x = (int) (e2.getRawX() - getViewRoot().getMeasuredWidth() / 2);
    mWLayoutParams.y = (int) (e2.getRawY() - getViewRoot().getMeasuredHeight() / 2);
    mWindowManager.updateViewLayout(getViewRoot(), mWLayoutParams);
  }

  public void setOnWindowViewCheckChangeListener(
      OnWindowViewCheckChangeListener onWindowViewCheckChangeListener) {
    this.onWindowViewCheckChangeListener = onWindowViewCheckChangeListener;
  }

  public void setOnChangeViewData(SharedPreferences sharedPreferences) {

    boolean isComment = sharedPreferences.getBoolean(Constant.Comment_Copy, false);

    if (checkComment != null) {
      checkComment.setChecked(isComment);
    }
  }

  @IntDef({IndexRelease, IndexBack, IndexComment})
  @Retention(RetentionPolicy.SOURCE)
  public @interface Index {

  }

  public interface ActionListener {
    void onAction(View view);
  }

  public interface ActionLongListener{
    void onAction(View view);
  }

  public interface OnWindowViewCheckChangeListener {

    void onChange(@Index int index, boolean isChecked);
  }
}
