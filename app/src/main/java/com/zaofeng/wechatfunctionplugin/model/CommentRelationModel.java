package com.zaofeng.wechatfunctionplugin.model;

/**
 * Created by 李可乐 on 2017/2/13 0013.
 * 评论关系model 主要保存内容和对象间的映射关系
 */

public class CommentRelationModel {

  private Integer index;
  private String content;
  private Boolean isMap;//是否有映射


  public CommentRelationModel(Integer index, String content) {
    this.index = index;
    this.content = content;
    this.isMap = false;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public boolean isMap() {
    return isMap;
  }

  public void setMap(boolean map) {
    isMap = map;
  }

  public Integer getIndex() {
    return index;
  }
}
