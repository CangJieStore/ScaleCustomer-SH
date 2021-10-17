package com.cangjie.scalage.kit.update.model;

public class TypeConfig {
    /**
     * 更新信息的来源
     */
    public static final int DATA_SOURCE_TYPE_MODEL = 10;//调用方提供信息model
    public static final int DATA_SOURCE_TYPE_URL = 11;//通过配置链接供sdk自主请求
    public static final int DATA_SOURCE_TYPE_JSON = 12;//调用方提供信息json

    /**
     * 请求方式类型
     */
    public static final int METHOD_GET = 20; //GET请求
    public static final int METHOD_POST = 21; //POST请求

    /**
     * UI样式类型
     */
    public static final int UI_THEME_AUTO = 300;
    public static final int UI_THEME_CUSTOM = 399;
    public static final int UI_THEME_L = 312;//类型K，具体样式效果请关注demo
}
