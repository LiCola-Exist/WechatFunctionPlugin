package com.zaofeng.wechatfunctionplugin.utils;

import android.os.Bundle;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.List;

/**
 * Created by 李可乐 on 2017/2/5 0005.
 */

public class PerformUtils {


  public static boolean performAction(AccessibilityNodeInfo info) {
    return performAction(info, AccessibilityNodeInfo.ACTION_CLICK);
  }

  /**
   * 执行动作 包含非空检查
   *
   * @param info 目标对象
   * @param action 行为
   * @return false 执行失败或者info为空 true if the action was performed
   */
  public static boolean performAction(AccessibilityNodeInfo info, int action) {
    return info != null && info.performAction(action);
  }

  public static boolean performAction(AccessibilityNodeInfo info, int action, Bundle arguments) {
    return info != null && info.performAction(action, arguments);
  }

  /**
   * 滚动控件 是否滚动到头部 即不包含向后backward动作
   */
  public static boolean checkScrollViewTop(AccessibilityNodeInfo info) {
    return !containAction(info, AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_BACKWARD);
  }

  /**
   * 滚动控件 是否滚动到底部 即不包含向前forward动作
   *
   * @return true 滚动到底部 false 还没有到底
   */
  public static boolean checkScrollViewBottom(AccessibilityNodeInfo info) {
    return !containAction(info, AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD);
  }

  /**
   * 检查info是否包含某个行为
   */
  public static boolean containAction(AccessibilityNodeInfo info,
      AccessibilityNodeInfo.AccessibilityAction action) {
    if (info==null)return false;
    List<AccessibilityNodeInfo.AccessibilityAction> listAction = info.getActionList();
    for (AccessibilityNodeInfo.AccessibilityAction item : listAction) {
      if (item.equals(action)) {
        return true;
      }
    }
    return false;
  }


}
