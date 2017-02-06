package cn.a6_79.bicycle;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

class CommonData {
    static ArrayList<Bicycle> bicycles = new ArrayList<>();
    static int currentFragment = -1;
    static OnAsyncTaskListener currentOnAsyncTaskListener = null;
    static boolean debug = false;
}

class Bicycle {
    private Account account;
    private boolean refresh;
    private BicycleRecord bicycleRecord;
    private Bitmap bitmap;
    private UserInfo userInfo;
    private ThreadTask threadTask;
    Bicycle(Account account) {
        this.account = account;
        this.refresh = true;
    }
    Account getAccount() { return account; }
    BicycleRecord getBicycleRecord() { return bicycleRecord; }
    void setBicycleRecord(BicycleRecord bicycleRecord) { this.bicycleRecord = bicycleRecord; }
    Bitmap getBitmap() { return bitmap; }
    void setBitmap(Bitmap bitmap) { this.bitmap = bitmap; }
    UserInfo getUserInfo() { return userInfo; }
    void setUserInfo(UserInfo userInfo) { this.userInfo = userInfo; }
    void setRefresh(boolean refresh) { this.refresh = refresh; }
    ThreadTask getThreadTask() { return threadTask; }
    void setThreadTaskNull() { threadTask = null; }
    static void refresh(int fragmentNumber, OnAsyncTaskListener onAsyncTaskListener) {
        try {
            Bicycle bicycle = CommonData.bicycles.get(fragmentNumber);
            ThreadTask threadTask = bicycle.threadTask;
            if (threadTask == null || threadTask.getStatus() == AsyncTask.Status.FINISHED && bicycle.refresh) {
                bicycle.refresh = false;
                bicycle.threadTask = new ThreadTask(bicycle, onAsyncTaskListener);
                bicycle.threadTask.execute();
            }
            else {
                onAsyncTaskListener.callback(bicycle);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class UserInfo {
    private String realname;
    private String phone;
    private String userID;
    UserInfo(String name, String phone, String id) {
        this.userID = id;
        this.realname = name;
        this.phone = phone;
    }
    String getRealname() { return realname; }
    String getPhone() { return phone; }
    String getUserID() { return userID; }
}

class BicycleRecord {
    private String number;
    private String startPoint;
    private String startTime;
    private String endPoint;
    private String endTime;
    BicycleRecord(String number, String startPoint, String startTime, String endPoint, String endTime) {
        this.number = number;
        this.startPoint = startPoint;
        this.startTime = startTime;
        this.endPoint = endPoint;
        this.endTime = endTime;
    }
    String getNumber() { return number; }
    String getStartPoint() { return startPoint; }
    String getStartTime() { return startTime; }
    String getEndPoint() { return endPoint; }
    String getEndTime() { return endTime; }
}

class Account {
    private String username;
    private String password;
    Account(String username, String password) {
        setAccount(username, password);
    }
    String getUsername() { return username; }
    String getPassword() { return password; }
    void setAccount(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
