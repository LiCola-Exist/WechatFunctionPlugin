package com.zaofeng.wechatfunctionplugin;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by 李可乐 on 2017/2/13 0013.
 */

public class WindowView {
    private View viewRoot;
    private TextView txtTitle;
    private ImageView imgImage;


    public WindowView(View viewRoot) {
        this.viewRoot = viewRoot;
        txtTitle= (TextView) viewRoot.findViewById(R.id.txt_window_title);
        imgImage= (ImageView) viewRoot.findViewById(R.id.img_window_image);

    }

    public void setViewRootClick(final OnWindowViewClickListener onWindowViewClickListener) {
        viewRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onWindowViewClickListener!=null){
                    onWindowViewClickListener.onWindowClick(v);
                }
            }
        });
    }

    public void setTitle(String text){
        txtTitle.setText(text);
    }

    public View getViewRoot() {
        return viewRoot;
    }

    public interface OnWindowViewClickListener{
        void onWindowClick(View view);
    }

}
