package com.zaofeng.wechatfunctionplugin.utils;

import com.zaofeng.wechatfunctionplugin.model.CommentDateModel;
import com.zaofeng.wechatfunctionplugin.model.CommentRelationModel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by 李可乐 on 2017/2/15 0015.
 */

public class RelationUtils {

  public static final String TargetComment = "回复";
  public static final String TargetSemicolon = ":";

  /**
   * 遍历原始数据 通过作者名筛选 得到朵朵评论集合和用户评论集合
   *
   * @param originalSet 原始数据
   * @param authorName 作者名
   * @param targetList 目标集合（将来要作用的用户评论集合）
   * @param coverList 覆盖集合（已经存在的朵朵评论集合）
   */
  public static void traverseCommentByName(
      Set<CommentDateModel> originalSet,
      String authorName,
      List<CommentRelationModel> targetList,
      List<CommentRelationModel> coverList) {

    Iterator<CommentDateModel> infoIterator = originalSet.iterator();
    while (infoIterator.hasNext()) {//遍历set集合
      CommentDateModel infoFor = infoIterator.next();
      Integer itemIndex = infoFor.getIndex();
      String itemTitle = infoFor.getTitle();
      String itemContent = infoFor.getContent();

      if ((itemContent.contains(TargetComment)) && (itemContent.contains(TargetSemicolon))) {
        // 包含特定字符 为嵌套回复
        int result = itemContent.indexOf(authorName);//查找内容中的作者名字
        if (result != -1 && result != 0) {
          //回复评论 回复中有朵朵 并且不为首位 即同学回复朵朵的 存入目标集合
          String offsetContent = itemContent.substring(itemContent.indexOf(TargetSemicolon) + 1);
          if (targetList != null) {
            targetList.add(new CommentRelationModel(itemIndex, offsetContent));
          }
        }
      } else {
        //普通回复
        if (itemTitle.equals(authorName)) {
          //朵朵 发布的评论
          if (coverList != null) {
            coverList.add(new CommentRelationModel(itemIndex, itemContent));
          }
        } else {
          //评论 且不是朵朵发布的 即同学的评论
          if (targetList != null) {
            targetList.add(new CommentRelationModel(itemIndex, itemContent));
          }
        }
      }
    }
  }

  /**
   * @param targetList 目标评论集合
   * @param coverList 已经存在的评论集合
   * @return 还没有被映射的评论内容集合
   */
  public static List<String> getMapRelationResult(List<CommentRelationModel> targetList,
      List<CommentRelationModel> coverList) {
    List<String> resultList = new ArrayList<>();

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
