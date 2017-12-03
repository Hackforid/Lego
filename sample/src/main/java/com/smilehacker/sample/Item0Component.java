package com.smilehacker.sample;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.smilehacker.lego.LegoComponent;
import com.smilehacker.lego.annotation.LegoField;
import com.smilehacker.lego.annotation.LegoIndex;

/**
 * Created by zhouquan on 17/2/18.
 */

public class Item0Component extends LegoComponent<Item0Component.ViewHolder, Item0Component.Model> {

    private LayoutInflater mLayoutInflater;
    private final static String TAG = Item0Component.class.getSimpleName();

    public Item0Component(Context context) {
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public ViewHolder getViewHolder(ViewGroup container) {
        View view = mLayoutInflater.inflate(R.layout.item_v0, container, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindData(ViewHolder viewHolder, Model model) {
        viewHolder.title.setText(model.title);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView title;

        public ViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.tv_title);
        }
    }

    public static class Model extends Base {
        @LegoField
        public String title;

        @LegoField
        public int content;

        @LegoField
        public int text;
    }

    public static class Base {
        @LegoIndex
        public String id;

        @LegoField
        public String desc;
    }

}
