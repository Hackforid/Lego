package com.smilehacker.lego;

import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ViewGroup;

import java.lang.reflect.Constructor;
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

    public static ILegoFactory legoFactory;

    private boolean mDiffUtilEnabled = false;

    private DiffCallback mDiffCallback = new DiffCallback();

    {
        init();
    }


    public void register(LegoComponent component) {
        mComponents.add(component);
    }

    public void setData(List<LegoModel> models) {
        mModels.clear();
        mModels.addAll(models);
    }

    public List<LegoModel> getData() {
        return mModels;
    }

    public void commitData(List<LegoModel> models) {
        if (mDiffUtilEnabled) {
            diffNotifyDataSetChanged(models);
            setData(models);
        } else {
            setData(models);
            notifyDataSetChanged();
        }
    }

    public void setDiffUtilEnabled(boolean enable) {
        mDiffUtilEnabled = enable;
    }


    private void diffNotifyDataSetChanged(List<LegoModel> newList) {
        mDiffCallback.setOldModels(mModels);
        mDiffCallback.setNewModels(newList);
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(mDiffCallback, true);
        result.dispatchUpdatesTo(this);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        for (LegoComponent component : mComponents) {
            if (component.getClass().hashCode() == viewType) {
                return component.getViewHolder(parent);
            }
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        LegoModel model = mModels.get(position);
        LegoComponent viewModel = getViewModelByModel(model);
        //noinspection unchecked
        viewModel.onBindData(holder, model);
    }


    @NonNull
    private LegoComponent getViewModelByModel(com.smilehacker.lego.LegoModel dataModel) {
        for (LegoComponent component : mComponents) {
            Class modelClass = legoFactory.getModelClass(component);
            if (dataModel.getClass().equals(modelClass)) {
                return component;
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

    @SuppressWarnings("unchecked")
    private static Method init() {
        Class factoryClass;
        try {
            factoryClass = Class.forName("com.smilehacker.lego.LegoFactory");
            Constructor<?> constructor = factoryClass.getDeclaredConstructor();
            legoFactory = (ILegoFactory) constructor.newInstance();
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
            Object oldIndex = legoFactory.getModelIndex(oldModel);
            Object newIndex = legoFactory.getModelIndex(newModel);
            if (oldIndex != null && newIndex != null && oldIndex.equals(newIndex)) {
                return true;
            }
            return false;
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            LegoModel oldModel = mOldModels.get(oldItemPosition);
            LegoModel newModel = mNewModels.get(newItemPosition);
            int r = legoFactory.isModelEquals(oldModel, newModel);
            if (r == -1) {
                return false;
            } else if (r == 1) {
                return true;
            } else {
                return oldModel.equals(newModel);
            }
        }
    }
}
