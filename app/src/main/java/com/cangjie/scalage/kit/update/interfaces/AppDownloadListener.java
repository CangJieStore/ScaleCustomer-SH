package com.cangjie.scalage.kit.update.interfaces;


public interface AppDownloadListener {
    void downloading(int progress);
    void downloadFail(String msg);
    void downloadComplete(String path);
    void downloadStart();
    void reDownload();
    void pause();
}
