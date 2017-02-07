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

import com.zaofeng.wechatfunctionplugin.Utils.Constant;
import com.zaofeng.wechatfunctionplugin.Utils.SPBuild;
import com.zaofeng.wechatfunctionplugin.Utils.SPUtils;

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
                    .addData(Constant.Release_Reply_Content, "默认的回复文字")
                    .addData(Constant.Quick_Reply_Content, "默认的好友回复文字")
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

        txtPostReplyContent.setText((String) SPUtils.get(mAppContext, Constant.Release_Reply_Content, Constant.Empty));
        txtQuickReplyContent.setText((String) SPUtils.get(mAppContext, Constant.Quick_Reply_Content, Constant.Empty));

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

    @OnCheckedChanged({R.id.check_quick_accept, R.id.check_quick_reply})
    public void onCheckedChangedQuick(CompoundButton button, boolean isChecked) {

        int id = button.getId();
        switch (id) {
            case R.id.check_quick_accept:
                SPUtils.putApply(mAppContext, Constant.Quick_Accept, isChecked);
                break;
            case R.id.check_quick_reply:
                SPUtils.putApply(mAppContext, Constant.Quick_Reply, isChecked);
                break;
        }
    }


    @OnClick({R.id.img_release_reply, R.id.img_quick_reply})
    public void onEditClick(View view) {
        editDialogFragment.show(getSupportFragmentManager(), null);
        editInputDateListener.setView(view);
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
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(intent);
                break;
        }

        return true;
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
            }
        }
    }
}
