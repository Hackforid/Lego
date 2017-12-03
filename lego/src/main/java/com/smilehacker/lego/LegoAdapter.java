package com.smilehacker.lego;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

/**
 * Created by zhouquan on 17/2/18.
 */

public class LegoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static String TAG = LegoAdapter.class.getSimpleName();

    private List<LegoComponent> mComponents = new ArrayList<>();
    private List<Object> mModels = new ArrayList<>();



    private boolean mDiffUtilEnabled = false;
    private boolean mDiffUtilDetectMoves = true;
    private boolean mModelHashEnabled = false;

    private DiffCallback mDiffCallback = new DiffCallback();


    public void register(LegoComponent component) {
        mComponents.add(component);
    }

    public void setData(List<Object> models) {
        mModels.clear();
        mModels.addAll(models);
    }

    public List<Object> getData() {
        return mModels;
    }

    public void commitData(List<Object> models) {
        if (mDiffUtilEnabled) {
            diffNotifyDataSetChanged(models);
            setData(models);
        } else {
            setData(models);
            notifyDataSetChanged();
        }
    }

    public void notifyModelChanged(Object model) {
        if (mDiffUtilEnabled) {
            Object modelIndex = Lego.legoFactoryProxy.getModelIndex(model, null);
            if (modelIndex == null) {
                return;
            }
            for (int i = 0, len = mModels.size(); i < len; i++) {
                Object obj = mModels.get(i);
                Object objIndex = Lego.legoFactoryProxy.getModelIndex(obj, null);
                if (modelIndex.equals(objIndex)) {
                    mModels.set(i, model);
                    notifyItemChanged(i);
                    break;
                }
            }
        } else {
            int index = mModels.indexOf(model);
            notifyItemChanged(index);
        }
    }

    public void setDiffUtilEnabled(boolean enable) {
        mDiffUtilEnabled = enable;
    }

    public void setDiffUtilDetectMoves(boolean detectMoves) {
        mDiffUtilDetectMoves = detectMoves;
    }

    public void setModelHashEnabled(boolean modelHashEnabled) {
        mModelHashEnabled = modelHashEnabled;
    }

    private void diffNotifyDataSetChanged(List<Object> newList) {
        mDiffCallback.setOldModels(mModels);
        mDiffCallback.setNewModels(newList);
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(mDiffCallback, mDiffUtilDetectMoves);
        result.dispatchUpdatesTo(this);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        for (LegoComponent component : mComponents) {
            if (component.getViewType() == viewType) {
                return component.getViewHolder(parent);
            }
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Object model = mModels.get(position);
        LegoComponent viewModel = getViewModelByModel(model);
        //noinspection unchecked
        viewModel.onBindData(holder, model);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position, List<Object> payloads) {
        Object model = mModels.get(position);
        LegoComponent viewModel = getViewModelByModel(model);
        //noinspection unchecked
        viewModel.onBindData(holder, model, payloads);
    }

    @NonNull
    private LegoComponent getViewModelByModel(Object dataModel) {
        for (LegoComponent component : mComponents) {
            Class modelClass = component.getModelClass();
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
        Object dataModel = mModels.get(position);
        LegoComponent viewModel = getViewModelByModel(dataModel);
        return viewModel.getViewType();
    }

    @SuppressWarnings("unchecked")

    public static void removeDuplication(List list) {
        for (int i = 0; i < list.size() - 1; i++) {
            for (int j = list.size() - 1; j > i; j--) {
                if (list.get(j).equals(list.get(i))) {
                    list.remove(j);
                }
                if (Lego.legoFactoryProxy.getModelIndex(list.get(i), null)
                        .equals(Lego.legoFactoryProxy.getModelIndex(list.get(j), null))) {
                    list.remove(j);
                }
            }
        }
    }

    private class DiffCallback extends DiffUtil.Callback {

        private WeakHashMap<Object, Double> mModelHashMap = new WeakHashMap<>();

        private List<Object> mOldModels;
        private List<Object> mNewModels;

        public void setOldModels(List<Object> models) {
            mOldModels = models;
        }

        public void setNewModels(List<Object> models) {
            mNewModels = models;
            for (Object model : models) {
                if (mModelHashMap.get(model) == null) {
                    mModelHashMap.put(model, Lego.legoFactoryProxy.getModelHash(model));
                }
            }
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
            Object oldModel = mOldModels.get(oldItemPosition);
            Object newModel = mNewModels.get(newItemPosition);
            Object oldIndex = Lego.legoFactoryProxy.getModelIndex(oldModel, null);
            Object newIndex = Lego.legoFactoryProxy.getModelIndex(newModel, null);
            if (oldIndex != null && newIndex != null && oldIndex.equals(newIndex)) {
                return true;
            }
            return false;
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            Object oldModel = mOldModels.get(oldItemPosition);
            Object newModel = mNewModels.get(newItemPosition);
            if (oldModel == newModel && mModelHashEnabled) {
                double oldHash = safeToDouble(mModelHashMap.get(oldModel));
                double newHash = safeToDouble(Lego.legoFactoryProxy.getModelHash(newModel));
                mModelHashMap.put(newModel, newHash);
                if (oldHash != -1 && newHash != -1) {
                    return oldHash == newHash;
                }

            }
            return Lego.legoFactoryProxy.isModelEquals(oldModel, newModel);
        }

        private double safeToDouble(Double obj) {
            if (obj == null) {
                return -1;
            } else {
                return obj;
            }
        }

        @Nullable
        @Override
        public Object getChangePayload(int oldItemPosition, int newItemPosition) {
            Object oldModel = mOldModels.get(oldItemPosition);
            Object newModel = mNewModels.get(newItemPosition);
            LegoComponent component = getComponentByModel(oldModel);
            if (component != null) {
                //noinspection unchecked
                return component.getChangePayload(oldModel, newModel);
            }
            return super.getChangePayload(oldItemPosition, newItemPosition);
        }
    }


    public LegoComponent getComponentByModel(Object model) {
        for (LegoComponent component: mComponents) {
            if (model.getClass().equals(component.getClass())) {
                return component;
            }
        }
        return null;
    }
}
