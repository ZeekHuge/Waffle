package com.demo;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class ReadStream {

    public static byte[] readinputStream(InputStream inputStream) throws Exception{
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int lns = 0;
        while ((lns = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, lns);
        }
        inputStream.close();
        return outputStream.toByteArray();
    }
}