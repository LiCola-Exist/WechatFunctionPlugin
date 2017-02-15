package com.zaofeng.wechatfunctionplugin.model;

/**
 * Created by 李可乐 on 2017/2/13 0013.
 * 评论关系model 主要保存内容和对象间的映射关系
 */

public class CommentRelationModel {

    private String content;
    private boolean isMap;//是否有映射
    private CommentRelationModel mapModel;//映射的对象


    public CommentRelationModel(String content) {
        this.content = content;
        this.isMap=false;
        this.mapModel=null;
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

    public CommentRelationModel getMapModel() {
        return mapModel;
    }

    public void setMapModel(CommentRelationModel mapModel) {
        this.mapModel = mapModel;
    }
}
