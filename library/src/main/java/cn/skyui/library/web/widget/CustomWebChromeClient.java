package cn.skyui.library.web.widget;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.widget.Toast;

import cn.skyui.library.web.activity.WebViewActivity;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author tianshaojie
 * @date 2018/2/9
 */
public class CustomWebChromeClient extends android.webkit.WebChromeClient {

    private WebViewActivity mWebViewActivity;
    private UploadHandler mUploadHandler;

    public CustomWebChromeClient(WebViewActivity mainActivity) {
        super();
        mWebViewActivity = mainActivity;
    }

    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        try {
            if (newProgress == 100) {
                ((CustomWebView)view).mProgressBar.setVisibility(View.GONE);
            } else {
                if (((CustomWebView)view).mProgressBar.getVisibility() == View.GONE) {
                    ((CustomWebView) view).mProgressBar.setVisibility(View.VISIBLE);
                }
                ((CustomWebView)view).mProgressBar.setProgress(newProgress);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onProgressChanged(view, newProgress);
    }

    @Override
    public void onReceivedTitle(WebView view, String title) {
        super.onReceivedTitle(view, title);
        if (view.getTitle() != null) {
            mWebViewActivity.setTitle(view.getTitle());
        }
    }

    // Android 2.x
    public void openFileChooser(ValueCallback<Uri> uploadMsg) {
        openFileChooser(uploadMsg, "");
    }

    // Android 3.0
    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
        openFileChooser(uploadMsg, "", "filesystem");
    }

    // Android 4.1
    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
        mUploadHandler = new UploadHandler(new Controller());
        mUploadHandler.openFileChooser(uploadMsg, acceptType, capture);
    }

    // Android 4.4, 4.4.1, 4.4.2 (注：可以使用JS调用方式 window.lfJsObj.chooseImage().)
    // openFileChooser function is not called on Android 4.4, 4.4.1, 4.4.2,
    // you may use your own java script interface or other hybrid framework.
    // https://code.google.com/p/android/issues/detail?id=62220

    // Android 5.0.1
    @Override
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public boolean onShowFileChooser(
            WebView webView, ValueCallback<Uri[]> filePathCallback,
            FileChooserParams fileChooserParams) {

        String acceptTypes[] = fileChooserParams.getAcceptTypes();

        String acceptType = "";
        for (int i = 0; i < acceptTypes.length; ++i) {
            if (acceptTypes[i] != null && acceptTypes[i].length() != 0) {
                acceptType += acceptTypes[i] + ";";
            }
        }
        if (acceptType.length() == 0) {
            acceptType = "*/*";
        }

        final ValueCallback<Uri[]> finalFilePathCallback = filePathCallback;

        ValueCallback<Uri> vc = new ValueCallback<Uri>() {
            @Override
            public void onReceiveValue(Uri value) {
                Uri[] result;
                if (value != null) {
                    result = new Uri[]{value};
                } else {
                    result = null;
                }
                finalFilePathCallback.onReceiveValue(result);
            }
        };
        openFileChooser(vc, acceptType, "filesystem");
        return true;
    }

    // The new code
    public void showPicker(ValueCallback<Uri> uploadMsg) {
        // Here is part of the issue, the uploadMsg is null since it is not triggered from Android
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");
        mWebViewActivity.startActivityForResult(Intent.createChooser(i, "File Chooser"), Controller.FILE_SELECTED);
    }

    class Controller {
        final static int FILE_SELECTED = 4;
        Activity getActivity() {
            return mWebViewActivity;
        }
    }

    class UploadHandler {
        /*
         * The Object used to inform the WebView of the file to upload.
         */
        private ValueCallback<Uri> mUploadMessage;
        private String mCameraFilePath;
        private boolean mHandled;
        private boolean mCaughtActivityNotFoundException;
        private Controller mController;

        public UploadHandler(Controller controller) {
            mController = controller;
        }

        String getFilePath() {
            return mCameraFilePath;
        }

        boolean handled() {
            return mHandled;
        }

        void onResult(int resultCode, Intent intent) {
            if (resultCode == Activity.RESULT_CANCELED && mCaughtActivityNotFoundException) {
                // Couldn't resolve an activity, we are going to try again so skip
                // this result.
                mCaughtActivityNotFoundException = false;
                return;
            }
            Uri result = intent == null || resultCode != Activity.RESULT_OK ? null
                    : intent.getData();
            // As we ask the camera to save the result of the user taking
            // a picture, the camera application does not return anything other
            // than RESULT_OK. So we need to check whether the file we expected
            // was written to disk in the in the case that we
            // did not get an intent returned but did get a RESULT_OK. If it was,
            // we assume that this result has came back from the camera.
            if (result == null && intent == null && resultCode == Activity.RESULT_OK) {
                File cameraFile = new File(mCameraFilePath);
                if (cameraFile.exists()) {
                    result = Uri.fromFile(cameraFile);
                    // Broadcast to the media scanner that we have a new photo
                    // so it will be added into the gallery for the user.
                    mController.getActivity().sendBroadcast(
                            new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, result));
                }
            }
            mUploadMessage.onReceiveValue(result);
            mHandled = true;
            mCaughtActivityNotFoundException = false;
        }

        void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
            final String imageMimeType = "image/*";
            final String videoMimeType = "video/*";
            final String audioMimeType = "audio/*";
            final String mediaSourceKey = "capture";
            final String mediaSourceValueCamera = "camera";
            final String mediaSourceValueFileSystem = "filesystem";
            final String mediaSourceValueCamcorder = "camcorder";
            final String mediaSourceValueMicrophone = "microphone";
            // According to the spec, media source can be 'filesystem' or 'camera' or 'camcorder'
            // or 'microphone' and the default value should be 'filesystem'.
            String mediaSource = mediaSourceValueFileSystem;
            if (mUploadMessage != null) {
                // Already a file picker operation in progress.
                return;
            }
            mUploadMessage = uploadMsg;
            // Parse the accept type.
            String params[] = acceptType.split(";");
            String mimeType = params[0];
            if (capture.length() > 0) {
                mediaSource = capture;
            }
            if (capture.equals(mediaSourceValueFileSystem)) {
                // To maintain backwards compatibility with the previous implementation
                // of the media capture API, if the value of the 'capture' attribute is
                // "filesystem", we should examine the accept-type for a MIME type that
                // may specify a different capture value.
                for (String p : params) {
                    String[] keyValue = p.split("=");
                    if (keyValue.length == 2) {
                        // Process key=value parameters.
                        if (mediaSourceKey.equals(keyValue[0])) {
                            mediaSource = keyValue[1];
                        }
                    }
                }
            }
            //Ensure it is not still set from a previous upload.
            mCameraFilePath = null;
            if (mimeType.equals(imageMimeType)) {
                if (mediaSource.equals(mediaSourceValueCamera)) {
                    // Specified 'image/*' and requested the camera, so go ahead and launch the
                    // camera directly.
                    startActivity(createCameraIntent());
                    return;
                } else {
                    // Specified just 'image/*', capture=filesystem, or an invalid capture parameter.
                    // In all these cases we show a traditional picker filetered on accept type
                    // so launch an intent for both the Camera and image/* OPENABLE.
                    Intent chooser = createChooserIntent(createCameraIntent());
                    chooser.putExtra(Intent.EXTRA_INTENT, createOpenableIntent(imageMimeType));
                    startActivity(chooser);
                    return;
                }
            } else if (mimeType.equals(videoMimeType)) {
                if (mediaSource.equals(mediaSourceValueCamcorder)) {
                    // Specified 'video/*' and requested the camcorder, so go ahead and launch the
                    // camcorder directly.
                    startActivity(createCamcorderIntent());
                    return;
                } else {
                    // Specified just 'video/*', capture=filesystem or an invalid capture parameter.
                    // In all these cases we show an intent for the traditional file picker, filtered
                    // on accept type so launch an intent for both camcorder and video/* OPENABLE.
                    Intent chooser = createChooserIntent(createCamcorderIntent());
                    chooser.putExtra(Intent.EXTRA_INTENT, createOpenableIntent(videoMimeType));
                    startActivity(chooser);
                    return;
                }
            } else if (mimeType.equals(audioMimeType)) {
                if (mediaSource.equals(mediaSourceValueMicrophone)) {
                    // Specified 'audio/*' and requested microphone, so go ahead and launch the sound
                    // recorder.
                    startActivity(createSoundRecorderIntent());
                    return;
                } else {
                    // Specified just 'audio/*',  capture=filesystem of an invalid capture parameter.
                    // In all these cases so go ahead and launch an intent for both the sound
                    // recorder and audio/* OPENABLE.
                    Intent chooser = createChooserIntent(createSoundRecorderIntent());
                    chooser.putExtra(Intent.EXTRA_INTENT, createOpenableIntent(audioMimeType));
                    startActivity(chooser);
                    return;
                }
            }
            // No special handling based on the accept type was necessary, so trigger the default
            // file upload chooser.
            startActivity(createDefaultOpenableIntent());
        }

        private void startActivity(Intent intent) {
            try {
                mController.getActivity().startActivityForResult(intent, Controller.FILE_SELECTED);
            } catch (ActivityNotFoundException e) {
                // No installed app was able to handle the intent that
                // we sent, so fallback to the default file upload control.
                try {
                    mCaughtActivityNotFoundException = true;
                    mController.getActivity().startActivityForResult(createDefaultOpenableIntent(), Controller.FILE_SELECTED);
                } catch (ActivityNotFoundException e2) {
                    // Nothing can return us a file, so file upload is effectively disabled.
                    Toast.makeText(mController.getActivity(), "File uploads are disabled", Toast.LENGTH_LONG).show();
                }
            }
        }

        private Intent createDefaultOpenableIntent() {
            // Create and return a chooser with the default OPENABLE
            // actions including the camera, camcorder and sound
            // recorder where available.
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("*/*");
            Intent chooser = createChooserIntent(createCameraIntent(), createCamcorderIntent(), createSoundRecorderIntent());
            chooser.putExtra(Intent.EXTRA_INTENT, i);
            return chooser;
        }

        private Intent createChooserIntent(Intent... intents) {
            Intent chooser = new Intent(Intent.ACTION_CHOOSER);
            chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents);
            chooser.putExtra(Intent.EXTRA_TITLE, "Choose file for upload");
            return chooser;
        }

        private Intent createOpenableIntent(String type) {
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType(type);
            return i;
        }

        private Intent createCameraIntent() {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File externalDataDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            File cameraDataDir = new File(externalDataDir.getAbsolutePath() + File.separator + "browser-photos");
            cameraDataDir.mkdirs();
            mCameraFilePath = cameraDataDir.getAbsolutePath() + File.separator + System.currentTimeMillis() + ".jpg";
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(mCameraFilePath)));
            return cameraIntent;
        }

        private Intent createCamcorderIntent() {
            return new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        }

        private Intent createSoundRecorderIntent() {
            return new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
        }

    }

    public static Map<String, String> splitQuery(URL url) throws UnsupportedEncodingException {
        Map<String, String> queryPairs = new LinkedHashMap<String, String>();
        String query = url.getQuery();
        if (query != null && query.length() > 0) {
            String[] pairs = query.split("&");
            if (pairs.length > 0) {
                for (String pair : pairs) {
                    int idx = pair.indexOf("=");
                    queryPairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"),
                            URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
                }
            }
        }
        return queryPairs;
    }
}