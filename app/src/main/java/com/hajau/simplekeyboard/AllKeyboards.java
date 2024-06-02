package com.hajau.simplekeyboard;

import android.content.Context;
import android.inputmethodservice.Keyboard;

class AllKeyboards {

    public Keyboard symbol;
    public Keyboard vietnam;
    public Keyboard qwerty;


    public AllKeyboards(Context context) {
        this.symbol = new Keyboard(context, R.xml.symbol);
        this.vietnam = new Keyboard(context, R.xml.vietnam);
        this.qwerty = new Keyboard(context, R.xml.qwerty);
    }
}
