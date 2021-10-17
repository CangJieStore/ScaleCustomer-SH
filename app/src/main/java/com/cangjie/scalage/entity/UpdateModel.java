package com.cangjie.scalage.entity;


import com.cangjie.scalage.kit.update.model.LibraryUpdateEntity;

public class UpdateModel implements LibraryUpdateEntity {


    private int isForceUpdate;
    private int preBaselineCode;
    private String versionName;
    private int versionCode;
    private String downurl;
    private String updateLog;
    private String size;
    private String hasAffectCodes;
    private long createTime;
    private int iosVersion;

    public int getIsForceUpdate() {
        return isForceUpdate;
    }

    public void setIsForceUpdate(int isForceUpdate) {
        this.isForceUpdate = isForceUpdate;
    }

    public int getPreBaselineCode() {
        return preBaselineCode;
    }

    public void setPreBaselineCode(int preBaselineCode) {
        this.preBaselineCode = preBaselineCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getDownurl() {
        return downurl;
    }

    public void setDownurl(String downurl) {
        this.downurl = downurl;
    }

    public String getUpdateLog() {
        return updateLog;
    }

    public void setUpdateLog(String updateLog) {
        this.updateLog = updateLog;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getHasAffectCodes() {
        return hasAffectCodes;
    }

    public void setHasAffectCodes(String hasAffectCodes) {
        this.hasAffectCodes = hasAffectCodes;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public int getIosVersion() {
        return iosVersion;
    }

    public void setIosVersion(int iosVersion) {
        this.iosVersion = iosVersion;
    }

    @Override
    public int getAppVersionCode() {
        return getVersionCode();
    }

    @Override
    public int forceAppUpdateFlag() {
        return getIsForceUpdate();
    }

    @Override
    public String getAppVersionName() {
        return getVersionName().replaceFirst("v", "");
    }

    @Override
    public String getAppApkUrls() {
        return getDownurl();
    }

    @Override
    public String getAppUpdateLog() {
        return getUpdateLog();
    }

    @Override
    public String getAppApkSize() {
        return getSize();
    }

    @Override
    public String getAppHasAffectCodes() {
        return getHasAffectCodes();
    }

    @Override
    public String getFileMd5Check() {
        return null;
    }
}
