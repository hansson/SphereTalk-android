package net.spheretalk.android;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import net.spheretalk.android.util.Constants;
import net.spheretalk.android.util.HiddenConstants;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChatActivity extends GCMActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v13.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;
    Location mLocation;
    DialogFragment mloginDialog;
    List<String> mUsers;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mloginDialog = new LoginDialogFragment();
        mloginDialog.show(getFragmentManager(), "Login Dialog");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return ChatFragment.newInstance();
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
            }
            return null;
        }
    }

    public static class ChatFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static ChatFragment newInstance() {
            ChatFragment fragment = new ChatFragment();
            return fragment;
        }

        public ChatFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle saveInstanceState) {
            View rootView = inflater.inflate(R.layout.main_chat, container, false);
            return rootView;
        }
    }




//    /**
//     * A placeholder fragment containing a simple view.
//     */
//    public static class PlaceholderFragment extends Fragment {
//        /**
//         * The fragment argument representing the section number for this
//         * fragment.
//         */
//        private static final String ARG_SECTION_NUMBER = "section_number";
//
//        /**
//         * Returns a new instance of this fragment for the given section
//         * number.
//         */
//        public static PlaceholderFragment newInstance(int sectionNumber) {
//            PlaceholderFragment fragment = new PlaceholderFragment();
//            Bundle args = new Bundle();
//            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
//            fragment.setArguments(args);
//            return fragment;
//        }
//
//        public PlaceholderFragment() {
//        }
//
//        @Override
//        public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                Bundle savedInstanceState) {
//            View rootView = inflater.inflate(R.layout.fragment_chat, container, false);
//            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
//            textView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));
//            return rootView;
//        }
//    }

    public class LoginDialogFragment extends DialogFragment {

        private TextView locationStatus;

        public LoginDialogFragment() {
            //This is required in a DialogFragment
        }
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getActivity().getLayoutInflater();
            setFinishOnTouchOutside(false);
            locationStatus = (TextView)findViewById(R.id.loginStatusMessage);
            if(mLocation != null) {
                locationStatus.setText(getString(R.string.location_found));
            }

            alertDialogBuilder.setView(inflater.inflate(R.layout.login_chat, null));
            alertDialogBuilder.setCancelable(false);

            alertDialogBuilder.setPositiveButton(getString(R.string.ok), null);

            Dialog loginDlg = alertDialogBuilder.create();
            loginDlg.setCanceledOnTouchOutside(false);

            new LocationFetcher(locationStatus).execute();

            return loginDlg;
        }

        @Override
        public void onResume()
        {
            super.onResume();
            AlertDialog alertDialog = (AlertDialog) getDialog();
            Button okButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            okButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    performOkButtonAction();
                }
            });
        }

        private void performOkButtonAction() {
            Log.d(Constants.LOG_TAG, "Checking if alert should be closed");
            EditText inputUsername = (EditText) getDialog().findViewById(R.id.username);
            String username = inputUsername.getText().toString();

            Log.d(Constants.LOG_TAG, "Length of username: " + username.length());

            if(mLocation != null && username.length() >= 5) {
                //TODO: Call the async login so that we know
                SharedPreferences settings = getSharedPreferences(Constants.PREF_TAG, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(Constants.PREF_USERNAME, username);
                editor.commit();
                new SendPosition().execute();
            }
        }

        public void updateLocationText(String text) {
            locationStatus.append(text);
        }
    }

    public class LocationFetcher extends AsyncTask<Void, Void, Location> implements LocationListener {

        //Set the max age of a location 2*60*1000 = 2min
        final long TOO_OLD = 2*60*1000;
        private Location location;
        private LocationManager lm;
        private TextView locationStatus;

        LocationFetcher(TextView tv) {
            locationStatus = tv;
        }

        protected void onPreExecute()
        {
            lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            //Specify what we are going to use to get the position Network/GPS
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, this);
        }

        protected Location doInBackground(Void... params)
        {
            Log.d(Constants.LOG_TAG, "Fetching last known location");
            Location lastLocation = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            if (System.currentTimeMillis() - lastLocation.getTime() > TOO_OLD)
            {
                Log.d(Constants.LOG_TAG, "Last location too old, requesting a new one");
                while (location == null)
                    try { Thread.sleep(100); } catch (Exception ex) {}

                Log.d(Constants.LOG_TAG, "Returning new location");
                return location;
            }
            Log.d(Constants.LOG_TAG, "Using last known location");
            return lastLocation;
        }

        protected void onPostExecute(Location location)
        {
            lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            lm.removeUpdates(this);

            //This is where we let the UI know we are done
            Log.d(Constants.LOG_TAG, "Changing the text in the dialog letting the user know we've found a location");
            Log.d(Constants.LOG_TAG, "Longitude: " + location.getLongitude());
            Log.d(Constants.LOG_TAG, "Longitude: " + location.getLatitude());

            if(location != null) {
                TextView locationStatus = (TextView) mloginDialog.getDialog().findViewById(R.id.loginStatusMessage);
                locationStatus.setText(getString(R.string.location_found));
                mLocation = location;
            }
        }

        @Override
        public void onLocationChanged(Location newLocation)
        {
            location = newLocation;
        }

        public void onProviderDisabled(String provider) {}
        public void onProviderEnabled(String provider) {}
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    }

    private class SendPosition extends AsyncTask<Void, Void, String> {

        //TODO: Get the real url here
        private String URL_SET_POSITION = HiddenConstants.URL + HiddenConstants.LOGIN_PATH;

        protected String doInBackground(Void... voids) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(URL_SET_POSITION);
            HttpResponse response;
            //Returns 1 if it works and 0 if not

            try {

                SharedPreferences settings = getSharedPreferences(Constants.PREF_TAG, 0);
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                nameValuePairs.add(new BasicNameValuePair("gcmKey", settings.getString(Constants.PREF_REGID,"")));
                nameValuePairs.add(new BasicNameValuePair("longitude", String.valueOf(mLocation.getLongitude())));
                nameValuePairs.add(new BasicNameValuePair("latitude", String.valueOf(mLocation.getLatitude())));
                nameValuePairs.add(new BasicNameValuePair("username", settings.getString(Constants.PREF_USERNAME,"")));
                Log.d(Constants.LOG_TAG, "Sending login to server:");
                Log.d(Constants.LOG_TAG, "gcmKey:" + settings.getString(Constants.PREF_REGID,""));
                Log.d(Constants.LOG_TAG, "longitude:" + String.valueOf(mLocation.getLongitude()));
                Log.d(Constants.LOG_TAG, "latitude:" + String.valueOf(mLocation.getLatitude()));
                Log.d(Constants.LOG_TAG, "username:" + settings.getString(Constants.PREF_USERNAME,""));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                response = httpclient.execute(httppost);
                StatusLine statusLine = response.getStatusLine();
                if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                    JSONObject json = new JSONObject(EntityUtils.toString(response.getEntity()));
                    String status = json.getString(Constants.WEBS_STATUS);
                    JSONArray users = json.getJSONArray(Constants.WEBS_USERS);

                    if(status.equals(Constants.WEBS_STATUS_OK)) {
                        //TODO: Where should the users be saved? Stored in an arraylist for now
                        mUsers = new ArrayList<String>();

                        for(int i=0; i < users.length(); i++) {
                            JSONObject user = users.getJSONObject(i);
                            mUsers.add(user.getString(Constants.WEBS_USERNAME));
                        }
                        Log.d(Constants.LOG_TAG, "Nr of users: " + mUsers.size());
                    }
                    return status;

                    //Maybe we should output some error here in case it doesn't work. Either an error from the server if that's where it breaks or generate our own error if we can't reach the server
                } else{
                    response.getEntity().getContent().close();
                    throw new IOException(statusLine.getReasonPhrase());
                }
            } catch (ClientProtocolException e) {
                //TODO Handle problems..
            } catch (IOException e) {
                //TODO Handle problems..
            } catch (JSONException e) {
                //TODO Handle problems...
            }
            return null;
        }

        protected void onPostExecute(String status) {
            Log.d(Constants.LOG_TAG, "Login results: " + status);
            if(status.equals(Constants.WEBS_STATUS_OK)) {
                mloginDialog.getDialog().dismiss();
            }
            else if(status.equals(Constants.WEBS_STATUS_NOK)) {
                //TODO: Let the user know something broke
                TextView locationStatus = (TextView) mloginDialog.getDialog().findViewById(R.id.loginStatusMessage);
                locationStatus.setText(getString(R.string.login_failed));
            } else if(status.equals(Constants.WEBS_STATUS_USER_TAKEN)) {
                //TODO: Let the user know the username is taken
                TextView locationStatus = (TextView) mloginDialog.getDialog().findViewById(R.id.loginStatusMessage);
                locationStatus.setText(getString(R.string.username_taken));
            } else if(status == null) {
                //Somehing broke
                TextView locationStatus = (TextView) mloginDialog.getDialog().findViewById(R.id.loginStatusMessage);
                locationStatus.setText(getString(R.string.login_failed));
            }
        }
    }

    /**
     * USAGE: String[0] = Recipient, String[1] = Message
     *
     */

    private class SendMessage extends AsyncTask<String, Void, Integer> {

        //TODO: Get the real url here
        private String URL_SET_POSITION = HiddenConstants.URL + HiddenConstants.MESSAGE_PATH;

        protected Integer doInBackground(String... strings) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(URL_SET_POSITION);
            HttpResponse response;
            //Returns 1 if it works and 0 if not
            Integer result = 0;

            try {
                SharedPreferences settings = getSharedPreferences(Constants.PREF_TAG, 0);
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                nameValuePairs.add(new BasicNameValuePair("gcmKey", settings.getString(Constants.PREF_REGID,"")));
                nameValuePairs.add(new BasicNameValuePair("recipient", strings[0] ));
                nameValuePairs.add(new BasicNameValuePair("message", strings[1]));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                response = httpclient.execute(httppost);
                StatusLine statusLine = response.getStatusLine();
                if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    out.close();
                    result = 1;
                    //Maybe we should output some error here in case it doesn't work. Either an error from the server if that's where it breaks or generate our own error if we can't reach the server
                } else{
                    //Somehing broke
                    response.getEntity().getContent().close();
                    throw new IOException(statusLine.getReasonPhrase());
                }
            } catch (ClientProtocolException e) {
                //TODO Handle problems..
            } catch (IOException e) {
                //TODO Handle problems..
            }
            return result;
        }

        protected void onPostExecute(Integer result) {
            //We should probably do something here. Maybe we lock the UI for the user until atleast a first position is set. Then we unlock it here
        }
    }

}
