package com.example.dl10.parking_lille;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

/**
 * Created by DL10 on 31/01/2017.
 */

public class API_G {
    private String API_KEY = "AIzaSyAW0DBHTZF2HuzncoLWR9sT6QION-I4gPc";
    private String url = null;
    private HashMap<Integer, Double> resultat = new HashMap();
    public HashMap getResultat(){
        return resultat;
    }

    public double distance_itineraire(double x_depart, double y_depart, double x_arriver, double y_arriver) {
        HttpURLConnection connection = null;
        InputStream flux = null;
        double dist = 0;
        String ligne = "";
        String resultat = "";
        this.url = "https://maps.googleapis.com/maps/api/directions/json?origin=" + x_depart + "," + y_depart + "&destination=" + x_arriver + "," + y_arriver + "&key="+API_KEY;

        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.setDoOutput(false);
            connection.connect();
            flux = connection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(flux));

            while ((ligne = br.readLine()) != null) {
                resultat = resultat + ligne + "\n";
            }

            JSONObject jsObj = new JSONObject(resultat);
            JSONArray jsarr = jsObj.getJSONArray("routes");
            jsObj = jsarr.getJSONObject(0);
            jsarr = jsObj.getJSONArray("legs");
            jsObj = jsarr.getJSONObject(0);

            dist= jsObj.getJSONObject("distance").getDouble("value");

        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            try {
                flux.close();
            } catch (Throwable t) {
            }
            try {
                connection.disconnect();
            } catch (Throwable t) {
            }
        }
        return dist;
    }

    public ThreadGoogleDistance getThread() {
        return new ThreadGoogleDistance();
    }

    public class ThreadGoogleDistance extends AsyncTask<Double, Void, Double> {

        double id = 0;
        @Override
        protected Double doInBackground(Double... params) {

            id = params[4];
            Double d = new Double(id);
            resultat.put(d.intValue(),distance_itineraire(params[0], params[1], params[2], params[3]));
            return 0.0;
        }

        @Override
        protected void onPostExecute(Double result)
        {
        }
    }
}