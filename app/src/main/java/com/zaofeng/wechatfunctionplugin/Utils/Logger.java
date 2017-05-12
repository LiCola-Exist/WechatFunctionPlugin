package com.zaofeng.wechatfunctionplugin.utils;

import android.text.TextUtils;
import com.zaofeng.wechatfunctionplugin.BuildConfig;


/**
 * Log加强工具类
 * 1.可以在发布后关闭日志打印功能
 * 2.可以直接调用  Logger.d() 打印方法调用
 * 详细说明：http://blog.csdn.net/card361401376/article/details/51438786
 */

public final class Logger {

  private static final String TAG = "BoxLog";

  /**
   * Set true or false if you want read logs or not
   */
  private static boolean logEnabled_d = BuildConfig.DEBUG;
  private static boolean logEnabled_i = BuildConfig.DEBUG;
  private static boolean logEnabled_e = BuildConfig.DEBUG;

  public static void d() {
    if (logEnabled_d) {
      android.util.Log.d(TAG, getLocation());
    }
  }

  public static void d(String msg) {
    if (logEnabled_d) {
      android.util.Log.d(TAG, getLocation() + msg);
    }
  }

  public static void d(String... msg) {
    if (logEnabled_d) {
      StringBuilder builder = new StringBuilder();
      for (int i = 0; i < msg.length; i++) {
        builder.append("msg").append(i).append("=").append(msg[i]).append(" ");
      }
      android.util.Log.d(TAG, getLocation() + builder.toString());
    }
  }

  public static void i(String msg) {
    if (logEnabled_i) {
      android.util.Log.i(TAG, getLocation() + msg);
    }
  }

  public static void i() {
    if (logEnabled_i) {
      android.util.Log.i(TAG, getLocation());
    }
  }

  public static void e(String msg) {
    if (logEnabled_e) {
      android.util.Log.e(TAG, getLocation() + msg);
    }
  }

  public static void e(String msg, Throwable e) {
    if (logEnabled_e) {
      android.util.Log.e(TAG, getLocation() + msg, e);
    }
  }

  public static void e(Throwable e) {
    if (logEnabled_e) {
      android.util.Log.e(TAG, getLocation(), e);
    }
  }

  public static void e() {
    if (logEnabled_e) {
      android.util.Log.e(TAG, getLocation());
    }
  }

  private static String getLocation() {
    final String className = Logger.class.getName();
    final StackTraceElement[] traces = Thread.currentThread()
        .getStackTrace();

    boolean found = false;

    for (StackTraceElement trace : traces) {
      try {
        if (found) {
          if (!trace.getClassName().startsWith(className)) {
            Class<?> clazz = Class.forName(trace.getClassName());
            return "[" + getClassName(clazz) + ":"
                + trace.getMethodName() + ":"
                + trace.getLineNumber() + "]: ";
          }
        } else if (trace.getClassName().startsWith(className)) {
          found = true;
        }
      } catch (ClassNotFoundException ignored) {
      }
    }

    return "[]: ";
  }

  private static String getClassName(Class<?> clazz) {
    if (clazz != null) {
      if (!TextUtils.isEmpty(clazz.getSimpleName())) {
        return clazz.getSimpleName();
      }

      return getClassName(clazz.getEnclosingClass());
    }

    return "";
  }

}
