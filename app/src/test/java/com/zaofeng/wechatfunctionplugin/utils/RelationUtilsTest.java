package com.zaofeng.wechatfunctionplugin.utils;

import com.zaofeng.wechatfunctionplugin.model.CommentRelationModel;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

/**
 * Created by 李可乐 on 2017/2/16 0016.
 */
public class RelationUtilsTest {
    @Test
    public void getRelationDates() throws Exception {

    }

    @Test
    public void getMapRelationResult() throws Exception {

        ArrayList<CommentRelationModel> targetList = new ArrayList<>();
        ArrayList<CommentRelationModel> coverList = new ArrayList<>();

        targetList.add(new CommentRelationModel(1, "文字内容123"));
        targetList.add(new CommentRelationModel(3, "文字内容123"));
        targetList.add(new CommentRelationModel(4, "文字内容123"));


        coverList.add(new CommentRelationModel(0, "文字内容123"));

        ArrayList<String> result = RelationUtils.getMapRelationResult(targetList, coverList);
        System.out.println("RelationUtilsTest result size= " + result.size());
        for (String item :
                result) {
            System.out.println("RelationUtilsTest date= " + item);
        }


        assertEquals(3, result.size());
    }

}