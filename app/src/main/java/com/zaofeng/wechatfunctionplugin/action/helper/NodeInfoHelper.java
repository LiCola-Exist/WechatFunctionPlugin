package com.zaofeng.wechatfunctionplugin.action.helper;

import android.accessibilityservice.AccessibilityService;
import android.os.Bundle;
import android.view.accessibility.AccessibilityNodeInfo;
import com.zaofeng.wechatfunctionplugin.model.CommentDateModel;
import com.zaofeng.wechatfunctionplugin.utils.CheckUtils;
import com.zaofeng.wechatfunctionplugin.utils.PerformUtils;
import com.zaofeng.wechatfunctionplugin.utils.ThreadUtils;
import java.util.LinkedHashSet;
import java.util.List;

import static android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK;
import static com.zaofeng.wechatfunctionplugin.model.ConstantData.delayTime;
import static com.zaofeng.wechatfunctionplugin.model.ConstantTargetName.IdEditTimeLineComment;
import static com.zaofeng.wechatfunctionplugin.model.ConstantTargetName.IdLayoutTimeLineDetailListItem;
import static com.zaofeng.wechatfunctionplugin.model.ConstantTargetName.IdListViewTimeLineCommentDetail;
import static com.zaofeng.wechatfunctionplugin.model.ConstantTargetName.IdTextViewTimeLineDetailItemContent;
import static com.zaofeng.wechatfunctionplugin.model.ConstantTargetName.IdTextViewTimeLineDetailItemName;
import static com.zaofeng.wechatfunctionplugin.utils.AccessibilityUtils.findViewById;
import static com.zaofeng.wechatfunctionplugin.utils.AccessibilityUtils.findViewClickByText;
import static com.zaofeng.wechatfunctionplugin.utils.AccessibilityUtils.findViewListById;
import static com.zaofeng.wechatfunctionplugin.utils.AccessibilityUtils.getNodeInfoText;

/**
 * Created by LiCola on 2017/6/10.
 */

public class NodeInfoHelper {
  /**
   * 获取评论列表 的关键数据 包含去重操作（set集合实现）和自动滚动 递归调用方法
   *
   * @return 返回有序去重的文字数据
   */
  public static LinkedHashSet<CommentDateModel> getCommentListViewItemInfo(
      AccessibilityService mService, LinkedHashSet<CommentDateModel> setDate) {

    AccessibilityNodeInfo info = findViewById(mService, IdListViewTimeLineCommentDetail);

    do {
      addCommentData(setDate, findViewListById(mService, IdLayoutTimeLineDetailListItem));
      ThreadUtils.sleepSecure(delayTime);
    } while (!PerformUtils.checkScrollViewBottom(info) && PerformUtils.performAction(info,
        AccessibilityNodeInfo.ACTION_SCROLL_FORWARD));
    info.recycle();
    return setDate;
  }

  /**
   * 根据传入Node节点 添加到评论集合中
   */
  private static void addCommentData(LinkedHashSet<CommentDateModel> setDate,
      List<AccessibilityNodeInfo> infoList) {
    final int indexValidStart = 0;/*评论Layout在父容器中的有效起始 索引值 因为评论列表有头部点赞数组 所以索引肯定大于0*/

    if (CheckUtils.isEmpty(infoList)) {
      return;
    }

    for (AccessibilityNodeInfo itemInfo : infoList) {

      if (itemInfo == null) {
        continue;
      }

      if (!itemInfo.refresh()) {
        itemInfo.recycle();
        continue;
      }

      if (itemInfo.getCollectionItemInfo() == null) {
        continue;
      }

      //获取索引 兼容处理 行索引有效还是列索引有效问题 即判定是否大于有效起始值
      int index = itemInfo.getCollectionItemInfo().getRowIndex() > indexValidStart
          ? itemInfo.getCollectionItemInfo().getRowIndex()
          : itemInfo.getCollectionItemInfo().getColumnIndex();

      String title = getNodeInfoText(findViewById(itemInfo, IdTextViewTimeLineDetailItemName));
      String content = getNodeInfoText(findViewById(itemInfo, IdTextViewTimeLineDetailItemContent));
      if (CheckUtils.isEmpty(title) || CheckUtils.isEmpty(content)) {
        continue;
      }
      CommentDateModel itemModel = new CommentDateModel(index, title, content);
      setDate.add(itemModel);
    }
  }

  public static boolean handlePasteAction(AccessibilityService mService, List<String> result) {

    if (result.isEmpty()) {
      return false;
    }

    final AccessibilityNodeInfo nodeInfo = findViewById(mService, IdEditTimeLineComment);

    //清除输入框带有的回复
    PerformUtils.performAction(nodeInfo);
    ThreadUtils.sleepSecure();
    mService.performGlobalAction(GLOBAL_ACTION_BACK);
    ThreadUtils.sleepSecure();

    //遍历自动发送
    for (String item : result) {
      Bundle arguments = new Bundle();
      arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, item);
      PerformUtils.performAction(nodeInfo, AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
      ThreadUtils.sleepSecure();
      PerformUtils.performAction(findViewClickByText(mService, "发送"));
      ThreadUtils.sleepSecure();
    }
    return true;
  }
}
