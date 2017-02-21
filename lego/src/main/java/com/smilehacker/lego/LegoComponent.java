package com.smilehacker.lego;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

/**
 * Created by zhouquan on 17/2/18.
 */

public abstract class LegoComponent<V extends RecyclerView.ViewHolder, M extends LegoModel> {

    protected abstract V getViewHolder(ViewGroup container);

    public abstract void onBindData(V viewHolder, M model);

    public int getViewType() {
        return this.getClass().hashCode();
    }
}
