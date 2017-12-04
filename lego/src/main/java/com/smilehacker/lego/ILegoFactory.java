package com.smilehacker.lego;

/**
 * Created by zhouquan on 17/2/19.
 */

public interface ILegoFactory {

    Object getModelIndex(Object model, Class clazz);

    boolean isModelEquals(Object model0, Object model1);

    boolean isModelEquals(Object model0, Object model1, Class clazz);

    double getModelHash(Object m);

    double getModelHash(Object m, Class clazz);

    Class[] getDefineModels();
}
