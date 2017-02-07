package com.zaofeng.wechatfunctionplugin.Model;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by 李可乐 on 2017/2/5 0005.
 * 自动快捷接受新增好友请求 带状态
 */

public class FastNewFriendAcceptModel {
    public static final int BreakOff = 0;
    public static final int Start = 1;
    public static final int OpenRequest = 2;
    public static final int Accept = 3;
    public static final int Finish = 5;

    @IntDef({BreakOff, Start, OpenRequest, Accept, Finish})
    @Retention(RetentionPolicy.SOURCE)
    public @interface State {
    }

    private int state;

    public FastNewFriendAcceptModel() {
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
