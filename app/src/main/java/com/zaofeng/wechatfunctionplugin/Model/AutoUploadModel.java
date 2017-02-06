package com.zaofeng.wechatfunctionplugin.Model;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by 李可乐 on 2017/2/5 0005.
 */

public class AutoUploadModel {
    public static final int BreakOff=0;
    public static final int Start=1;
    public static final int Jump=2;
    public static final int Choose=3;
    public static final int FillOut=4;
    public static final int Finish=5;

    @IntDef({BreakOff,Start,Jump,Choose,FillOut,Finish})
    @Retention(RetentionPolicy.SOURCE)
    public @interface State{}

    private int state;
    private String clipText;

    public AutoUploadModel(String clipText) {
        this.state = Start;
        this.clipText = clipText;
    }

    public @State int getState() {
        return state;
    }

    public void setState(@State int state) {
        this.state = state;
    }

    public String getClipText() {
        return clipText;
    }

}
