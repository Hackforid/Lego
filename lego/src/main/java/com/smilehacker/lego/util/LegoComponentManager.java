package com.smilehacker.lego.util;

import android.util.Log;

import com.smilehacker.lego.LegoComponent;

import java.lang.reflect.Type;
import java.util.HashMap;

/**
 * Created by zhouquan on 17/2/28.
 */

public class LegoComponentManager {
    public static final String TAG = LegoComponentManager.class.getSimpleName();

    public static LegoComponentManager mInstance;

    private HashMap<Class, Class> mComponentMap;

    public static LegoComponentManager getInstance() {
        if (mInstance == null) {
            synchronized (LegoComponentManager.class) {
                if (mInstance == null) {
                    mInstance = new LegoComponentManager();
                }
            }
        }
        return mInstance;
    }

    private LegoComponentManager() {
        mComponentMap = new HashMap<>();
    }

    public Class getModel(LegoComponent component) {
        Class clazz = mComponentMap.get(component.getClass());
        if (clazz == null) {
            clazz = getModelByReflection(component);
            mComponentMap.put(component.getClass(), clazz);
        }

        return clazz;
    }

    private Class getModelByReflection(LegoComponent component) {
        Type[] genericTypes = ReflectionUtil.getParameterizedTypes(component);
        if (genericTypes.length < 2) {
            return null;
        }
        try {
            return ReflectionUtil.getClass(genericTypes[1]);
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "get class error", e);
        }

        return null;
    }
}
