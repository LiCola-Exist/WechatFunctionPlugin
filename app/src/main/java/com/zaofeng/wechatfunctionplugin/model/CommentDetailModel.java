package com.zaofeng.wechatfunctionplugin.model;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by 李可乐 on 2017/2/13 0013.
 */

public class CommentDetailModel {

    public static final int Normal = 0;
    public static final int Reply = 1;
    public static final int ReplyOther = 2;

    @IntDef({Normal, Reply, ReplyOther})
    @Retention(RetentionPolicy.SOURCE)
    public @interface MessageStatus {
    }

    private String authorName;
    @MessageStatus
    private int messageStatus;
    private String content;

    public CommentDetailModel(String authorName, @MessageStatus int messageStatus, String content) {
        this.authorName = authorName;
        this.messageStatus = messageStatus;
        this.content = content;
    }

    public String getAuthorName() {
        return authorName;
    }

    public int getMessageStatus() {
        return messageStatus;
    }

    public String getContent() {
        return content;
    }
}
