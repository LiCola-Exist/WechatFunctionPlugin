package com.zaofeng.wechatfunctionplugin.utils;

import com.zaofeng.wechatfunctionplugin.model.CommentRelationModel;

import java.util.ArrayList;

/**
 * Created by 李可乐 on 2017/2/15 0015.
 */

public class RelationUtils {

    public static ArrayList<String> getResult(ArrayList<CommentRelationModel> targetList ,ArrayList<CommentRelationModel> coverList){
        ArrayList<String> resultList = new ArrayList<>();

        boolean isFind=false;
        for (CommentRelationModel targetItem : targetList) {

            for (CommentRelationModel coverItem : coverList) {
                if (targetItem.getContent().equals(coverItem.getContent())) {
                    if (coverItem.isMap()){
                        isFind=false;
                    }else {
                        isFind=true;
                        coverItem.setMap(true);
                    }
                }else {
                    isFind=false;
                }
            }
            if (!isFind){
                resultList.add(targetItem.getContent());
            }
        }

        return resultList;
    }
}
