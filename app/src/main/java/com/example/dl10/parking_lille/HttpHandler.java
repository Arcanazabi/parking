package com.example.dl10.parking_lille;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
/**
 * Created by DL10 on 31/01/2017.
 */

public class HttpHandler {

    private static final String TAG = HttpHandler.class.getSimpleName();

    public HttpHandler(){

    }

    public String makeServiceCall(String reqURL){
        String response = null;
        try{
            URL url = new URL(reqURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            InputStream in = new BufferedInputStream(conn.getInputStream());
            response = convertStreamtoString(in);
        }
        catch ( MalformedURLException e){
            Log.e(TAG, "MalformedURLexception: "+e.getMessage());
        }
        catch (ProtocolException e){
            Log.e(TAG, "ProtocolException: "+e.getMessage());
        }
        catch (IOException e){
            Log.e(TAG,"IOException: "+e.getMessage());
        }
        catch (Exception e){
            Log.e(TAG, "Exception: "+e.getMessage());
        }
        return  response;
    }
    private String convertStreamtoString(InputStream is){
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        try{
            while((line = reader.readLine()) != null){
                sb.append(line).append('\n');
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
        finally {
            try{
                is.close();
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

}
