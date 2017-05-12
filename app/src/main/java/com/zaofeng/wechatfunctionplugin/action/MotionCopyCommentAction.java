package com.zaofeng.wechatfunctionplugin.action;

import static android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK;
import static com.zaofeng.wechatfunctionplugin.model.ConstantData.delayTime;
import static com.zaofeng.wechatfunctionplugin.model.ConstantTargetName.IdEditTimeLineComment;
import static com.zaofeng.wechatfunctionplugin.model.ConstantTargetName.IdLayoutTimeLineDetailListItem;
import static com.zaofeng.wechatfunctionplugin.model.ConstantTargetName.IdListViewTimeLineCommentDetail;
import static com.zaofeng.wechatfunctionplugin.model.ConstantTargetName.IdTextTimeLineAuthor;
import static com.zaofeng.wechatfunctionplugin.model.ConstantTargetName.IdTextViewTimeLineDetailItemContent;
import static com.zaofeng.wechatfunctionplugin.model.ConstantTargetName.IdTextViewTimeLineDetailItemName;
import static com.zaofeng.wechatfunctionplugin.utils.AccessibilityUtils.findViewById;
import static com.zaofeng.wechatfunctionplugin.utils.AccessibilityUtils.findViewClickByText;
import static com.zaofeng.wechatfunctionplugin.utils.AccessibilityUtils.findViewListById;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.os.Bundle;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;
import com.zaofeng.wechatfunctionplugin.WindowView;
import com.zaofeng.wechatfunctionplugin.model.CommentDateModel;
import com.zaofeng.wechatfunctionplugin.model.CommentRelationModel;
import com.zaofeng.wechatfunctionplugin.model.ConstantData;
import com.zaofeng.wechatfunctionplugin.utils.Constant;
import com.zaofeng.wechatfunctionplugin.utils.Logger;
import com.zaofeng.wechatfunctionplugin.utils.PerformUtils;
import com.zaofeng.wechatfunctionplugin.utils.RelationUtils;
import com.zaofeng.wechatfunctionplugin.utils.SPUtils;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Created by 李可乐 on 2017/5/12.
 * 行为触发
 * 朋友圈评论复制回复 目前只支持朋友圈详情
 */

public class MotionCopyCommentAction {

  Context mContext;
  WindowView mWindowView;
  AccessibilityService mService;
  boolean isOpen;

  boolean isWork = false;

  public MotionCopyCommentAction(
      Context mContext,
      WindowView mWindowView,
      AccessibilityService mService,
      boolean isOpen
  ) {
    this.mContext = mContext;
    this.mWindowView = mWindowView;
    this.mService = mService;
    this.isOpen = isOpen;
  }

  public void setOpen(boolean open) {
    isOpen = open;
  }

  public void action() {

    if (!isOpen) {
      Toast.makeText(mContext, "请开启自动回复", Toast.LENGTH_SHORT).show();
      return;
    }

    if (isWork) {
      Toast.makeText(mContext, ConstantData.Working, Toast.LENGTH_SHORT).show();
      return;
    }
    isWork = true;

    String setAuthorName = (String) SPUtils
        .get(mContext, Constant.Comment_Auto_Content, Constant.Empty);
    mWindowView.setMainTitle(ConstantData.WorkBegin);

    AccessibilityNodeInfo infoTargetName = findViewById(mService, IdTextTimeLineAuthor);
    if (infoTargetName == null) {
      mWindowView.setMainTitle(ConstantData.WorkClickable);
      Toast.makeText(mContext, "请滚动到顶部显示该条朋友圈作者", Toast.LENGTH_SHORT).show();
      isWork = false;
      return;
    }

    String authorName = infoTargetName.getText().toString();
    if (!authorName.equals(setAuthorName)) {
      mWindowView.setMainTitle(ConstantData.WorkClickable);
      Toast.makeText(mContext, "该条朋友圈作者与插件中输入的名字不符", Toast.LENGTH_SHORT).show();
      isWork = false;
      return;
    }

    LinkedHashSet<CommentDateModel> setDate = new LinkedHashSet<>();
    getCommentListViewItemInfo(setDate);

    ArrayList<CommentRelationModel> targetList = new ArrayList<>();
    ArrayList<CommentRelationModel> coverList = new ArrayList<>();

    RelationUtils.getRelationDates(setDate, authorName, targetList, coverList);
    ArrayList<String> result = RelationUtils.getMapRelationResult(targetList, coverList);

    if (result.isEmpty()) {
      Toast.makeText(mContext, "没有需要处理的内容", Toast.LENGTH_SHORT).show();
      mWindowView.setMainTitle(ConstantData.WorkClickable);
      isWork = false;
      return;
    }

    final AccessibilityNodeInfo nodeInfo = findViewById(mService, IdEditTimeLineComment);

    try {
      //清除输入框带有的回复
      PerformUtils.performAction(nodeInfo);
      Thread.sleep(delayTime);
      mService.performGlobalAction(GLOBAL_ACTION_BACK);
      Thread.sleep(delayTime);

      //遍历自动发送
      for (String item : result) {
        Logger.d("comment item=" + item);
        Bundle arguments = new Bundle();
        arguments
            .putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, item);
        PerformUtils.performAction(nodeInfo, AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
        Thread.sleep(delayTime);
        PerformUtils.performAction(findViewClickByText(mService, "发送"));
        Thread.sleep(delayTime);
      }

    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    mWindowView.setMainTitle(ConstantData.WorkClickable);
    isWork = false;
  }

  /**
   * 获取评论列表 的关键数据 包含去重操作（set集合实现）和自动滚动 递归调用方法
   *
   * @return 返回有序去重的文字数据
   */
  private LinkedHashSet<CommentDateModel> getCommentListViewItemInfo(
      LinkedHashSet<CommentDateModel> setDate) {
    final int indexValidStart = 0;/*评论Layout在父容器中的有效起始 索引值 因为评论列表索引肯定大于0*/

    List<AccessibilityNodeInfo> infoList = findViewListById(mService,
        IdLayoutTimeLineDetailListItem);

    CommentDateModel itemModel;
    int index;
    String title;
    String content;

    for (AccessibilityNodeInfo itemInfo : infoList) {
      //获取索引 兼容处理 行索引有效还是列索引有效问题 即判定是否大于有效起始值
      index = itemInfo.getCollectionItemInfo().getRowIndex() > indexValidStart ? itemInfo
          .getCollectionItemInfo().getRowIndex()
          : itemInfo.getCollectionItemInfo().getColumnIndex();


      title = findViewById(itemInfo, IdTextViewTimeLineDetailItemName).getText().toString().trim();
      content = findViewById(itemInfo, IdTextViewTimeLineDetailItemContent).getText().toString()
          .trim();
      itemModel = new CommentDateModel(index, title, content);
      setDate.add(itemModel);

    }

    AccessibilityNodeInfo info = findViewById(mService, IdListViewTimeLineCommentDetail);
    if (!PerformUtils.checkScrollViewBottom(info)) {
      PerformUtils.performAction(info, AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
      try {
        Thread.sleep(delayTime);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      return getCommentListViewItemInfo(setDate);
    } else {
      Logger.d("已经到底了");
      return setDate;
    }

  }

}
