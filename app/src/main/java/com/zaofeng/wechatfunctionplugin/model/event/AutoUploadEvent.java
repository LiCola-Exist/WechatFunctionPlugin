package com.zaofeng.wechatfunctionplugin.model.event;

import android.support.annotation.IntDef;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by 李可乐 on 2017/2/5 0005.
 * 自动快速发布model 带状态
 */

public class AutoUploadEvent {

  public static final int BreakOff = 0;
  public static final int Start = 1;
  public static final int Jump = 2;
  public static final int Choose = 3;
  public static final int FillOut = 4;
  public static final int Finish = 5;
  @State
  private int state;
  private String clipText;
  public AutoUploadEvent(String clipText) {
    this.state = Start;
    this.clipText = clipText;
  }

  public
  @State
  int getState() {
    return state;
  }

  public void setState(@State int state) {
    this.state = state;
  }

  public String getClipText() {
    return clipText;
  }

  @IntDef({BreakOff, Start, Jump, Choose, FillOut, Finish})
  @Retention(RetentionPolicy.SOURCE)
  public @interface State {

  }

}
