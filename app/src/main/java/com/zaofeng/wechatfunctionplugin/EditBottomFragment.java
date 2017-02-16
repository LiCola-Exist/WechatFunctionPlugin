package com.zaofeng.wechatfunctionplugin;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by 李可乐 on 2017/2/6 0006.
 */

public class EditBottomFragment extends BaseBottomSheetFrag {

    @BindView(R.id.btn_ok)
    Button btnOk;
    @BindView(R.id.edit_input)
    EditText editInput;

    private String content;

    private onEditInputListener onEditInputListener;

    public void setOnEditInputListener(EditBottomFragment.onEditInputListener onEditInputListener) {
        this.onEditInputListener = onEditInputListener;
    }

    @Override
    public int getLayoutResId() {
        return R.layout.layout_edit_bottom;
    }

    @Override
    public void initView() {

    }

    public void showWithKey(FragmentManager manager, String tag, String content){
        this.show(manager, tag);
        this.content = content;
    }

    @Override
    public void resetView() {

        editInput.requestFocus();
        if (!TextUtils.isEmpty(content)){
            editInput.setText(content);
            editInput.setSelection(content.length());
        }
        editInput.post(new Runnable() {
            @Override
            public void run() {
                InputMethodManager inManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                inManager.showSoftInput(editInput, 0);
            }
        });
    }

    @OnClick(R.id.btn_ok)
    public void onClick(View view) {
        String input = editInput.getText().toString();
        if (TextUtils.isEmpty(input)) {
            Toast.makeText(mContext,"请输入内容",Toast.LENGTH_SHORT).show();
            return;
        }

        if (onEditInputListener != null)
            onEditInputListener.onDate(editInput.getText().toString());
        editInput.setText(null);
        close(true);
    }


    public interface onEditInputListener {
        void onDate(String input);
    }
}
