package cn.skyui.aidl;

// :h5 进程下 WebViewActivity 与 宿主 App 通信，需要 App 暴露的方法
interface IAppInterface {
    // data
    String getToken();
    boolean isLogin();
    long getUserId();

    // ui
    void invokeShare();
}
