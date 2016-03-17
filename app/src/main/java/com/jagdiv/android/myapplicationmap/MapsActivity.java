package com.jagdiv.android.myapplicationmap;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

public class MapsActivity extends Activity {// FragmentActivity // implements OnMapReadyCallback {

    private GoogleMap mMap;
    ArrayList<InfoPoint> resultspoint = new ArrayList<InfoPoint>();
    double lng = 0.0;
    double lat = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        //  SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapp);
        mMap = mapFragment.getMap();
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.setTrafficEnabled(true);
        mMap.setIndoorEnabled(true);
        mMap.setBuildingsEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
       // mapFragment.getMapAsync(this);
        LatLng addresslatlng = new LatLng(25, 74);
        mMap.addMarker(new MarkerOptions().position(addresslatlng).title("Marker in guess"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(addresslatlng));
       // CameraUpdate update = CameraUpdateFactory.newLatLngZoom(addresslatlng, 15);
       // mMap.moveCamera(update);

        System.out.println("22222222222222nd type");
        //2nd we can get by url
        String address = "mumbai";
        getLatLngfromJson(address);
    }

    private class DataLongOperationAsynchTask extends AsyncTask<String, Void, String[]> {
        ProgressDialog dialog = new ProgressDialog(MapsActivity.this);
        String addr="";
        private final GoogleMap mMap;

// ---------------------------------------------------------

        public DataLongOperationAsynchTask( GoogleMap gmap)
        {

            this.mMap = gmap;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.setMessage("Please wait...");
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }

        @Override
        protected String[] doInBackground(String... params) {
            String response;
            try {
                // "http://maps.googleapis.com/maps/api/geocode/json?address={address}&sensor=false";
                 addr = params[0].toString();
                response = getRespByURL("http://maps.googleapis.com/maps/api/geocode/json?address=" + addr + "&sensor=false");
                System.out.println("response" + response);
                return new String[]{response};
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("here in getting response error");
                return new String[]{"error"};
            }
        }

        @Override
        protected void onPostExecute(String... result) {
            // GoogleMap mMap=GoogleMap;
            /*try {
                JSONObject jsonObject = new JSONObject(result[0]);

                 lng = ((JSONArray)jsonObject.get("results")).getJSONObject(0)
                        .getJSONObject("geometry").getJSONObject("location")
                        .getDouble("lng");

                 lat = ((JSONArray)jsonObject.get("results")).getJSONObject(0)
                        .getJSONObject("geometry").getJSONObject("location")
                        .getDouble("lat");

                Log.d("latitude", "" + lat);
                Log.d("longitude", "" + lng);
            } catch (JSONException e) {
                e.printStackTrace();
            }*/
            //ArrayList<InfoPoint> resultspoint=new ArrayList<InfoPoint>();

            resultspoint = parsePoints(result[0]);
            getLatlngInfoPoint(resultspoint);
            InfoPoint point = resultspoint.get(0);
            LatLng addresslatlng = new LatLng(point.getDblLatitude(), point.getDblLongitude());
            LatLng addresslatlng1 = new LatLng(28, 79   );
            mMap.addMarker(new MarkerOptions().position(addresslatlng).title("Marker in" + addr+point.getDblLatitude()+","+point.getDblLongitude()));
            mMap.addMarker(new MarkerOptions().position(addresslatlng1).title("Marker in" + addresslatlng1.describeContents()+addresslatlng1.latitude+","+addresslatlng1.longitude));

            mMap.moveCamera(CameraUpdateFactory.newLatLng(addresslatlng));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(addresslatlng1));


          //  CameraUpdate update = CameraUpdateFactory.newLatLngZoom(addresslatlng, 1);
            //mMap.moveCamera(update);
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }

    private void getLatlngInfoPoint(ArrayList<InfoPoint> results) {
        InfoPoint point = new InfoPoint();
        point = results.get(0);
       lng= point.getDblLongitude();
        lat=point.getDblLatitude();
        System.out.println("point.getDblLongitude()" + point.getDblLongitude() + " point.getDblLatitude() " + point.getDblLatitude());
    }

    public String getRespByURL(String requestURL) {
        URL url;
        String response = "";
        try {
            url = new URL(requestURL);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");
            conn.setDoOutput(true);
            int responseCode = conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = br.readLine()) != null) {
                    response += line;
                }
            } else {
                response = "";
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    private ArrayList<InfoPoint> parsePoints(String strResponse) {
        // TODO Auto-generated method stub
        ArrayList<InfoPoint> result = new ArrayList<InfoPoint>();
        try {
            JSONObject obj = new JSONObject(strResponse);
            JSONArray array = obj.getJSONArray("results");
            for (int i = 0; i < array.length(); i++) {
                InfoPoint point = new InfoPoint();

                JSONObject item = array.getJSONObject(i);
                ArrayList<HashMap<String, Object>> tblPoints = new ArrayList<HashMap<String, Object>>();
                JSONArray jsonTblPoints = item.getJSONArray("address_components");
                for (int j = 0; j < jsonTblPoints.length(); j++) {
                    JSONObject jsonTblPoint = jsonTblPoints.getJSONObject(j);
                    HashMap<String, Object> tblPoint = new HashMap<String, Object>();
                    Iterator<String> keys = jsonTblPoint.keys();
                    while (keys.hasNext()) {
                        String key = (String) keys.next();
                        if (tblPoint.get(key) instanceof JSONArray) {
                            tblPoint.put(key, jsonTblPoint.getJSONArray(key));
                        }
                        tblPoint.put(key, jsonTblPoint.getString(key));
                    }
                    tblPoints.add(tblPoint);
                }
                point.setAddressFields(tblPoints);
                point.setStrFormattedAddress(item.getString("formatted_address"));
                JSONObject geoJson = item.getJSONObject("geometry");
                JSONObject locJson = geoJson.getJSONObject("location");
                point.setDblLatitude(Double.parseDouble(locJson.getString("lat")));
                point.setDblLongitude(Double.parseDouble(locJson.getString("lng")));

                result.add(point);
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return result;
    }

    public class InfoPoint {
        ArrayList<HashMap<String, Object>> addressFields = new ArrayList<HashMap<String, Object>>();
        String strFormattedAddress = "";
        double dblLatitude = 0;
        double dblLongitude = 0;

        public ArrayList<HashMap<String, Object>> getAddressFields() {
            return addressFields;
        }

        public void setAddressFields(ArrayList<HashMap<String, Object>> addressFields) {
            this.addressFields = addressFields;
        }

        public String getStrFormattedAddress() {
            return strFormattedAddress;
        }

        public void setStrFormattedAddress(String strFormattedAddress) {
            this.strFormattedAddress = strFormattedAddress;
        }

        public double getDblLatitude() {
            return dblLatitude;
        }

        public void setDblLatitude(double dblLatitude) {
            this.dblLatitude = dblLatitude;
        }

        public double getDblLongitude() {
            return dblLongitude;
        }

        public void setDblLongitude(double dblLongitude) {
            this.dblLongitude = dblLongitude;
        }

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
   // @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        String address = "mumbai";
        getLatLngfromJson(address);
     //this will not work because calls are only handled in async task we cant get lat long from async task to this main thread
    /*    if (resultspoint != null && resultspoint.size() > 0) {
            InfoPoint point = resultspoint.get(0);
            System.out.println("point.getDblLongitude()" + point.getDblLongitude() + " point.getDblLatitude() " + point.getDblLatitude());
            // Add a marker in Sydney and move the camera
            LatLng addresslatlng = new LatLng(point.getDblLatitude(), point.getDblLongitude());
            mMap.addMarker(new MarkerOptions().position(addresslatlng).title("Marker in" + address));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(addresslatlng));
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(addresslatlng, 15);
            mMap.moveCamera(update);
        } else
            System.out.println("problem in getting address");*/
       System.out.println("lat " + lat + " lng " + lng);

  /*      LatLng addresslatlng = new LatLng(lat, lng);
        mMap.addMarker(new MarkerOptions().position(addresslatlng).title("Marker in" + address+" "+lat+","+lng));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(addresslatlng));
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(addresslatlng, 15);
        mMap.moveCamera(update);
      try {
            pinaddress();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("errorrrrrrrrrrrrrrrrrrrr");
        }*/
        // displayCurrentLocation();


    }

    private void getLatLngfromJson(String address) {
       (new DataLongOperationAsynchTask(mMap)).execute(new String[]{address});
    }

    private void gotoLocation(double lat, double lon, float zoom) {
        LatLng l1 = new LatLng(lat, lon);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(l1, zoom);
        mMap.moveCamera(update);
    }

    private void pinaddress() throws IOException {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> list;

        list = geocoder.getFromLocationName("2 howard street canterbury nsw 2193", 1);
        System.out.println("list" + list + "list.size" + list.size());
        if (list == null)
            System.out.println("list is null");
        else {
            System.out.println("list is not null address");

        }

        if (list.size() > 0) {

            System.out.println(list.get(0));
            Address address = list.get(0);
            String locality = address.getLocality();
            System.out.println("locality" + locality);
            Toast.makeText(this, locality, Toast.LENGTH_LONG).show();
            double lat = address.getLatitude();
            double lon = address.getLongitude();
            gotoLocation(lat, lon, 15);
        } else
            Toast.makeText(this, "checkout connection", Toast.LENGTH_LONG).show();

    }

    public void displayCurrentLocation() {
        // Get the current location's latitude & longitude
        // Location currentLocation = mLocationClient.getLastLocation();
        // toConnect();
      /*  if (mLastLocation != null) {
            Location currentLocation = mLastLocation;
            String msg = "Current Location: " +
                    Double.toString(currentLocation.getLatitude()) + "," +
                    Double.toString(currentLocation.getLongitude());

            // Display the current location in the UI
            locationLabel.setText(msg);

            // To display the current address in the UI
            (new GetAddressTask(this)).execute(currentLocation);
        }else
            locationLabel.setText("Need to check location");*/
        // Location currentLocation=new Location()
        (new GetAddressTask(this)).execute(new String[]{"latlong"});
    }

    /*
   * Following is a subclass of AsyncTask which has been used to get
   * address corresponding to the given latitude & longitude.
   */
    private class GetAddressTask extends AsyncTask<String, String, String> {
        Context mContext;
        List<Address> addresses = null;

        public GetAddressTask(Context context) {
            super();
            mContext = context;
        }

        /*
        * When the task finishes, onPostExecute() displays the address.
        */
        @Override
        protected void onPostExecute(String address) {
            // Display the current address in the UI
            //addressLabel.setText(address);
            String address1 = getAddress();
            System.out.println("1111111111111111 address1" + address1);
        }

        @Override
        protected void onProgressUpdate(String... text) {
            System.out.println("in progressupdate" + text[0] + "address" + getAddress());
        }

        @Override
        protected String doInBackground(String... params) {
            Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());

            // Get the current location from the input parameter list
            //Location loc = params[0];

            // Create a list to contain the result address

            try {
                //  System.out.println("location loc.getLatitude()" + loc.getLatitude() + " loc.getLongitude() " + loc.getLongitude() + "geocoder.isPresent() " + geocoder.isPresent());

                //  addresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
                addresses = geocoder.getFromLocationName("2 howad street canterbury nsw 2193", 1);
                try {
                    Thread.sleep(10 * 1000);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                publishProgress("working on it");
            } catch (IOException e1) {
                Log.e("LocationSampleActivity", "IOException in getFromLocation()");
                e1.printStackTrace();
                return ("IO Exception trying to get address");
            } catch (IllegalArgumentException e2) {
                // Error message to post in the log
                String errorString = "Illegal arguments ";
                //  + Double.toString(loc.getLatitude()) + " , " + Double.toString(loc.getLongitude()) + " passed to address service";
                Log.e("LocationSampleActivity", errorString);
                e2.printStackTrace();
                return errorString;
            }
            // If the reverse geocode returned an address
            return getAddress();
        }

        private String getAddress() {
            if (addresses != null && addresses.size() > 0) {
                // Get the first address
                Address address = addresses.get(0);

            /*
            * Format the first line of address (if available),
            * city, and country name.
            */
                String addressText = String.format("%s, %s, %s",

                        // If there's a street address, add it
                        address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",

                        // Locality is usually a city
                        address.getLocality(),

                        // The country of the address
                        address.getCountryName());

                // Return the text
                return addressText;
            } else {
                return "No address found";
            }
        }
    }
}
