package com.zaofeng.wechatfunctionplugin.action;

import static com.zaofeng.wechatfunctionplugin.model.ConstantTargetName.IdEditChat;
import static com.zaofeng.wechatfunctionplugin.model.ConstantTargetName.IdListViewChatItemContent;
import static com.zaofeng.wechatfunctionplugin.model.ConstantTargetName.IdTextChatItemContent;
import static com.zaofeng.wechatfunctionplugin.utils.AccessibilityUtils.findViewById;
import static com.zaofeng.wechatfunctionplugin.utils.AccessibilityUtils.findViewClickByText;
import static com.zaofeng.wechatfunctionplugin.utils.AccessibilityUtils.findViewListById;

import android.accessibilityservice.AccessibilityService;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import com.zaofeng.wechatfunctionplugin.WindowView;
import com.zaofeng.wechatfunctionplugin.model.WeChatUIContract;
import com.zaofeng.wechatfunctionplugin.model.WeChatUIContract.StatusUI;
import com.zaofeng.wechatfunctionplugin.model.Constant;
import com.zaofeng.wechatfunctionplugin.utils.Logger;
import com.zaofeng.wechatfunctionplugin.utils.LoggerHelp;
import com.zaofeng.wechatfunctionplugin.utils.PerformUtils;
import com.zaofeng.wechatfunctionplugin.utils.SPUtils;
import java.util.List;

/**
 * Created by 李可乐 on 2017/5/12.
 * 事件触发
 * 自动回复
 */

public class EventAutoReplyAction extends BaseAction {

  private ClipboardManager mClipboardManager;
  private KeyguardManager mKeyguardManager;


  public static final int Idle = 0;
  public static final int Jump = 1;

  private int status = Idle;

  public EventAutoReplyAction(Context mContext,
      WindowView mWindowView,
      AccessibilityService mService, boolean isOpen) {
    super(mContext, mWindowView, mService, isOpen);
    mClipboardManager = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
    mKeyguardManager = (KeyguardManager) mContext.getSystemService(Context.KEYGUARD_SERVICE);
  }

  @Override
  public boolean action(@Step int step, @StatusUI int statusUi, AccessibilityEvent event) {
    Logger.d("被调用:" + step + " 页面信息：" + LoggerHelp.getDesc(statusUi));

    if(true){
      return false;
    }

    if (!isOpen) {
      return false;
    }

    /**
     * 修正第一步 重试
     */
    if (step == Step0) {
      status = Idle;
    }

    switch (step) {
      case BaseAction.Step0:
        if (status == Idle) {
          handleNotification(event);
          status = Jump;
        }
        break;
      case BaseAction.Step1:
        if (status == Jump && statusUi == WeChatUIContract.ChatUI) {
          autoReplyText();
          status = Idle;
        }
        break;
    }

    return true;
  }

  private void autoReplyText() {

    String data = (String) SPUtils.get(mContext, Constant.Quick_Offline_Content, "");
    AccessibilityNodeInfo groupInfo = findViewById(mService, IdListViewChatItemContent);
    List<AccessibilityNodeInfo> infoList = findViewListById(groupInfo, IdTextChatItemContent);

    for (AccessibilityNodeInfo itemInfo :
        infoList) {
      if (data.equals(itemInfo.getText().toString())) {
        showToast("已经回复过");
        mService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
        return;
      }
    }

    mClipboardManager.setPrimaryClip(ClipData.newPlainText(null, data));

    final AccessibilityNodeInfo nodeInfo = findViewById(mService, IdEditChat);
    performActionDelayed(
        () -> PerformUtils.performAction(nodeInfo, AccessibilityNodeInfo.ACTION_FOCUS),
        () -> PerformUtils.performAction(nodeInfo, AccessibilityNodeInfo.ACTION_PASTE),
        () -> PerformUtils.performAction(findViewClickByText(mService, "发送")),
        () -> mService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
    );

  }

  private void handleNotification(AccessibilityEvent event) {

    if (isScreenLock()) {
      //获取电源管理器对象
//      PowerManager powerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
//      PowerManager.WakeLock wakeLock=powerManager.newWakeLock((PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_BRIGHT_WAKE_LOCK), "bright");
//      //点亮屏幕
//      wakeLock.acquire(10000);
//
//      //得到键盘锁管理器对象
//      KeyguardManager km = (KeyguardManager) mContext.getSystemService(Context.KEYGUARD_SERVICE);
//      KeyguardLock kl = km.newKeyguardLock("unLock");
//
//      //解锁
//      kl.disableKeyguard();

      sendToNotification(event);
    } else {
      sendToNotification(event);
    }


  }

  private void sendToNotification(AccessibilityEvent event) {
    Notification notification = null;
    if (event.getParcelableData() instanceof Notification) {
      notification = (Notification) event.getParcelableData();
    }
    if (notification == null) {
      return;
    }
    PendingIntent pendingIntent = notification.contentIntent;
    try {
      pendingIntent.send();
    } catch (PendingIntent.CanceledException e) {
      Logger.d(e.toString());
      e.printStackTrace();
    }
  }

  private boolean isScreenLock() {
    return mKeyguardManager.inKeyguardRestrictedInputMode();
  }
}
