package com.zaofeng.wechatfunctionplugin.model;

import android.support.annotation.IntDef;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by 李可乐 on 2017/5/12.
 */

public interface WeChatUIContract {

  int Unknown = 0;//未知
  int LauncherUI = 1;//主页
  int ChatUI = 2;//聊天页
  int SnsTimeLineUI = 3;//朋友圈列表页
  int SnsUploadUI = 4;//朋友圈发布页
  int SnsCommentDetailUI = 5;//朋友圈详情页
  int AlbumPreviewUI = 6;//相册选择页
  int FMessageConversationUI = 7;//新增好友列表页
  int ContactInfoUI = 8;//联系人信息页
  int SnsTimeLineMsgUI = 9;//朋友圈新消息列表页

  @IntDef({Unknown, LauncherUI, ChatUI, SnsTimeLineUI, SnsUploadUI, SnsCommentDetailUI,
      AlbumPreviewUI, FMessageConversationUI, ContactInfoUI,SnsTimeLineMsgUI})
  @Retention(RetentionPolicy.SOURCE)
  @interface StatusUI {

  }


}
