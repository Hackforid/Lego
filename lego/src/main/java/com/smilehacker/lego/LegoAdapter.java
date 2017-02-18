package com.smilehacker.lego;

import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ViewGroup;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhouquan on 17/2/18.
 */

public class LegoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static String TAG = LegoAdapter.class.getSimpleName();

    private List<com.smilehacker.lego.LegoComponent> mComponents = new ArrayList<>();
    private List<com.smilehacker.lego.LegoModel> mModels = new ArrayList<>();

    private static Method getModelClass;
    private static Method getModelIndex;
    private static Method isModelEquals;

    private DiffCallback mDiffCallback = new DiffCallback();

    {
        init();
    }


    public void register(com.smilehacker.lego.LegoComponent component) {
        mComponents.add(component);
    }

    // TODO add data diff
    public void setData(List<com.smilehacker.lego.LegoModel> models) {
        diffNotifyDataSetChanged(models);
    }


    public void diffNotifyDataSetChanged(List<LegoModel> newList) {
        mDiffCallback.setOldModels(mModels);
        mDiffCallback.setNewModels(newList);
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(mDiffCallback, true);
        result.dispatchUpdatesTo(this);

        mModels.clear();
        mModels.addAll(newList);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        for (com.smilehacker.lego.LegoComponent component: mComponents) {
            if (component.getClass().hashCode() == viewType) {
                return component.getViewHolder(parent);
            }
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        com.smilehacker.lego.LegoModel model = mModels.get(position);
        com.smilehacker.lego.LegoComponent viewModel = getViewModelByModel(model);
        if (viewModel == null) {
            return;
        }
        //noinspection unchecked
        viewModel.onBindData(holder, model);
    }



    @NonNull
    private com.smilehacker.lego.LegoComponent getViewModelByModel(com.smilehacker.lego.LegoModel dataModel) {
        for (com.smilehacker.lego.LegoComponent component: mComponents) {
            try {
                Class modelClass = (Class) getModelClass.invoke(this, component);
                if (dataModel.getClass().equals(modelClass)) {
                    return component;
                }
            } catch (Exception e) {
                Log.e(TAG, "method error", e);
            }
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return mModels.size();
    }


    @Override
    public int getItemViewType(int position) {
        LegoModel dataModel = mModels.get(position);
        LegoComponent viewModel = getViewModelByModel(dataModel);
        return viewModel.getClass().hashCode();
    }

    private static Method init() {
        Class factoryClass;
        try {
            factoryClass = Class.forName("com.smilehacker.lego.LegoFactory");
            getModelClass = factoryClass.getDeclaredMethod("getModelClass", LegoComponent.class);
            getModelIndex = factoryClass.getDeclaredMethod("getModelIndex", LegoModel.class);
            isModelEquals = factoryClass.getDeclaredMethod("isModelEquals", LegoModel.class, LegoModel.class);
        } catch (Exception e) {
            Log.e(TAG, "method error", e);
        }
        return null;
    }

    private static class DiffCallback extends DiffUtil.Callback {

        private List<LegoModel> mOldModels;
        private List<LegoModel> mNewModels;

        public void setOldModels(List<LegoModel> models) {
            mOldModels = models;
        }

        public void setNewModels(List<LegoModel> models) {
            mNewModels = models;
        }

        @Override
        public int getOldListSize() {
            return mOldModels != null ? mOldModels.size() : 0;
        }

        @Override
        public int getNewListSize() {
            return mNewModels != null ? mNewModels.size() : 0;
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            LegoModel oldModel = mOldModels.get(oldItemPosition);
            LegoModel newModel = mNewModels.get(newItemPosition);
            try {
                Object oldIndex = getModelIndex.invoke(this, oldModel);
                Object newIndex = getModelIndex.invoke(this, newModel);
                if (oldIndex != null && newIndex != null && oldIndex.equals(newIndex)) {
                    return true;
                }
            } catch (Exception e) {
                Log.e(TAG, "method error", e);
                return false;
            }
            return false;
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            LegoModel oldModel = mOldModels.get(oldItemPosition);
            LegoModel newModel = mNewModels.get(newItemPosition);
            try {
                int r = (int) isModelEquals.invoke(this, oldModel, newModel);
                if (r == -1) {
                    return false;
                } else if (r == 1) {
                    return true;
                } else {
                    return oldModel.equals(newModel);
                }
            } catch (Exception e) {
                Log.e(TAG, "method error", e);
                return false;
            }

        }
    }
}
