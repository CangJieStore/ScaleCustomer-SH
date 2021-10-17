package com.cangjie.scalage.kit.lib.config;

import android.app.Application;


public interface IToastStrategy {

    /**
     * 注册策略
     */
    void registerStrategy(Application application);

    /**
     * 绑定样式
     */
    void bindStyle(IToastStyle<?> style);

    /**
     * 创建 Toast
     */
    IToast createToast(Application application);

    /**
     * 显示 Toast
     */
    void showToast(CharSequence text);

    /**
     * 取消 Toast
     */
    void cancelToast();
}