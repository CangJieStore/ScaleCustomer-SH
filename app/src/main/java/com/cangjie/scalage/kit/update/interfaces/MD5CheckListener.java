package com.cangjie.scalage.kit.update.interfaces;


public interface MD5CheckListener {
    void fileMd5CheckFail(String originMD5, String localMD5);

    void fileMd5CheckSuccess();
}
