package com.zaofeng.wechatfunctionplugin.Model;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by 李可乐 on 2017/2/5 0005.
 * 自动快速发布后回复model 带状态
 */

public class AutoReplyModel {
    public static final int BreakOff=0;
    public static final int Start=1;
    public static final int Upload=2;
    public static final int Jump =3;
    public static final int Finish=6;

    @IntDef({BreakOff,Start,Upload, Jump,Finish})
    @Retention(RetentionPolicy.SOURCE)
    public @interface State{}

    private int state;
    private String TimeLineContent;
    private String ReplyContent;

    public AutoReplyModel(String timeLineContent, String replyContent) {
        state=Start;
        TimeLineContent = timeLineContent;
        ReplyContent = replyContent;
    }

    public @State int getState() {
        return state;
    }

    public void setState(@State int state) {
        this.state = state;
    }

    public String getTimeLineContent() {
        return TimeLineContent;
    }

    public void setTimeLineContent(String timeLineContent) {
        TimeLineContent = timeLineContent;
    }

    public String getReplyContent() {
        return ReplyContent;
    }

    public void setReplyContent(String replyContent) {
        ReplyContent = replyContent;
    }

    @Override
    public String toString() {
        return "AutoReplyModel{" +
                "state=" + state +
                ", TimeLineContent='" + TimeLineContent + '\'' +
                ", ReplyContent='" + ReplyContent + '\'' +
                '}';
    }
}
