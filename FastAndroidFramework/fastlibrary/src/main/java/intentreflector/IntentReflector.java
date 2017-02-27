package intentreflector;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import base.ManagerFragment;
import constant.Constant;
import log.Logger;

/**
 * Created by weber_zheng on 17/1/16.
 */

public class IntentReflector {
    private Map<Class, Object> objectMap = new HashMap<>();
    private static IntentReflector intentUtils;
    private final String FRAGMENT_TAG = "fragment_tag";

    public void bindManager(FragmentActivity activity){
        FragmentManager manager = activity.getSupportFragmentManager();
        ManagerFragment baseFragment = new ManagerFragment();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(baseFragment, FRAGMENT_TAG);
        transaction.commitAllowingStateLoss();
    }

    public Intent initIntent(Context context, Class clazz, Class targetClazz, String methodName){
       return initIntent(context, clazz, targetClazz, methodName, null, null);
    }

    public Intent initIntent(Context context, Class clazz, Class targetClazz, String methodName, Object value, Class valueClazz){
        Intent intent = new Intent(context, targetClazz);
        intent.putExtra(Constant.IntentParams.INTENT_CLASS, clazz);
        intent.putExtra(Constant.IntentParams.INTENT_METHOD, methodName);
        intent.putExtra(Constant.IntentParams.INTENT_CLASS, clazz);

        if(Constant.IntentParams.INTENT_PARAMS == null || Constant.IntentParams.INTENT_PARAMS_TYPE == null){
            return intent;
        }
        intent.putExtra(Constant.IntentParams.INTENT_PARAMS_TYPE, valueClazz);
        if(valueClazz == Integer.class){
            intent.putExtra(Constant.IntentParams.INTENT_PARAMS, (Integer)value);
        }else if(valueClazz == String.class){
            intent.putExtra(Constant.IntentParams.INTENT_PARAMS, (String)value);
        }else if(valueClazz == Long.class){
            intent.putExtra(Constant.IntentParams.INTENT_PARAMS, (Long)value);
        }else if(valueClazz == Double.class){
            intent.putExtra(Constant.IntentParams.INTENT_PARAMS, (Double)value);
        }else if(valueClazz == Float.class){
            intent.putExtra(Constant.IntentParams.INTENT_PARAMS, (Float)value);
        }else if(valueClazz == Boolean.class){
            intent.putExtra(Constant.IntentParams.INTENT_PARAMS, (Boolean)value);
        }
        return intent;
    }

    private IntentReflector(){}

    public static IntentReflector getInstance(){
        if(intentUtils == null){
            intentUtils = new IntentReflector();
        }

        return intentUtils;
    }

    public void intentProxy(Intent intent) throws Exception {
        if(!intent.hasExtra(Constant.IntentParams.INTENT_CLASS)){
            throw new Exception("Please deliver Class before");
        }

        if(!intent.hasExtra(Constant.IntentParams.INTENT_METHOD)){
            throw new Exception("Please deliver Method before");
        }

        Class clazz = (Class)intent.getSerializableExtra(Constant.IntentParams.INTENT_CLASS);
        try {
            Method[] methods = clazz.getDeclaredMethods();
            Class typeClazz = null;
            if(intent.hasExtra(Constant.IntentParams.INTENT_PARAMS_TYPE)){
                typeClazz = (Class)intent.getSerializableExtra(Constant.IntentParams.INTENT_PARAMS_TYPE);
            }
            Object params = null;
            if(typeClazz != null) {
                if (typeClazz.equals(String.class)) {
                    params = intent.getStringExtra(Constant.IntentParams.INTENT_PARAMS);
                } else if (typeClazz.equals(Integer.class)) {
                    params = intent.getIntExtra(Constant.IntentParams.INTENT_PARAMS, -1);
                } else if (typeClazz.equals(Long.class)) {
                    params = intent.getLongExtra(Constant.IntentParams.INTENT_PARAMS, -1);
                } else if (typeClazz.equals(Double.class)) {
                    params = intent.getDoubleExtra(Constant.IntentParams.INTENT_PARAMS, -1);
                } else if (typeClazz.equals(Float.class)) {
                    params = intent.getFloatExtra(Constant.IntentParams.INTENT_PARAMS, -1);
                }else if(typeClazz.equals(Boolean.class)){
                    params = intent.getBooleanExtra(Constant.IntentParams.INTENT_PARAMS, false);
                }
            }

            String methodName = intent.getStringExtra(Constant.IntentParams.INTENT_METHOD);
            Object object = objectMap.get(clazz);
            boolean isMethodSearch = false;
            for(Method method : methods) {
                if(method.getName().equals(methodName) && method.isAnnotationPresent(DeliverMethod.class)) {
                    isMethodSearch = true;
                    method.setAccessible(true);
                    if(params != null) {
                        method.invoke(object, params);
                    }else{
                        method.invoke(object);
                    }
                    break;
                }
            }

            if(!isMethodSearch){
                throw new Exception("Deliver method's name is" + methodName + ", is it right or @DeliverMethod?");
            }
        }catch (InvocationTargetException e) {
            Logger.e(e.toString());
        } catch (IllegalAccessException e) {
            Logger.e(e.toString());
        }
    }

    public void register(Object object) {
        objectMap.put(object.getClass(), object);
    }

    public void unregister(Object object){
        objectMap.remove(object);
    }
}
