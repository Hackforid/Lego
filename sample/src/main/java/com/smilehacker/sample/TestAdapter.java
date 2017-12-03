package com.smilehacker.sample;

import android.content.Context;

import com.smilehacker.lego.LegoAdapter;

/**
 * Created by zhouquan on 17/2/18.
 */

public class TestAdapter extends LegoAdapter {
    public TestAdapter(Context context) {
        setDiffUtilEnabled(true);
        //setModelHashEnabled(true);
        setDiffInheritance(true);
        Item0Component item0 = new Item0Component(context);
        register(item0);
        register(new Item1Component(context));
    }

}
