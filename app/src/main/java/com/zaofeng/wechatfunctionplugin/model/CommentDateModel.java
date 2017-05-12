package com.zaofeng.wechatfunctionplugin.model;

/**
 * Created by 李可乐 on 2017/2/14 0014.
 * 评论数据Model
 */

public class CommentDateModel {

  private Integer index;
  private String title;
  private String content;

  public CommentDateModel(Integer index, String title, String content) {
    this.index = index;
    this.title = title;
    this.content = content;
  }

  public String getTitle() {
    return title;
  }

  public String getContent() {
    return content;
  }

  public Integer getIndex() {
    return index;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    CommentDateModel that = (CommentDateModel) o;

    return index.equals(that.index);

  }

  @Override
  public int hashCode() {
    return index.hashCode();
  }

  @Override
  public String toString() {
    return "CommentDateModel{" +
        "index=" + index +
        ", title='" + title + '\'' +
        ", content='" + content + '\'' +
        '}';
  }
}
