package com.zaofeng.wechatfunctionplugin.model.event;

import android.support.annotation.IntDef;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by 李可乐 on 2017/2/5 0005.
 * 自动快捷回复新增好友 带状态
 */

public class FastNewFriendReplyEvent {

  public static final int BreakOff = 0;
  public static final int Start = 1;
  public static final int FillOut = 2;
  public static final int Finish = 5;
  private int state;
  private String ReplyContent;
  public FastNewFriendReplyEvent(String replyContent) {
    state = Start;
    ReplyContent = replyContent;
  }

  public
  @State
  int getState() {
    return state;
  }

  public void setState(@State int state) {
    this.state = state;
  }

  public String getReplyContent() {
    return ReplyContent;
  }

  @IntDef({BreakOff, Start, FillOut, Finish})
  @Retention(RetentionPolicy.SOURCE)
  public @interface State {

  }
}
