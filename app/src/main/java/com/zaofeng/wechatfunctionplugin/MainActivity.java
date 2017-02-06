package com.zaofeng.wechatfunctionplugin;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zaofeng.wechatfunctionplugin.Utils.Constant;
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
    @BindView(R.id.layout_release_reply)
    LinearLayout layoutPostReply;

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

        initDate();

    }

    private void initDate() {
        editDialogFragment = new EditBottomFragment();
        editInputDateListener = new OnEditInputDateListener();
        editDialogFragment.setOnEditInputListener(editInputDateListener);

        checkCopyRelease.setChecked((boolean) SPUtils.get(mAppContext, Constant.Release_Copy, false));
        checkPostReply.setChecked((boolean) SPUtils.get(mAppContext, Constant.Release_Reply, false));

        txtPostReplyContent.setText((String) SPUtils.get(mAppContext, Constant.Release_Reply_Content, ""));

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

    @OnClick({R.id.img_release_reply})
    public void onEditClick(View view) {
        editDialogFragment.show(getSupportFragmentManager(), null);
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
            }
        }
    }
}
