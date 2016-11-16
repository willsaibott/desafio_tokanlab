package com.tokenlab.guinb.desafio_tokenlab;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by guinb on 11/10/2016.
 *
 * json factory is a Parser Class
 */

public class JsonDataFactory {

    // read json input stream
    public static String readStream(InputStream inputStream) throws IOException {
        if (inputStream != null) {
            Writer writer = new StringWriter();
            char[] buffer = new char[1024];
            try {
                Reader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"),1024);
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
            } finally {
                inputStream.close();
            }
            return writer.toString();
        } else {
            return "";
        }
    }

    // parsing inputStream with gjson
    public static JsonData getData(InputStream inputStream){
        Gson gson = new Gson();
        JsonData jsonData = null;
        try {
            jsonData = gson.fromJson(readStream(inputStream), JsonData.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonData;
    }
}
