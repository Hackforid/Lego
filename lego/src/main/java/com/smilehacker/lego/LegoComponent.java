package com.smilehacker.lego;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.smilehacker.lego.util.LegoComponentManager;

import java.util.List;

/**
 * Created by zhouquan on 17/2/18.
 */

public abstract class LegoComponent<V extends RecyclerView.ViewHolder, M> {

    public abstract V getViewHolder(ViewGroup container);

    public abstract void onBindData(V viewHolder, M model);

    private Class mModelClass;

    /**
     * Called by RecyclerView to display the data at the specified position. This method should
     * update the contents of the {@link ViewHolder#itemView} to reflect the item at the given
     * position.
     * <p>
     * Note that unlike {@link android.widget.ListView}, RecyclerView will not call this method
     * again if the position of the item changes in the data set unless the item itself is
     * invalidated or the new position cannot be determined. For this reason, you should only
     * use the <code>position</code> parameter while acquiring the related data item inside
     * this method and should not keep a copy of it. If you need the position of an item later
     * on (e.g. in a click listener), use {@link ViewHolder#getAdapterPosition()} which will
     * have the updated adapter position.
     *
     * Override {@link #onBindViewHolder(ViewHolder, int, List)} instead if Adapter can
     * handle efficient partial bind.
     *
     * @param holder The ViewHolder which should be updated to represent the contents of the
     *        item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    public void onBindData(V viewHolder, M model, List<Object> payloads) {
        onBindData(viewHolder, model);
    }

    public int getViewType() {
        return this.getClass().hashCode();
    }

    public Object getChangePayload(M oldModel, M newModel) {
        return null;
    }

    public Class getModelClass() {
        if (mModelClass == null) {
            mModelClass = LegoComponentManager.getInstance().getModel(this);
        }
        return mModelClass;
    }
}
