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
import com.zaofeng.wechatfunctionplugin.action.helper.NodeInfoHelper;
import com.zaofeng.wechatfunctionplugin.model.CommentDateModel;
import com.zaofeng.wechatfunctionplugin.model.CommentRelationModel;
import com.zaofeng.wechatfunctionplugin.model.Constant;
import com.zaofeng.wechatfunctionplugin.model.ConstantData;
import com.zaofeng.wechatfunctionplugin.model.WeChatUIContract.StatusUI;
import com.zaofeng.wechatfunctionplugin.utils.CheckUtils;
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



    if (statusUi != SnsCommentDetailUI) {
      showToast("该功能只在朋友圈详情页生效");
      return false;
    }

    AccessibilityNodeInfo infoTargetName = findViewById(mService, IdTextTimeLineAuthor);
    if (infoTargetName == null) {
      showToast("请滚动到顶部显示该条朋友圈作者");
      return false;
    }

    String setAuthorName = (String) SPUtils
        .get(mContext, Constant.Comment_Auto_Content, Constant.Empty);
    String authorName = infoTargetName.getText().toString();
    if (!authorName.equals(setAuthorName)) {
      showToast("该条朋友圈作者与插件中输入的名字不符");
      return false;
    }

    return detailCommentHandler(authorName);
  }

  private boolean detailCommentHandler(String authorName) {

    LinkedHashSet<CommentDateModel> originalSet = new LinkedHashSet<>();//原始评论数据
    NodeInfoHelper.getCommentListViewItemInfo(mService,originalSet);//递归到底

    ArrayList<CommentRelationModel> targetList = new ArrayList<>();//目标集合
    ArrayList<CommentRelationModel> coverList = new ArrayList<>();//已经存在的评论集合
    //按规则加工 得到两个数据集合
    RelationUtils.traverseCommentByName(originalSet, authorName, targetList, coverList);
    //得到待处理数据
    List<String> result = RelationUtils.getMapRelationResult(targetList, coverList);

    if (CheckUtils.isEmpty(result)){
      showToast("没有需要处理的内容");
      return false;
    }

    NodeInfoHelper.handlePasteAction(mService,result);
    showToast("处理完成");

    return true;
  }

}
