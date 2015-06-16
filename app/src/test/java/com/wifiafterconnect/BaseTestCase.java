package com.wifiafterconnect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;


/**
 * Created by brad on 5/12/15.
 *
 * Other tests can inherit from this class to gain access to it's methods
 */
public class BaseTestCase {
    public Object getPrivateField(Object obj, String field_name) throws Exception {
        Field field = obj.getClass().getDeclaredField(field_name);
        field.setAccessible(true);
        return field.get(obj);
    }

    public Method getPrivateMethod(Object obj, String method_name) throws Exception {
        Method method = obj.getClass().getDeclaredMethod(method_name);
        method.setAccessible(true);
        return method;
    }

    public Object invokePrivateMethod(Object obj, String method_name) throws Exception {
        return getPrivateMethod(obj, method_name).invoke(obj);
    }

    public Object invokePrivateMethod(Object obj, String method_name, Object arg1) throws Exception {
        return getPrivateMethod(obj, method_name).invoke(obj, arg1);
    }
}