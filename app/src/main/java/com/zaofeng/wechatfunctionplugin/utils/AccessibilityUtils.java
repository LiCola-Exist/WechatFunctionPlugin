package com.zaofeng.wechatfunctionplugin.utils;

import android.accessibilityservice.AccessibilityService;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.Collections;
import java.util.List;

/**
 * Created by 李可乐 on 2017/2/13 0013.
 * AccessibilityNodeInfo 的控件节点info相关工具类
 */

public class AccessibilityUtils {

  public static boolean hasViewById(AccessibilityService service, String id) {
    AccessibilityNodeInfo info = service.getRootInActiveWindow();
    return hasNodeInfosByViewId(info, id);
  }

  public static boolean hasViewById(AccessibilityNodeInfo info, String id) {
    return hasNodeInfosByViewId(info, id);
  }

  public static List<AccessibilityNodeInfo> findViewListById(AccessibilityService service,
      String id) {
    AccessibilityNodeInfo info = service.getRootInActiveWindow();
    return getListAccessibilityNodeInfos(info, id, true);
  }

  public static List<AccessibilityNodeInfo> findViewListById(AccessibilityNodeInfo info,
      String id) {
    return getListAccessibilityNodeInfos(info, id, true);
  }

  public static List<AccessibilityNodeInfo> findViewListByText(AccessibilityService service,
      String text) {
    AccessibilityNodeInfo info = service.getRootInActiveWindow();
    return getListAccessibilityNodeInfos(info, text, false);
  }

  public static List<AccessibilityNodeInfo> findViewListByText(AccessibilityNodeInfo info,
      String text) {
    return getListAccessibilityNodeInfos(info, text, false);
  }


  public static AccessibilityNodeInfo findViewById(AccessibilityService service, String id) {
    AccessibilityNodeInfo info = service.getRootInActiveWindow();
    return getAccessibilityNodeInfo(info, id, true);
  }

  public static AccessibilityNodeInfo findViewById(AccessibilityNodeInfo info, String id) {
    return getAccessibilityNodeInfo(info, id, true);
  }

  public static AccessibilityNodeInfo findViewByText(AccessibilityService service, String text) {
    AccessibilityNodeInfo info = service.getRootInActiveWindow();
    return getAccessibilityNodeInfo(info, text, false);
  }

  public static AccessibilityNodeInfo findViewByText(AccessibilityNodeInfo info, String text) {
    return getAccessibilityNodeInfo(info, text, false);
  }


  public static AccessibilityNodeInfo findViewClickById(AccessibilityService service, String id) {
    AccessibilityNodeInfo info = service.getRootInActiveWindow();
    return getAccessibilityClickNodeInfo(info, id, true);
  }

  public static AccessibilityNodeInfo findViewClickByText(AccessibilityService service,
      String text) {
    AccessibilityNodeInfo info = service.getRootInActiveWindow();
    return getAccessibilityClickNodeInfo(info, text, false);
  }

  public static AccessibilityNodeInfo findViewClickById(AccessibilityNodeInfo info, String Id) {
    return getAccessibilityClickNodeInfo(info, Id, true);
  }

  public static AccessibilityNodeInfo findViewClickByText(AccessibilityNodeInfo info, String text) {
    return getAccessibilityClickNodeInfo(info, text, false);
  }


  /**
   * 遍历得到可以点击的节点 向上（父节点）遍历
   */
  public static AccessibilityNodeInfo forNodeInfoByClick(AccessibilityNodeInfo info) {
      if (info == null) {
          return null;
      }
    AccessibilityNodeInfo parent = info;
    while (parent != null) {
      if (parent.isClickable()) {
        return parent;
      }
      parent = parent.getParent();
    }
    return null;
  }


  private static boolean hasNodeInfosByViewId(AccessibilityNodeInfo info, String id) {
      if (info == null) {
          return false;
      }
    List<AccessibilityNodeInfo> nodeInfoList = info.findAccessibilityNodeInfosByViewId(id);
    info.recycle();
    return !nodeInfoList.isEmpty();
  }

  private static List<AccessibilityNodeInfo> getListAccessibilityNodeInfos(
      AccessibilityNodeInfo info, String key, boolean isId) {
      if (info == null || TextUtils.isEmpty(key)) {
          return Collections.emptyList();
      }
    List<AccessibilityNodeInfo> infos;
    if (isId) {
      infos = info.findAccessibilityNodeInfosByViewId(key);
    } else {
      infos = info.findAccessibilityNodeInfosByText(key);
    }
//        info.recycle();
    return infos;
  }

  /**
   * 从节点中根据查找条件和查找规则 得到需要的节点 可能返回null
   *
   * @return 查找成功的节点
   */
  private static AccessibilityNodeInfo getAccessibilityNodeInfo(AccessibilityNodeInfo info,
      String key, boolean isId) {
      if (info == null || TextUtils.isEmpty(key)) {
          return null;
      }
    List<AccessibilityNodeInfo> infos;
    if (isId) {
      infos = info.findAccessibilityNodeInfosByViewId(key);
    } else {
      infos = info.findAccessibilityNodeInfosByText(key);
    }
//        info.recycle();
    return checkNodeListRule(infos) ? infos.get(0) : null;
  }

  /**
   * 从节点中根据查找条件、查找规则、遍历可点击节点 得到需要的节点 可能返回null
   *
   * @return 查找成功的可点击节点
   */
  private static AccessibilityNodeInfo getAccessibilityClickNodeInfo(AccessibilityNodeInfo info,
      String key, boolean isId) {
    return forNodeInfoByClick(getAccessibilityNodeInfo(info, key, isId));
  }

  /**
   * 检查规则方法
   *
   * @return true 符合判定条件 即非空并且只有一个元素 能够准确定位 false 其他情况都是
   */
  private static boolean checkNodeListRule(List<AccessibilityNodeInfo> infos) {
//        return (!infos.isEmpty()) && (infos.size() == 1);
    return !infos.isEmpty();
  }
}
