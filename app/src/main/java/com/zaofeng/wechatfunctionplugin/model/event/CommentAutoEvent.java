package com.zaofeng.wechatfunctionplugin.model.event;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by 李可乐 on 2017/2/5 0005.
 * 带状态
 */

public class CommentAutoEvent {
    public static final int BreakOff = 0;
    public static final int Start = 1;
    public static final int Finish = 4;

    @IntDef({BreakOff, Start, Finish})
    @Retention(RetentionPolicy.SOURCE)
    public @interface State {
    }

    private int state;

    public CommentAutoEvent() {
        state = Start;
    }

    public
    @State
    int getState() {
        return state;
    }

    public void setState(@State int state) {
        this.state = state;
    }

}
