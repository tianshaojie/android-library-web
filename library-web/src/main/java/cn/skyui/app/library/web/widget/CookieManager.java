package cn.skyui.app.library.web.widget;

import android.content.Context;
import android.webkit.CookieSyncManager;

//import cn.skyui.app.library.data.model.User;
//import cn.skyui.app.library.http.model.Header;
//import cn.skyui.app.library.utils.AppUtils;
//import cn.skyui.app.library.utils.NetworkUtils;
//import cn.skyui.app.library.utils.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CookieManager {
    
    public static void syncCookies(Context context, String url) {
        String regEx = "^\\w+://([\\w\\d]+\\.)*([\\w\\d]+\\.[\\w\\d]+)(:\\d+)?/";
        Pattern pat = Pattern.compile(regEx);
        Matcher mat = pat.matcher(url);
        if (!mat.find()) {
            return;
        }

        String domainUrl = mat.group(2);
        if (domainUrl != null && domainUrl.length() > 0) {
            CookieSyncManager.createInstance(context);
            android.webkit.CookieManager cookieManager = android.webkit.CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);
            cookieManager.removeAllCookie();//移除
            cookieManager.setCookie(url, "path=" + "/; ");
            cookieManager.setCookie(url, "domain=" + "." + domainUrl);
            cookieManager.setCookie(url, "expires=" + expires());

            /*User user = UserManager.getInstance().getUser();
            cookieManager.setCookie(url, "username=" + StringUtils.enCodeRUL(user.userName()));
            cookieManager.setCookie(url, "nick_name=" + StringUtils.enCodeRUL(user.name()));
            cookieManager.setCookie(url, "userphoto=" + StringUtils.enCodeRUL(user.image()));
            cookieManager.setCookie(url, "userid=" + user.userId());
            cookieManager.setCookie(url, "authkey=" + Encode.encode(user.userName()));*/

            // TODO: 2018/2/13  
//            cookieManager.setCookie(url, "version=" + AppUtils.getAppVersionName());
//            cookieManager.setCookie(url, "token=" + Header.token);
//            cookieManager.setCookie(url, "anonymous=" + (User.getInstance().isLogin ? 1 : 0));
//            String wifi = NetworkUtils.isWifiConnected() ? "1" : "0";
//            cookieManager.setCookie(url, "wifi=" + wifi);
            CookieSyncManager.getInstance().sync();
        }
    }  
    
    private static String expires(){
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("E,dd MMM yyyy HH:mm:ss ", Locale.ENGLISH);
        String nowDate = sdf.format(date);
        Calendar cal = Calendar.getInstance();
        try {
            cal.setTime(sdf.parse(nowDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }  
        cal.add(Calendar.DAY_OF_YEAR, + 1);
        String nextDate_1 = sdf.format(cal.getTime());
        return nextDate_1 + "GMT";
    }
}
