package com.smilehacker.lego;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by zhouquan on 17/2/18.
 */

public abstract class LegoComponent<V extends RecyclerView.ViewHolder, M extends LegoModel> {

    protected abstract V getViewHolder(ViewGroup container);

    public abstract void onBindData(V viewHolder, M model);

    public void onBindData(V viewHolder, M model, List<Object> payloads) {
        onBindData(viewHolder, model);
    }

    public int getViewType() {
        return this.getClass().hashCode();
    }

    public Object getChangePayload(M oldModel, M newModel) {
        return null;
    }

}
