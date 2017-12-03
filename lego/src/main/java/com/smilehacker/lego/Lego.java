package com.smilehacker.lego;

import android.support.annotation.NonNull;
import android.util.Log;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by zhouquan on 17/8/16.
 */

public final class Lego {
    public final static String TAG = Lego.class.getSimpleName();

    private static List<ILegoFactory> mLegoFactories = new LinkedList<>();
    private static List<Class> mLegoClasses = new LinkedList<>();
    private static Map<Class, List<Class>> mLegoInheritanceMap  = new HashMap<>();

    static {
        Class factoryClass;
        try {
            factoryClass = Class.forName("com.smilehacker.lego.factory.LegoFactory");
            Constructor<?> constructor = factoryClass.getDeclaredConstructor();
            ILegoFactory legoFactory = (ILegoFactory) constructor.newInstance();
            Log.d(TAG, "find factory class");
            mLegoFactories.add(legoFactory);
            addLegoClasses(legoFactory);
        } catch (Exception e) {
            Log.e(TAG, "method error", e);
        }
    }

    private static void addLegoClasses(ILegoFactory legoFactory) {
        for (Class clazz : legoFactory.getDefineModels()) {
            if (!mLegoClasses.contains(clazz)) {
                mLegoClasses.add(clazz);
            }
        }
        Collections.sort(mLegoClasses, new Comparator<Class>() {
            @Override
            public int compare(Class class1, Class class2) {
                return class1.isAssignableFrom(class2) ? 1 : -1;
            }
        });
    }



    public static void addFactory(Class<? extends ILegoFactory> factoryClazz) {
        try {
            if (!mLegoFactories.contains(factoryClazz)) {
                ILegoFactory legoFactory = factoryClazz.newInstance();
                mLegoFactories.add(legoFactory);
                addLegoClasses(legoFactory);
            }
        } catch (Exception e) {
            Log.e(TAG, "fail to invoke class", e);
        }
    }

    public static ILegoFactory legoFactoryProxy = new ILegoFactory() {

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
                if (contain(legoFactory.getDefineModels(), model0)) {
                    return legoFactory.isModelEquals(model0, model1);
                }
            }
            return false;
        }

        private boolean contain(Class[] classes, Object obj) {
            for (Class clazz : classes) {
                if (clazz.equals(obj.getClass())) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean isModelEquals(Object model0, Object model1, Class clazz) {
            for (ILegoFactory legoFactory: mLegoFactories) {
                if (contain(legoFactory.getDefineModels(), model0)) {
                    return legoFactory.isModelEquals(model0, model1, clazz);
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

        @Override
        public Class[] getDefineModels() {
            return null;
        }

    };

    // todo 添加继承cache  diffhash添加继承判断
    public static boolean isModelEqualsExtend(@NonNull Object model0, @NonNull Object model1) {
        if (!model0.getClass().equals(model1.getClass())) {
            return false;
        }
        Class modelClass = model0.getClass();
        int index = mLegoClasses.indexOf(modelClass);
        Log.d("clazz", "index = " + index);
        for (int i = index, len = mLegoClasses.size(); i < len; i++) {
            Class clazz = mLegoClasses.get(i);
            if (clazz.isAssignableFrom(modelClass)) {
                Log.d("lego", "check " + model0.getClass() + " == " + clazz);
                if (!legoFactoryProxy.isModelEquals(model0, model1, clazz)) {
                    return false;
                }
            }
        }
        return true;
    }
}
