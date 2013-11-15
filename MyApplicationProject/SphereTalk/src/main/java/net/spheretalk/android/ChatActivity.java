package net.spheretalk.android;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChatActivity extends Activity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v13.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

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
            return PlaceholderFragment.newInstance(position + 1);
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




    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_chat, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

    public class LocationFetcher extends AsyncTask<Void, Void, Location> implements LocationListener {

        //Set the max age of a location 2*60*1000 = 2min
        final long TOO_OLD = 2*60*1000;
        private Location location;
        private LocationManager lm;

        protected void onPreExecute()
        {
            lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            //Specify what we are going to use to get the position Network/GPS
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, this);
        }

        protected Location doInBackground(Void... params)
        {
            // Try to use the last known position
            Location lastLocation = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            // If it's too old, get a new one by location manager
            if (System.currentTimeMillis() - lastLocation.getTime() > TOO_OLD)
            {
                while (location == null)
                    try { Thread.sleep(100); } catch (Exception ex) {}

                return location;
            }

            return lastLocation;
        }

        protected void onPostExecute(Location location)
        {
            lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            lm.removeUpdates(this);
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

    private class SendPosition extends AsyncTask<Location, Void, Integer> {

        //TODO: Get the real url here
        private String URL_SET_POSITION = "http://some_url.com";

        protected Integer doInBackground(Location... locations) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(URL_SET_POSITION);
            HttpResponse response;
            //Returns 1 if it works and 0 if not
            Integer result = 0;

            try {

                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                //TODO: Check with tobias if this is the correct id
                nameValuePairs.add(new BasicNameValuePair("device", Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID)));
                nameValuePairs.add(new BasicNameValuePair("longitude", String.valueOf(locations[0].getLongitude())));
                nameValuePairs.add(new BasicNameValuePair("latitude", String.valueOf(locations[0].getLatitude())));
                //TODO: This needs to come from somehere
                nameValuePairs.add(new BasicNameValuePair("username", "Stoffe"));
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

    /**
     * USAGE: String[0] = Recipient, String[1] = Message
     *
     */

    private class SendMessage extends AsyncTask<String, Void, Integer> {

        //TODO: Get the real url here
        private String URL_SET_POSITION = "http://some_url.com";

        protected Integer doInBackground(String... strings) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(URL_SET_POSITION);
            HttpResponse response;
            //Returns 1 if it works and 0 if not
            Integer result = 0;

            try {

                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                //TODO: Check with tobias if this is the correct id
                nameValuePairs.add(new BasicNameValuePair("device", Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID)));
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
