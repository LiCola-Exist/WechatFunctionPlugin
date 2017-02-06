package com.zaofeng.wechatfunctionplugin;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.zaofeng.wechatfunctionplugin.Model.AutoUploadModel;
import com.zaofeng.wechatfunctionplugin.Utils.Logger;
import com.zaofeng.wechatfunctionplugin.Utils.PerformUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

/**
 * Created by 李可乐 on 2017/2/5 0005.
 * <p>
 * com.tencent.mm:id/a22 聊天界面的ListView
 * com.tencent.mm:id/f_ 朋友圈发布按钮
 * com.tencent.mm:id/cn4 发布页文字输入框
 */

public class WechatService extends AccessibilityService {

    public static final String ClassLauncherUI = "com.tencent.mm.ui.LauncherUI";//主页
    public static final String ClassSnsUploadUI = "com.tencent.mm.plugin.sns.ui.SnsUploadUI";//朋友圈发布页
    public static final String ClassAlbumPreviewUI = "com.tencent.mm.plugin.gallery.ui.AlbumPreviewUI";//图片和视频选择列表
    public static final String ClassBizConversationUI = "com.tencent.mm.ui.conversation.BizConversationUI";//订阅号会话列表

    private final static int Unknown = 0;//未知
    private final static int LauncherUI = 1;
    private final static int ChatUI = 2;
    private final static int SnsTimeLineUI = 3;
    private final static int SnsUploadUI = 4;
    private final static int AlbumPreviewUI = 5;


    private static final long delayTime = 300;//为兼容微信的防抖动处理 的点击延迟时间

    @IntDef({Unknown, LauncherUI, ChatUI, SnsTimeLineUI, SnsUploadUI, AlbumPreviewUI})
    @Retention(RetentionPolicy.SOURCE)
    public @interface StateUI {
    }

    private boolean isDebug = true;

    private Context mContext;
    private ClipboardManager clipboardManager;

    @StateUI
    private int stateUi;

    private Handler handler = new Handler();

    private AutoUploadModel autoUploadModel;


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
        Logger.d();
    }


    /**
     * @param event
     */
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Logger.d(event.toString());
        int type = event.getEventType();
        String className = event.getClassName().toString();
        switch (type) {
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                if (className.equals(ClassLauncherUI)) {
                    Logger.d("正在主页");
                    stateUi = LauncherUI;
                } else if (className.equals(ClassSnsUploadUI)) {
                    Logger.d("正在朋友圈发布页");
                    if (autoUploadModel != null && autoUploadModel.getState() == AutoUploadModel.Choose) {
                        autoUploadModel.setState(AutoUploadModel.FillOut);
                        PerformUtils.performAction(findViewById("com.tencent.mm:id/cn4"), AccessibilityNodeInfo.ACTION_PASTE);
                        autoUploadModel.setState(AutoUploadModel.Finish);
                    }
                    stateUi = SnsUploadUI;
                } else if (className.equals(ClassAlbumPreviewUI)) {
                    if (autoUploadModel != null && autoUploadModel.getState() == AutoUploadModel.Jump) {
                        autoUploadModel.setState(AutoUploadModel.Choose);
                    }
                }

                break;

            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                if (className.equals("android.widget.ListView")) {

                    if (hasViewById("com.tencent.mm:id/a22")) {
                        Logger.d("正在聊天页");
                        stateUi = ChatUI;
                    }
                }

                break;
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                if (stateUi == ChatUI && className.equals("android.widget.Toast$TN")) {
                    String toastText = event.getText().get(0).toString();
                    if (toastText.contains("已复制")) {
                        //在聊天界面复制的一段文字
                        autoUploadModel = new AutoUploadModel(getClipBoardDate());
                        autoReleaseTimeLine(autoUploadModel);
                    }
                }
                break;
        }
        Logger.d("stateUi=" + stateUi);

    }


    private void autoReleaseTimeLine(AutoUploadModel autoUploadModel) {
        if (autoUploadModel.getState() != AutoUploadModel.Start) return;
        autoUploadModel.setState(AutoUploadModel.Jump);
        int position = 0;
        PerformUtils.performAction(findViewClickByText("返回"));
        PerformUtils.performAction(findViewClickByText("发现"));
        //微信应该做了防抖动处理 所以需要延迟后执行
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                PerformUtils.performAction(findViewClickByText("朋友圈"));
            }
        }, delayTime * ++position);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                PerformUtils.performAction(findViewClickById("com.tencent.mm:id/f_"));
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

    private AccessibilityNodeInfo findViewById(String id) {
        AccessibilityNodeInfo info = getRootInActiveWindow();
        if (info == null) return null;
        List<AccessibilityNodeInfo> nodeInfoList = info.findAccessibilityNodeInfosByViewId(id);
        info.recycle();
        return !nodeInfoList.isEmpty() ? nodeInfoList.get(0) : null;
    }

    private boolean hasViewByText(String text) {
        AccessibilityNodeInfo info = getRootInActiveWindow();
        if (info == null) return false;
        List<AccessibilityNodeInfo> nodeInfoList = info.findAccessibilityNodeInfosByText(text);
        info.recycle();
        return !nodeInfoList.isEmpty();
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
        Logger.d("服务中断，如授权关闭或者将服务杀死  ");
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        Logger.d(event.toString());
        return super.onKeyEvent(event);
    }
}
