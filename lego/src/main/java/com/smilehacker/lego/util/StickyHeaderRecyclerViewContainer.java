package com.smilehacker.lego.util;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.smilehacker.lego.Lego;
import com.smilehacker.lego.LegoAdapter;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by kleist on 2017/2/21.
 */

public class StickyHeaderRecyclerViewContainer extends FrameLayout {

    public static final String TAG = "StickyHeader";
    private final static int NO_POSITION = -100;

    private RecyclerView mRecyclerView;

    private List<Integer> mHeaderTypes = new LinkedList<>();
    private int mCurrentHeaderPos = NO_POSITION;
    private View mCurrentHeaderView;
    private SparseArray<RecyclerView.ViewHolder> mCachedHeaderViewHolders = new SparseArray<>();
    private RecyclerView.Adapter mAdapter;

    private final RecyclerView.AdapterDataObserver mAdapterDataObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            super.onItemRangeChanged(positionStart, itemCount);
            refresh();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            super.onItemRangeInserted(positionStart, itemCount);
            refresh();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            super.onItemRangeRemoved(positionStart, itemCount);
            refresh();
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            super.onItemRangeMoved(fromPosition, toPosition, itemCount);
            refresh();
        }

        @Override
        public void onChanged() {
            super.onChanged();
            refresh();
        }
    };

    public StickyHeaderRecyclerViewContainer(Context context) {
        super(context);
    }

    public StickyHeaderRecyclerViewContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StickyHeaderRecyclerViewContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        init();
    }

    private void init() {
        if (getChildCount() < 1) {
            throw new IllegalStateException("You should put a RecyclerView in this view.");
        }

        View view = getChildAt(0);
        if (!(view instanceof RecyclerView)) {
            throw new IllegalStateException("First child must be a RecyclerView.");
        }

        mRecyclerView = (RecyclerView) view;
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (mAdapter != mRecyclerView.getAdapter()) {
                    if (mAdapter != null) {
                        mAdapter.unregisterAdapterDataObserver(mAdapterDataObserver);
                    }
                    mAdapter = mRecyclerView.getAdapter();
                    mAdapter.registerAdapterDataObserver(mAdapterDataObserver);
                    refresh();
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                refresh();
            }
        });

    }

    public void refresh() {
        if (isLayoutRequested()) {
            this.post(new Runnable() {
                @Override
                public void run() {
                    refreshDirectly();
                }
            });
        } else {
            refreshDirectly();
        }
    }

    private void refreshDirectly() {
        View header = getHeaderView(mRecyclerView);
        if (header == null) {
            mCurrentHeaderPos = NO_POSITION;
            if (mCurrentHeaderView != null) {
                removeView(mCurrentHeaderView);
                mCurrentHeaderView = null;
            }
            return;
        } else {
            if (mCurrentHeaderView != header) {
                removeView(mCurrentHeaderView);
                addView(header);
                bringChildToFront(header);
                mCurrentHeaderView = header;
            }
        }

        int top = 0;
        View v = mRecyclerView.findChildViewUnder(getWidth() / 2, (header.getTop() + header.getHeight()));
        if (isHeaderView(mRecyclerView, v)) {
            top = v.getTop() - header.getHeight();
        }

        mCurrentHeaderView.setTranslationY(top);
    }

    public void addHeaderViewType(int viewType) {
        mHeaderTypes.add(viewType);
    }

    public void removeHeaderViewType(int viewType) {
        mHeaderTypes.remove(viewType);
    }

    public void clearViewHolderCache() {
        mCachedHeaderViewHolders.clear();
    }

    private boolean isHeaderView(RecyclerView parent, View view) {
        int position = parent.getChildAdapterPosition(view);
        if (position == RecyclerView.NO_POSITION) {
            return false;
        }

        RecyclerView.Adapter adapter = parent.getAdapter();
        return isHeader(adapter.getItemViewType(position));
    }

    private Object mLastHeaderModelIndex;

    private View getHeaderView(RecyclerView parent) {
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        RecyclerView.Adapter adapter = parent.getAdapter();
        if (layoutManager == null || layoutManager.getChildCount() <= 0 || adapter == null) {
            return null;
        }

        int firstVisibleItemPos = -1;
        if (layoutManager instanceof LinearLayoutManager) {
            firstVisibleItemPos = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
        } else {
            firstVisibleItemPos = ((RecyclerView.LayoutParams) layoutManager.getChildAt(0)
                    .getLayoutParams()).getViewAdapterPosition();
        }

        int headerPos = findStickyHeaderPosition(parent, firstVisibleItemPos);

        if (headerPos < 0) {
            return null;
        }


        boolean headerDataChanged = false;
        if (mAdapter instanceof LegoAdapter) {
            try {
                Object model = ((LegoAdapter) mAdapter).getData().get(headerPos);
                Object currentIndex = Lego.legoFactoryProxy.getModelIndex(model, model.getClass());
                headerDataChanged = !currentIndex.equals(mLastHeaderModelIndex);
                mLastHeaderModelIndex = currentIndex;
            } catch (Exception e) {
                Log.e(TAG, "", e);
            }
        }

        if (headerPos == mCurrentHeaderPos && mCurrentHeaderView != null && !headerDataChanged) {
            return mCurrentHeaderView;
        }

        mCurrentHeaderPos = headerPos;

        int viewType = adapter.getItemViewType(headerPos);
        RecyclerView.ViewHolder viewHolder = mCachedHeaderViewHolders.get(viewType);
        if (viewHolder == null) {
            viewHolder = adapter.createViewHolder(parent, viewType);
        }
        //noinspection unchecked
        adapter.bindViewHolder(viewHolder, headerPos);
        View itemView = viewHolder.itemView;

        ViewGroup.LayoutParams lp = itemView.getLayoutParams();
        if (lp == null) {
            lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            itemView.setLayoutParams(lp);
        }

        int heightMode = MeasureSpec.getMode(lp.height);
        int heightSize = MeasureSpec.getSize(lp.height);

        if (heightMode == MeasureSpec.UNSPECIFIED) {
            heightMode = MeasureSpec.EXACTLY;
        }

        int maxHeight = parent.getHeight() - parent.getPaddingTop() - parent.getPaddingBottom();
        if (heightSize > maxHeight) {
            heightSize = maxHeight;
        }

        int width = MeasureSpec.makeMeasureSpec(parent.getWidth() - parent.getPaddingLeft() - parent.getPaddingRight(), MeasureSpec.EXACTLY);
        int height = MeasureSpec.makeMeasureSpec(heightSize, heightMode);
        itemView.measure(width, height);
        itemView.layout(0, 0, itemView.getMeasuredWidth(), itemView.getMeasuredHeight());
        mCachedHeaderViewHolders.append(viewType, viewHolder);

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

        for (int i = fromPos; i >= 0; i--) {
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
