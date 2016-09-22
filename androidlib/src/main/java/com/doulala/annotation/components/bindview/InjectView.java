package com.doulala.annotation.components.bindview;

import android.app.Activity;

/**
 * 尝试查找class文件
 */

public class InjectView {
    public static void bind(Activity activity){
        String clsName = activity.getClass().getName();
        try {
            Class<?> viewBindingClass = Class.forName(clsName + "$ViewBinder");
            ViewBinder viewBinder = (ViewBinder)viewBindingClass.newInstance();
            viewBinder.bind(activity);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
