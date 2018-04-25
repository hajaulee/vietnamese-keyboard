package com.hajau.simplekeyboard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.media.AudioManager;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

public class SimpleIME extends InputMethodService implements OnKeyboardActionListener {

    private KeyboardView kv;
    private Keyboard keyboard;

    private boolean caps = false;
    private String nguyen_am = "aăâeêioôơuưyAĂÂEÊIOÔƠUƯY";
    private String currentKeyboard = "vietnam";
    private long LONG_PRESS_DELAY = 400;

    final float MIN_DISTANCE = 100f;
    private float x1 = 0, x2 = 0, y1 = 0, y2 = 0;

    boolean isLongPressed = false;
    Handler handler = new Handler();
    Runnable switchKeyboardTask = new Runnable() {
        public void run() {
            switchKeyboard();
            isLongPressed = true;
        }
    };
    Runnable inputMessage = new Runnable() {
        @Override
        public void run() {
            getCurrentInputConnection().commitText("Con lên đến nhà trọ rồi",1);
        }
    };

    @SuppressLint("InflateParams")
    @Override
    public View onCreateInputView() {
        kv = (KeyboardView) getLayoutInflater().inflate(R.layout.keyboard, null);
        keyboard = new Keyboard(this, R.xml.vietnam);
        kv.setKeyboard(keyboard);
        kv.setOnKeyboardActionListener(this);
        kv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        x1 = event.getX();
                        y1 = event.getY();
                        Log.d("dispatchTouchEvent", "DOWN::\t\t" + x1 + "|" + y1);
                        break;
                    case MotionEvent.ACTION_UP:
                        x2 = event.getX();
                        y2 = event.getY();
                        Log.d("dispatchTouchEvent", "UP::\t\t" + x2 + "|" + y2);
                        boolean isSWipe = false;
                        boolean isVerticalSwipe = false;
                        boolean isHorizontalSwipe = false;
                        float deltaX = x2 - x1;
                        float deltaY = y2 - y1;
                        Log.d("dispatchTouchEvent", "delta::\t\tX:" + deltaX + "|Y:" + deltaY + "|MIN:" + MIN_DISTANCE);
                        if (Math.abs(deltaX) > MIN_DISTANCE)
                            isHorizontalSwipe = true;
                        if (Math.abs(deltaY) > MIN_DISTANCE)
                            isVerticalSwipe = true;
                        isSWipe = (isHorizontalSwipe || isVerticalSwipe);

                        if (isSWipe) {
                            Log.d("dispatchTouchEvent", "SWIPE");
                            if (isHorizontalSwipe) {
                                if (x2 > x1) { //Right swipe
                                    if (!currentKeyboard.equals("vietnam")) {
                                        currentKeyboard = "vietnam";
                                        keyboard = new Keyboard(SimpleIME.this, R.xml.vietnam);
                                    }
                                } else { // Left swipe
                                    if (!currentKeyboard.equals("qwerty")) {
                                        currentKeyboard = "qwerty";
                                        keyboard = new Keyboard(SimpleIME.this, R.xml.qwerty);
                                    }
                                }
                                kv.setKeyboard(keyboard);
                                InputConnection ic = getCurrentInputConnection();
                                if (ic.getTextBeforeCursor(1, 0) == null || ic.getTextBeforeCursor(1, 1).equals("")) {
                                    caps = true;
                                    keyboard.setShifted(caps);
                                    kv.invalidateAllKeys();
                                }
                            }
                        }
                        break;
                }
                return false;
            }
        });
        InputConnection ic = getCurrentInputConnection();
        if (ic.getTextBeforeCursor(1, 0) == null || ic.getTextBeforeCursor(1, 1).equals("")) {
            caps = true;
            keyboard.setShifted(caps);
            kv.invalidateAllKeys();
        }
        return kv;
    }

    private void playClick(int keyCode) {
        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        switch (keyCode) {
            case 32:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR);
                break;
            case Keyboard.KEYCODE_DONE:
            case 10:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_RETURN);
                break;
            case Keyboard.KEYCODE_DELETE:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE);
                break;
            default:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD);
        }
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        InputConnection ic = getCurrentInputConnection();
        playClick(primaryCode);
        switch (primaryCode) {
            case Keyboard.KEYCODE_DELETE:
                if (ic.getSelectedText(0) == null)
                    ic.deleteSurroundingText(1, 0);
                else
                    ic.commitText("", 1);
                if (ic.getTextBeforeCursor(1, 0) == null || ic.getTextBeforeCursor(1, 1).equals("")) {
                    caps = true;
                    keyboard.setShifted(caps);
                    kv.invalidateAllKeys();
                }
                break;
            case Keyboard.KEYCODE_SHIFT:
                caps = !caps;
                keyboard.setShifted(caps);
                kv.invalidateAllKeys();
                break;
            case Keyboard.KEYCODE_DONE:
                //ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                final int options = getCurrentInputEditorInfo().imeOptions;
                final int actionId = options & EditorInfo.IME_MASK_ACTION;

                switch (actionId) {
                    case EditorInfo.IME_ACTION_SEARCH:
                        sendDefaultEditorAction(true);
                        break;
                    case EditorInfo.IME_ACTION_GO:
                        sendDefaultEditorAction(true);
                        break;
                    case EditorInfo.IME_ACTION_SEND:
                        sendDefaultEditorAction(true);
                        break;
                    default:
                        ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                }
                break;
            case 777:
            case 768:
            case 769:
            case 803:
            case 771:
                String lastword = ic.getTextBeforeCursor(10, 0).toString();
                try {
                    lastword = lastword.substring(lastword.lastIndexOf(' ') + 1);
                } catch (Exception e) {
                    break;
                }
                int lengoc = lastword.length();
                if (lengoc < 1)
                    break;
                lastword = lastword.replaceAll("[̀ ́ ̉ ̃ ̣ ]", "");
                int len = lastword.length();
                int i;
                for (i = len - 1; i > 0; i--) {
                    if (nguyen_am.indexOf(lastword.charAt(i)) != -1) {
                        break;
                    }
                }
                String ph_am = lastword.substring(0, len > 2 ? 2 : 1).toLowerCase();
                int vitri = i;
                if (i > 0 && nguyen_am.indexOf(lastword.charAt(i - 1)) != -1 && !ph_am.equals("gi") && !ph_am.equals("qu")
                        && lastword.charAt(i) != 'ơ' && lastword.charAt(i) != 'ê' && i == len - 1) {// Nguyên
                    // âm
                    // 2,3

                    vitri = i - 1;
                } else {// 1 nguyên âm
                    vitri = i;
                }
                ic.deleteSurroundingText(lengoc, 0);
                String Sindex = String.valueOf(lastword.charAt(vitri));
                ic.commitText(lastword.replaceAll(Sindex, Sindex + String.valueOf((char) primaryCode)), 1);
                break;
            case 1998:
                keyboard = new Keyboard(this, R.xml.symbol);
                kv.setKeyboard(keyboard);
                break;
            case 1997:
                if (currentKeyboard.equals("vietnam"))
                    keyboard = new Keyboard(this, R.xml.vietnam);
                else
                    keyboard = new Keyboard(this, R.xml.qwerty);
                kv.setKeyboard(keyboard);
                if (ic.getTextBeforeCursor(1, 0) == null || ic.getTextBeforeCursor(1, 1).equals("")) {
                    caps = true;
                    keyboard.setShifted(caps);
                    kv.invalidateAllKeys();
                }
                break;
            default:
                char code = (char) primaryCode;
                if (Character.isLetter(code) && caps) {
                    code = Character.toUpperCase(code);
                }
                caps = false;
                keyboard.setShifted(caps);
                kv.invalidateAllKeys();
                ic.commitText(String.valueOf(code), 1);
        }
    }

    @Override
    public void onPress(int primaryCode) {
        isLongPressed = false;
        if (primaryCode == '.') {
            handler.postDelayed(switchKeyboardTask, LONG_PRESS_DELAY);
        }
        if (primaryCode == ','){
            handler.postDelayed(inputMessage, LONG_PRESS_DELAY);
        }
    }

    @Override
    public void onRelease(int primaryCode) {
        if(isLongPressed)
            getCurrentInputConnection().deleteSurroundingText(1, 0);
        handler.removeCallbacks(switchKeyboardTask);
        handler.removeCallbacks(inputMessage);
    }

    @Override
    public void onText(CharSequence text) {
    }

    @Override
    public void swipeDown() {
        SimpleIME.this.onFinishInput();
    }

    @Override
    public void swipeLeft() {

    }

    @Override
    public void swipeRight() {

    }

    public void switchKeyboard() {
        InputMethodManager imm = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showInputMethodPicker();
    }

    @Override
    public void swipeUp() {
        switchKeyboard();
    }
}