package com.hajau.simplekeyboard;

import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;

public class CustomPopupKeyboardView extends KeyboardView {

    public CustomPopupKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomPopupKeyboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        this.requestFocus();
    }

    private void handlePopupOpened(){
        Keyboard.Key firstKey = getKeyboard().getKeys().get(0);
        firstKey.pressed = true;
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
//        handlePopupOpened();
    }

}