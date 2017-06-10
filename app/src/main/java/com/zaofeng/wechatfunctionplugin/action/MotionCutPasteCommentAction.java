package com.zaofeng.wechatfunctionplugin.action;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import com.zaofeng.wechatfunctionplugin.WindowView;
import com.zaofeng.wechatfunctionplugin.action.helper.NodeInfoHelper;
import com.zaofeng.wechatfunctionplugin.model.CommentDateModel;
import com.zaofeng.wechatfunctionplugin.model.CommentRelationModel;
import com.zaofeng.wechatfunctionplugin.model.Constant;
import com.zaofeng.wechatfunctionplugin.model.WeChatUIContract.StatusUI;
import com.zaofeng.wechatfunctionplugin.utils.CheckUtils;
import com.zaofeng.wechatfunctionplugin.utils.RelationUtils;
import com.zaofeng.wechatfunctionplugin.utils.SPUtils;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

import static com.zaofeng.wechatfunctionplugin.model.ConstantTargetName.IdTextTimeLineAuthor;
import static com.zaofeng.wechatfunctionplugin.model.WeChatUIContract.SnsCommentDetailUI;
import static com.zaofeng.wechatfunctionplugin.utils.AccessibilityUtils.findViewById;

/**
 * Created by LiCola on 2017/6/10.
 */

public class MotionCutPasteCommentAction extends BaseAction{

  private List<String> contentPaste;

  public MotionCutPasteCommentAction(Context mContext,
      WindowView mWindowView,
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

    if (step==Step0){
      List<String> result=handleCutComment(setAuthorName);
      if (CheckUtils.isEmpty(result)){
        showToast(String.format(Locale.CHINA,"没有%s的评论内容",authorName));
        return false;
      }else {
        showToast(String.format(Locale.CHINA,"已经剪贴得到%d条评论，可以在其他朋友圈详情下粘贴",result.size()));
        contentPaste =result;
        return true;
      }
    }else if (step==Step1){
      if (handlePasteComment(contentPaste)){
        showToast("处理完成,剪贴板清空");
        contentPaste=null;
        return true;
      }
    }

    return false;
  }

  private boolean handlePasteComment(List<String> contentPaste) {

    if (CheckUtils.isEmpty(contentPaste)){
      showToast("没有剪贴的内容");
      return false;
    }

    NodeInfoHelper.handlePasteAction(mService,contentPaste);
    return true;
  }

  private List<String> handleCutComment(String authorName) {
    LinkedHashSet<CommentDateModel> originalSet = new LinkedHashSet<>();//原始评论数据
    NodeInfoHelper.getCommentListViewItemInfo(mService,originalSet);//递归到底

    List<CommentRelationModel> coverList = new ArrayList<>();//已经存在的评论集合
    //按规则加工 得到已经存在的评论数据集合
    RelationUtils.traverseCommentByName(originalSet, authorName, null, coverList);

    if (coverList.isEmpty()){
      return null;
    }

    List<String> result=new ArrayList<>(coverList.size());

    for (CommentRelationModel model : coverList) {
      result.add(model.getContent());
    }

    return result;
  }
}
