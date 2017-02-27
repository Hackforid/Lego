package com.smilehacker.lego;

/**
 * Created by zhouquan on 17/2/19.
 */

public interface ILegoFactory {

    Class getModelClass(LegoComponent component);

    Object getModelIndex(Object model);

    boolean isModelEquals(Object model0, Object model1);
}
