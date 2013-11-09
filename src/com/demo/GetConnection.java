package com.demo;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class GetConnection{
    public static byte[] getimage(String path) throws Exception {
        URL url = new URL(path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5 * 1000);
        InputStream inStream = conn.getInputStream();
        byte[] data = ReadStream.readinputStream(inStream);
        return data;
    }

}