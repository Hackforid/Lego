package com.smilehacker.sample;

import android.support.v7.util.DiffUtil;

import com.smilehacker.lego.LegoModel;

import java.util.List;

/**
 * Created by zhouquan on 17/2/18.
 */

public class DiffCallback extends DiffUtil.Callback {

    private List<LegoModel> mOldList, mNewList;

    public DiffCallback(List<LegoModel> oldList, List<LegoModel> newList) {
        mOldList = oldList;
        mNewList = newList;
    }

    @Override
    public int getOldListSize() {
        return mOldList == null ? 0 :mOldList.size();
    }

    @Override
    public int getNewListSize() {
        return mNewList == null ? 0 : mNewList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        LegoModel oldModel = mOldList.get(oldItemPosition);
        LegoModel newModel = mNewList.get(newItemPosition);
        if (!oldModel.getClass().equals(newModel.getClass())) {
            return false;
        }
        if (Item0Component.Model.class.equals((oldModel.getClass()))) {
            return ((Item0Component.Model) oldModel).title.equals(((Item0Component.Model) newModel).title);
        }
        return false;
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        LegoModel oldModel = mOldList.get(oldItemPosition);
        LegoModel newModel = mNewList.get(newItemPosition);
        if (Item0Component.Model.class.equals((oldModel.getClass()))) {
            return ((Item0Component.Model) oldModel).title.equals(((Item0Component.Model) newModel).title);
        }
        return false;
    }
}
