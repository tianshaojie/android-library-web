# WebView 独立进程，JsBridge调用

## 1. WebView独立进程配置

* 独立H5进程

## 2. 独立进程-JS调用

* AIDL + JsBridge，参考JavaScriptMethod.java写法


## 3. 独立进程-首次启动速度优化，预加载进程

* WebViewPreLoadService.java

```
 * 进程预加载，一次性使用，主要为了创建H5进程
 * 一般在SplashActivity内调用：
 * SplashActivity onCreate  调用 startHideService
 * SplashActivity onDestory 调用 stopHideService
```