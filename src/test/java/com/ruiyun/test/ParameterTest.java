package com.ruiyun.test;

import com.ruiyun.jvppeteer.events.impl.DefaultBrowserListener;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

public class ParameterTest {
    public static void main(String[] args) {
        DefaultBrowserListener<String> traceListener = new DefaultBrowserListener<String>() {
            @Override
            public void onBrowserEvent(String event) {

            }
        };
        DefaultBrowserListener traceListener2 = new DefaultBrowserListener() {
            @Override
            public void onBrowserEvent(Object event) {

            }
        };
        TypeVariable<? extends Class<? extends DefaultBrowserListener>>[] typeParameters = traceListener.getClass().getTypeParameters();
        System.out.println(typeParameters.length);
        for (TypeVariable<? extends Class<? extends DefaultBrowserListener>> typeParameter : typeParameters) {
            System.out.println(typeParameter.getName());
        }

        Type genericSuperclass2 = traceListener2.getClass().getGenericSuperclass();
        if(genericSuperclass2 instanceof ParameterizedType){
            System.out.println("2shi fanyinglei ");
        }
        Type genericSuperclass1 = traceListener.getClass().getGenericSuperclass();
        if(genericSuperclass1 instanceof ParameterizedType){
            System.out.println("1shi fanyinglei ");
        }
        String typeName = genericSuperclass1.getTypeName();
        ParameterizedType genericType =  (ParameterizedType)genericSuperclass1;
        Type[] actualTypeArguments = genericType.getActualTypeArguments();
        System.out.println(actualTypeArguments[0]);
        System.out.println("typeName:"+typeName);
    }
}
