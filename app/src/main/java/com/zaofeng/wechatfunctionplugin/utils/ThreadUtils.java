package com.zaofeng.wechatfunctionplugin.utils;

import static com.zaofeng.wechatfunctionplugin.model.ConstantData.delayTime;

/**
 * Created by 李可乐 on 2017/5/15.
 */

public class ThreadUtils {
  public static void sleepSecure() {
    sleepSecure(delayTime);
  }

  public static void sleepSecure(long delayTime) {
    try {
      Thread.sleep(delayTime);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
