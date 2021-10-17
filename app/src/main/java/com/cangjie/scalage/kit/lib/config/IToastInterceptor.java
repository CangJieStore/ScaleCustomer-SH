package com.cangjie.scalage.kit.lib.config;


public interface IToastInterceptor {

    /**
     * 根据显示的文本决定是否拦截该 Toast
     */
    boolean intercept(CharSequence text);
}