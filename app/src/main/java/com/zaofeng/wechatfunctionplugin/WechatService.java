package com.zaofeng.wechatfunctionplugin;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Handler;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.zaofeng.wechatfunctionplugin.Model.AutoReplyModel;
import com.zaofeng.wechatfunctionplugin.Model.AutoUploadModel;
import com.zaofeng.wechatfunctionplugin.Model.FastNewFriendAcceptModel;
import com.zaofeng.wechatfunctionplugin.Model.FastNewFriendReplyModel;
import com.zaofeng.wechatfunctionplugin.Model.FastOfflineReplyModel;
import com.zaofeng.wechatfunctionplugin.Model.TimeLineCopyReplyModel;
import com.zaofeng.wechatfunctionplugin.Utils.Constant;
import com.zaofeng.wechatfunctionplugin.Utils.Logger;
import com.zaofeng.wechatfunctionplugin.Utils.PerformUtils;
import com.zaofeng.wechatfunctionplugin.Utils.SPUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collections;
import java.util.List;

/**
 * Created by 李可乐 on 2017/2/5 0005.
 */

public class WechatService extends AccessibilityService {


    public static final String IdListViewChat = "com.tencent.mm:id/a22";//聊天页 ListView
    public static final String IdListViewTimeLine = "com.tencent.mm:id/cn0";//朋友圈 ListView
    public static final String IdListViewFMessageConversation = "com.tencent.mm:id/auv";//新的朋友 ListView
    public static final String IdEditTimeLineUpload = "com.tencent.mm:id/cn4";//朋友圈发布页 输入框
    public static final String IdEditChat = "com.tencent.mm:id/a2v";//聊天页 输入框
    public static final String IdEditTimeLineComment = "com.tencent.mm:id/cjo";//朋友圈评论 输入框
    public static final String IdTextTimeLineContent = "com.tencent.mm:id/co3";//朋友圈主页 文字列表item
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


    private static final long delayTime = 400;//为兼容微信的防抖动处理 的点击延迟时间

    @IntDef({Unknown, LauncherUI, ChatUI, SnsTimeLineUI, SnsUploadUI, SnsCommentDetailUI, AlbumPreviewUI, FMessageConversationUI, ContactInfoUI})
    @Retention(RetentionPolicy.SOURCE)
    public @interface StateUI {
    }

    private boolean isDebug = true;

    private boolean isReleaseCopy = false;//聊天内容快速发布功能 开关
    private boolean isReleaseReply = false;//朋友圈发布快速回复 开关

    private boolean isQuickNewFriendsAccept = false;
    private boolean isQuickNewFriendsReply = false;
    private boolean isQuickOffLine = false;

    private boolean isCommentCopy = false;//朋友圈评论复制后快速回复

    private Context mContext;
    private ClipboardManager clipboardManager;

    @StateUI
    private int stateUi;

    private Handler handler = new Handler();

    private AutoUploadModel autoUploadModel;
    private AutoReplyModel autoReplyModel;//朋友圈发布 快速回复model
    private FastNewFriendAcceptModel fastNewFriendAcceptModel;
    private FastNewFriendReplyModel fastNewFriendReplyModel;
    private FastOfflineReplyModel fastOfflineReplyModel;
    private TimeLineCopyReplyModel lineCopyReplyModel;

    private SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Logger.d("key=" + key);
            if (key.equals(Constant.Release_Copy)) {
                isReleaseCopy = sharedPreferences.getBoolean(Constant.Release_Copy, isReleaseCopy);
            } else if (key.equals(Constant.Release_Reply)) {
                isReleaseReply = sharedPreferences.getBoolean(Constant.Release_Reply, isReleaseReply);
            } else if (key.equals(Constant.Quick_Accept)) {
                isQuickNewFriendsAccept = sharedPreferences.getBoolean(Constant.Quick_Accept, isQuickNewFriendsAccept);
            } else if (key.equals(Constant.Quick_Reply)) {
                isQuickNewFriendsReply = sharedPreferences.getBoolean(Constant.Quick_Reply, isQuickNewFriendsReply);
            } else if (key.equals(Constant.Quick_Offline)) {
                isQuickOffLine = sharedPreferences.getBoolean(Constant.Quick_Offline, isQuickOffLine);
            } else if (key.equals(Constant.Comment_Timeline)) {
                isCommentCopy = sharedPreferences.getBoolean(Constant.Comment_Timeline, isCommentCopy);
            }


        }
    };

    /**
     * 系统会在成功连接上服务时候调用这个方法
     * 初始化参数和工具类
     */
    @Override
    protected void onServiceConnected() {
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;//响应的事件类型
        info.packageNames = new String[]{"com.tencent.mm"};//响应的包名
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK;//反馈类型
        info.notificationTimeout = 100;//响应时间
        this.setServiceInfo(info);
        mContext = getApplicationContext();
        // 得到剪贴板管理器
        clipboardManager = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
        stateUi = Unknown;
        setOperation();

        Logger.d();
    }

    private void setOperation() {
        isReleaseCopy = (boolean) SPUtils.get(mContext, Constant.Release_Copy, false);
        isReleaseReply = (boolean) SPUtils.get(mContext, Constant.Release_Reply, false);

        isQuickNewFriendsAccept = (boolean) SPUtils.get(mContext, Constant.Quick_Accept, false);
        isQuickNewFriendsReply = (boolean) SPUtils.get(mContext, Constant.Quick_Reply, false);
        isQuickOffLine = (boolean) SPUtils.get(mContext, Constant.Quick_Offline, false);

        isCommentCopy = (boolean) SPUtils.get(mContext, Constant.Comment_Timeline, false);

        SPUtils.getSharedPreference(mContext).registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);

    }


    /**
     * @param event [210,1098][1035,1157]
     *              9895
     */
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Logger.d("event date = " + event.toString());
//        Logger.d("getSource = " + event.getSource().toString());
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

                    //正在聊天页
                    onChatUI();

                } else if (className.equals(ClassSnsUploadUI)) {
                    Logger.d("正在朋友圈发布页");
                    stateUi = SnsUploadUI;
                    if (autoUploadModel != null && autoUploadModel.getState() == AutoUploadModel.Choose) {
                        autoUploadFillOutTimeLine();
                    }
                } else if (className.equals(ClassAlbumPreviewUI)) {
                    Logger.d("正在相册选择页");
                    stateUi = AlbumPreviewUI;
                    if (autoUploadModel != null && autoUploadModel.getState() == AutoUploadModel.Jump) {
                        autoUploadWaitChoose();
                    }
                } else if (className.equals(ClassSnsTimeLineUI)) {
                    Logger.d("正在朋友圈页");
                    stateUi = SnsTimeLineUI;

                    if (autoUploadModel != null && autoUploadModel.getState() == AutoUploadModel.Jump) {
                        autoToUploadToChooseTimeLine();
                    }

                    if (autoReplyModel != null && autoReplyModel.getState() == AutoReplyModel.Upload) {
                        autoReplyUploadToChoose();
                    }
                } else if (className.equals(ClassSnsCommentDetailUI)) {
                    Logger.d("正在朋友圈评论详情页");
                    stateUi = SnsCommentDetailUI;

                } else if (className.equals(ClassFMessageConversationUI)) {
                    Logger.d("正在新朋友功能列表");
                    stateUi = FMessageConversationUI;
                    if (fastNewFriendAcceptModel != null && fastNewFriendAcceptModel.getState() == FastNewFriendAcceptModel.OpenRequest) {
                        autoAcceptJump();
                    }


                } else if (className.equals(ClassContactInfoUI)) {
                    Logger.d("正在好友详细资料页");
                    stateUi = ContactInfoUI;
                    if (fastNewFriendAcceptModel != null && fastNewFriendAcceptModel.getState() == FastNewFriendAcceptModel.Accept) {
                        autoAcceptBackMainOrReply();
                    }
                }
                break;

            case AccessibilityEvent.TYPE_VIEW_FOCUSED:
                if (className.equals("android.widget.ListView")) {
                    if (hasViewById(IdListViewChat)) {
                        Logger.d("正在聊天页");
                        stateUi = ChatUI;
                        onChatUI();
                    }
                }
                break;
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED://窗口内容变化事件

                break;
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED://通知事件 toast也包括
                Logger.d("isQuickOffLine=" + isQuickOffLine);
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
                        if (stateUi == SnsTimeLineUI && lineCopyReplyModel != null) {
                            autoCopyFindViewAndCopy();
                        }

                        if (stateUi == SnsCommentDetailUI && lineCopyReplyModel != null) {
                            autoDetailCopyFillOut();
                        }
                        return;
                    }


                }


                break;
            case AccessibilityEvent.TYPE_VIEW_CLICKED://点击事件


                if ("发送".equals(text) && autoReplyModel != null && autoReplyModel.getState() == AutoReplyModel.Start) {
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

                if (isReleaseReply && stateUi == SnsUploadUI) {
                    autoReplySetDate(text);
                }

                break;

            case AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED:

                if (lineCopyReplyModel != null && lineCopyReplyModel.getState() == TimeLineCopyReplyModel.Find) {
                    autoCopyFillOut();
                }

                break;

        }
        Logger.d("stateUi=" + stateUi);

    }

    private void onChatUI() {
        //正在聊天页 且有输入框
        if (hasViewById(IdEditChat)) {
            if (autoReplyModel != null && autoReplyModel.getState() == AutoReplyModel.Choose) {
                autoReplyFillOutReplyContent();
            }

            if (fastNewFriendReplyModel != null && fastNewFriendReplyModel.getState() == FastNewFriendReplyModel.Start) {
                autoNewFriendReply();
            }

            if (fastOfflineReplyModel != null && fastOfflineReplyModel.getState() == FastOfflineReplyModel.OpenRequest) {
                autoOfflineFillOutReplyContent();
            }
        }
    }


    /**
     * 第三步 自动填写
     */
    private void autoCopyFillOut() {
        lineCopyReplyModel.setState(TimeLineCopyReplyModel.FillOut);

        final AccessibilityNodeInfo nodeInfo = findViewById(IdEditTimeLineComment);
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
                PerformUtils.performAction(findViewClickByText("发送"));
            }
        }, delayTime * position);

        lineCopyReplyModel.setState(TimeLineCopyReplyModel.Finish);
        lineCopyReplyModel = null;
    }

    /**
     * 第二步 根据上一步保存的点击事件响应范围 查找之上最近的评论（即该条消息的评论）按钮并点击
     * 通过距离查找是因为 微信在朋友圈评论里读取不到一条条的评论内容 只能通过控件范围距离判断
     */
    private void autoCopyFindViewAndCopy() {

        lineCopyReplyModel.setState(TimeLineCopyReplyModel.Find);
        List<AccessibilityNodeInfo> list = findViewListById(IdButtonComment);
        Rect rectTarget = lineCopyReplyModel.getEventRect();
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
        PerformUtils.performAction(findViewClickById(IdButtonMenuComment));
    }

    /**
     * 第一步 保存长按事件响应范围
     *
     * @param event
     */
    private void autoCopySaveRect(AccessibilityEvent event) {

        /**
         * 拦截朋友圈非目标view的长按事件
         * 根据分析 朋友圈的内容正文和评论正文 差别 在于是否包含点击行为（ACTION_CLICK）
         */
        if (!checkSourceContainsClickAction(event.getSource())) return;

        Rect rect = new Rect();
        event.getSource().getBoundsInScreen(rect);
        lineCopyReplyModel = new TimeLineCopyReplyModel(getClipBoardDate(), rect);
    }

    /**
     * 第二步 自动填写
     */
    private void autoDetailCopyFillOut() {
        autoCopyFillOut();
    }

    /**
     * 第一步 判断是否目标View的长按事件 并初始化
     */
    private void autoDetailCopyClick(AccessibilityEvent event) {

        /**
         * 拦截朋友圈非目标view的长按事件
         */
        if (!checkSourceContainsClickAction(event.getSource())) return;

        lineCopyReplyModel = new TimeLineCopyReplyModel(getClipBoardDate(), null);

        lineCopyReplyModel.setState(TimeLineCopyReplyModel.Find);//详情页只有一个评论按钮 不需要遍历匹配查找


    }

    /**
     * 只有一步 填写新增好友自动回复 并返回主页
     */
    private void autoNewFriendReply() {
        setClipboarDate(fastNewFriendReplyModel.getReplyContent());
        fastNewFriendReplyModel.setState(FastNewFriendReplyModel.FillOut);


        final AccessibilityNodeInfo nodeInfo = findViewById(IdEditChat);
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
                PerformUtils.performAction(findViewClickById(IdButtonSend));
            }
        }, delayTime * position++);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                PerformUtils.performAction(findViewClickByText("返回"));
            }
        }, delayTime * position);

        fastNewFriendReplyModel.setState(FastNewFriendReplyModel.Finish);
        fastNewFriendReplyModel = null;
    }

    /**
     * 第三步 返回主页或回复
     * 关联新增好友自动回复功能
     */
    private void autoAcceptBackMainOrReply() {
        fastNewFriendAcceptModel.setState(FastNewFriendAcceptModel.Finish);
        int position = 0;
        PerformUtils.performAction(findViewClickByText("发消息"));

        if (isQuickNewFriendsReply) {
            String content = (String) SPUtils.get(mContext, Constant.Quick_Reply_Content, Constant.Empty);
            fastNewFriendReplyModel = new FastNewFriendReplyModel(content);
        } else {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    PerformUtils.performAction(findViewClickByText("返回"));
                }
            }, delayTime * ++position);
        }

        fastNewFriendAcceptModel = null;

    }

    /**
     * 第二步 点击按钮跳转界面
     */
    private void autoAcceptJump() {
        fastNewFriendAcceptModel.setState(FastNewFriendAcceptModel.Accept);

        int position = 0;


        AccessibilityNodeInfo infoListView = findViewById(IdListViewFMessageConversation);
        if (infoListView == null) return;
        /**
         * 微信的新朋友列表 采用ListView position=0 为搜索栏
         * 代码 默认点击第一个即 最新的 好友请求 位置为1
         */
        AccessibilityNodeInfo infoFirstItem = infoListView.getChild(1);
        PerformUtils.performAction(findViewClickByText("接受", infoFirstItem));

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                PerformUtils.performAction(findViewClickByText("完成"));
            }
        }, delayTime * ++position);

    }

    /**
     * 第一步 打开通知栏
     *
     * @param event
     */
    private void autoAcceptRequest(AccessibilityEvent event) {

        fastNewFriendAcceptModel = new FastNewFriendAcceptModel();

        Notification notification = (Notification) event.getParcelableData();
        PendingIntent pendingIntent = notification.contentIntent;
        try {
            fastNewFriendAcceptModel.setState(FastNewFriendAcceptModel.OpenRequest);
            pendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            Logger.d(e.toString());
            e.printStackTrace();
            fastNewFriendAcceptModel = null;
        }
    }

    /**
     * 第二步 自动填写离线回复内容
     */
    private void autoOfflineFillOutReplyContent() {

        setClipboarDate(fastOfflineReplyModel.getReplyContent());
        fastOfflineReplyModel.setState(FastOfflineReplyModel.FillOut);

        final AccessibilityNodeInfo nodeInfo = findViewById(IdEditChat);
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
                PerformUtils.performAction(findViewClickById(IdButtonSend));
            }
        }, delayTime * position++);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                PerformUtils.performAction(findViewClickByText("返回"));
            }
        }, delayTime * position);
        fastOfflineReplyModel.setState(FastOfflineReplyModel.Finish);
        fastOfflineReplyModel = null;

    }

    /**
     * 第一步 打开新消息的请求
     *
     * @param event
     */
    private void autoOfflineRequest(AccessibilityEvent event) {
        String content = (String) SPUtils.get(mContext, Constant.Quick_Offline_Content, Constant.Empty);
        fastOfflineReplyModel = new FastOfflineReplyModel(content);

        Notification notification = (Notification) event.getParcelableData();
        PendingIntent pendingIntent = notification.contentIntent;
        try {
            fastOfflineReplyModel.setState(FastOfflineReplyModel.OpenRequest);
            pendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            Logger.d(e.toString());
            e.printStackTrace();
            fastOfflineReplyModel = null;
        }
    }

    /**
     * 第四步 填写快速回复内容
     */
    private void autoReplyFillOutReplyContent() {
        setClipboarDate(autoReplyModel.getReplyContent());
        autoReplyModel.setState(AutoReplyModel.FillOut);

        final AccessibilityNodeInfo nodeInfo = findViewById(IdEditChat);
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
                PerformUtils.performAction(findViewClickById(IdButtonSend));
            }
        }, delayTime * position++);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                PerformUtils.performAction(findViewClickByText("返回"));
            }
        }, delayTime * position);

        autoReplyModel.setState(AutoReplyModel.Finish);
        autoReplyModel = null;

    }


    /**
     * 第三步 检查发布是否成功 然后跳转到主页会话列表
     */
    private void autoReplyUploadToChoose() {
//        List<AccessibilityNodeInfo> list = findViewListById(IdTextTimeLineContent);
//
//        for (AccessibilityNodeInfo item : list) {
//            if (autoReplyModel.getTimeLineContent().equals(item.getText())) {
//                autoReplyModel.setState(AutoReplyModel.Jump);
//                break;
//            }
//        }

        PerformUtils.performAction(findViewClickByText("返回"));

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                List<AccessibilityNodeInfo> listMain = findViewListById(IdButtonBottomMain);
                for (AccessibilityNodeInfo item : listMain) {
                    if (item.getText().equals("微信")) {
                        PerformUtils.performAction(forNodeInfoByClick(item));
                        autoReplyModel.setState(AutoReplyModel.Choose);
                        return;
                    }
                }
            }
        }, delayTime);


    }

    /**
     * 第二步 设置状态为发送成功
     */
    private void autoReplyUploadSuccess() {
        autoReplyModel.setState(AutoReplyModel.Upload);
    }

    /**
     * 第一步 可能会多次调用
     * 初始化传入参数 或 修改参数
     *
     * @param text
     */
    private void autoReplySetDate(String text) {
        if (autoReplyModel == null) {
            String replyContent = (String) SPUtils.get(mContext, Constant.Release_Reply_Content, Constant.Empty);
            autoReplyModel = new AutoReplyModel(text, replyContent);
        } else {
            autoReplyModel.setTimeLineContent(text);
        }
    }


    /**
     * 第四步 填写粘贴板的内容到输入框 并结束
     */
    private void autoUploadFillOutTimeLine() {
        autoUploadModel.setState(AutoUploadModel.FillOut);
        PerformUtils.performAction(findViewById(IdEditTimeLineUpload), AccessibilityNodeInfo.ACTION_PASTE);
        autoUploadModel.setState(AutoUploadModel.Finish);
        autoUploadModel = null;
    }

    /**
     * 第三步 等待用户选择 图片
     */
    private void autoUploadWaitChoose() {
        autoUploadModel.setState(AutoUploadModel.Choose);
    }

    /**
     * 第二步打开朋友圈发布功能 并选择从相册开始
     */
    private void autoToUploadToChooseTimeLine() {

        int position = 1;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                PerformUtils.performAction(findViewClickByText("更多功能按钮"));
//                PerformUtils.performAction(findViewClickById(IdButtonTimeLine));
            }
        }, delayTime * position++);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                PerformUtils.performAction(findViewClickByText("从相册选择"));
            }
        }, delayTime * position);
    }

    /**
     * 第一步 跳转
     *
     * @return
     */
    private void autoToUploadTimeLine() {
        autoUploadModel = new AutoUploadModel(getClipBoardDate());

        autoUploadModel.setState(AutoUploadModel.Jump);

        //微信应该做了防抖动处理 所以需要延迟后执行
        int position = 1;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                PerformUtils.performAction(findViewClickByText("返回"));
                PerformUtils.performAction(findViewClickByText("发现"));
            }
        }, delayTime * position++);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                PerformUtils.performAction(findViewClickByText("朋友圈"));
            }
        }, delayTime * position);


    }


    private boolean checkSourceContainsClickAction(AccessibilityNodeInfo info) {
        List<AccessibilityNodeInfo.AccessibilityAction> actions = info.getActionList();
        boolean isContains = false;
        for (AccessibilityNodeInfo.AccessibilityAction item : actions) {
            if (item == AccessibilityNodeInfo.AccessibilityAction.ACTION_CLICK)
                isContains = true;
        }
        return isContains;
    }

    private boolean hasViewById(String id) {
        AccessibilityNodeInfo info = getRootInActiveWindow();
        if (info == null) return false;
        List<AccessibilityNodeInfo> nodeInfoList = info.findAccessibilityNodeInfosByViewId(id);
        info.recycle();
        return !nodeInfoList.isEmpty();
    }

    private boolean hasViewByText(String text) {
        AccessibilityNodeInfo info = getRootInActiveWindow();
        if (info == null) return false;
        List<AccessibilityNodeInfo> nodeInfoList = info.findAccessibilityNodeInfosByText(text);
        info.recycle();
        return !nodeInfoList.isEmpty();
    }


    private List<AccessibilityNodeInfo> findViewListById(String id) {
        AccessibilityNodeInfo info = getRootInActiveWindow();
        if (info == null) return Collections.emptyList();
        List<AccessibilityNodeInfo> nodeInfoList = info.findAccessibilityNodeInfosByViewId(id);
        info.recycle();
        return nodeInfoList;
    }


    private AccessibilityNodeInfo findViewById(String id) {
        AccessibilityNodeInfo info = getRootInActiveWindow();
        if (info == null) return null;
        List<AccessibilityNodeInfo> nodeInfoList = info.findAccessibilityNodeInfosByViewId(id);
        info.recycle();
        return !nodeInfoList.isEmpty() ? nodeInfoList.get(0) : null;
    }


    private List<AccessibilityNodeInfo> findViewListByText(String text) {
        AccessibilityNodeInfo info = getRootInActiveWindow();
        if (info == null) return Collections.emptyList();
        List<AccessibilityNodeInfo> nodeInfoList = info.findAccessibilityNodeInfosByText(text);
        info.recycle();
        return nodeInfoList;
    }

    private AccessibilityNodeInfo findViewClickById(String id) {
        AccessibilityNodeInfo info = getRootInActiveWindow();
        if (info == null) return null;
        List<AccessibilityNodeInfo> nodeInfoList = info.findAccessibilityNodeInfosByViewId(id);
        info.recycle();
        return forNodeInfoListByClick(nodeInfoList);
    }

    private AccessibilityNodeInfo findViewClickByText(String key) {
        AccessibilityNodeInfo info = getRootInActiveWindow();
        if (info == null) return null;
        List<AccessibilityNodeInfo> nodeInfoList = info.findAccessibilityNodeInfosByText(key);
        info.recycle();
        return forNodeInfoListByClick(nodeInfoList);
    }

    private AccessibilityNodeInfo findViewClickByText(String key, AccessibilityNodeInfo info) {
        if (info == null) return null;
        List<AccessibilityNodeInfo> nodeInfoList = info.findAccessibilityNodeInfosByText(key);
        info.recycle();
        return forNodeInfoListByClick(nodeInfoList);
    }

    /**
     * @param nodeInfoList
     * @return
     */
    @Nullable
    private AccessibilityNodeInfo forNodeInfoListByClick(List<AccessibilityNodeInfo> nodeInfoList) {
        for (int i = nodeInfoList.size() - 1; i >= 0; i--) {
            AccessibilityNodeInfo parent = nodeInfoList.get(i);
            while (parent != null) {
                if (parent.isClickable()) {
                    return parent;
                }
                parent = parent.getParent();
            }
        }
        return null;
    }

    /**
     * 遍历得到可以点击的节点 向上（父节点）遍历
     *
     * @param info
     * @return
     */
    private AccessibilityNodeInfo forNodeInfoByClick(AccessibilityNodeInfo info) {
        AccessibilityNodeInfo parent = info;
        while (parent != null) {
            if (parent.isClickable()) {
                return parent;
            }
            parent = parent.getParent();
        }
        return null;
    }

    private void setClipboarDate(String date) {
        clipboardManager.setPrimaryClip(ClipData.newPlainText(null, date));
    }

    private String getClipBoardDate() {
        if (clipboardManager.hasPrimaryClip()) {
            ClipData clipData = clipboardManager.getPrimaryClip();
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

    @Override
    public void onInterrupt() {
        SPUtils.getSharedPreference(mContext).unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
        Logger.d("服务中断，如授权关闭或者将服务杀死  ");
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        Logger.d(event.toString());
        return super.onKeyEvent(event);
    }
}
