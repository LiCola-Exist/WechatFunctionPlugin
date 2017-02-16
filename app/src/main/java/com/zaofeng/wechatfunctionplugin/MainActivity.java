package com.zaofeng.wechatfunctionplugin;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zaofeng.wechatfunctionplugin.utils.Constant;
import com.zaofeng.wechatfunctionplugin.utils.SPBuild;
import com.zaofeng.wechatfunctionplugin.utils.SPUtils;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.check_release_copy)
    CheckBox checkCopyRelease;
    @BindView(R.id.layout_release_copy)
    LinearLayout layoutCopyRelease;

    @BindView(R.id.check_release_reply)
    CheckBox checkPostReply;
    @BindView(R.id.txt_release_reply_content)
    TextView txtPostReplyContent;
    @BindView(R.id.img_release_reply)
    ImageView imgPostReply;


    @BindView(R.id.check_quick_accept)
    CheckBox checkQuickAccept;
    @BindView(R.id.layout_quick_accept)
    LinearLayout layoutQuickAccept;

    @BindView(R.id.check_quick_reply)
    CheckBox checkQuickReply;
    @BindView(R.id.txt_quick_reply_content)
    TextView txtQuickReplyContent;
    @BindView(R.id.img_quick_reply)
    ImageView imgQuickReply;

    @BindView(R.id.check_quick_offline)
    CheckBox checkQuickOffline;
    @BindView(R.id.txt_quick_offline_content)
    TextView txtQuickOfflineContent;
    @BindView(R.id.img_quick_offline)
    ImageView imgQuickOffline;

    @BindView(R.id.check_comment_timeline)
    CheckBox checkCommentTimeLine;

    @BindView(R.id.check_comment_auto)
    CheckBox checkCommentAuto;
    @BindView(R.id.txt_comment_auto_content)
    TextView txtCommentAutoContent;
    @BindView(R.id.img_comment_auto)
    ImageView imgCommentAuto;


    private Context mContext;
    private Context mAppContext;

    private EditBottomFragment editDialogFragment;
    private OnEditInputDateListener editInputDateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        mAppContext = getApplicationContext();
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initFirstDate();
        initDate();

    }

    private void initFirstDate() {
        boolean isFirst = (boolean) SPUtils.get(mAppContext, Constant.First, true);
        if (isFirst) {
            new SPBuild(mAppContext)
                    .addData(Constant.First, false)
                    .addData(Constant.Release_Copy, true)
                    .addData(Constant.Release_Reply, true)
                    .addData(Constant.Quick_Accept, true)
                    .addData(Constant.Quick_Reply, true)
                    .addData(Constant.Quick_Offline, false)
                    .addData(Constant.Comment_Timeline, true)
                    .addData(Constant.Comment_Auto, true)
                    .addData(Constant.Release_Reply_Content, "默认的回复文字")
                    .addData(Constant.Quick_Reply_Content, "默认的好友回复文字")
                    .addData(Constant.Quick_Offline_Content, "默认的离线回复文字")
                    .addData(Constant.Comment_Auto_Content, "朵朵的名字？")
                    .build();
        }

    }

    private void initDate() {
        editDialogFragment = new EditBottomFragment();
        editInputDateListener = new OnEditInputDateListener();
        editDialogFragment.setOnEditInputListener(editInputDateListener);

        checkCopyRelease.setChecked((boolean) SPUtils.get(mAppContext, Constant.Release_Copy, false));
        checkPostReply.setChecked((boolean) SPUtils.get(mAppContext, Constant.Release_Reply, false));

        checkQuickAccept.setChecked((boolean) SPUtils.get(mAppContext, Constant.Quick_Accept, false));
        checkQuickReply.setChecked((boolean) SPUtils.get(mAppContext, Constant.Quick_Reply, false));
        checkQuickOffline.setChecked((boolean) SPUtils.get(mAppContext, Constant.Quick_Offline, false));

        checkCommentTimeLine.setChecked((boolean) SPUtils.get(mAppContext, Constant.Comment_Timeline, false));
        checkCommentAuto.setChecked((boolean) SPUtils.get(mAppContext, Constant.Comment_Auto, false));


        txtPostReplyContent.setText((String) SPUtils.get(mAppContext, Constant.Release_Reply_Content, Constant.Empty));

        txtQuickReplyContent.setText((String) SPUtils.get(mAppContext, Constant.Quick_Reply_Content, Constant.Empty));
        txtQuickOfflineContent.setText((String) SPUtils.get(mAppContext, Constant.Quick_Offline_Content, Constant.Empty));

        txtCommentAutoContent.setText((String) SPUtils.get(mAppContext, Constant.Comment_Auto_Content, Constant.Empty));


    }

    @OnCheckedChanged({R.id.check_release_copy, R.id.check_release_reply})
    public void onCheckedChangedRelease(CompoundButton button, boolean isChecked) {

        int id = button.getId();
        switch (id) {
            case R.id.check_release_copy:
                SPUtils.putApply(mAppContext, Constant.Release_Copy, isChecked);
                break;
            case R.id.check_release_reply:
                SPUtils.putApply(mAppContext, Constant.Release_Reply, isChecked);
                break;
        }
    }

    @OnCheckedChanged({R.id.check_quick_accept, R.id.check_quick_reply, R.id.check_quick_offline})
    public void onCheckedChangedQuick(CompoundButton button, boolean isChecked) {

        int id = button.getId();
        switch (id) {
            case R.id.check_quick_accept:
                SPUtils.putApply(mAppContext, Constant.Quick_Accept, isChecked);
                break;
            case R.id.check_quick_reply:
                SPUtils.putApply(mAppContext, Constant.Quick_Reply, isChecked);
                break;
            case R.id.check_quick_offline:
                SPUtils.putApply(mAppContext, Constant.Quick_Offline, isChecked);
                break;
        }
    }

    @OnCheckedChanged({R.id.check_comment_timeline,R.id.check_comment_auto})
    public void onCheckChangedComment(CompoundButton button, boolean isChecked) {
        int id = button.getId();
        switch (id) {
            case R.id.check_comment_timeline:
                SPUtils.putApply(mAppContext, Constant.Comment_Timeline, isChecked);
                break;
            case R.id.check_comment_auto:
                SPUtils.putApply(mAppContext, Constant.Comment_Auto, isChecked);
                break;
        }
    }


    @OnClick({R.id.img_release_reply, R.id.img_quick_reply, R.id.img_quick_offline,R.id.img_comment_auto})
    public void onEditClick(View view) {
        String content=null;
        switch (view.getId()){
            case R.id.img_release_reply:
                content=txtPostReplyContent.getText().toString();
                break;
            case R.id.img_quick_reply:
                content=txtQuickReplyContent.getText().toString();
                break;
            case R.id.img_quick_offline:
                content=txtQuickOfflineContent.getText().toString();
                break;
            case R.id.img_comment_auto:
                content=txtCommentAutoContent.getText().toString();
                break;
        }

        editDialogFragment.showWithKey(getSupportFragmentManager(),null,content);
        editInputDateListener.setView(view);
    }


    private class OnEditInputDateListener implements EditBottomFragment.onEditInputListener {

        private View view;

        public void setView(View view) {
            this.view = view;
        }

        @Override
        public void onDate(String input) {
            if (view == null) return;
            int id = view.getId();
            switch (id) {
                case R.id.img_release_reply:
                    txtPostReplyContent.setText(input);
                    SPUtils.putApply(mAppContext, Constant.Release_Reply_Content, input);
                    break;
                case R.id.img_quick_reply:
                    txtQuickReplyContent.setText(input);
                    SPUtils.putApply(mAppContext, Constant.Quick_Reply_Content, input);
                    break;
                case R.id.img_quick_offline:
                    txtQuickOfflineContent.setText(input);
                    SPUtils.putApply(mAppContext, Constant.Quick_Offline_Content, input);
                    break;
                case R.id.img_comment_auto:
                    txtCommentAutoContent.setText(input);
                    SPUtils.putApply(mAppContext, Constant.Comment_Auto_Content, input);
                    break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_setting:
                String toastContent = String.format(Locale.CHINA, "请在更多设置->无障碍->开启%s服务", mAppContext.getString(R.string.app_name));
                Toast.makeText(mContext, toastContent, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(intent);
                break;
        }

        return true;
    }
}
