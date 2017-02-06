package com.zaofeng.wechatfunctionplugin.Utils;

import android.view.accessibility.AccessibilityNodeInfo;

/**
 * Created by 李可乐 on 2017/2/5 0005.
 */

public class PerformUtils {


    public static boolean performAction(AccessibilityNodeInfo info) {
        return performAction(info, AccessibilityNodeInfo.ACTION_CLICK);
    }

    public static boolean performAction(AccessibilityNodeInfo info, int action) {
        if (info == null) return false;
        return info.performAction(action);
    }
}
