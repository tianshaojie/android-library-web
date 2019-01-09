// IAppInterface.aidl
package cn.skyui.aidl;

// Declare any non-default types here with import statements

interface IAppInterface {
    // data
    String getToken();
    boolean isLogin();
    long getUserId();

    // ui
    void invokeShare();
}
