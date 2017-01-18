package com.test.kumar.xingrepo;


        /*
        * Created by Kumar Saurabh on 1/17/2017.
        * This class is for downloadin the reading the JSON from the web and convert into string
        */

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpHandler {


    public String makeServiceCall(String reqUrl)
    {
        String response=null;

        try{
            URL url=new URL(reqUrl);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("GET");

            //read the response
            InputStream in= new BufferedInputStream(conn.getInputStream());
            response=convertStreamToString(in);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return response;
    }

    /**
     *
     * @param is strwam for the JSON
     * @return String object of the JSON content
     */
    private String convertStreamToString(InputStream is)
    {
        BufferedReader reader =new BufferedReader(new InputStreamReader(is));
        StringBuilder sb= new StringBuilder();
        String line;

        try
        {
            while ((line =reader.readLine())!=null)
            {
                sb.append(line).append('\n');
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return  sb.toString();
    }

}
