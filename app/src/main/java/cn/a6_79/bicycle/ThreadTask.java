package cn.a6_79.bicycle;

import android.os.AsyncTask;
import android.util.Log;

import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


class ThreadTask extends AsyncTask<Void, Integer, Bicycle> {
    private OnAsyncTaskListener listener;
    private Bicycle bicycle;
    private String cookie = null;
    ThreadTask(Bicycle bicycle, OnAsyncTaskListener listener) {
        this.bicycle = bicycle;
        this.listener = listener;
    }

    @Override
    protected void onPreExecute() {

    }

    /*
    * 获取登录界面的 'randomStr'
    * */
    private String getRandomStr(HtmlTaskResponse htmlTaskResponse) {
        Pattern pattern = Pattern.compile("id =\"RandomStr\".*?>(.*?)<");
        Matcher matcher = pattern.matcher(htmlTaskResponse.getResponse());
        if (matcher.find()) {
            MatchResult matchResult = matcher.toMatchResult();
            if (CommonData.debug)
                Log.d("randomStr", matchResult.group(1));
            return matchResult.group(1);
        } else
            return null;
    }

    private String getBicycleData(HtmlTaskResponse htmlTaskResponse) {
        Pattern pattern = Pattern.compile("&Data=(.*?)&");
        Matcher matcher = pattern.matcher(htmlTaskResponse.getResponse());
        if (matcher.find()) {
            MatchResult matchResult = matcher.toMatchResult();
            if (CommonData.debug)
                Log.d("bicycleData", matchResult.group(1));
            return matchResult.group(1);
        } else {
            if (CommonData.debug)
                Log.d("bicycleData", "null");
            return null;
        }
    }

    private BicycleRecord getBicycleRecord(HtmlTaskResponse htmlTaskResponse) {
        String s = ".*?</span>";
        String p = "(.*?)</p>";
        String patternStr = "<p class=\"things\">"+p+s+"(.*?)</h3>"+s+p+s+s+s+s+s+s+p+s+p;
        Pattern pattern = Pattern.compile(patternStr, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(htmlTaskResponse.getResponse());
        if (matcher.find()) {
            MatchResult matchResult = matcher.toMatchResult();
            return new BicycleRecord(
                    matchResult.group(1),
                    matchResult.group(2),
                    matchResult.group(3),
                    matchResult.group(4),
                    matchResult.group(5));
        }
        else
            return null;
    }

    private UserInfo getUserInfo(HtmlTaskResponse htmlTaskResponse) {
        Pattern pattern = Pattern.compile("usercd.*?value=\"(.*?)\".*?value=\"(.*?)\".*?value=\"(.*?)\"", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(htmlTaskResponse.getResponse());
        if (matcher.find()) {
            MatchResult matchResult = matcher.toMatchResult();
            if (CommonData.debug) {
                Log.d("userID", matchResult.group(1));
                Log.d("phone", matchResult.group(2));
                Log.d("realName", matchResult.group(3));
            }
            return new UserInfo(matchResult.group(3), matchResult.group(2), matchResult.group(1));
        } else {
            if (CommonData.debug)
                Log.d("userInfo", "null");
            return null;
        }
    }

    private String getPostValue(String randomStr) {
        String phoneNo = bicycle.getAccount().getUsername();
        String password = bicycle.getAccount().getPassword();
        String encryptedPwd = HmacMD5.execute(password, randomStr);
        return "phoneNo="+phoneNo+"&encryptedPwd="+encryptedPwd;
    }

    private String getBicycleUrl(String bicycleData) {
        return "http://www.lbbcol.com/Barcode.aspx?BarcodeType=CODE128A&Data="+bicycleData
                    +"&ImageFormat=Png&CopyrightText=%20"
                    +"&BarWidth=1&Height=120&AddOnTextPosition=NotShown&InvalidDataAction=DisplayTextOnly";
    }

    @Override
    protected Bicycle doInBackground(Void... params) {
        HtmlTaskRequest htmlTaskRequest =
                new HtmlTaskRequest("http://www.lbbcol.com/UserLogin.aspx", "GET", "", cookie);
        HtmlTaskResponse htmlTaskResponse = Download.htmlTask(htmlTaskRequest);
        if (htmlTaskResponse == null || htmlTaskResponse.getResponse() == null)
            return null;
        cookie = htmlTaskResponse.getCookie();
        String randomStr = getRandomStr(htmlTaskResponse);
        if (randomStr == null)
            return null;
        if (isCancelled()) return null;

        String postValue = getPostValue(randomStr);
        htmlTaskRequest =
                new HtmlTaskRequest("http://www.lbbcol.com/UserLogin.aspx?Action=login", "POST", postValue, cookie);
        htmlTaskResponse = Download.htmlTask(htmlTaskRequest);
        if (htmlTaskResponse == null || htmlTaskResponse.getResponse() == null)
            return null;
        if (!htmlTaskResponse.getResponse().contains("萝卜白菜，一个专注于丰富生活的服务平台。"))
            return null;
        if (CommonData.debug)
            Log.d("cookie", htmlTaskResponse.getCookie());
        if (isCancelled()) return null;

        htmlTaskRequest =
                new HtmlTaskRequest("http://www.lbbcol.com/BikeIndex.aspx", "GET", "", cookie);
        htmlTaskResponse = Download.htmlTask(htmlTaskRequest);
        if (htmlTaskResponse == null || htmlTaskResponse.getResponse() == null)
            return null;
        String bicycleData = getBicycleData(htmlTaskResponse);
        String bicycleUrl = getBicycleUrl(bicycleData);
        if (CommonData.debug)
            Log.d("bicycleUrl", bicycleUrl);
        BicycleRecord bicycleRecord = getBicycleRecord(htmlTaskResponse);
        if (isCancelled()) return null;

        htmlTaskRequest =
                new HtmlTaskRequest("http://www.lbbcol.com/UserInfo.aspx", "GET", "", cookie);
        htmlTaskResponse = Download.htmlTask(htmlTaskRequest);
        if (htmlTaskResponse == null || htmlTaskResponse.getResponse() == null)
            return null;
        UserInfo userInfo = getUserInfo(htmlTaskResponse);
        bicycle.setBicycleRecord(bicycleRecord);
        bicycle.setBitmap(Download.getBitmap(bicycleUrl));
        bicycle.setUserInfo(userInfo);
        return bicycle;
    }

    @Override
    protected void onPostExecute(Bicycle bicycle) {
        super.onPostExecute(bicycle);
        if (isCancelled()) return;
        if (listener != null)
            listener.callback(bicycle);
    }
}

interface OnAsyncTaskListener {
    void callback(Bicycle bicycle);
}
