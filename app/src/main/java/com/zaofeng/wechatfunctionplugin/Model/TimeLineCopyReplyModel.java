package com.zaofeng.wechatfunctionplugin.Model;

import android.graphics.Rect;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by 李可乐 on 2017/2/5 0005.
 * 自动快捷回复新增好友 带状态
 */

public class TimeLineCopyReplyModel {
    public static final int BreakOff = 0;
    public static final int Start = 1;
    public static final int Find = 2;
    public static final int FillOut = 3;
    public static final int Finish = 4;

    @IntDef({BreakOff, Start,Find, FillOut, Finish})
    @Retention(RetentionPolicy.SOURCE)
    public @interface State {
    }

    private int state;
    private String ReplyContent;
    private Rect eventRect;//事件开始的范围


    public TimeLineCopyReplyModel(String replyContent,Rect rect) {
        state = Start;
        ReplyContent = replyContent;
        eventRect=rect;
    }

    public
    @State
    int getState() {
        return state;
    }

    public void setState(@State int state) {
        this.state = state;
    }

    public String getReplyContent() {
        return ReplyContent;
    }

    public Rect getEventRect() {
        return eventRect;
    }
}
