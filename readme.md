# WebView 独立进程，JsBridge调用

## 1. WebView独立进程配置

* 独立H5进程；
* WebView优化；

## 2. 进程间通信，H5进程调用APP进程信息

* WebView 组件内定义的 AIDL 接口（IAppInterface.aidl）；
* App相当于服务端，实现 WebView AIDL 接口 (WebViewBridgeService.java)；
* WebViewActivity bindService 拿到 service 实例调用App容器逻辑 (ServiceConnection.java)；
* JsBridge 维护，参考JavaScriptMethod.java
* [js 调用 demo](http://skyui.cn/interest/lib-web.html)



## 3. 独立进程-首次启动速度优化，预加载进程

* WebViewPreLoadService.java

```
 * 进程预加载
 * 一般在SplashActivity内调用：
 * SplashActivity onCreate  调用 startHideService
 * SplashActivity onDestory 调用 stopHideService
```