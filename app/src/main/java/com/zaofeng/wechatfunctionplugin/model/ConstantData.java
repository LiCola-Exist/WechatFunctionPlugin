package com.zaofeng.wechatfunctionplugin.model;

/**
 * Created by 李可乐 on 2017/5/12.
 */

public interface ConstantData {

  long delayTime = 800;//为兼容微信的防抖动处理 的点击延迟时间

  String Working="正在处理任务,请稍后";

  String WorkBegin="任务开始";
  String WorkEnd="任务结束";
  String WorkClickable="点击开始";

}
