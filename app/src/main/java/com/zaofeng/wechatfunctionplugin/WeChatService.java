package com.zaofeng.wechatfunctionplugin;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.view.accessibility.AccessibilityEvent;
import com.zaofeng.wechatfunctionplugin.action.BaseAction;
import com.zaofeng.wechatfunctionplugin.action.EventAutoReplyAction;
import com.zaofeng.wechatfunctionplugin.action.MotionAutoCopyCommentAction;
import com.zaofeng.wechatfunctionplugin.action.MotionCutPasteCommentAction;
import com.zaofeng.wechatfunctionplugin.action.MotionFastBackChatAction;
import com.zaofeng.wechatfunctionplugin.action.MotionFastCopyCommentAction;
import com.zaofeng.wechatfunctionplugin.action.MotionFastReleaseLineAction;
import com.zaofeng.wechatfunctionplugin.model.Constant;
import com.zaofeng.wechatfunctionplugin.model.WeChatUIContract.StatusUI;
import com.zaofeng.wechatfunctionplugin.utils.Logger;
import com.zaofeng.wechatfunctionplugin.utils.SPUtils;

import static com.zaofeng.wechatfunctionplugin.model.ConstantTargetName.ClassAlbumPreviewUI;
import static com.zaofeng.wechatfunctionplugin.model.ConstantTargetName.ClassContactInfoUI;
import static com.zaofeng.wechatfunctionplugin.model.ConstantTargetName.ClassFMessageConversationUI;
import static com.zaofeng.wechatfunctionplugin.model.ConstantTargetName.ClassLauncherUI;
import static com.zaofeng.wechatfunctionplugin.model.ConstantTargetName.ClassSnsCommentDetailUI;
import static com.zaofeng.wechatfunctionplugin.model.ConstantTargetName.ClassSnsTimeLineUI;
import static com.zaofeng.wechatfunctionplugin.model.ConstantTargetName.ClassSnsTimeLineUploadUI;
import static com.zaofeng.wechatfunctionplugin.model.ConstantTargetName.ClassSnsTimeMsgUI;
import static com.zaofeng.wechatfunctionplugin.model.ConstantTargetName.IdButtonVoiceChat;
import static com.zaofeng.wechatfunctionplugin.model.ConstantTargetName.IdListViewChat;
import static com.zaofeng.wechatfunctionplugin.model.WeChatUIContract.AlbumPreviewUI;
import static com.zaofeng.wechatfunctionplugin.model.WeChatUIContract.ChatUI;
import static com.zaofeng.wechatfunctionplugin.model.WeChatUIContract.ContactInfoUI;
import static com.zaofeng.wechatfunctionplugin.model.WeChatUIContract.FMessageConversationUI;
import static com.zaofeng.wechatfunctionplugin.model.WeChatUIContract.SnsCommentDetailUI;
import static com.zaofeng.wechatfunctionplugin.model.WeChatUIContract.SnsTimeLineMsgUI;
import static com.zaofeng.wechatfunctionplugin.model.WeChatUIContract.SnsTimeLineUI;
import static com.zaofeng.wechatfunctionplugin.model.WeChatUIContract.SnsUploadUI;
import static com.zaofeng.wechatfunctionplugin.model.WeChatUIContract.Unknown;
import static com.zaofeng.wechatfunctionplugin.utils.AccessibilityUtils.hasViewById;

/**
 * Created by 李可乐 on 2017/2/5 0005.
 */

public class WeChatService extends AccessibilityService {

  /**
   * 基本组件
   */
  private Context mContext;
  private AccessibilityService mService;
  private WindowView mWindowView;

  @StatusUI private int statusUi;

  private MotionFastReleaseLineAction motionFastReleaseLineAction;//复制快速发布操作
  private MotionFastBackChatAction motionFastBackChatAction;//发布快速返回

  private MotionAutoCopyCommentAction motionAutoCopyCommentAction;//自动复制评论
  private MotionFastCopyCommentAction motionFastCopyCommentAction;//快速评论复制回复
  private MotionCutPasteCommentAction motionCutPasteCommentAction;//快速剪贴复制评论
  private EventAutoReplyAction eventAutoReplyAction;//自动回复

  /**
   * 系统会在成功连接上服务时候调用这个方法
   * 初始化参数和工具类
   */
  @Override protected void onServiceConnected() {
    mContext = getApplicationContext();
    mService = this;
    this.setServiceInfo(initServiceInfo());
    statusUi = Unknown;
    initOperationVariable();
    initWindowView();
    initAction();
    Logger.d();
  }

  private void initAction() {
    motionFastReleaseLineAction = new MotionFastReleaseLineAction(mContext, mWindowView, mService,
        (boolean) SPUtils.get(mContext, Constant.Release_Copy, false));

    motionFastBackChatAction = new MotionFastBackChatAction(mContext, mWindowView, mService,
        (boolean) SPUtils.get(mContext, Constant.Release_Back, false));

    eventAutoReplyAction = new EventAutoReplyAction(mContext, mWindowView, mService,
        (boolean) SPUtils.get(mContext, Constant.Release_Back, false));

    motionFastCopyCommentAction = new MotionFastCopyCommentAction(mContext, mWindowView, mService,
        (boolean) SPUtils.get(mContext, Constant.Comment_Copy, false));

    boolean isOpenCopy = (boolean) SPUtils.get(mContext, Constant.Comment_Auto, false);

    motionAutoCopyCommentAction =
        new MotionAutoCopyCommentAction(mContext, mWindowView, mService, isOpenCopy);

    motionCutPasteCommentAction =
        new MotionCutPasteCommentAction(mContext, mWindowView, mService, isOpenCopy);
  }

  private void initWindowView() {
    mWindowView = new WindowView(mContext);
    mWindowView.setOnViewMainActionListener(view -> {
      motionAutoCopyCommentAction.action(BaseAction.Step0, statusUi, null);
    });

    mWindowView.setOnViewMainActionLongListener(view -> {
      motionCutPasteCommentAction.action(BaseAction.Step0, statusUi, null);
    });

    mWindowView.setTxtPasteClickListener(view -> {
      motionCutPasteCommentAction.action(BaseAction.Step1, statusUi, null);
    });

    mWindowView.setOnWindowViewCheckChangeListener(
        new WindowView.OnWindowViewCheckChangeListener() {

          @Override public void onChange(@WindowView.Index int index, boolean isChecked) {
            String key = null;
            switch (index) {
              case WindowView.IndexRelease:
                motionFastReleaseLineAction.setOpen(isChecked);
                key = Constant.Release_Copy;
                break;
              case WindowView.IndexBack:
                motionFastBackChatAction.setOpen(isChecked);
                key = Constant.Release_Back;
                break;
              case WindowView.IndexComment:
                motionFastCopyCommentAction.setOpen(isChecked);
                key = Constant.Comment_Copy;
                break;
            }
            if (key != null) {
              SPUtils.putApply(mContext, key, isChecked);
            }
          }
        });
  }

  /**
   * SP数据监听器实例
   */
  private SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener =
      new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
          Logger.d("key=" + key);
          if (key.equals(Constant.Release_Copy)) {
            motionFastReleaseLineAction.setOpen(
                sharedPreferences.getBoolean(Constant.Release_Copy, false));
          } else if (key.equals(Constant.Release_Back)) {
            motionFastBackChatAction.setOpen(
                sharedPreferences.getBoolean(Constant.Release_Back, false));
          } else if (key.equals(Constant.Quick_Offline)) {
            eventAutoReplyAction.setOpen(
                sharedPreferences.getBoolean(Constant.Quick_Offline, false));
          } else if (key.equals(Constant.Comment_Copy)) {
            motionFastCopyCommentAction.setOpen(
                sharedPreferences.getBoolean(Constant.Comment_Copy, false));
          } else if (key.equals(Constant.Comment_Auto)) {
            motionAutoCopyCommentAction.setOpen(
                sharedPreferences.getBoolean(Constant.Comment_Auto, false));
          }

          if (mWindowView != null) {
            mWindowView.setOnChangeViewData(sharedPreferences);
          }
        }
      };

  @Override public void onInterrupt() {
    SPUtils.getSharedPreference(mContext)
        .unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
    Logger.d("服务中断，如授权关闭或者将服务杀死");
    mWindowView.removeView();
  }

  @Override public boolean onUnbind(Intent intent) {
    Logger.d("服务被解绑");
    mWindowView.removeView();
    return super.onUnbind(intent);
  }

  @NonNull private AccessibilityServiceInfo initServiceInfo() {
    AccessibilityServiceInfo info = new AccessibilityServiceInfo();
    info.eventTypes = getEventTypes();//响应的事件类型
    info.packageNames = new String[] { "com.tencent.mm" };//响应的包名
    info.feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK;//反馈类型
    info.flags = AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
    info.notificationTimeout = 150;//响应时间
    return info;
  }

  private int getEventTypes() {
    return AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
        | AccessibilityEvent.TYPE_VIEW_FOCUSED
        //| AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        | AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
        | AccessibilityEvent.TYPE_VIEW_CLICKED
        | AccessibilityEvent.TYPE_VIEW_LONG_CLICKED
        | AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED;
  }

  private void initOperationVariable() {

    SPUtils.getSharedPreference(mContext)
        .registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
  }

  /**
   * 接收Accessibility事件方法
   */
  @Override public void onAccessibilityEvent(AccessibilityEvent event) {
    Logger.d("event date = " + event.toString());
    int type = event.getEventType();
    String className = event.getClassName().toString();
    String text = event.getText().isEmpty() ? Constant.Empty : event.getText().get(0).toString();
    switch (type) {
      case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED://窗口状态变化事件
        switch (className) {
          case ClassLauncherUI:
            Logger.d("正在主页或聊天页");
            statusUi = ChatUI;
            eventAutoReplyAction.action(BaseAction.Step1, statusUi, event);
            break;
          case ClassAlbumPreviewUI:
            Logger.d("正在相册选择页");
            statusUi = AlbumPreviewUI;
            if (motionFastReleaseLineAction.action(BaseAction.Step2, statusUi, event)) {
              return;
            }
            break;
          case ClassSnsTimeLineUI:
            Logger.d("正在朋友圈页");
            statusUi = SnsTimeLineUI;

            if (motionFastReleaseLineAction.action(BaseAction.Step1, statusUi, event)) {
              return;
            }

            break;
          case ClassSnsCommentDetailUI:
            Logger.d("正在朋友圈评论详情页");
            statusUi = SnsCommentDetailUI;

            break;
          case ClassFMessageConversationUI:
            Logger.d("正在新朋友功能列表");
            statusUi = FMessageConversationUI;

            break;
          case ClassSnsTimeLineUploadUI:
            statusUi = SnsUploadUI;
            if (motionFastReleaseLineAction.action(BaseAction.Step3, statusUi, event)) {
              return;
            }
            break;
          case ClassContactInfoUI:
            Logger.d("正在好友详细资料页");
            statusUi = ContactInfoUI;
            break;
          case ClassSnsTimeMsgUI:
            Logger.d("正在朋友圈新消息页");
            statusUi = SnsTimeLineMsgUI;
            break;
        }
        break;

      case AccessibilityEvent.TYPE_VIEW_FOCUSED:
        if (className.equals("android.widget.ListView")) {
          if (hasViewById(mService, IdListViewChat)) {
            Logger.d("正在聊天页");
            statusUi = ChatUI;
          }
        }
        break;
      case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED://窗口内容变化事件
        if (className.equals("android.widget.TextView")) {
          if (hasInputBox()) {
            statusUi = ChatUI;
          }
        }

        break;
      case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED://通知事件 toast也包括
        if (className.equals("android.widget.Toast$TN") && "已复制".equals(text)) {
          if (motionFastCopyCommentAction.action(BaseAction.Step1, statusUi, event)) {
            return;
          }
          if (motionFastReleaseLineAction.action(BaseAction.Step0, statusUi, event)) {
            return;
          }
        } else if ((className.equals("android.app.Notification") && (!text.isEmpty()))) {
          eventAutoReplyAction.action(BaseAction.Step0, statusUi, event);
        }

        break;
      case AccessibilityEvent.TYPE_VIEW_CLICKED://点击事件
        if ("发送".equals(text)) {
          if (motionFastBackChatAction.action(BaseAction.Step0, statusUi, event)) {
            return;
          }
        }
        break;

      case AccessibilityEvent.TYPE_VIEW_LONG_CLICKED:
        if (motionFastCopyCommentAction.action(BaseAction.Step0, statusUi, event)) {
          return;
        }

        break;
      case AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED:
        if (motionFastCopyCommentAction.action(BaseAction.Step2, statusUi, event)) {
          return;
        }

        break;
    }
    Logger.d("statusUi=" + statusUi);
  }

  private boolean hasInputBox() {
    if (hasViewById(mService, IdButtonVoiceChat)) {
      return true;
    }
    return false;
  }
}
