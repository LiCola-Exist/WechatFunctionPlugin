package com.zaofeng.wechatfunctionplugin.model;

import android.support.annotation.IntDef;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by 李可乐 on 2017/5/12.
 */

public interface WeChatUIContract {

  int Unknown = 0;//未知
  int LauncherUI = 1;
  int ChatUI = 2;
  int SnsTimeLineUI = 3;
  int SnsUploadUI = 4;
  int SnsCommentDetailUI = 5;
  int AlbumPreviewUI = 6;
  int FMessageConversationUI = 7;
  int ContactInfoUI = 8;

  @IntDef({Unknown, LauncherUI, ChatUI, SnsTimeLineUI, SnsUploadUI, SnsCommentDetailUI,
      AlbumPreviewUI, FMessageConversationUI, ContactInfoUI})
  @Retention(RetentionPolicy.SOURCE)
  @interface StatusUI {

  }


}
