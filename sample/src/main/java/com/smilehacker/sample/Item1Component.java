package com.smilehacker.sample;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.smilehacker.lego.LegoComponent;
import com.smilehacker.lego.annotation.LegoIndex;

/**
 * Created by kleist on 2017/2/21.
 */

public class Item1Component extends LegoComponent<Item1Component.ViewHolder, Item1Component.Model> {


    private LayoutInflater mLayoutInflater;

    public Item1Component(Context context) {
        mLayoutInflater = LayoutInflater.from(context);
    }
    @Override
    public ViewHolder getViewHolder(ViewGroup container) {
        View view = mLayoutInflater.inflate(R.layout.item_v1, container, false);
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

    public static class Model {

        @LegoIndex
        public String title;
    }
}
