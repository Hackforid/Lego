package com.smilehacker.lego;

import android.support.annotation.NonNull;
import android.util.Log;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.WeakHashMap;

/**
 * Created by zhouquan on 17/8/16.
 */

public final class Lego {
    public final static String TAG = Lego.class.getSimpleName();

    private static List<ILegoFactory> mLegoFactories = new LinkedList<>();
    private static List<Class> mLegoClasses = new LinkedList<>();

    protected static WeakHashMap<Object, Boolean> mModelMap = new WeakHashMap<>();

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
                if (contain(legoFactory.getDefineModels(), clazz)) {
                    return legoFactory.getModelIndex(model, clazz);
                }
            }
            return null;
        }

        @Override
        public boolean isModelEquals(Object model0, Object model1) {
            for (ILegoFactory legoFactory: mLegoFactories) {
                if (contain(legoFactory.getDefineModels(), model0.getClass())) {
                    return legoFactory.isModelEquals(model0, model1);
                }
            }
            return false;
        }

        private boolean contain(Class[] classes, Class objClass) {
            for (Class clazz : classes) {
                if (clazz.equals(objClass)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean isModelEquals(Object model0, Object model1, Class clazz) {
            for (ILegoFactory legoFactory: mLegoFactories) {
                if (contain(legoFactory.getDefineModels(), clazz)) {
                    return legoFactory.isModelEquals(model0, model1, clazz);
                }
            }
            return false;
        }

        @Override
        public double getModelHash(Object m) {
            for (ILegoFactory legoFactory: mLegoFactories) {
                if (contain(legoFactory.getDefineModels(), m.getClass())) {
                    return legoFactory.getModelHash(m);
                }
            }
            return -1;
        }

        @Override
        public double getModelHash(Object m, Class clazz) {
            for (ILegoFactory legoFactory: mLegoFactories) {
                if (contain(legoFactory.getDefineModels(), clazz)) {
                    return legoFactory.getModelHash(m, clazz);
                }
            }
            return -1;
        }

        @Override
        public Class[] getDefineModels() {
            return null;
        }

    };

    // todo 添加继承cache  diffhash添加继承判断
    public static boolean isModelEqualsInheritance(@NonNull Object model0, @NonNull Object model1) {
        if (!model0.getClass().equals(model1.getClass())) {
            return false;
        }
        Class modelClass = model0.getClass();
        int index = mLegoClasses.indexOf(modelClass);
        if (index < 0) {
            return false;
        }
        for (int i = index, len = mLegoClasses.size(); i < len; i++) {
            Class clazz = mLegoClasses.get(i);
            if (clazz.isAssignableFrom(modelClass)) {
                if (!legoFactoryProxy.isModelEquals(model0, model1, clazz)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static double getModelHashWithInheritance(@NonNull Object model) {
        Class modelClass = model.getClass();
        int index = mLegoClasses.indexOf(modelClass);
        if (index < 0) {
            return -1;
        }
        double hash = -1;
        for (int i = index, len = mLegoClasses.size(); i < len; i++) {
            Class clazz = mLegoClasses.get(i);
            if (clazz.isAssignableFrom(modelClass)) {
                hash += legoFactoryProxy.getModelHash(model, clazz);
            }
        }
        return hash;
    }

    public static Object getModelIndexInheritance(Object model) {
        Class  modelClass = model.getClass();
        int pos = mLegoClasses.indexOf(modelClass);
        if (pos < 0) {
            return null;
        }
        for (int i = pos, len = mLegoClasses.size(); i < len; i++) {
            Class clazz = mLegoClasses.get(i);
            Log.d("lego", "index check " + clazz);
            if (clazz.isAssignableFrom(modelClass)) {
                Object index = legoFactoryProxy.getModelIndex(model, clazz);
                if (index != null) {
                    Log.d("lego", "index check find " + index);
                    return index;
                }
            }
        }
        return null;

    }

    public static void modelChanged(Object model) {
        mModelMap.put(model, true);
    }
}
