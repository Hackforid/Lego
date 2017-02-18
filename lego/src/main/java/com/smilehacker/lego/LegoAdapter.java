package com.smilehacker.lego;

import android.support.annotation.NonNull;
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

    {
        init();
    }


    public void register(com.smilehacker.lego.LegoComponent component) {
        mComponents.add(component);
    }

    // TODO add data diff
    public void setData(List<com.smilehacker.lego.LegoModel> models) {
        mModels.clear();
        mModels.addAll(models);
        notifyDataSetChanged();
    }

    public void appendData(List<com.smilehacker.lego.LegoModel> models) {
        models.addAll(models);
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        for (com.smilehacker.lego.LegoComponent component: mComponents) {
            if (component.hashCode() == viewType) {
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
        com.smilehacker.lego.LegoModel dataModel = mModels.get(position);
        com.smilehacker.lego.LegoComponent viewModel = getViewModelByModel(dataModel);
        return viewModel.hashCode();
    }

    private static Method init() {
        Class factoryClass;
        try {
            factoryClass = Class.forName("com.smilehacker.lego.LegoFactory");
            getModelClass = factoryClass.getDeclaredMethod("getModelClass", LegoComponent.class);
        } catch (Exception e) {
            Log.e(TAG, "method error", e);
        }
        return null;
    }
}
