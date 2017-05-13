package com.zaofeng.wechatfunctionplugin.action;

import static android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK;
import static com.zaofeng.wechatfunctionplugin.model.ConstantData.delayTime;
import static com.zaofeng.wechatfunctionplugin.model.ConstantTargetName.IdButtonComment;
import static com.zaofeng.wechatfunctionplugin.model.ConstantTargetName.IdButtonMenuComment;
import static com.zaofeng.wechatfunctionplugin.model.ConstantTargetName.IdEditTimeLineComment;
import static com.zaofeng.wechatfunctionplugin.model.WeChatUIContract.SnsCommentDetailUI;
import static com.zaofeng.wechatfunctionplugin.model.WeChatUIContract.SnsTimeLineUI;
import static com.zaofeng.wechatfunctionplugin.utils.AccessibilityUtils.findViewById;
import static com.zaofeng.wechatfunctionplugin.utils.AccessibilityUtils.findViewClickById;
import static com.zaofeng.wechatfunctionplugin.utils.AccessibilityUtils.findViewClickByText;
import static com.zaofeng.wechatfunctionplugin.utils.AccessibilityUtils.findViewListById;
import static com.zaofeng.wechatfunctionplugin.utils.AccessibilityUtils.forNodeInfoByClick;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.graphics.Rect;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import com.zaofeng.wechatfunctionplugin.WindowView;
import com.zaofeng.wechatfunctionplugin.model.WeChatUIContract.StatusUI;
import com.zaofeng.wechatfunctionplugin.utils.PerformUtils;
import java.util.List;

/**
 * Created by 李可乐 on 2017/5/13.
 * 行为触发
 * 朋友圈评论复制后快速回复
 */

public class MotionFastCopyCommentAction extends BaseAction {

  public static final int Idle = 0;
  public static final int Start = 1;
  public static final int Find = 2;

  private int status = Idle;
  private Rect eventRect;//事件开始的范围

  public MotionFastCopyCommentAction(Context mContext, WindowView mWindowView,
      AccessibilityService mService, boolean isOpen) {
    super(mContext, mWindowView, mService, isOpen);
  }


  @Override
  public boolean action(@Step int step, @StatusUI int statusUi, AccessibilityEvent event) {
    //拦截非目标页响应动作
    if (statusUi != SnsTimeLineUI && statusUi != SnsCommentDetailUI) {
      return false;
    }

    if (!isOpen) {
//      shouToast("请开启朋友圈复制快捷回复功能");
      return false;
    }

    switch (step) {
      case BaseAction.Step0:
        if (status == Idle) {
          //空闲状态
          //根据处理结果决定是否进入下一状态
          eventRect = null;
          status = autoCopySaveRect(event) ? Start : Idle;

        }
        break;
      case BaseAction.Step1:
        if (status == Start) {
          if (statusUi == SnsCommentDetailUI) {
            autoDetailCopyFillOut();
            eventRect = null;
            status = Idle;
          }

          if (statusUi == SnsTimeLineUI) {
            autoCopyFindViewAndCopy();
            status = Find;
          }
        }
        break;
      case BaseAction.Step2:
        if (status == Find) {
          autoCopyFillOut();
          eventRect = null;
          status = Idle;
        }
        break;
    }

    return true;
  }


  private boolean autoCopySaveRect(AccessibilityEvent event) {

    /**
     * 拦截朋友圈非目标（评论正文）view的长按事件
     * 根据分析 朋友圈的内容正文和评论正文 差别 评论正文包含点击行为
     */
    if (!PerformUtils
        .containAction(event.getSource(),
            AccessibilityNodeInfo.AccessibilityAction.ACTION_CLICK)) {
      return false;
    }

    eventRect = new Rect();
    event.getSource().getBoundsInScreen(eventRect);
    return true;
  }

  /**
   * 详情页
   * 自动填写 在朋友圈详情页需要处理 输入框自带的回复某人
   * 使用获取焦点后返回 取消回复某人为评论
   */
  private void autoDetailCopyFillOut() {

    final AccessibilityNodeInfo nodeInfo = findViewById(mService, IdEditTimeLineComment);

    //微信应该做了防抖动处理 所以需要延迟后执行
    int position = 0;
    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        PerformUtils.performAction(nodeInfo);
      }
    }, delayTime * position++);

    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        mService.performGlobalAction(GLOBAL_ACTION_BACK);
      }
    }, delayTime * position++);

    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        PerformUtils.performAction(nodeInfo, AccessibilityNodeInfo.ACTION_PASTE);
      }
    }, delayTime * position++);

    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        PerformUtils.performAction(findViewClickByText(mService, "发送"));
      }
    }, delayTime * position);
  }


  /**
   * 列表页
   * 根据上一步保存的点击事件响应范围 查找之上最近的评论（即该条消息的评论）按钮并点击
   * 通过距离查找是因为 微信在朋友圈评论里读取不到一条条的评论内容 只能通过控件范围距离判断
   */
  private void autoCopyFindViewAndCopy() {

    List<AccessibilityNodeInfo> list = findViewListById(mService, IdButtonComment);
    Rect rectTarget = eventRect;
    Rect rectItem = new Rect();

    AccessibilityNodeInfo info = null;
    for (AccessibilityNodeInfo item : list) {
      if (info == null) {
        info = item;
      }
      info.getBoundsInScreen(rectItem);
      int infoMargin = rectTarget.top - rectItem.top;
      item.getBoundsInScreen(rectItem);
      int itemMargin = rectTarget.top - rectItem.top;

      if (itemMargin > 0 && (itemMargin < infoMargin)) {
        info = item;
      }
    }

    PerformUtils.performAction(forNodeInfoByClick(info));
    PerformUtils.performAction(findViewClickById(mService, IdButtonMenuComment));
  }

  /**
   * 列表页
   * 自动填写
   */
  private void autoCopyFillOut() {

    final AccessibilityNodeInfo nodeInfo = findViewById(mService, IdEditTimeLineComment);
    //微信应该做了防抖动处理 所以需要延迟后执行
    int position = 0;
    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        PerformUtils.performAction(nodeInfo, AccessibilityNodeInfo.ACTION_FOCUS);
      }
    }, delayTime * position++);

    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        PerformUtils.performAction(nodeInfo, AccessibilityNodeInfo.ACTION_PASTE);
      }
    }, delayTime * position++);

    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        PerformUtils.performAction(findViewClickByText(mService, "发送"));
      }
    }, delayTime * position);
  }
}
