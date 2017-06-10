package com.zaofeng.wechatfunctionplugin.action;

import static com.zaofeng.wechatfunctionplugin.model.ConstantData.delayTime;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.IntDef;
import android.support.annotation.StringRes;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;
import com.zaofeng.wechatfunctionplugin.WindowView;
import com.zaofeng.wechatfunctionplugin.model.WeChatUIContract.StatusUI;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;

/**
 * Created by 李可乐 on 2017/5/13.
 */

public abstract class BaseAction {

  public static final int Step0 = 0;
  public static final int Step1 = 1;
  public static final int Step2 = 2;
  public static final int Step3 = 3;
  public static final int Step4 = 4;

  @IntDef({Step0, Step1, Step2, Step3, Step4})
  @Retention(RetentionPolicy.SOURCE)
  public @interface Step {

  }

  Context mContext;
  WindowView mWindowView;
  AccessibilityService mService;
  boolean isOpen;

  protected static final Handler handler = new Handler();

  public BaseAction(Context mContext, WindowView mWindowView,
      AccessibilityService mService, boolean isOpen) {
    this.mContext = mContext;
    this.mWindowView = mWindowView;
    this.mService = mService;
    this.isOpen = isOpen;
  }


  public abstract boolean action(@Step int step, @StatusUI int statusUi, AccessibilityEvent event);

  public void setOpen(boolean open) {
    isOpen = open;
  }

  protected void showToast(CharSequence charSequence) {
    Toast.makeText(mContext, charSequence, Toast.LENGTH_SHORT).show();
  }

  protected void showToast(@StringRes int resId) {
    Toast.makeText(mContext, resId, Toast.LENGTH_SHORT).show();
  }

  protected void performActionDelayed(Runnable... runnables) {
    int position = 0;
    for (Runnable runnable :
        runnables) {
      handler.postDelayed(runnable, delayTime * position++);
    }
  }

  protected void performAction(Runnable... runnables) {
    for (Runnable runnable :
        runnables) {
      handler.post(runnable);
    }
  }
}
