package com.zaofeng.wechatfunctionplugin.action;

import static com.zaofeng.wechatfunctionplugin.model.ConstantData.delayTime;
import static com.zaofeng.wechatfunctionplugin.model.ConstantTargetName.IdTextViewBottomMain;
import static com.zaofeng.wechatfunctionplugin.model.WeChatUIContract.SnsUploadUI;
import static com.zaofeng.wechatfunctionplugin.utils.AccessibilityUtils.findViewClickByText;
import static com.zaofeng.wechatfunctionplugin.utils.AccessibilityUtils.findViewListById;
import static com.zaofeng.wechatfunctionplugin.utils.AccessibilityUtils.forNodeInfoByClick;

import android.accessibilityservice.AccessibilityService;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import com.zaofeng.wechatfunctionplugin.WindowView;
import com.zaofeng.wechatfunctionplugin.model.WeChatUIContract.StatusUI;
import com.zaofeng.wechatfunctionplugin.model.Constant;
import com.zaofeng.wechatfunctionplugin.utils.Logger;
import com.zaofeng.wechatfunctionplugin.utils.LoggerHelp;
import com.zaofeng.wechatfunctionplugin.utils.PerformUtils;
import com.zaofeng.wechatfunctionplugin.utils.SPUtils;
import java.util.List;

/**
 * Created by 李可乐 on 2017/5/13.
 * 行为触发
 * 发布朋友圈成功后快速返回并设置粘贴板内容
 */

public class MotionFastBackChatAction  extends BaseAction{

  public static final int Idle = 0;
  public static final int Jump = 1;

  private int status = Idle;

  private ClipboardManager mClipboardManager;

  public MotionFastBackChatAction(Context mContext,
      WindowView mWindowView,
      AccessibilityService mService, boolean isOpen) {
    super(mContext, mWindowView, mService, isOpen);
    mClipboardManager = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
  }

  @Override
  public boolean action(@Step int step, @StatusUI int statusUi, AccessibilityEvent event) {
    Logger.d("被调用:" + step + " 页面信息：" + LoggerHelp.getDesc(statusUi));

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
      //该功能只在朋友圈发布页生效
      case BaseAction.Step0:
        if (status==Idle&&statusUi==SnsUploadUI){
          autoBackChat();
          status=Jump;
        }
        break;
    }

    return true;
  }

  private void autoBackChat() {
    PerformUtils.performAction(findViewClickByText(mService, "返回"));

    handler.postDelayed(() -> {
      List<AccessibilityNodeInfo> listMain = findViewListById(mService, IdTextViewBottomMain);
      for (AccessibilityNodeInfo item : listMain) {
        if (item.getText().equals("微信")) {
          PerformUtils.performAction(forNodeInfoByClick(item));
          String data= (String) SPUtils.get(mContext, Constant.Release_Reply_Content,"");
          mClipboardManager.setPrimaryClip(ClipData.newPlainText(null,data));
          showToast("已经把回复文字复制到粘贴板");
          return;
        }
      }
    }, delayTime);
  }


}
