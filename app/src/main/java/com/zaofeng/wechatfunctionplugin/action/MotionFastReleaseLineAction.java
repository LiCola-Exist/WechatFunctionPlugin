package com.zaofeng.wechatfunctionplugin.action;

import static com.zaofeng.wechatfunctionplugin.model.ConstantData.delayTime;
import static com.zaofeng.wechatfunctionplugin.model.ConstantTargetName.IdEditTimeLineUpload;
import static com.zaofeng.wechatfunctionplugin.model.WeChatUIContract.AlbumPreviewUI;
import static com.zaofeng.wechatfunctionplugin.model.WeChatUIContract.ChatUI;
import static com.zaofeng.wechatfunctionplugin.model.WeChatUIContract.SnsTimeLineUI;
import static com.zaofeng.wechatfunctionplugin.model.WeChatUIContract.SnsUploadUI;
import static com.zaofeng.wechatfunctionplugin.utils.AccessibilityUtils.findViewById;
import static com.zaofeng.wechatfunctionplugin.utils.AccessibilityUtils.findViewClickByText;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import com.zaofeng.wechatfunctionplugin.WindowView;
import com.zaofeng.wechatfunctionplugin.model.WeChatUIContract.StatusUI;
import com.zaofeng.wechatfunctionplugin.utils.Logger;
import com.zaofeng.wechatfunctionplugin.utils.LoggerHelp;
import com.zaofeng.wechatfunctionplugin.utils.PerformUtils;

/**
 * Created by 李可乐 on 2017/5/13.
 */

public class MotionFastReleaseLineAction extends BaseAction {

  public static final int Idle = 0;
  public static final int Jump = 1;
  public static final int Start = 2;
  public static final int Wait = 3;

  private int status = Idle;

  public MotionFastReleaseLineAction(Context mContext,
      WindowView mWindowView,
      AccessibilityService mService, boolean isOpen) {
    super(mContext, mWindowView, mService, isOpen);
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
      case BaseAction.Step0:
        //该功能只在聊天界面开始生效
        if (status == Idle && statusUi == ChatUI) {
          autoToUploadTimeLine();
          status = Start;
        }
        break;
      case BaseAction.Step1:
        if (status == Start && statusUi == SnsTimeLineUI) {
          autoToUploadToChooseTimeLine();
          status = Jump;
        }
        break;
      case BaseAction.Step2:
        if (status == Jump && statusUi == AlbumPreviewUI) {
          autoUploadWaitChoose();
          status = Wait;
        }
        break;
      case BaseAction.Step3:
        if (status == Wait && statusUi == SnsUploadUI) {
          autoUploadFillOutTimeLine();
          status = Idle;
        }
        break;
    }

    return true;
  }

  /**
   * 第一步 跳转
   */
  private void autoToUploadTimeLine() {

    PerformUtils.performAction(findViewClickByText(mService, "返回"));
    PerformUtils.performAction(findViewClickByText(mService, "发现"));
    //微信应该做了防抖动处理 所以需要延迟后执行
    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        PerformUtils.performAction(findViewClickByText(mService, "朋友圈"));
      }
    }, delayTime);
  }

  /**
   * 第二步打开朋友圈发布功能 并选择从相册开始
   */
  private void autoToUploadToChooseTimeLine() {

    PerformUtils.performAction(findViewClickByText(mService, "更多功能按钮"));

    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        PerformUtils.performAction(findViewClickByText(mService, "从相册选择"));
      }
    }, delayTime);
  }


  /**
   * 第三步 等待用户选择 图片
   */
  private void autoUploadWaitChoose() {

  }

  /**
   * 第四步 填写粘贴板的内容到输入框 并结束
   */
  private void autoUploadFillOutTimeLine() {
    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        PerformUtils.performAction(findViewById(mService, IdEditTimeLineUpload),
            AccessibilityNodeInfo.ACTION_PASTE);
      }
    }, delayTime);

  }
}
