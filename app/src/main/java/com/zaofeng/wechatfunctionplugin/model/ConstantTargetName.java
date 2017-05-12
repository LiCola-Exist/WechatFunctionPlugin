package com.zaofeng.wechatfunctionplugin.model;

/**
 * Created by 李可乐 on 2017/5/12.
 */

public interface ConstantTargetName {

  String IdListViewTimeLineCommentDetail = "com.tencent.mm:id/ckv";//朋友圈详情页 ListView
  String IdLayoutTimeLineDetailListItem = "com.tencent.mm:id/clk";//朋友圈评论详情页 ListView的itemLayout
  String IdTextViewTimeLineDetailItemName="com.tencent.mm:id/clo";//朋友圈详情 列表项 用户名
  String IdTextViewTimeLineDetailItemContent="com.tencent.mm:id/clq";//朋友圈详情 列表项 评论内容
  String IdEditTimeLineComment = "com.tencent.mm:id/cls";//朋友圈评论 输入框



  String IdListViewChat = "com.tencent.mm:id/a22";//聊天页 ListView
  String IdListViewTimeLine = "com.tencent.mm:id/cn0";//朋友圈 ListView

  String IdListViewFMessageConversation = "com.tencent.mm:id/auv";//新的朋友 ListView
  String IdEditTimeLineUpload = "com.tencent.mm:id/cpe";//朋友圈发布页 输入框
  String IdEditChat = "com.tencent.mm:id/a2v";//聊天页 输入框
  String IdTextTimeLineContent = "com.tencent.mm:id/co3";//朋友圈主页 文字列表item
  String IdTextTimeLineAuthor = "com.tencent.mm:id/afv";//朋友圈详情页 作者名称
  String IdTextTimeLineCommentTitle = "com.tencent.mm:id/cjk";//朋友圈详情页 评论的标题 包含作者和被回复者
  String IdTextTimeLineCommentContent = "com.tencent.mm:id/cjm";//朋友圈详情页 评论的内容

  String IdButtonBottomMain = "com.tencent.mm:id/bq0";//微信主页 底部4个主按钮id
  String IdButtonTimeLine = "com.tencent.mm:id/f_";//朋友圈 发布按钮
  String IdButtonSend = "com.tencent.mm:id/a31";//聊天页 发送按钮
  String IdButtonComment = "com.tencent.mm:id/cj9";//朋友圈 评论按钮
  String IdButtonMenuComment = "com.tencent.mm:id/cj8";//朋友圈 评论弹出菜单的评论按钮


  String ClassLauncherUI = "com.tencent.mm.ui.LauncherUI";//主页 聊天页有时会发布和主页一样事件
  String ClassChattingUI = "com.tencent.mm.ui.chatting.ChattingUI";//聊天页
  String ClassSnsUploadUI = "com.tencent.mm.plugin.sns.ui.SnsUploadUI";//朋友圈发布页
  String ClassSnsTimeLineUI = "com.tencent.mm.plugin.sns.ui.En_424b8e16";//朋友圈页
  String ClassSnsCommentDetailUI = "com.tencent.mm.plugin.sns.ui.SnsCommentDetailUI";//朋友圈评论详情页
  String ClassAlbumPreviewUI = "com.tencent.mm.plugin.gallery.ui.AlbumPreviewUI";//图片和视频选择列表
  String ClassBizConversationUI = "com.tencent.mm.ui.conversation.BizConversationUI";//订阅号会话列表
  String ClassFMessageConversationUI = "com.tencent.mm.plugin.subapp.ui.friend.FMessageConversationUI";//新朋友功能列表
  String ClassContactInfoUI = "com.tencent.mm.plugin.profile.ui.ContactInfoUI";//好友详细资料页
}
