package com.zaofeng.wechatfunctionplugin.utils;

import com.zaofeng.wechatfunctionplugin.model.CommentDateModel;
import com.zaofeng.wechatfunctionplugin.model.CommentRelationModel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by 李可乐 on 2017/2/15 0015.
 */

public class RelationUtils {

  public static final String TargetComment = "回复";
  public static final String TargetSemicolon = ":";

  /**
   *
   * @param originalSet
   * @param authorName
   * @param targetList
   * @param coverList
   */
  public static void getCommentDatesByRule(Set<CommentDateModel> originalSet, String authorName,
      ArrayList<CommentRelationModel> targetList, ArrayList<CommentRelationModel> coverList) {

    Iterator<CommentDateModel> infoIterator = originalSet.iterator();
    while (infoIterator.hasNext()) {
      CommentDateModel infoFor = infoIterator.next();
      Integer itemIndex = infoFor.getIndex();
      String itemTitle = infoFor.getTitle();
      String itemContent = infoFor.getContent();

      if ((itemContent.contains(TargetComment)) && (itemContent.contains(TargetSemicolon))) {
        //嵌套回复
        int result = itemContent.indexOf(authorName);
        if (result != -1 && result != 0) {
          //回复评论 回复中有朵朵 并且不为首位 即同学回复朵朵的title 存入目标集合
          String offsetContent = itemContent.substring(itemContent.indexOf(TargetSemicolon) + 1);
          targetList.add(new CommentRelationModel(itemIndex, offsetContent));
        }
      } else {
        //普通回复
        if (itemTitle.equals(authorName)) {
          //朵朵 发布的评论
          coverList.add(new CommentRelationModel(itemIndex, itemContent));
        } else {
          //评论 且不是朵朵发布的 即同学的评论
          targetList.add(new CommentRelationModel(itemIndex, itemContent));
        }
      }
    }

  }

  /**
   * @param targetList 目标评论集合
   * @param coverList 已经存在的评论集合
   * @return 还没有被映射的评论内容集合
   */
  public static ArrayList<String> getMapRelationResult(ArrayList<CommentRelationModel> targetList,
      ArrayList<CommentRelationModel> coverList) {
    ArrayList<String> resultList = new ArrayList<>();

    boolean isFind = false;
    for (CommentRelationModel targetItem : targetList) {

      for (CommentRelationModel coverItem : coverList) {
        if (targetItem.getContent().equals(coverItem.getContent()) && !coverItem.isMap()
            && targetItem.getIndex() < coverItem.getIndex()) {
          //相同文字内容 且没有被映射 且target索引小于cover索引（即目标评论早于朵朵评论）
          coverItem.setMap(true);
          isFind = true;
          break;
        } else {
          isFind = false;
        }
      }
      if (!isFind) {
        resultList.add(targetItem.getContent());
      }
    }

    return resultList;
  }
}
