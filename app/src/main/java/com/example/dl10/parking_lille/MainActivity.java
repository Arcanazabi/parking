package com.example.dl10.parking_lille;

import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import java.util.Timer;
import java.util.TimerTask;

import im.delight.android.location.SimpleLocation;

public class MainActivity extends AppCompatActivity {

    private static String url = "https://opendata.lillemetropole.fr/api/records/1.0/search/?dataset=disponibilite-parkings&rows=24&facet=libelle&facet=ville&facet=etat";
    private static double[] touteCoordonnees = null;
    private ListView lv;
    private String coord;
    private boolean is_set =false;
    private Timer refresh = new Timer();
    SimpleLocation location = null;
    static ArrayList<HashMap<String, String>> contactList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        contactList = new ArrayList<>();
        lv = (ListView) findViewById(R.id.list);
    }

    @Override
    protected void onResume() {

        super.onResume();
        location = new SimpleLocation(MainActivity.this);
        if (!location.hasLocationEnabled()) {
            // ask the user to enable location access
            android.app.DialogFragment dialog = new PermissionGps();
            dialog.show(getFragmentManager(),"dialog");
        }else
        {
        //Si on a la permission, on récupère par GPS la mise à jour de la position
        //On lance le thread qui récupère la liste des parkings
            refresh = new Timer();
            refresh.schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            contactList = new ArrayList<>();
                            //Si la vue existe
                            if (is_set)
                            {
                                lv.setAdapter(null);
                                is_set = true;
                            }
                            new RecupParking().execute();
                        }
                    });
                }
            },0,300000);
        }
    }

    @Override
    protected void onPause()
    {
        refresh.cancel();
        super.onPause();
    }

    public static String ajout_distance(int id , double dist)
    {
        contactList.get(id).put("distance", dist+"");

        //On rempli le tableau avec la latitude et la longitude à l'index +1
        touteCoordonnees[id] = Double.parseDouble(contactList.get(id).get("latitude"));
        touteCoordonnees[(id+1)] = Double.parseDouble(contactList.get(id).get("longitude"));
        return null;
    }

    public void Go(View v){

        if(coord != null) {

            Intent launchMap = new Intent(this, Affichemap.class);
            TextView latitude = (TextView) v.findViewById(R.id.Latitute);
            TextView longitude =(TextView) v.findViewById(R.id.Long);
            launchMap.putExtra("latitude", latitude.getText());
            launchMap.putExtra("longitude", longitude.getText());
            launchMap.putExtra("coordonnes", touteCoordonnees);
            startActivity(launchMap);
        }
        else
        {
            Alert();
        }

    }

    public void Alert()
    {
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage("Champ manquant");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    public class RecupParking extends AsyncTask<Void, Void, Void> {

        Intent intent = new Intent(MainActivity.this, MainActivity.class);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            String etat;
            String dispo;
            HttpHandler sh = new HttpHandler();
            String jsonStr = sh.makeServiceCall(url);
            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    JSONArray parking = jsonObj.getJSONArray("records");
                    for (int i = 0; i < parking.length(); i++) {
                        JSONObject c = parking.getJSONObject(i);
                        JSONObject field = c.getJSONObject("fields");
                        String libelle = field.getString("libelle");
                        String adresse = field.getString("adresse");
                        String ville = field.getString("ville");
                        Calendar cale = Calendar.getInstance();
                        int seconds = cale.get(Calendar.MILLISECOND);
                        try{
                            dispo  = field.getString("dispo");
                            etat = field.getString("etat");
                        }
                        catch (Exception e){
                            dispo = "Indisponible";
                            etat = "Indisponible";
                        }

                        coord = field.getString("coordgeo");
                        String[] recupCoord = coord.split(",");
                        HashMap<String, String> contact = new HashMap<>();
                        contact.put("adresse", adresse);
                        contact.put("libelle", libelle);
                        contact.put("ville", ville);
                        contact.put("Dispo", dispo);
                        contact.put("Etat", etat);
                        contact.put("date", seconds+"");
                        contact.put("Longitude", recupCoord[1].substring(0,recupCoord[1].length()-1));
                        contact.put("Latitude", recupCoord[0].substring(1));
                        contactList.add(contact);

                    }
                }
                catch (final JSONException e)
                {
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            Toast.makeText(getApplicationContext(), "Json parsing error: "+e.getMessage(),Toast.LENGTH_LONG).show();
                        }
                    });
                }

            }
            else{
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });
                DialogFragment reseau = new Dialog_Reseau();
                reseau.show(getFragmentManager(),"dialog");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            //Quand j'ai récupéré les données, je créer mon tableau que j'enverrai a la Gmap pour ajouter tout les marqueurs.
            //Je le remplis avec le thread qui calcul la distance pour plus de performance.
            //Il contient le double de la taille de la liste des parking (latitude, longitude)
            touteCoordonnees = new double[(contactList.size()*2)];

            API_G gestion_google =  new API_G();

            for(int i =0; i<contactList.size(); i++)
            {
                API_G.ThreadGoogleDistance test = gestion_google.getThread();
                test.execute(location.getLatitude(), location.getLongitude(), Double.parseDouble(contactList.get(i).get("Latitude")), Double.parseDouble(contactList.get(i).get("Longitude")), Double.parseDouble(i+""));

            }
            while(gestion_google.getResultat().size() != contactList.size())
            {
            }
            for (int i = 0 ; i< contactList.size(); i++)
            {
                //On récupère l'id et on ajoute la distance
                contactList.get(i).put("distanceEnDouble" , gestion_google.getResultat().get(i).toString());
                Log.d("..............", gestion_google.getResultat().get(i).toString());

                Double resultat = Double.parseDouble(gestion_google.getResultat().get(i).toString())/1000;
                String distance = ((Double.parseDouble(gestion_google.getResultat().get(i).toString())/1000)+"").split("\\.")[0]+ "." +((Double.parseDouble(gestion_google.getResultat().get(i).toString())/1000)+"").split("\\.")[1].substring(0,1) + " km";
                contactList.get(i).put("distance", distance);

                //On rempli le tableau avec la latitude et la longitude à l'index +1
                touteCoordonnees[i*2] = Double.parseDouble(contactList.get(i).get("Latitude"));
                touteCoordonnees[(i*2)+1] = Double.parseDouble(contactList.get(i).get("Longitude"));
            }
            Collections.sort(contactList, new Comparator<HashMap<String, String>>() {
                @Override
                public int compare(HashMap<String, String> o1, HashMap<String, String> o2) {
                    String etat1;
                    String etat2;

                    if (o1.get("Etat") == "LIBRE") etat1 = "OUVERT";
                    else etat1 =o1.get("Etat");
                    if (o2.get("Etat") == "LIBRE") etat2 = "OUVERT";
                    else etat2 = o2.get("Etat");

                    if(etat1.equals(etat2))
                    {
                        return o1.get("distanceEnDouble").compareTo(o2.get("distanceEnDouble"));
                    }
                    else
                    {
                        return etat2.compareTo(etat1);
                    }
                }
            });
            ListAdapter adapter = new SimpleAdapter(MainActivity.this, contactList,
                    R.layout.content_list_item, new String[]{"libelle", "adresse",
                    "ville", "Longitude", "Latitude", "distance", "Dispo", "Etat"}, new int[]{R.id.libelle,
                    R.id.adresse, R.id.ville, R.id.Long, R.id.Latitute, R.id.distance, R.id.disponible, R.id.Etat}){
                public View getView(int position, View convertView, ViewGroup parent)
                {
                    View view = super.getView(position , convertView,parent);
                    TextView text1 = (TextView) view.findViewById(R.id.Etat);

                    switch (text1.getText()+"")
                    {
                        case "OUVERT":
                        case "LIBRE":
                            text1.setTextColor(Color.GREEN);
                            break;
                        case "Indisponible":
                        case "Information non disponible":
                            text1.setTextColor(Color.parseColor("#FFA500"));
                            break;
                        case "COMPLET":
                        case "FERME":
                            text1.setTextColor(Color.RED);
                            break;
                        default:
                            text1.setTextColor(Color.BLACK);
                            break;
                    }
                    return view;
                }
            };
            lv.setAdapter(adapter);
        }

    }
}
