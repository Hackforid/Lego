package com.smilehacker.sample;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.smilehacker.lego.LegoComponent;
import com.smilehacker.lego.LegoModel;
import com.smilehacker.lego.annotation.Component;
import com.smilehacker.lego.annotation.LegoField;
import com.smilehacker.lego.annotation.LegoIndex;

/**
 * Created by kleist on 2017/2/21.
 */

@Component
public class Item1Component extends LegoComponent<Item1Component.ViewHolder, Item1Component.Model> {

    @Override
    protected ViewHolder getViewHolder(ViewGroup container) {
        return null;
    }

    @Override
    public void onBindData(ViewHolder viewHolder, Model model) {

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

    public static class Model implements LegoModel {
        @LegoIndex
        public int id;

        @LegoField
        public String content;
    }
}
