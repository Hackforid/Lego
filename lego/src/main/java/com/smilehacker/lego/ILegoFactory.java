package com.smilehacker.lego;

/**
 * Created by zhouquan on 17/2/19.
 */

public interface ILegoFactory {

    Class getModelClass(LegoComponent component);

    Object getModelIndex(LegoModel model);

    int isModelEquals(LegoModel model0, LegoModel model1);
}
