package com.zaofeng.wechatfunctionplugin;

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
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import com.zaofeng.wechatfunctionplugin.model.CommentDateModel;
import com.zaofeng.wechatfunctionplugin.model.CommentRelationModel;
import com.zaofeng.wechatfunctionplugin.model.event.AutoReplyEvent;
import com.zaofeng.wechatfunctionplugin.model.event.AutoUploadEvent;
import com.zaofeng.wechatfunctionplugin.model.event.CommentAutoEvent;
import com.zaofeng.wechatfunctionplugin.model.event.CommentCopyEvent;
import com.zaofeng.wechatfunctionplugin.model.event.FastNewFriendAcceptEvent;
import com.zaofeng.wechatfunctionplugin.model.event.FastNewFriendReplyEvent;
import com.zaofeng.wechatfunctionplugin.model.event.FastOfflineReplyEvent;
import com.zaofeng.wechatfunctionplugin.utils.Constant;
import com.zaofeng.wechatfunctionplugin.utils.Logger;
import com.zaofeng.wechatfunctionplugin.utils.PerformUtils;
import com.zaofeng.wechatfunctionplugin.utils.RelationUtils;
import com.zaofeng.wechatfunctionplugin.utils.SPUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import static com.zaofeng.wechatfunctionplugin.utils.AccessibilityUtils.findViewById;
import static com.zaofeng.wechatfunctionplugin.utils.AccessibilityUtils.findViewClickById;
import static com.zaofeng.wechatfunctionplugin.utils.AccessibilityUtils.findViewClickByText;
import static com.zaofeng.wechatfunctionplugin.utils.AccessibilityUtils.findViewListById;
import static com.zaofeng.wechatfunctionplugin.utils.AccessibilityUtils.forNodeInfoByClick;
import static com.zaofeng.wechatfunctionplugin.utils.AccessibilityUtils.hasViewById;


/**
 * Created by 李可乐 on 2017/2/5 0005.
 */

public class WechatService extends AccessibilityService {

    public static final String IdLayoutTimeLineDetailListItem = "com.tencent.mm:id/cjg";//朋友圈评论详情页 ListView的itemLayout

    public static final String IdListViewChat = "com.tencent.mm:id/a22";//聊天页 ListView
    public static final String IdListViewTimeLine = "com.tencent.mm:id/cn0";//朋友圈 ListView
    public static final String IdListViewTimeLineCommentDetail = "com.tencent.mm:id/cje";//朋友圈详情页 ListView
    public static final String IdListViewFMessageConversation = "com.tencent.mm:id/auv";//新的朋友 ListView

    public static final String IdEditTimeLineUpload = "com.tencent.mm:id/cn4";//朋友圈发布页 输入框
    public static final String IdEditChat = "com.tencent.mm:id/a2v";//聊天页 输入框
    public static final String IdEditTimeLineComment = "com.tencent.mm:id/cjo";//朋友圈评论 输入框
    public static final String IdTextTimeLineContent = "com.tencent.mm:id/co3";//朋友圈主页 文字列表item
    public static final String IdTextTimeLineAuthor = "com.tencent.mm:id/afa";//朋友圈详情页 作者名称
    public static final String IdTextTimeLineCommentTitle = "com.tencent.mm:id/cjk";//朋友圈详情页 评论的标题 包含作者和被回复者
    public static final String IdTextTimeLineCommentContent = "com.tencent.mm:id/cjm";//朋友圈详情页 评论的内容

    public static final String IdButtonBottomMain = "com.tencent.mm:id/bq0";//微信主页 底部4个主按钮id
    public static final String IdButtonTimeLine = "com.tencent.mm:id/f_";//朋友圈 发布按钮
    public static final String IdButtonSend = "com.tencent.mm:id/a31";//聊天页 发送按钮
    public static final String IdButtonComment = "com.tencent.mm:id/cj9";//朋友圈 评论按钮
    public static final String IdButtonMenuComment = "com.tencent.mm:id/cj8";//朋友圈 评论弹出菜单的评论按钮


    public static final String ClassLauncherUI = "com.tencent.mm.ui.LauncherUI";//主页 聊天页有时会发布和主页一样事件
    public static final String ClassChattingUI = "com.tencent.mm.ui.chatting.ChattingUI";//聊天页
    public static final String ClassSnsUploadUI = "com.tencent.mm.plugin.sns.ui.SnsUploadUI";//朋友圈发布页
    public static final String ClassSnsTimeLineUI = "com.tencent.mm.plugin.sns.ui.SnsTimeLineUI";//朋友圈页
    public static final String ClassSnsCommentDetailUI = "com.tencent.mm.plugin.sns.ui.SnsCommentDetailUI";//朋友圈评论详情页
    public static final String ClassAlbumPreviewUI = "com.tencent.mm.plugin.gallery.ui.AlbumPreviewUI";//图片和视频选择列表
    public static final String ClassBizConversationUI = "com.tencent.mm.ui.conversation.BizConversationUI";//订阅号会话列表
    public static final String ClassFMessageConversationUI = "com.tencent.mm.plugin.subapp.ui.friend.FMessageConversationUI";//新朋友功能列表
    public static final String ClassContactInfoUI = "com.tencent.mm.plugin.profile.ui.ContactInfoUI";//好友详细资料页

    private final static int Unknown = 0;//未知
    private final static int LauncherUI = 1;
    private final static int ChatUI = 2;
    private final static int SnsTimeLineUI = 3;
    private final static int SnsUploadUI = 4;
    private final static int SnsCommentDetailUI = 5;
    private final static int AlbumPreviewUI = 6;
    private final static int FMessageConversationUI = 7;
    private final static int ContactInfoUI = 8;


    private static final long delayTime = 600;//为兼容微信的防抖动处理 的点击延迟时间

    @IntDef({Unknown, LauncherUI, ChatUI, SnsTimeLineUI, SnsUploadUI, SnsCommentDetailUI, AlbumPreviewUI, FMessageConversationUI, ContactInfoUI})
    @Retention(RetentionPolicy.SOURCE)
    public @interface StateUI {
    }

    private boolean isDebug = true;

    private boolean isReleaseCopy = false;//聊天内容快速发布功能 开关
    private boolean isReleaseBack = false;//朋友圈发布快速回复 开关

    private boolean isQuickNewFriendsAccept = false;
    private boolean isQuickNewFriendsReply = false;
    private boolean isQuickOffLine = false;

    private boolean isCommentCopy = false;//朋友圈评论复制后快速回复
    private boolean isCommentAuto = false;//朋友圈评论自动回复

    private Context mContext;
    private AccessibilityService mService;
    private ClipboardManager mClipboardManager;

    private WindowView mWindowView;


    @StateUI
    private int stateUi;

    private Handler handler = new Handler();

    private AutoUploadEvent autoUploadEvent;
    private AutoReplyEvent autoReplyEvent;//朋友圈发布 快速回复
    private FastNewFriendAcceptEvent fastNewFriendAcceptEvent;
    private FastNewFriendReplyEvent fastNewFriendReplyEvent;
    private FastOfflineReplyEvent fastOfflineReplyEvent;
    private CommentCopyEvent commentCopyEvent;
    private CommentAutoEvent commentAutoEvent;


    private SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Logger.d("key=" + key);
            if (key.equals(Constant.Release_Copy)) {
                isReleaseCopy = sharedPreferences.getBoolean(Constant.Release_Copy, isReleaseCopy);
            } else if (key.equals(Constant.Release_Back)) {
                isReleaseBack = sharedPreferences.getBoolean(Constant.Release_Back, isReleaseBack);
            } else if (key.equals(Constant.Quick_Accept)) {
                isQuickNewFriendsAccept = sharedPreferences.getBoolean(Constant.Quick_Accept, isQuickNewFriendsAccept);
            } else if (key.equals(Constant.Quick_Reply)) {
                isQuickNewFriendsReply = sharedPreferences.getBoolean(Constant.Quick_Reply, isQuickNewFriendsReply);
            } else if (key.equals(Constant.Quick_Offline)) {
                isQuickOffLine = sharedPreferences.getBoolean(Constant.Quick_Offline, isQuickOffLine);
            } else if (key.equals(Constant.Comment_Copy)) {
                isCommentCopy = sharedPreferences.getBoolean(Constant.Comment_Copy, isCommentCopy);
            } else if (key.equals(Constant.Comment_Auto)) {
                isCommentAuto = sharedPreferences.getBoolean(Constant.Comment_Auto, isCommentAuto);
            }

            if (mWindowView != null) {
                mWindowView.setViewCheckList(isReleaseCopy, isReleaseBack, isCommentCopy);
            }

        }
    };

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
        Logger.d();
    }

    private void initWindowView() {
        mWindowView = new WindowView(mContext);
        mWindowView.setViewCheckList(isReleaseCopy, isReleaseBack, isCommentCopy);
        mWindowView.setOnViewRootClick(new WindowView.OnWindowViewClickListener() {
            @Override
            public void onWindowClick(View view) {
                if (isCommentAuto) {
                    if (stateUi == SnsCommentDetailUI) {
                        if (commentAutoEvent == null) {
                            startAutoHandleComment();
                        }
                    } else {
                        Toast.makeText(mContext, "该功能只在朋友圈详情页生效", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(mContext, "请开启自动回复评论", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mWindowView.setOnWindowViewCheckChangeListener(new WindowView.OnWindowViewCheckChangeListener() {

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

    private void startAutoHandleComment() {
        commentAutoEvent = new CommentAutoEvent();
        mWindowView.setMainTitle("任务开始");

        AccessibilityNodeInfo infoTargetName = findViewById(mService, IdTextTimeLineAuthor);
        if (infoTargetName == null) {
            mWindowView.setMainTitle("点击开始");
            commentAutoEvent.setState(CommentAutoEvent.Finish);
            commentAutoEvent = null;
            Toast.makeText(mContext, "请滚动到顶部显示该条朋友圈作者", Toast.LENGTH_SHORT).show();
            return;
        }
        String authorName = infoTargetName.getText().toString();
        String setAuthorName = (String) SPUtils.get(mContext, Constant.Comment_Auto_Content, Constant.Empty);
        if (!authorName.equals(setAuthorName)) {
            mWindowView.setMainTitle("点击开始");
            commentAutoEvent.setState(CommentAutoEvent.Finish);
            commentAutoEvent = null;
            Toast.makeText(mContext, "该条朋友圈作者与插件中输入的名字不符", Toast.LENGTH_SHORT).show();
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
            mWindowView.setMainTitle("点击开始");
            commentAutoEvent.setState(CommentAutoEvent.Finish);
            commentAutoEvent = null;
            return;
        }

        final AccessibilityNodeInfo nodeInfo = findViewById(mService, IdEditTimeLineComment);

        try {
            //清除输入框带有的回复
            PerformUtils.performAction(nodeInfo);
            Thread.sleep(delayTime);
            performGlobalAction(GLOBAL_ACTION_BACK);
            Thread.sleep(delayTime);

            //遍历自动发送
            for (String item : result) {
                Logger.d("comment item=" + item);
                Bundle arguments = new Bundle();
                arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, item);
                PerformUtils.performAction(nodeInfo, AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
                Thread.sleep(delayTime >> 1);
                PerformUtils.performAction(findViewClickByText(mService, "发送"));
                Thread.sleep(delayTime >> 1);
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mWindowView.setMainTitle("点击开始");
        commentAutoEvent.setState(CommentAutoEvent.Finish);
        commentAutoEvent = null;
    }


    /**
     * 获取评论列表 的关键数据 包含去重操作（set集合实现）和自动滚动 递归调用方法
     *
     * @param setDate
     * @return 返回有序去重的文字数据
     */
    private LinkedHashSet<CommentDateModel> getCommentListViewItemInfo(LinkedHashSet<CommentDateModel> setDate) {
        final int indexValidStart = 0;/*评论Layout在父容器中的有效起始 索引值 因为评论列表索引肯定大于0*/
        final int indexViewTitle = 1;/*评论标题 位置索引*/
        final int indexViewContent = 3;/*评论内容 位置索引*/

        List<AccessibilityNodeInfo> infoList = findViewListById(mService, IdLayoutTimeLineDetailListItem);

        CommentDateModel itemModel;
        int index;
        String title;
        String content;

        for (AccessibilityNodeInfo itemInfo : infoList) {
            //获取索引 兼容处理 行索引有效还是列索引有效问题 即判定是否大于有效起始值
            index = itemInfo.getCollectionItemInfo().getRowIndex() > indexValidStart ? itemInfo.getCollectionItemInfo().getRowIndex() : itemInfo.getCollectionItemInfo().getColumnIndex();

            //非空检查
            if (itemInfo.getChild(indexViewTitle) != null && itemInfo.getChild(indexViewContent) != null) {
                //trim() 是为去除控件产生的空格
                title = itemInfo.getChild(indexViewTitle).getText().toString().trim();
                content = itemInfo.getChild(indexViewContent).getText().toString().trim();
                itemModel = new CommentDateModel(index, title, content);
                setDate.add(itemModel);
            }
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

    @Override
    public void onInterrupt() {
        SPUtils.getSharedPreference(mContext).unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
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
        isCommentAuto = (boolean) SPUtils.get(mContext, Constant.Comment_Auto, false);

        SPUtils.getSharedPreference(mContext).registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);

    }

    /**
     * @param event [210,1098][1035,1157]
     *              9895
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
                    if (autoUploadEvent != null && autoUploadEvent.getState() == AutoUploadEvent.Choose) {
                        autoUploadFillOutTimeLine();
                    }
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

                } else if (className.equals(ClassSnsCommentDetailUI)) {
                    Logger.d("正在朋友圈评论详情页");
                    stateUi = SnsCommentDetailUI;

                } else if (className.equals(ClassFMessageConversationUI)) {
                    Logger.d("正在新朋友功能列表");
                    stateUi = FMessageConversationUI;
                    if (fastNewFriendAcceptEvent != null && fastNewFriendAcceptEvent.getState() == FastNewFriendAcceptEvent.OpenRequest) {
                        autoAcceptJump();
                    }


                } else if (className.equals(ClassContactInfoUI)) {
                    Logger.d("正在好友详细资料页");
                    stateUi = ContactInfoUI;
                    if (fastNewFriendAcceptEvent != null && fastNewFriendAcceptEvent.getState() == FastNewFriendAcceptEvent.Accept) {
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
                    if (stateUi == ChatUI && className.equals("android.widget.Toast$TN") && "已复制".equals(text)) {
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


                if ("发送".equals(text) && autoReplyEvent != null && autoReplyEvent.getState() == AutoReplyEvent.Start) {
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

            if (fastNewFriendReplyEvent != null && fastNewFriendReplyEvent.getState() == FastNewFriendReplyEvent.Start) {
                autoNewFriendReply();
            }

            if (fastOfflineReplyEvent != null && fastOfflineReplyEvent.getState() == FastOfflineReplyEvent.OpenRequest) {
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
     *
     * @param event
     */
    private void autoCopySaveRect(AccessibilityEvent event) {

        /**
         * 拦截朋友圈非目标（评论正文）view的长按事件
         * 根据分析 朋友圈的内容正文和评论正文 差别 评论正文包含点击行为
         */
        if (!PerformUtils.containAction(event.getSource(), AccessibilityNodeInfo.AccessibilityAction.ACTION_CLICK))
            return;

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
        if (!PerformUtils.containAction(event.getSource(), AccessibilityNodeInfo.AccessibilityAction.ACTION_CLICK))
            return;

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
        if (infoListView == null) return;
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
     *
     * @param event
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
     *
     * @param event
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
     *
     * @param text
     */
    private void autoReplySetDate(String text) {
        if (autoReplyEvent == null) {
            String replyContent = (String) SPUtils.get(mContext, Constant.Release_Reply_Content, Constant.Empty);
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
        PerformUtils.performAction(findViewById(mService, IdEditTimeLineUpload), AccessibilityNodeInfo.ACTION_PASTE);
        autoUploadEvent.setState(AutoUploadEvent.Finish);
        autoUploadEvent = null;
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
     *
     * @return
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
