package ua.edu.zu.zsutimetable;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, DatePickerDialog.OnDateSetListener {

    private String script, n, autocomplete_type, faculty, query;
    private Integer n2, group;
    private String[] faculty_keys;
    private int currFacultyPos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Context context = MainActivity.this;
        script = this.getResources().getString(R.string.cgi_script);
        n = this.getResources().getString(R.string.network);
        n2 = this.getResources().getInteger(R.integer.network_type2);
        autocomplete_type = this.getResources().getString(R.string.autocomplete_type_param);
        group = this.getResources().getInteger(R.integer.autocomplete_group_type);
        faculty = this.getResources().getString(R.string.faculty_param);
        faculty_keys = this.getResources().getStringArray(R.array.faculty_keys);
        query = this.getResources().getString(R.string.autocomplete_query_param);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        String[] values = this.getResources().getStringArray(R.array.faculty_values);
        Spinner facultySpinner = (Spinner) findViewById(R.id.spinner);
        //values
        ArrayAdapter<String> facultySpinnerAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, values);
        facultySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        facultySpinner.setAdapter(facultySpinnerAdapter);
        //listener
        AdapterView.OnItemSelectedListener spinnerListener = new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currFacultyPos = position;
            }

            public void onNothingSelected(AdapterView<?> parent) {
                //meh
            }
        };
        facultySpinner.setOnItemSelectedListener(spinnerListener);

        AutoCompleteTextView groupField = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView);
        groupField.setAdapter(new GroupAutoCompleteAdapter(this, R.layout.list_item));

        EditText sdate = (EditText) findViewById(R.id.sdate);
        EditText edate = (EditText) findViewById(R.id.edate);

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");


        Calendar calendar = Calendar.getInstance();
        Date currDate = calendar.getTime();
        sdate.setText(format.format(currDate));
        edate.setText(format.format(currDate));

        View.OnClickListener dateListener = new View.OnClickListener() {
            @SuppressLint("SimpleDateFormat")
            @Override
            public void onClick(final View v) {
                Calendar now = Calendar.getInstance();
                EditText t = (EditText) v;
                try {
                    now.setTime(new SimpleDateFormat("dd.MM.yyyy").parse(t.getText().toString()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                DatePickerDialog dpd = DatePickerDialog.newInstance(
                        MainActivity.this,
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH)
                );
                dpd.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
                    @SuppressLint("SimpleDateFormat")
                    @Override
                    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
                        Date newDate = null, edateDate = null;
                        monthOfYear++;
                        EditText t = (EditText) v;
                        try {
                            newDate = format.parse("" + dayOfMonth + "." + monthOfYear + "." + year);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        if (t == findViewById(R.id.sdate)) {
                            EditText edate = (EditText) findViewById(R.id.edate);
                            String edateValue = edate.getText().toString();
                            try {
                                edateDate = new SimpleDateFormat("dd.MM.yyyy").parse(edateValue);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            if (newDate != null && edateDate != null && newDate.compareTo(edateDate) > 0) {
                                edate.setText(format.format(newDate));
                            }
                        }

                        if (newDate != null) {
                            t.setText(format.format(newDate));
                        }
                    }
                });
                dpd.show(getFragmentManager(), "Datepickerdialog");
            }
        };

        sdate.setOnClickListener(dateListener);
        edate.setOnClickListener(dateListener);

    }


    public void volleyStringRequest(String url) {

        StringRequest strReq = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Context context = getApplicationContext();
                Toast.makeText(context, response, Toast.LENGTH_SHORT).show();
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Context context = getApplicationContext();
                Toast.makeText(context, error.toString(), Toast.LENGTH_SHORT).show();
            }
        });
        // Adding String request to request queue
        MySingleton.getInstance(getApplicationContext()).addToRequestQueue(strReq);
    }

    public void volleyJsonObjectRequest(String url, final VolleyCallback callback) {

        JsonObjectRequest jsonObjectReq = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            callback.onSuccess(response.getJSONArray("suggestions"));
                            //Toast.makeText(context, s.toString(), Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(),
                                    "Error: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Context context = getApplicationContext();
                Toast.makeText(context, error.toString(), Toast.LENGTH_SHORT).show();
            }
        });

        MySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectReq);
    }

    public void volleyJsonArrayRequest(String url) {

        JsonArrayRequest jsonArrayReq = new JsonArrayRequest(url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Context context = getApplicationContext();
                        Toast.makeText(context, response.toString(), Toast.LENGTH_SHORT).show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Context context = getApplicationContext();
                Toast.makeText(context, error.toString(), Toast.LENGTH_SHORT).show();
            }
        });

        // Adding JsonObject request to request queue
        MySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonArrayReq);
    }

    private ArrayList<String> autocomplete(String input) {
        ArrayList<String> resultList = new ArrayList<>();
        try {
            input = URLEncoder.encode(input, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String urlJSON = script + "?" + n + "=" + n2 + "&" + autocomplete_type + "=" + group + "&"
                + faculty + "=" + faculty_keys[currFacultyPos] + "&" + query + "=" + input;

        RequestQueue queue = Volley.newRequestQueue(this);

        RequestFuture<JSONObject> future = RequestFuture.newFuture();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, urlJSON, null, future, future);
        queue.add(jsonObjectRequest);

        try {
            JSONObject response = future.get();
            JSONArray r = response.getJSONArray("suggestions");
            if (r != null) {
                for (int i = 0; i < r.length(); i++) {
                    resultList.add(r.getString(i));
                }
            }
        } catch (InterruptedException | JSONException | ExecutionException e) {
            e.printStackTrace();
        }

        return resultList;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        Toast.makeText(getApplicationContext(), "wtf", Toast.LENGTH_SHORT).show();
    }

    public interface VolleyCallback {
        void onSuccess(JSONArray result);
    }

    private class GroupAutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {
        private ArrayList<String> resultList;

        public GroupAutoCompleteAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        @Override
        public int getCount() {
            return resultList.size();
        }

        @Override
        public String getItem(int index) {
            return resultList.get(index);
        }

        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    if (constraint != null) {
                        // Retrieve the autocomplete results.
                        resultList = autocomplete(constraint.toString());

                        // Assign the data to the FilterResults
                        filterResults.values = resultList;
                        filterResults.count = resultList.size();
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if (results != null && results.count > 0) {
                        notifyDataSetChanged();
                    } else {
                        notifyDataSetInvalidated();
                    }
                }
            };
        }
    }
}
