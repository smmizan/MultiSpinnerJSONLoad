package com.smmizan.multiplespinnerapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String KEY_STATE = "division";
    private static final String KEY_CITIES = "district";
    Spinner stateSpinner;
    Spinner citiesSpinner;
    private ProgressDialog pDialog;
    private String cities_url = "https://gist.githubusercontent.com/smmizan/1a5a6aa6ed4cf72631be6759d2dd7cfe/raw/526ce24fbc210b3a481173b14c95e5a77cc0c035/bd_division_district_thana.json";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        stateSpinner = findViewById(R.id.stateSpinner);
        citiesSpinner = findViewById(R.id.citiesSpinner);
        displayLoader();
        loadStateCityDetails();

        Button submitButton = findViewById(R.id.buttonSubmit);

        //Display state and city name when submit button is pressed
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                State state = (State) stateSpinner.getSelectedItem();
                String city = citiesSpinner.getSelectedItem().toString();
                Toast.makeText(getApplicationContext(), "Selected Division: " + state.getStateName()
                        + " District: " + city, Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void displayLoader() {
        pDialog = new ProgressDialog(MainActivity.this);
        pDialog.setMessage("Loading Data.. Please wait...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();

    }

    /**
     * Helps in downloading the state and city details
     * and populating the spinner
     */
    private void loadStateCityDetails() {
        final List<State> statesList = new ArrayList<>();
        final List<String> states = new ArrayList<>();
        JsonArrayRequest jsArrayRequest = new JsonArrayRequest
                (Request.Method.GET, cities_url, null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray responseArray) {
                        pDialog.dismiss();
                        try {
                            //Parse the JSON response array by iterating over it
                            for (int i = 0; i < responseArray.length(); i++) {
                                JSONObject response = responseArray.getJSONObject(i);
                                String state = response.getString(KEY_STATE);
                                JSONArray cities = response.getJSONArray(KEY_CITIES);
                                List<String> citiesList = new ArrayList<>();
                                for (int j = 0; j < cities.length(); j++) {
                                    citiesList.add(cities.getString(j));
                                }
                                statesList.add(new State(state, citiesList));
                                states.add(state);

                            }
                            final StateAdapter stateAdapter = new StateAdapter(MainActivity.this,
                                    R.layout.state_list, R.id.spinnerText, statesList);
                            stateSpinner.setAdapter(stateAdapter);

                            stateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                                    //Populate City list to the second spinner when
                                    // a state is chosen from the first spinner
                                    State cityDetails = stateAdapter.getItem(position);
                                    List<String> cityList = cityDetails.getCities();
                                    ArrayAdapter citiesAdapter = new ArrayAdapter<>(MainActivity.this,
                                            R.layout.city_list, R.id.citySpinnerText, cityList);
                                    citiesSpinner.setAdapter(citiesAdapter);
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {

                                }
                            });

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        pDialog.dismiss();

                        //Display error message whenever an error occurs
                        Toast.makeText(getApplicationContext(),
                                error.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

        // Access the RequestQueue through your singleton class.
        MySingleton.getInstance(this).addToRequestQueue(jsArrayRequest);
    }
}
