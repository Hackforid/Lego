package com.smilehacker.lego;

import android.util.Log;

import java.lang.reflect.Constructor;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by zhouquan on 17/8/16.
 */

public final class Lego {
    public final static String TAG = Lego.class.getSimpleName();

    private static List<ILegoFactory> mLegoFactories = new LinkedList<>();

    static {
        Class factoryClass;
        try {
            factoryClass = Class.forName("com.smilehacker.lego.factory.LegoFactory");
            Constructor<?> constructor = factoryClass.getDeclaredConstructor();
            ILegoFactory legoFactory = (ILegoFactory) constructor.newInstance();
            Log.d(TAG, "find factory class");
            mLegoFactories.add(legoFactory);
        } catch (Exception e) {
            Log.e(TAG, "method error", e);
        }
    }




    public static void addFactory(Class<? extends ILegoFactory> factoryClazz) {
        try {
            mLegoFactories.add(factoryClazz.newInstance());
        } catch (Exception e) {
            Log.e(TAG, "fail to invoke class", e);
        }
    }

    public static ILegoFactory legoFactoryProxy = new ILegoFactory() {
        @Override
        public Class getModelClass(LegoComponent component) {
            for (ILegoFactory legoFactory: mLegoFactories) {
                Class clazz = legoFactory.getModelClass(component);
                if (clazz != null) {
                    return clazz;
                }
            }
            return null;
        }

        @Override
        public Object getModelIndex(Object model, Class clazz) {
            if (clazz == null) {
                clazz = model.getClass();
            }
            for (ILegoFactory legoFactory: mLegoFactories) {
                Object index = legoFactory.getModelIndex(model, clazz);
                if (index != null) {
                    return index;
                }
            }
            Class superClass = clazz.getSuperclass();
            if (superClass != null) {
                return getModelIndex(model, superClass);
            }
            return null;
        }

        @Override
        public boolean isModelEquals(Object model0, Object model1) {
            for (ILegoFactory legoFactory: mLegoFactories) {
                boolean r = legoFactory.isModelEquals(model0, model1);
                if (r) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public double getModelHash(Object m) {
            for (ILegoFactory legoFactory: mLegoFactories) {
                double r = legoFactory.getModelHash(m);
                if (r != -1) {
                    return r;
                }
            }
            return 0;
        }
    };
}
