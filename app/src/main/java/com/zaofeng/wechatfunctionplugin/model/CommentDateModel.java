package com.zaofeng.wechatfunctionplugin.model;

/**
 * Created by 李可乐 on 2017/2/14 0014.
 */

public class CommentDateModel {
    private String title;
    private String content;

    public CommentDateModel(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }
}
