package com.zaofeng.wechatfunctionplugin.action;

import static android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK;
import static com.zaofeng.wechatfunctionplugin.model.ConstantTargetName.IdEditTimeLineComment;
import static com.zaofeng.wechatfunctionplugin.model.ConstantTargetName.IdLayoutTimeLineDetailListItem;
import static com.zaofeng.wechatfunctionplugin.model.ConstantTargetName.IdListViewTimeLineCommentDetail;
import static com.zaofeng.wechatfunctionplugin.model.ConstantTargetName.IdTextTimeLineAuthor;
import static com.zaofeng.wechatfunctionplugin.model.ConstantTargetName.IdTextViewTimeLineDetailItemContent;
import static com.zaofeng.wechatfunctionplugin.model.ConstantTargetName.IdTextViewTimeLineDetailItemName;
import static com.zaofeng.wechatfunctionplugin.model.WeChatUIContract.SnsCommentDetailUI;
import static com.zaofeng.wechatfunctionplugin.utils.AccessibilityUtils.findViewById;
import static com.zaofeng.wechatfunctionplugin.utils.AccessibilityUtils.findViewClickByText;
import static com.zaofeng.wechatfunctionplugin.utils.AccessibilityUtils.findViewListById;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.os.Bundle;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import com.zaofeng.wechatfunctionplugin.WindowView;
import com.zaofeng.wechatfunctionplugin.model.CommentDateModel;
import com.zaofeng.wechatfunctionplugin.model.CommentRelationModel;
import com.zaofeng.wechatfunctionplugin.model.Constant;
import com.zaofeng.wechatfunctionplugin.model.ConstantData;
import com.zaofeng.wechatfunctionplugin.model.WeChatUIContract.StatusUI;
import com.zaofeng.wechatfunctionplugin.utils.Logger;
import com.zaofeng.wechatfunctionplugin.utils.PerformUtils;
import com.zaofeng.wechatfunctionplugin.utils.RelationUtils;
import com.zaofeng.wechatfunctionplugin.utils.SPUtils;
import com.zaofeng.wechatfunctionplugin.utils.ThreadUtils;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Created by 李可乐 on 2017/5/12.
 * 行为触发
 * 朋友圈评论自动复制回复
 */

public class MotionAutoCopyCommentAction extends BaseAction {

  boolean isWork = false;

  public MotionAutoCopyCommentAction(Context mContext, WindowView mWindowView,
      AccessibilityService mService, boolean isOpen) {
    super(mContext, mWindowView, mService, isOpen);
  }

  @Override
  public boolean action(@Step int step, @StatusUI int statusUi, AccessibilityEvent event) {

    if (!isOpen) {
      showToast("请开启自动回复");
      return false;
    }

    if (isWork) {
      showToast(ConstantData.Working);
      return false;
    }

    if (statusUi == SnsCommentDetailUI) {
      return detailCommentHandler();
    } else {
      showToast("该功能只在朋友圈详情页生效");
      return false;
    }

  }

  private boolean detailCommentHandler() {
    String setAuthorName = (String) SPUtils
        .get(mContext, Constant.Comment_Auto_Content, Constant.Empty);
    mWindowView.setMainTitle(ConstantData.WorkBegin);

    AccessibilityNodeInfo infoTargetName = findViewById(mService, IdTextTimeLineAuthor);
    if (infoTargetName == null) {
      mWindowView.setMainTitle(ConstantData.WorkClickable);
      showToast("请滚动到顶部显示该条朋友圈作者");
      isWork = false;
      return false;
    }

    String authorName = infoTargetName.getText().toString();
    if (!authorName.equals(setAuthorName)) {
      mWindowView.setMainTitle(ConstantData.WorkClickable);
      showToast("该条朋友圈作者与插件中输入的名字不符");
      isWork = false;
      return false;
    }

    LinkedHashSet<CommentDateModel> originalSet = new LinkedHashSet<>();//原始评论数据
    getCommentListViewItemInfo(originalSet);//递归到底

    ArrayList<CommentRelationModel> targetList = new ArrayList<>();//目标集合
    ArrayList<CommentRelationModel> coverList = new ArrayList<>();//已经存在的评论集合
    //按规则加工 得到两个数据集合
    RelationUtils.getCommentDatesByRule(originalSet, authorName, targetList, coverList);
    //得到待处理数据
    ArrayList<String> result = RelationUtils.getMapRelationResult(targetList, coverList);

    if (result.isEmpty()) {
      showToast("没有需要处理的内容");
      mWindowView.setMainTitle(ConstantData.WorkClickable);
      isWork = false;
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
      arguments
          .putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, item);
      PerformUtils.performAction(nodeInfo, AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
      ThreadUtils.sleepSecure();
      PerformUtils.performAction(findViewClickByText(mService, "发送"));
      ThreadUtils.sleepSecure();
    }
    showToast("处理完成");
    mWindowView.setMainTitle(ConstantData.WorkClickable);
    isWork = false;

    return true;
  }


  /**
   * 获取评论列表 的关键数据 包含去重操作（set集合实现）和自动滚动 递归调用方法
   *
   * @return 返回有序去重的文字数据
   */
  private LinkedHashSet<CommentDateModel> getCommentListViewItemInfo(
      LinkedHashSet<CommentDateModel> setDate) {

    addCommentData(setDate, findViewListById(mService, IdLayoutTimeLineDetailListItem));

    AccessibilityNodeInfo info = findViewById(mService, IdListViewTimeLineCommentDetail);
    if (!PerformUtils.checkScrollViewBottom(info)) {
      PerformUtils.performAction(info, AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
      ThreadUtils.sleepSecure();
      return getCommentListViewItemInfo(setDate);
    } else {
      Logger.d("已经到底了");
      return setDate;
    }

  }

  /**
   * 根据传入Node节点 添加到评论集合中
   * @param setDate
   * @param infoList
   */
  private void addCommentData(LinkedHashSet<CommentDateModel> setDate,
      List<AccessibilityNodeInfo> infoList) {
    final int indexValidStart = 0;/*评论Layout在父容器中的有效起始 索引值 因为评论列表索引肯定大于0*/

    if (infoList == null || infoList.isEmpty()) {
      return;
    }

    for (AccessibilityNodeInfo itemInfo : infoList) {
      //获取索引 兼容处理 行索引有效还是列索引有效问题 即判定是否大于有效起始值
      if (itemInfo == null || itemInfo.getCollectionItemInfo() == null) {
        continue;
      }
      int index = itemInfo.getCollectionItemInfo().getRowIndex() > indexValidStart ? itemInfo
          .getCollectionItemInfo().getRowIndex()
          : itemInfo.getCollectionItemInfo().getColumnIndex();

      String title = findViewById(itemInfo, IdTextViewTimeLineDetailItemName).getText().toString()
          .trim();
      String content = findViewById(itemInfo, IdTextViewTimeLineDetailItemContent).getText()
          .toString()
          .trim();
      CommentDateModel itemModel = new CommentDateModel(index, title, content);
      setDate.add(itemModel);
    }
  }

}
