package com.hajau.simplekeyboard;


import android.annotation.SuppressLint;
import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.media.AudioManager;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

public class MainIME extends InputMethodService implements OnKeyboardActionListener {

    final long LONG_PRESS_DELAY = 400;
    final float MIN_DISTANCE = 100f;
    private String nguyen_am = "aăâeêioôơuưyAĂÂEÊIOÔƠUƯY";


    private KeyboardView keyboardView;

    private AllKeyboards allKeyboards;

    private boolean shiftKey = false;

    private String currentKeyboard = "vietnam";
    private float x1 = 0, x2 = 0, y1 = 0, y2 = 0;

    boolean isLongPressed = false;
    Handler handler = new Handler();
    Runnable switchKeyboardTask = () -> {
        switchKeyboard();
        isLongPressed = true;
    };

    @SuppressLint("InflateParams")
    @Override
    public View onCreateInputView() {
        keyboardView = (KeyboardView) getLayoutInflater().inflate(R.layout.keyboard, null);
        allKeyboards = new AllKeyboards(this);
        keyboardView.setKeyboard(allKeyboards.vietnam);
        keyboardView.setOnKeyboardActionListener(this);

        InputConnection ic = getCurrentInputConnection();
        if (isPointedInHeadOfText(ic)) {
            shiftKey = true;
            this.updateShiftKey();
        }
        return keyboardView;
    }

    private static boolean isPointedInHeadOfText(InputConnection ic) {
        return ic.getTextBeforeCursor(1, 0) == null || ic.getTextBeforeCursor(1, 1).equals("");
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
                handleDeleteKey(ic);
                break;
            case Keyboard.KEYCODE_SHIFT:
                shiftKey = !shiftKey;
                this.updateShiftKey();
                break;
            case Keyboard.KEYCODE_DONE:
                handleEnterKey(ic);
                break;
            case 777:
            case 768:
            case 769:
            case 803:
            case 771:
                handleToneCharacter((char) primaryCode, ic);
                break;
            case 1997:
                this.setAbcKeyboard();
                break;
            case 1998:
                keyboardView.setKeyboard(allKeyboards.symbol);
                break;
            case 1999:
                this.switchLanguage();
                break;
            default:
                handleNormalKey((char) primaryCode, ic);
        }
        this.updateShiftKey();
    }

    private void handleNormalKey(char primaryCode, InputConnection ic) {
        char code = primaryCode;
        if (Character.isLetter(code) && shiftKey) {
            code = Character.toUpperCase(code);
        }
        shiftKey = false;
        ic.commitText(String.valueOf(code), 1);
    }

    private void handleToneCharacter(char primaryCode, InputConnection ic) {
        String lastword = ic.getTextBeforeCursor(10, 0).toString();
        try {
            lastword = lastword.substring(lastword.lastIndexOf(' ') + 1);
        } catch (Exception e) {
            return;
        }
        int lengoc = lastword.length();
        if (lengoc < 1)
            return;
        lastword = lastword.replaceAll("[̀ ́ ̉ ̃ ̣ ]", "");
        int vitri = getVitri(lastword);
        ic.deleteSurroundingText(lengoc, 0);
        String Sindex = String.valueOf(lastword.charAt(vitri));
        ic.commitText(lastword.replaceAll(Sindex, Sindex + String.valueOf(primaryCode)), 1);
    }

    private void handleEnterKey(InputConnection ic) {
        //ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
        final int options = getCurrentInputEditorInfo().imeOptions;
        final int actionId = options & EditorInfo.IME_MASK_ACTION;

        switch (actionId) {
            case EditorInfo.IME_ACTION_SEARCH:
            case EditorInfo.IME_ACTION_SEND:
            case EditorInfo.IME_ACTION_GO:
                sendDefaultEditorAction(true);
                break;
            default:
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
        }
    }

    private void handleDeleteKey(InputConnection ic) {
        if (ic.getSelectedText(0) == null)
            ic.deleteSurroundingText(1, 0);
        else
            ic.commitText("", 1);
        if (isPointedInHeadOfText(ic)) {
            shiftKey = true;
        }
    }

    private int getVitri(String lastWord) {
        int len = lastWord.length();
        int i;
        for (i = len - 1; i > 0; i--) {
            if (nguyen_am.indexOf(lastWord.charAt(i)) != -1) {
                break;
            }
        }
        String ph_am = lastWord.substring(0, len > 2 ? 2 : 1).toLowerCase();
        int vitri = i;
        if (
                i > 0 && nguyen_am.indexOf(lastWord.charAt(i - 1)) != -1 && !ph_am.equals("gi") && !ph_am.equals("qu")
                && lastWord.charAt(i) != 'ơ' && lastWord.charAt(i) != 'ê' && i == len - 1
        ) {
            // Nguyên
            // âm
            // 2,3

            vitri = i - 1;
        }


        return vitri;
    }

    @Override
    public void onPress(int primaryCode) {
        isLongPressed = false;
        if (primaryCode == 1999) {
            handler.postDelayed(switchKeyboardTask, LONG_PRESS_DELAY);
        }
        if (primaryCode == ',') {
            handler.postDelayed(inputMessage, LONG_PRESS_DELAY);
        }
    }

    @Override
    public void onRelease(int primaryCode) {
        if (isLongPressed)
            getCurrentInputConnection().deleteSurroundingText(1, 0);
        handler.removeCallbacks(switchKeyboardTask);
    }

    @Override
    public void onText(CharSequence text) {
    }

    @Override
    public void swipeDown() {
        MainIME.this.onFinishInput();
    }

    @Override
    public void swipeLeft() {

    }

    @Override
    public void swipeRight() {

    }

    @Override
    public void swipeUp(){

    }

    public void switchKeyboard() {
        InputMethodManager imm = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showInputMethodPicker();
    }

    public void switchLanguage() {
        if (!currentKeyboard.equals("vietnam")) {
            currentKeyboard = "vietnam";
        } else {
            currentKeyboard = "qwerty";
        }
        this.setAbcKeyboard();
    }

    public void setAbcKeyboard() {
        if (currentKeyboard.equals("vietnam")) {
            keyboardView.setKeyboard(allKeyboards.vietnam);
        } else {
            keyboardView.setKeyboard(allKeyboards.qwerty);
        }
        InputConnection ic = getCurrentInputConnection();
        if (isPointedInHeadOfText(ic)) {
            shiftKey = true;
            this.updateShiftKey();
        }
    }

    public void updateShiftKey(){
        keyboardView.getKeyboard().setShifted(shiftKey);
        keyboardView.invalidateAllKeys();
    }

}


