package com.smilehacker.sample;

import android.graphics.Canvas;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by kleist on 2017/2/21.
 */

public class StickyHeaderDecoration extends RecyclerView.ItemDecoration {

    public final static String TAG = StickyHeaderDecoration.class.getName();

    private List<Integer> mHeaderTypes = new LinkedList<>();
    private int mHeaderPos = -100;
    private View mHeaderView;

    public void addHeaderViewType(int viewType) {
        mHeaderTypes.add(viewType);
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDrawOver(c, parent, state);
        View header = getHeaderView(parent);

        if (header != null) {
            int top = 0;
            View v = parent.findChildViewUnder(c.getWidth() / 2, (header.getTop() + header.getHeight()));
            if (isHeaderView(parent, v)) {
                top = v.getTop() - header.getHeight();
            }
            c.save();
            c.translate(0, top);
            header.draw(c);
            c.restore();
        }
    }

    private boolean isHeaderView(RecyclerView parent, View view) {
        int position = parent.getChildAdapterPosition(view);
        if (position == RecyclerView.NO_POSITION) {
            return false;
        }

        RecyclerView.Adapter adapter = parent.getAdapter();
        return isHeader(adapter.getItemViewType(position));
    }

    private View getHeaderView(RecyclerView parent) {
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        RecyclerView.Adapter adapter = parent.getAdapter();
        if (layoutManager == null || layoutManager.getChildCount() <= 0 || adapter == null) {
            return null;
        }

        int firstVisibleItemPos = ((RecyclerView.LayoutParams) layoutManager.getChildAt(0)
                .getLayoutParams()).getViewAdapterPosition(); // 这方式好屌

        int headerPos = findStickyHeaderPosition(parent, firstVisibleItemPos);

        if (headerPos < 0) {
            return null;
        }

        if (headerPos == mHeaderPos && mHeaderView != null) {
            return mHeaderView;
        }
        mHeaderPos = headerPos;

        int viewType = adapter.getItemViewType(headerPos);
        RecyclerView.ViewHolder viewHolder = adapter.createViewHolder(parent, viewType);
        adapter.bindViewHolder(viewHolder, headerPos);
        View itemView = viewHolder.itemView;

        ViewGroup.LayoutParams lp = itemView.getLayoutParams();
        if (lp == null) {
            lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            itemView.setLayoutParams(lp);
        }

        int heightMode = View.MeasureSpec.getMode(lp.height);
        int heightSize = View.MeasureSpec.getSize(lp.height);

        if (heightMode == View.MeasureSpec.UNSPECIFIED) {
            heightMode = View.MeasureSpec.EXACTLY;
        }

        int maxHeight = parent.getHeight() - parent.getPaddingTop() - parent.getPaddingBottom();
        if (heightSize > maxHeight) {
            heightSize = maxHeight;
        }

        int width = View.MeasureSpec.makeMeasureSpec(parent.getWidth() - parent.getPaddingLeft() - parent.getPaddingRight(), View.MeasureSpec.EXACTLY);
        int height = View.MeasureSpec.makeMeasureSpec(heightSize, heightMode);
        itemView.measure(width, height);
        itemView.layout(0, 0, itemView.getMeasuredWidth(), itemView.getMeasuredHeight());

        mHeaderView = itemView;
        return itemView;
    }


    private int findStickyHeaderPosition(RecyclerView parent, int fromPos) {
        RecyclerView.Adapter adapter = parent.getAdapter();
        if (adapter == null) {
            return RecyclerView.NO_POSITION;
        }
        if (fromPos > adapter.getItemCount() || fromPos < 0) {
            return RecyclerView.NO_POSITION;
        }

        for (int i = fromPos; i >=0; i--) {
            int viewType = adapter.getItemViewType(i);
            if (isHeader(viewType)) {
                return i;
            }
        }

        return RecyclerView.NO_POSITION;
    }

    private boolean isHeader(int viewType) {
        return mHeaderTypes.contains(viewType);
    }
}
