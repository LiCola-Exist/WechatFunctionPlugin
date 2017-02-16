package com.zaofeng.wechatfunctionplugin.utils;

import com.zaofeng.wechatfunctionplugin.model.CommentRelationModel;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Created by 李可乐 on 2017/2/16 0016.
 */
public class RelationUtilsTest {
    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void getResult() throws Exception {
        ArrayList<CommentRelationModel> targetList=new ArrayList<>();
        ArrayList<CommentRelationModel> coverList=new ArrayList<>();

        targetList.add(new CommentRelationModel("文字内容123"));
        targetList.add(new CommentRelationModel("文字内容321"));
        targetList.add(new CommentRelationModel("文字内容123"));
        targetList.add(new CommentRelationModel("文字内容321"));


        coverList.add(new CommentRelationModel("文字内容32"));

        ArrayList<String> result=RelationUtils.getResult(targetList,coverList);
        System.out.println("RelationUtilsTest result size= "+result.size());
        for (String item :
                result) {
            System.out.println("RelationUtilsTest date= "+item);
        }
        assertEquals(4,result.size());
    }

}