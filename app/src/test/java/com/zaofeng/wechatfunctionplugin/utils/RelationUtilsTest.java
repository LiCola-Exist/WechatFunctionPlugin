package com.zaofeng.wechatfunctionplugin.utils;

import static org.junit.Assert.assertEquals;

import com.zaofeng.wechatfunctionplugin.model.CommentDateModel;
import com.zaofeng.wechatfunctionplugin.model.CommentRelationModel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;

/**
 * Created by 李可乐 on 2017/2/16 0016.
 */
public class RelationUtilsTest {

  @Test
  public void getCommentDatesByRule() throws Exception {

    Set<CommentDateModel> originalSet = new HashSet<>();
    originalSet.add(new CommentDateModel(1, "其他人", "内容1"));
    originalSet.add(new CommentDateModel(2, "其他人", "内容2"));
    originalSet.add(new CommentDateModel(3, "朵朵", "内容1"));
    originalSet.add(new CommentDateModel(4, "其他人", "回复朵朵:内容4"));
    originalSet.add(new CommentDateModel(5, "朵朵", "回复其他人:内容5"));
    originalSet.add(new CommentDateModel(6, "其他人", "内容6"));

    ArrayList<CommentRelationModel> targetList = new ArrayList<>();
    ArrayList<CommentRelationModel> coverList = new ArrayList<>();

    targetList.add(new CommentRelationModel(1, "文字内容123"));

    coverList.add(new CommentRelationModel(0, "文字内容123"));
    RelationUtils.getCommentDatesByRule(originalSet,"朵朵",targetList,coverList);

    assertEquals(1,targetList);
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