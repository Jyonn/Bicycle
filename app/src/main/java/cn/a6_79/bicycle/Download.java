package cn.a6_79.bicycle;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

class Download {
    static HtmlTaskResponse htmlTask(HtmlTaskRequest htmlTaskRequest) {
        try {
            URL url = new URL(htmlTaskRequest.getUrl());
            HttpURLConnection mHttpURLConnection = (HttpURLConnection) url.openConnection();
            mHttpURLConnection.setConnectTimeout(15000);
            mHttpURLConnection.setReadTimeout(15000);
            mHttpURLConnection.setRequestMethod(htmlTaskRequest.getMethod());
            mHttpURLConnection.setRequestProperty("Connection", "Keep-Alive");
            if (htmlTaskRequest.getSession() != null)
                mHttpURLConnection.setRequestProperty("Cookie", htmlTaskRequest.getSession());
            mHttpURLConnection.setDoInput(true);
            mHttpURLConnection.setDoOutput(true);
            mHttpURLConnection.setUseCaches(false);
            mHttpURLConnection.connect();

            DataOutputStream dos = new DataOutputStream(mHttpURLConnection.getOutputStream());
            dos.write(htmlTaskRequest.getValue().getBytes());
            dos.flush();
            dos.close();

            int respondCode = mHttpURLConnection.getResponseCode();
            String cookieValue = mHttpURLConnection.getHeaderField("Set-Cookie");
            try {
                if (cookieValue != null)
                    cookieValue = cookieValue.substring(0, cookieValue.indexOf(";"));
            } catch (Exception e) {
                Log.d("Error", "1");
                e.printStackTrace();
                return null;
            }
            if (respondCode != 200) {
                Log.d("Error", "2 "+respondCode);
                return null;
            }

            InputStream is = mHttpURLConnection.getInputStream();
            ByteArrayOutputStream response = new ByteArrayOutputStream();
            int len;
            byte buffer[] = new byte[1024];
            while ((len = is.read(buffer)) != -1)
                response.write(buffer, 0, len);
            is.close();
            response.close();

            return new HtmlTaskResponse(cookieValue, new String(response.toByteArray()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("Error", "3");
        return null;
    }
    static Bitmap getBitmap(String s) {
        Bitmap bitmap, bitmapFinal = null;
        try {
            URL url = new URL(s);
            HttpURLConnection mHttpURLConnection = (HttpURLConnection) url.openConnection();
            mHttpURLConnection.setDoInput(true);
            mHttpURLConnection.connect();
            InputStream is = mHttpURLConnection.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);
            is.close();

            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int[] pixels = new int[width * height];
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
            for (int i = 0; i < width * height; i++)
                if ((pixels[i] & 0X00FFFFFF) == 0X00FFFFFF)
                    pixels[i] = 0X00000000;
            bitmapFinal = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmapFinal.setPixels(pixels, 0, width, 0, 0, width, height);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmapFinal;
    }
}

class HtmlTaskResponse {
    private String cookie;
    private String response;
    HtmlTaskResponse(String cookie, String response) {
        this.cookie = cookie;
        this.response = response;
    }
    String getCookie() { return cookie; }
    String getResponse() { return response; }
}

class HtmlTaskRequest {
    private String url;
    private String method;
    private String value;
    private String session;
    HtmlTaskRequest(String url, String method, String value, String session) {
        this.url = url;
        this.method = method;
        this.value = value;
        this.session = session;
    }
    String getUrl() { return url; }
    String getMethod() { return method; }
    String getValue() { return value; }
    String getSession() { return session; }
}
