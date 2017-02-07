package com.zaofeng.wechatfunctionplugin;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
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
    public static final String IdButtonTimeLine = "com.tencent.mm:id/f_";//朋友圈 发布按钮
    public static final String IdEditTimeLineUpload = "com.tencent.mm:id/cn4";//朋友圈发布页 输入框
    public static final String IdTextTimeLineContent = "com.tencent.mm:id/co3";//朋友圈主页 文字列表item
    public static final String IdButtonBottomMain = "com.tencent.mm:id/bq0";//微信主页 底部4个主按钮id
    public static final String IdEditChat = "com.tencent.mm:id/a2v";//聊天页 输入框
    public static final String IdButtonSend = "com.tencent.mm:id/a31";//聊天页 发送按钮


    public static final String ClassLauncherUI = "com.tencent.mm.ui.LauncherUI";//主页 聊天页有时会发布和主页一样事件
    public static final String ClassSnsUploadUI = "com.tencent.mm.plugin.sns.ui.SnsUploadUI";//朋友圈发布页
    public static final String ClassSnsTimeLine = "com.tencent.mm.plugin.sns.ui.SnsTimeLineUI";//朋友圈页
    public static final String ClassAlbumPreviewUI = "com.tencent.mm.plugin.gallery.ui.AlbumPreviewUI";//图片和视频选择列表
    public static final String ClassBizConversationUI = "com.tencent.mm.ui.conversation.BizConversationUI";//订阅号会话列表
    public static final String ClassFMessageConversationUI = "com.tencent.mm.plugin.subapp.ui.friend.FMessageConversationUI";//新朋友功能列表
    public static final String ClassContactInfoUI = "com.tencent.mm.plugin.profile.ui.ContactInfoUI";//好友详细资料页

    private final static int Unknown = 0;//未知
    private final static int LauncherUI = 1;
    private final static int ChatUI = 2;
    private final static int SnsTimeLineUI = 3;
    private final static int SnsUploadUI = 4;
    private final static int AlbumPreviewUI = 5;
    private final static int FMessageConversationUI = 6;
    private final static int ContactInfoUI = 7;


    private static final long delayTime = 400;//为兼容微信的防抖动处理 的点击延迟时间

    @IntDef({Unknown, LauncherUI, ChatUI, SnsTimeLineUI, SnsUploadUI, AlbumPreviewUI, FMessageConversationUI, ContactInfoUI})
    @Retention(RetentionPolicy.SOURCE)
    public @interface StateUI {
    }

    private boolean isDebug = true;

    private boolean isReleaseCopy = false;//聊天内容快速发布功能 开关
    private boolean isReleaseReply = false;//朋友圈发布快速回复 开关

    private boolean isQuickNewFriendsAccept = false;
    private boolean isQuickNewFriendsReply = false;
    private boolean isQuickNotOnLine = true;


    private Context mContext;
    private ClipboardManager clipboardManager;

    @StateUI
    private int stateUi;

    private Handler handler = new Handler();

    private AutoUploadModel autoUploadModel;
    private AutoReplyModel autoReplyModel;
    private FastNewFriendAcceptModel fastNewFriendAcceptModel;
    private FastNewFriendReplyModel fastNewFriendReplyModel;


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
                isQuickNewFriendsReply = sharedPreferences.getBoolean(Constant.Quick_Accept, isQuickNewFriendsReply);
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

        SPUtils.getSharedPreference(mContext).registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);

    }


    /**
     * @param event
     */
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Logger.d("event date=" + event.toString());
        int type = event.getEventType();
        String className = event.getClassName().toString();
        String text = event.getText().isEmpty() ? Constant.Empty : event.getText().get(0).toString();
        switch (type) {
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED://窗口状态变化事件
                if (className.equals(ClassLauncherUI)) {
                    Logger.d("正在主页或聊天页");
                    stateUi = ChatUI;

                } else if (className.equals(ClassSnsUploadUI)) {
                    Logger.d("正在朋友圈发布页");
                    stateUi = SnsUploadUI;
                    if (autoUploadModel != null && autoUploadModel.getState() == AutoUploadModel.Choose) {
                        fastUploadFillOutTimeLine();
                    }
                } else if (className.equals(ClassAlbumPreviewUI)) {
                    Logger.d("正在相册选择页");
                    stateUi = AlbumPreviewUI;
                    if (autoUploadModel != null && autoUploadModel.getState() == AutoUploadModel.Jump) {
                        fastUploadWaitChoose();
                    }
                } else if (className.equals(ClassSnsTimeLine)) {
                    Logger.d("正在朋友圈页");
                    stateUi = SnsTimeLineUI;
                    if (autoReplyModel != null && autoReplyModel.getState() == AutoReplyModel.Start) {
                        fastReplyUploadSuccess();
                    }
                } else if (className.equals(ClassFMessageConversationUI)) {
                    Logger.d("正在新朋友功能列表");
                    stateUi = FMessageConversationUI;
                    if (fastNewFriendAcceptModel != null && fastNewFriendAcceptModel.getState() == FastNewFriendAcceptModel.OpenRequest) {
                        fastAcceptJump();
                    }


                } else if (className.equals(ClassContactInfoUI)) {
                    Logger.d("正在好友详细资料页");
                    stateUi = ContactInfoUI;
                    if (fastNewFriendAcceptModel != null && fastNewFriendAcceptModel.getState() == FastNewFriendAcceptModel.Accept) {
                        fastAcceptBackMain();
                    }
                }
                break;

            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED://窗口内容变化事件
                if (className.equals("android.widget.ListView")) {

                    if (hasViewById(IdListViewChat)) {
                        Logger.d("正在聊天页");
                        stateUi = ChatUI;

                        if (hasViewById(IdEditChat)) {
                            if (autoReplyModel != null && autoReplyModel.getState() == AutoReplyModel.Choose) {
                                fastReplyFillOutReplyContent();
                            }

                            if (fastNewFriendReplyModel != null && fastNewFriendReplyModel.getState() == FastNewFriendReplyModel.Start) {
                                fastNewFriendReply();
                            }
                        }


                    }
                }

                break;
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED://通知事件 toast也包括
                if (isReleaseCopy) {
                    if (stateUi == ChatUI && className.equals("android.widget.Toast$TN") && "已复制".equals(text)) {
                        fastToUploadTimeLine();
                    }
                }

                if (isQuickNewFriendsAccept) {
                    if (className.equals("android.app.Notification") && text.contains("请求添加你为朋友")) {
                        fastAcceptRequest(event);
                    }
                }

                break;
            case AccessibilityEvent.TYPE_VIEW_CLICKED://点击事件


                break;
            case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED:
                if (stateUi == SnsUploadUI) {
                    if (isReleaseReply) {
                        fastReplySetDate(text);
                    }
                }
                break;

        }
        Logger.d("stateUi=" + stateUi);

    }

    /**
     * 只有一步 填写快速回复文本 并返回主页
     */
    private void fastNewFriendReply() {
        setClipboarDate(fastNewFriendReplyModel.getReplyContent());
        fastNewFriendReplyModel.setState(FastNewFriendReplyModel.FillOut);
        AccessibilityNodeInfo nodeInfo = findViewById(IdEditChat);
        PerformUtils.performAction(nodeInfo, AccessibilityNodeInfo.ACTION_FOCUS);
        PerformUtils.performAction(nodeInfo, AccessibilityNodeInfo.ACTION_PASTE);
        PerformUtils.performAction(findViewClickById(IdButtonSend));
        fastNewFriendReplyModel.setState(FastNewFriendReplyModel.Finish);
        fastNewFriendReplyModel = null;
        PerformUtils.performAction(findViewClickByText("返回"));

    }

    /**
     * 第三步 返回主页
     */
    private void fastAcceptBackMain() {
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
    private void fastAcceptJump() {
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
    private void fastAcceptRequest(AccessibilityEvent event) {

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
     * 第三步 填写快速回复内容
     */
    private void fastReplyFillOutReplyContent() {
        setClipboarDate(autoReplyModel.getReplyContent());
        autoReplyModel.setState(AutoReplyModel.FillOut);
        AccessibilityNodeInfo nodeInfo = findViewById(IdEditChat);
        PerformUtils.performAction(nodeInfo, AccessibilityNodeInfo.ACTION_FOCUS);
        PerformUtils.performAction(nodeInfo, AccessibilityNodeInfo.ACTION_PASTE);
        PerformUtils.performAction(findViewClickById(IdButtonSend));
        autoReplyModel.setState(AutoReplyModel.Finish);
        autoReplyModel = null;
    }


    /**
     * 第二步 检查发布是否成功 然后跳转到主页会话列表
     */
    private void fastReplyUploadSuccess() {
        autoReplyModel.setState(AutoReplyModel.Find);
        List<AccessibilityNodeInfo> list = findViewListById(IdTextTimeLineContent);

        for (AccessibilityNodeInfo item : list) {
            Logger.d("getText=" + item.getText());
            if (autoReplyModel.getTimeLineContent().equals(item.getText())) {
                autoReplyModel.setState(AutoReplyModel.Jump);
                break;
            }
        }

        if (autoReplyModel.getState() == AutoReplyModel.Jump) {
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

    }

    /**
     * 第一步 可能会多次调用
     * 初始化传入参数 或 修改参数
     *
     * @param text
     */
    private void fastReplySetDate(String text) {
        if (autoReplyModel == null) {
            String replyContent = (String) SPUtils.get(mContext, Constant.Release_Reply_Content, Constant.Empty);
            autoReplyModel = new AutoReplyModel(text, replyContent);
        } else {
            autoReplyModel.setTimeLineContent(text);
        }
    }


    /**
     * 第三步 填写粘贴板的内容到输入框 并结束
     */
    private void fastUploadFillOutTimeLine() {
        autoUploadModel.setState(AutoUploadModel.FillOut);
        PerformUtils.performAction(findViewById(IdEditTimeLineUpload), AccessibilityNodeInfo.ACTION_PASTE);
        autoUploadModel.setState(AutoUploadModel.Finish);
        autoUploadModel = null;
    }

    /**
     * 第二步 等待用户选择 图片
     */
    private void fastUploadWaitChoose() {
        autoUploadModel.setState(AutoUploadModel.Choose);
    }

    /**
     * 第一步 跳转
     *
     * @return
     */
    private void fastToUploadTimeLine() {
        autoUploadModel = new AutoUploadModel(getClipBoardDate());

        autoUploadModel.setState(AutoUploadModel.Jump);
        PerformUtils.performAction(findViewClickByText("返回"));
        PerformUtils.performAction(findViewClickByText("发现"));
        //微信应该做了防抖动处理 所以需要延迟后执行
        int position = 0;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                PerformUtils.performAction(findViewClickByText("朋友圈"));
            }
        }, delayTime * ++position);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                PerformUtils.performAction(findViewClickByText("更多功能按钮"));
            }
        }, delayTime * ++position);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                PerformUtils.performAction(findViewClickByText("从相册选择"));
            }
        }, delayTime * ++position);

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
