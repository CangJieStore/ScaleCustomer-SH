package com.cangjie.scalage.kit.update.net;

public interface  HttpCallbackModelListener<T> {
    // 网络请求成功
    void onFinish(T response);

    // 网络请求失败
    void onError(Exception e);
}