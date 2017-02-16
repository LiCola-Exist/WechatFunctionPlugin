package com.zaofeng.wechatfunctionplugin.utils;

import com.zaofeng.wechatfunctionplugin.model.CommentRelationModel;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Created by 李可乐 on 2017/2/16 0016.
 */
public class RelationUtilsTest {

    @Test
    public void getResult() throws Exception {


        ArrayList<CommentRelationModel> targetList=new ArrayList<>();
        ArrayList<CommentRelationModel> coverList=new ArrayList<>();

        targetList.add(new CommentRelationModel("1"));
        targetList.add(new CommentRelationModel("2"));
        targetList.add(new CommentRelationModel("3"));
        targetList.add(new CommentRelationModel("1"));
        targetList.add(new CommentRelationModel("1"));

        coverList.add(new CommentRelationModel("1"));
        coverList.add(new CommentRelationModel("1"));

        ArrayList<String> result=RelationUtils.getResult(targetList,coverList);
        System.out.println("RelationUtilsTest result size= "+result.size());
        for (String item :
                result) {
            System.out.println("RelationUtilsTest date= "+item);
        }
    }

}