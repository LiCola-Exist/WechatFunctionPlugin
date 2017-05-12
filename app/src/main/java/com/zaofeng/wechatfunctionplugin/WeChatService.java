package com.zaofeng.wechatfunctionplugin;

import static com.zaofeng.wechatfunctionplugin.model.ConstantData.delayTime;
import static com.zaofeng.wechatfunctionplugin.model.ConstantTargetName.ClassAlbumPreviewUI;
import static com.zaofeng.wechatfunctionplugin.model.ConstantTargetName.ClassChattingUI;
import static com.zaofeng.wechatfunctionplugin.model.ConstantTargetName.ClassContactInfoUI;
import static com.zaofeng.wechatfunctionplugin.model.ConstantTargetName.ClassFMessageConversationUI;
import static com.zaofeng.wechatfunctionplugin.model.ConstantTargetName.ClassLauncherUI;
import static com.zaofeng.wechatfunctionplugin.model.ConstantTargetName.ClassSnsCommentDetailUI;
import static com.zaofeng.wechatfunctionplugin.model.ConstantTargetName.ClassSnsTimeLineUI;
import static com.zaofeng.wechatfunctionplugin.model.ConstantTargetName.ClassSnsUploadUI;
import static com.zaofeng.wechatfunctionplugin.model.ConstantTargetName.IdButtonBottomMain;
import static com.zaofeng.wechatfunctionplugin.model.ConstantTargetName.IdButtonComment;
import static com.zaofeng.wechatfunctionplugin.model.ConstantTargetName.IdButtonMenuComment;
import static com.zaofeng.wechatfunctionplugin.model.ConstantTargetName.IdButtonSend;
import static com.zaofeng.wechatfunctionplugin.model.ConstantTargetName.IdEditChat;
import static com.zaofeng.wechatfunctionplugin.model.ConstantTargetName.IdEditTimeLineComment;
import static com.zaofeng.wechatfunctionplugin.model.ConstantTargetName.IdEditTimeLineUpload;
import static com.zaofeng.wechatfunctionplugin.model.ConstantTargetName.IdListViewChat;
import static com.zaofeng.wechatfunctionplugin.model.ConstantTargetName.IdListViewFMessageConversation;
import static com.zaofeng.wechatfunctionplugin.model.WeChatUIContract.AlbumPreviewUI;
import static com.zaofeng.wechatfunctionplugin.model.WeChatUIContract.ChatUI;
import static com.zaofeng.wechatfunctionplugin.model.WeChatUIContract.ContactInfoUI;
import static com.zaofeng.wechatfunctionplugin.model.WeChatUIContract.FMessageConversationUI;
import static com.zaofeng.wechatfunctionplugin.model.WeChatUIContract.SnsCommentDetailUI;
import static com.zaofeng.wechatfunctionplugin.model.WeChatUIContract.SnsTimeLineUI;
import static com.zaofeng.wechatfunctionplugin.model.WeChatUIContract.SnsUploadUI;
import static com.zaofeng.wechatfunctionplugin.model.WeChatUIContract.Unknown;
import static com.zaofeng.wechatfunctionplugin.utils.AccessibilityUtils.findViewById;
import static com.zaofeng.wechatfunctionplugin.utils.AccessibilityUtils.findViewClickById;
import static com.zaofeng.wechatfunctionplugin.utils.AccessibilityUtils.findViewClickByText;
import static com.zaofeng.wechatfunctionplugin.utils.AccessibilityUtils.findViewListById;
import static com.zaofeng.wechatfunctionplugin.utils.AccessibilityUtils.forNodeInfoByClick;
import static com.zaofeng.wechatfunctionplugin.utils.AccessibilityUtils.hasViewById;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;
import com.zaofeng.wechatfunctionplugin.action.MotionCopyCommentAction;
import com.zaofeng.wechatfunctionplugin.model.WeChatUIContract.StateUI;
import com.zaofeng.wechatfunctionplugin.model.event.AutoReplyEvent;
import com.zaofeng.wechatfunctionplugin.model.event.AutoUploadEvent;
import com.zaofeng.wechatfunctionplugin.model.event.CommentCopyEvent;
import com.zaofeng.wechatfunctionplugin.model.event.FastNewFriendAcceptEvent;
import com.zaofeng.wechatfunctionplugin.model.event.FastNewFriendReplyEvent;
import com.zaofeng.wechatfunctionplugin.model.event.FastOfflineReplyEvent;
import com.zaofeng.wechatfunctionplugin.utils.Constant;
import com.zaofeng.wechatfunctionplugin.utils.Logger;
import com.zaofeng.wechatfunctionplugin.utils.PerformUtils;
import com.zaofeng.wechatfunctionplugin.utils.SPUtils;
import java.util.List;


/**
 * Created by 李可乐 on 2017/2/5 0005.
 */

public class WeChatService extends AccessibilityService {


  /**
   * 基本组件
   */
  private Context mContext;
  private Handler handler = new Handler();
  private AccessibilityService mService;
  private ClipboardManager mClipboardManager;
  private WindowView mWindowView;

  private boolean isDebug = BuildConfig.DEBUG;

  /**
   * 变量开关
   */
  private boolean isReleaseCopy = false;//聊天内容快速发布功能 开关
  private boolean isReleaseBack = false;//朋友圈发布快速回复 开关
  private boolean isQuickNewFriendsAccept = false;
  private boolean isQuickNewFriendsReply = false;
  private boolean isQuickOffLine = false;
  private boolean isCommentCopy = false;//朋友圈评论复制后快速回复


  @StateUI
  private int stateUi;

  private MotionCopyCommentAction motionCopyCommentAction;

  private AutoUploadEvent autoUploadEvent;
  private AutoReplyEvent autoReplyEvent;//朋友圈发布 快速回复
  private FastNewFriendAcceptEvent fastNewFriendAcceptEvent;
  private FastNewFriendReplyEvent fastNewFriendReplyEvent;
  private FastOfflineReplyEvent fastOfflineReplyEvent;
  private CommentCopyEvent commentCopyEvent;

  /**
   * 系统会在成功连接上服务时候调用这个方法
   * 初始化参数和工具类
   */
  @Override
  protected void onServiceConnected() {
    mContext = getApplicationContext();
    mService = this;
    this.setServiceInfo(initServiceInfo());
    initManager();
    stateUi = Unknown;
    initOperationVariable();
    initWindowView();
    initAction();
    Logger.d();
  }

  private void initAction() {
    motionCopyCommentAction = new MotionCopyCommentAction(
        mContext, mWindowView, mService,
        (boolean) SPUtils.get(mContext, Constant.Comment_Auto, false));
  }

  private void initWindowView() {
    mWindowView = new WindowView(mContext);
    mWindowView.setViewCheckList(isReleaseCopy, isReleaseBack, isCommentCopy);
    mWindowView.setOnViewRootClick(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if (stateUi == SnsCommentDetailUI) {
          motionCopyCommentAction.action();
        } else {
          Toast.makeText(mContext, "该功能只在朋友圈详情页生效", Toast.LENGTH_SHORT).show();
        }
      }
    });

    mWindowView
        .setOnWindowViewCheckChangeListener(new WindowView.OnWindowViewCheckChangeListener() {

          @Override
          public void onChange(@WindowView.Index int index, boolean isChecked) {
            String key = null;
            switch (index) {
              case WindowView.IndexRelease:
                isReleaseCopy = isChecked;
                key = Constant.Release_Copy;
                break;
              case WindowView.IndexBack:
                isReleaseBack = isChecked;
                key = Constant.Release_Back;
                break;
              case WindowView.IndexComment:
                isCommentCopy = isChecked;
                key = Constant.Comment_Copy;
                break;
            }
            if (key != null) {
              SPUtils.putApply(mContext, key, isChecked);
            }
          }
        });
  }

  /**
   * SP数据监听器实例
   */
  private SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
      Logger.d("key=" + key);
      if (key.equals(Constant.Release_Copy)) {
        isReleaseCopy = sharedPreferences.getBoolean(Constant.Release_Copy, isReleaseCopy);
      } else if (key.equals(Constant.Release_Back)) {
        isReleaseBack = sharedPreferences.getBoolean(Constant.Release_Back, isReleaseBack);
      } else if (key.equals(Constant.Quick_Accept)) {
        isQuickNewFriendsAccept = sharedPreferences
            .getBoolean(Constant.Quick_Accept, isQuickNewFriendsAccept);
      } else if (key.equals(Constant.Quick_Reply)) {
        isQuickNewFriendsReply = sharedPreferences
            .getBoolean(Constant.Quick_Reply, isQuickNewFriendsReply);
      } else if (key.equals(Constant.Quick_Offline)) {
        isQuickOffLine = sharedPreferences.getBoolean(Constant.Quick_Offline, isQuickOffLine);
      } else if (key.equals(Constant.Comment_Copy)) {
        isCommentCopy = sharedPreferences.getBoolean(Constant.Comment_Copy, isCommentCopy);
      } else if (key.equals(Constant.Comment_Auto)) {
        motionCopyCommentAction.setOpen(sharedPreferences.getBoolean(Constant.Comment_Auto, false));
      }

      if (mWindowView != null) {
        mWindowView.setViewCheckList(isReleaseCopy, isReleaseBack, isCommentCopy);
      }
    }
  };


  @Override
  public void onInterrupt() {
    SPUtils.getSharedPreference(mContext)
        .unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
    Logger.d("服务中断，如授权关闭或者将服务杀死");
    mWindowView.removeView();
  }

  @Override
  public boolean onUnbind(Intent intent) {
    Logger.d("服务被解绑");
    mWindowView.removeView();
    return super.onUnbind(intent);
  }

  @Override
  protected boolean onKeyEvent(KeyEvent event) {
    Logger.d(event.toString());
    return super.onKeyEvent(event);
  }

  private void initManager() {
    mClipboardManager = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
  }

  @NonNull
  private AccessibilityServiceInfo initServiceInfo() {
    AccessibilityServiceInfo info = new AccessibilityServiceInfo();
    info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;//响应的事件类型
    info.packageNames = new String[]{"com.tencent.mm"};//响应的包名
    info.feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK;//反馈类型
    info.flags = AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
    info.notificationTimeout = 80;//响应时间
    return info;
  }

  private void initOperationVariable() {
    isReleaseCopy = (boolean) SPUtils.get(mContext, Constant.Release_Copy, false);
    isReleaseBack = (boolean) SPUtils.get(mContext, Constant.Release_Back, false);

    isQuickNewFriendsAccept = (boolean) SPUtils.get(mContext, Constant.Quick_Accept, false);
    isQuickNewFriendsReply = (boolean) SPUtils.get(mContext, Constant.Quick_Reply, false);
    isQuickOffLine = (boolean) SPUtils.get(mContext, Constant.Quick_Offline, false);

    isCommentCopy = (boolean) SPUtils.get(mContext, Constant.Comment_Copy, false);

    SPUtils.getSharedPreference(mContext)
        .registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);

  }

  /**
   * @param event [210,1098][1035,1157]
   * 9895
   */
  @Override
  public void onAccessibilityEvent(AccessibilityEvent event) {
    Logger.d("event date = " + event.toString());
    int type = event.getEventType();
    String className = event.getClassName().toString();
    String text = event.getText().isEmpty() ? Constant.Empty : event.getText().get(0).toString();
    switch (type) {
      case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED://窗口状态变化事件
        if (className.equals(ClassLauncherUI)) {
          Logger.d("正在主页或聊天页");
          stateUi = ChatUI;

        } else if (className.equals(ClassChattingUI)) {
          Logger.d("正在聊天页");
          stateUi = ChatUI;
          //正在聊天页
          onChatUI();

        } else if (className.equals(ClassSnsUploadUI)) {
          Logger.d("正在朋友圈发布页");
          stateUi = SnsUploadUI;
        } else if (className.equals(ClassAlbumPreviewUI)) {
          Logger.d("正在相册选择页");
          stateUi = AlbumPreviewUI;
          if (autoUploadEvent != null && autoUploadEvent.getState() == AutoUploadEvent.Jump) {
            autoUploadWaitChoose();
          }
        } else if (className.equals(ClassSnsTimeLineUI)) {
          Logger.d("正在朋友圈页");
          stateUi = SnsTimeLineUI;

          if (autoUploadEvent != null && autoUploadEvent.getState() == AutoUploadEvent.Jump) {
            autoToUploadToChooseTimeLine();
          }

          if (autoReplyEvent != null && autoReplyEvent.getState() == AutoReplyEvent.Upload) {
            autoReplyUploadToChoose();
          }

          if (autoUploadEvent != null && autoUploadEvent.getState() == AutoUploadEvent.Choose) {
            autoUploadFillOutTimeLine();
          }

        } else if (className.equals(ClassSnsCommentDetailUI)) {
          Logger.d("正在朋友圈评论详情页");
          stateUi = SnsCommentDetailUI;

        } else if (className.equals(ClassFMessageConversationUI)) {
          Logger.d("正在新朋友功能列表");
          stateUi = FMessageConversationUI;
          if (fastNewFriendAcceptEvent != null
              && fastNewFriendAcceptEvent.getState() == FastNewFriendAcceptEvent.OpenRequest) {
            autoAcceptJump();
          }


        } else if (className.equals(ClassContactInfoUI)) {
          Logger.d("正在好友详细资料页");
          stateUi = ContactInfoUI;
          if (fastNewFriendAcceptEvent != null
              && fastNewFriendAcceptEvent.getState() == FastNewFriendAcceptEvent.Accept) {
            autoAcceptBackMainOrReply();
          }
        }
        break;

      case AccessibilityEvent.TYPE_VIEW_FOCUSED:
        if (className.equals("android.widget.ListView")) {
          if (hasViewById(mService, IdListViewChat)) {
            Logger.d("正在聊天页");
            stateUi = ChatUI;
            onChatUI();
          }
        }
        break;
      case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED://窗口内容变化事件

        break;
      case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED://通知事件 toast也包括
        if (isReleaseCopy) {
          if (stateUi == ChatUI && className.equals("android.widget.Toast$TN") && "已复制"
              .equals(text)) {
            autoToUploadTimeLine();
            return;
          }
        }

        if (isQuickNewFriendsAccept) {
          if (className.equals("android.app.Notification") && text.contains("请求添加你为朋友")) {
            autoAcceptRequest(event);
            return;
          }
        }

        if (isQuickOffLine) {
          if (className.equals("android.app.Notification")) {
            autoOfflineRequest(event);
            return;
          }
        }

        if (isCommentCopy) {
          if (className.equals("android.widget.Toast$TN") && "已复制".equals(text)) {
            if (stateUi == SnsTimeLineUI && commentCopyEvent != null) {
              autoCopyFindViewAndCopy();
            }

            if (stateUi == SnsCommentDetailUI && commentCopyEvent != null) {
              autoDetailCopyFillOut();
            }
            return;
          }


        }

        break;
      case AccessibilityEvent.TYPE_VIEW_CLICKED://点击事件

        if ("发送".equals(text) && autoReplyEvent != null
            && autoReplyEvent.getState() == AutoReplyEvent.Start) {
          autoReplyUploadSuccess();
        }

        break;

      case AccessibilityEvent.TYPE_VIEW_LONG_CLICKED:
        if (isCommentCopy) {
          if (stateUi == SnsTimeLineUI) {
            autoCopySaveRect(event);
          }

          if (stateUi == SnsCommentDetailUI) {
            autoDetailCopyClick(event);
          }

        }

        break;

      case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED://view的文字内容改变

        if (isReleaseBack && stateUi == SnsUploadUI) {
          autoReplySetDate(text);
        }

        break;

      case AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED:

        if (commentCopyEvent != null && commentCopyEvent.getState() == CommentCopyEvent.Find) {
          autoCopyFillOut();
        }

        break;

    }
    Logger.d("stateUi=" + stateUi);

  }

  private void onChatUI() {
    //正在聊天页 且有输入框
    if (hasViewById(mService, IdEditChat)) {

      if (fastNewFriendReplyEvent != null
          && fastNewFriendReplyEvent.getState() == FastNewFriendReplyEvent.Start) {
        autoNewFriendReply();
      }

      if (fastOfflineReplyEvent != null
          && fastOfflineReplyEvent.getState() == FastOfflineReplyEvent.OpenRequest) {
        autoOfflineFillOutReplyContent();
      }
    }
  }

  /**
   * 第三步 自动填写
   */
  private void autoCopyFillOut() {
    commentCopyEvent.setState(CommentCopyEvent.FillOut);

    final AccessibilityNodeInfo nodeInfo = findViewById(mService, IdEditTimeLineComment);
    //微信应该做了防抖动处理 所以需要延迟后执行
    int position = 0;
    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        PerformUtils.performAction(nodeInfo, AccessibilityNodeInfo.ACTION_FOCUS);
      }
    }, delayTime * position++);

    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        PerformUtils.performAction(nodeInfo, AccessibilityNodeInfo.ACTION_PASTE);
      }
    }, delayTime * position++);

    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        PerformUtils.performAction(findViewClickByText(mService, "发送"));
      }
    }, delayTime * position);

    commentCopyEvent.setState(CommentCopyEvent.Finish);
    commentCopyEvent = null;
  }

  /**
   * 第二步 根据上一步保存的点击事件响应范围 查找之上最近的评论（即该条消息的评论）按钮并点击
   * 通过距离查找是因为 微信在朋友圈评论里读取不到一条条的评论内容 只能通过控件范围距离判断
   */
  private void autoCopyFindViewAndCopy() {

    commentCopyEvent.setState(CommentCopyEvent.Find);
    List<AccessibilityNodeInfo> list = findViewListById(mService, IdButtonComment);
    Rect rectTarget = commentCopyEvent.getEventRect();
    Rect rectItem = new Rect();

    AccessibilityNodeInfo info = null;
    for (AccessibilityNodeInfo item : list) {
      if (info == null) {
        info = item;
      }
      info.getBoundsInScreen(rectItem);
      int infoMargin = rectTarget.top - rectItem.top;
      item.getBoundsInScreen(rectItem);
      int itemMargin = rectTarget.top - rectItem.top;

      if (itemMargin > 0 && (itemMargin < infoMargin)) {
        info = item;
      }
    }

    PerformUtils.performAction(forNodeInfoByClick(info));
    PerformUtils.performAction(findViewClickById(mService, IdButtonMenuComment));
  }

  /**
   * 第一步 保存长按事件响应范围
   */
  private void autoCopySaveRect(AccessibilityEvent event) {

    /**
     * 拦截朋友圈非目标（评论正文）view的长按事件
     * 根据分析 朋友圈的内容正文和评论正文 差别 评论正文包含点击行为
     */
    if (!PerformUtils
        .containAction(event.getSource(),
            AccessibilityNodeInfo.AccessibilityAction.ACTION_CLICK)) {
      return;
    }

    Rect rect = new Rect();
    event.getSource().getBoundsInScreen(rect);
    commentCopyEvent = new CommentCopyEvent(getClipBoardDate(), rect);
  }

  /**
   * 第二步 自动填写 在朋友圈详情页需要处理 输入框自带的回复某人
   * 使用获取焦点后返回 取消回复某人为评论
   */
  private void autoDetailCopyFillOut() {

    commentCopyEvent.setState(CommentCopyEvent.FillOut);

    final AccessibilityNodeInfo nodeInfo = findViewById(mService, IdEditTimeLineComment);

    //微信应该做了防抖动处理 所以需要延迟后执行
    int position = 0;
    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
//                PerformUtils.performAction(nodeInfo, AccessibilityNodeInfo.ACTION_FOCUS);
        PerformUtils.performAction(nodeInfo);
      }
    }, delayTime * position++);

    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        performGlobalAction(GLOBAL_ACTION_BACK);
      }
    }, delayTime * position++);

    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        PerformUtils.performAction(nodeInfo, AccessibilityNodeInfo.ACTION_PASTE);
      }
    }, delayTime * position++);

    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        PerformUtils.performAction(findViewClickByText(mService, "发送"));
      }
    }, delayTime * position);

    commentCopyEvent.setState(CommentCopyEvent.Finish);
    commentCopyEvent = null;
  }

  /**
   * 第一步 判断是否目标View的长按事件 并初始化
   */
  private void autoDetailCopyClick(AccessibilityEvent event) {

    /**
     * 拦截朋友圈非目标view的长按事件
     */
    if (!PerformUtils
        .containAction(event.getSource(),
            AccessibilityNodeInfo.AccessibilityAction.ACTION_CLICK)) {
      return;
    }

    commentCopyEvent = new CommentCopyEvent(getClipBoardDate(), null);

    commentCopyEvent.setState(CommentCopyEvent.Find);//详情页只有一个评论按钮 不需要遍历匹配查找

  }

  /**
   * 只有一步 填写新增好友自动回复 并返回主页
   */
  private void autoNewFriendReply() {
    setClipBoarDate(fastNewFriendReplyEvent.getReplyContent());
    fastNewFriendReplyEvent.setState(FastNewFriendReplyEvent.FillOut);

    final AccessibilityNodeInfo nodeInfo = findViewById(mService, IdEditChat);
    //微信应该做了防抖动处理 所以需要延迟后执行
    int position = 0;
    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        PerformUtils.performAction(nodeInfo, AccessibilityNodeInfo.ACTION_FOCUS);
      }
    }, delayTime * position++);

    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        PerformUtils.performAction(nodeInfo, AccessibilityNodeInfo.ACTION_PASTE);
      }
    }, delayTime * position++);

    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        PerformUtils.performAction(findViewClickById(mService, IdButtonSend));
      }
    }, delayTime * position++);

    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        PerformUtils.performAction(findViewClickByText(mService, "返回"));
      }
    }, delayTime * position);

    fastNewFriendReplyEvent.setState(FastNewFriendReplyEvent.Finish);
    fastNewFriendReplyEvent = null;
  }

  /**
   * 第三步 返回主页或回复
   * 关联新增好友自动回复功能
   */
  private void autoAcceptBackMainOrReply() {
    fastNewFriendAcceptEvent.setState(FastNewFriendAcceptEvent.Finish);
    int position = 0;
    PerformUtils.performAction(findViewClickByText(mService, "发消息"));

    if (isQuickNewFriendsReply) {
      String content = (String) SPUtils.get(mContext, Constant.Quick_Reply_Content, Constant.Empty);
      fastNewFriendReplyEvent = new FastNewFriendReplyEvent(content);
    } else {
      handler.postDelayed(new Runnable() {
        @Override
        public void run() {
          PerformUtils.performAction(findViewClickByText(mService, "返回"));
        }
      }, delayTime * ++position);
    }

    fastNewFriendAcceptEvent = null;

  }

  /**
   * 第二步 点击按钮跳转界面
   */
  private void autoAcceptJump() {
    fastNewFriendAcceptEvent.setState(FastNewFriendAcceptEvent.Accept);

    int position = 0;

    AccessibilityNodeInfo infoListView = findViewById(mService, IdListViewFMessageConversation);
    if (infoListView == null) {
      return;
    }
    /**
     * 微信的新朋友列表 采用ListView position=0 为搜索栏
     * 代码 默认点击第一个即 最新的 好友请求 位置为1
     */
    AccessibilityNodeInfo infoFirstItem = infoListView.getChild(1);
    PerformUtils.performAction(findViewClickByText(infoFirstItem, "接受"));

    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        PerformUtils.performAction(findViewClickByText(mService, "完成"));
      }
    }, delayTime * ++position);

  }

  /**
   * 第一步 打开通知栏
   */
  private void autoAcceptRequest(AccessibilityEvent event) {

    fastNewFriendAcceptEvent = new FastNewFriendAcceptEvent();

    Notification notification = (Notification) event.getParcelableData();
    PendingIntent pendingIntent = notification.contentIntent;
    try {
      fastNewFriendAcceptEvent.setState(FastNewFriendAcceptEvent.OpenRequest);
      pendingIntent.send();
    } catch (PendingIntent.CanceledException e) {
      Logger.d(e.toString());
      e.printStackTrace();
      fastNewFriendAcceptEvent = null;
    }
  }

  /**
   * 第二步 自动填写离线回复内容
   */
  private void autoOfflineFillOutReplyContent() {

    setClipBoarDate(fastOfflineReplyEvent.getReplyContent());
    fastOfflineReplyEvent.setState(FastOfflineReplyEvent.FillOut);

    final AccessibilityNodeInfo nodeInfo = findViewById(mService, IdEditChat);
    //微信应该做了防抖动处理 所以需要延迟后执行
    int position = 0;
    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        PerformUtils.performAction(nodeInfo, AccessibilityNodeInfo.ACTION_FOCUS);
      }
    }, delayTime * position++);

    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        PerformUtils.performAction(nodeInfo, AccessibilityNodeInfo.ACTION_PASTE);
      }
    }, delayTime * position++);

    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        PerformUtils.performAction(findViewClickById(mService, IdButtonSend));
      }
    }, delayTime * position++);

    handler.postDelayed(new Runnable() {
      @Override
      public void run() {

        PerformUtils.performAction(findViewClickByText(mService, "返回"));
      }
    }, delayTime * position);
    fastOfflineReplyEvent.setState(FastOfflineReplyEvent.Finish);
    fastOfflineReplyEvent = null;

  }

  /**
   * 第一步 打开新消息的请求
   */
  private void autoOfflineRequest(AccessibilityEvent event) {
    String content = (String) SPUtils.get(mContext, Constant.Quick_Offline_Content, Constant.Empty);
    fastOfflineReplyEvent = new FastOfflineReplyEvent(content);

    Notification notification = (Notification) event.getParcelableData();
    PendingIntent pendingIntent = notification.contentIntent;
    try {
      fastOfflineReplyEvent.setState(FastOfflineReplyEvent.OpenRequest);
      pendingIntent.send();
    } catch (PendingIntent.CanceledException e) {
      Logger.d(e.toString());
      e.printStackTrace();
      fastOfflineReplyEvent = null;
    }
  }

  /**
   * 第三步 检查发布是否成功 然后跳转到主页会话列表
   */
  private void autoReplyUploadToChoose() {

    PerformUtils.performAction(findViewClickByText(mService, "返回"));

    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        List<AccessibilityNodeInfo> listMain = findViewListById(mService, IdButtonBottomMain);
        for (AccessibilityNodeInfo item : listMain) {
          if (item.getText().equals("微信")) {
            PerformUtils.performAction(forNodeInfoByClick(item));
            autoReplyEvent.setState(AutoReplyEvent.Jump);
            autoReplyEvent = null;
            return;
          }
        }
      }
    }, delayTime);


  }

  /**
   * 第二步 设置状态为发送成功 并复制文字到粘贴板
   */
  private void autoReplyUploadSuccess() {
    autoReplyEvent.setState(AutoReplyEvent.Upload);
    setClipBoarDate(autoReplyEvent.getReplyContent());
  }

  /**
   * 第一步 可能会多次调用
   * 初始化传入参数 或 修改参数
   */
  private void autoReplySetDate(String text) {
    if (autoReplyEvent == null) {
      String replyContent = (String) SPUtils
          .get(mContext, Constant.Release_Reply_Content, Constant.Empty);
      autoReplyEvent = new AutoReplyEvent(text, replyContent);
    } else {
      autoReplyEvent.setTimeLineContent(text);
    }
  }

  /**
   * 第四步 填写粘贴板的内容到输入框 并结束
   */
  private void autoUploadFillOutTimeLine() {
    autoUploadEvent.setState(AutoUploadEvent.FillOut);
    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        PerformUtils.performAction(findViewById(mService, IdEditTimeLineUpload),
            AccessibilityNodeInfo.ACTION_PASTE);
        autoUploadEvent.setState(AutoUploadEvent.Finish);
        autoUploadEvent = null;
      }
    }, delayTime);

  }

  /**
   * 第三步 等待用户选择 图片
   */
  private void autoUploadWaitChoose() {
    autoUploadEvent.setState(AutoUploadEvent.Choose);
  }

  /**
   * 第二步打开朋友圈发布功能 并选择从相册开始
   */
  private void autoToUploadToChooseTimeLine() {

    PerformUtils.performAction(findViewClickByText(mService, "更多功能按钮"));

    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        PerformUtils.performAction(findViewClickByText(mService, "从相册选择"));
      }
    }, delayTime);
  }

  /**
   * 第一步 跳转
   */
  private void autoToUploadTimeLine() {
    autoUploadEvent = new AutoUploadEvent(getClipBoardDate());

    autoUploadEvent.setState(AutoUploadEvent.Jump);

    PerformUtils.performAction(findViewClickByText(mService, "返回"));
    PerformUtils.performAction(findViewClickByText(mService, "发现"));
    //微信应该做了防抖动处理 所以需要延迟后执行
    int position = 1;

    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        PerformUtils.performAction(findViewClickByText(mService, "朋友圈"));
      }
    }, delayTime);


  }

  private void setClipBoarDate(String date) {
    mClipboardManager.setPrimaryClip(ClipData.newPlainText(null, date));
  }

  private String getClipBoardDate() {
    if (mClipboardManager.hasPrimaryClip()) {
      ClipData clipData = mClipboardManager.getPrimaryClip();
      if (clipData != null && clipData.getItemCount() > 0) {
        return clipData.getItemAt(0).coerceToText(mContext).toString();
      } else {
        Logger.e("not has clip date");
        return null;
      }
    } else {
      Logger.e("not has clip date");
      return null;
    }
  }


}
