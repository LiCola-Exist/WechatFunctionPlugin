package com.zaofeng.wechatfunctionplugin.utils;

import com.zaofeng.wechatfunctionplugin.model.WeChatUIContract;

/**
 * Created by 李可乐 on 2017/5/13.
 */

public class LoggerHelp {

  public static String getDesc(@WeChatUIContract.StatusUI int status){
    String desc="缺省";
    switch (status) {

      case WeChatUIContract.AlbumPreviewUI:
        desc="相册页";
        break;
      case WeChatUIContract.ChatUI:
        desc="聊天页";
        break;
      case WeChatUIContract.ContactInfoUI:
        desc="联系人信息";
        break;
      case WeChatUIContract.FMessageConversationUI:
        desc="未知";
        break;
      case WeChatUIContract.LauncherUI:
        desc="启动主页";
        break;
      case WeChatUIContract.SnsCommentDetailUI:
        desc="朋友圈详情页";
        break;
      case WeChatUIContract.SnsTimeLineUI:
        desc="朋友圈列表页";
        break;
      case WeChatUIContract.SnsUploadUI:
        desc="朋友圈发布页";
        break;
      case WeChatUIContract.Unknown:
        desc="未知";
        break;
    }

    return desc;
  }
}
