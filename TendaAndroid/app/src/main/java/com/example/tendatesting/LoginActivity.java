package com.example.tendatesting;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private Button button;
    //When screen is loaded show the designed layout
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    //Function will be called when the button is clicked.
    public void login(View view) throws IOException {
        //Get email and password from user text box entries.
        final EditText email = (EditText)findViewById(R.id.email);
        EditText password = (EditText)findViewById(R.id.password);
        final String emailString = email.getText().toString();
        final String passwordString = password.getText().toString();

        //Format what is needed for request: place to go if verified, a request queue to send a request to the server, and url for server.
        final Intent homeIntent = new Intent(this, HomeActivity.class);
        RequestQueue queue = Volley.newRequestQueue(this);
        String serverURL = "http://ec2-18-218-174-216.us-east-2.compute.amazonaws.com";
        String url = serverURL + "/login";

        //Create request
        final StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            //When the request is recieved:
            @Override
            public void onResponse(String response) {
                try {
                    //Convert response to a json and check if the response what 200 (which means password is valid)
                    JSONObject jsonObj = new JSONObject(response.toString());
                    JSONObject newjsonObj = new JSONObject(jsonObj.getString("result"));
                    String user_id = newjsonObj.getString("data");
                    String result = newjsonObj.getString("status");
                    Log.d("PasswordLog", user_id);
                    Log.d("PasswordLog", result);
                    boolean containsVal = result.toLowerCase().contains("200");
                    String contains = Boolean.toString(containsVal);
                    if (containsVal == true) {
                        homeIntent.putExtra("user_id", user_id);
                        startActivity(homeIntent);
                    } else {
                        //implement message to user for re-entering their password
                        Log.d("PasswordLog", "Incorrect Password.");
                        Context context = getApplicationContext();
                        CharSequence text = "Incorrect Password";
                        int duration = Toast.LENGTH_SHORT;
                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("PasswordLog", "Incorrect Password.");
                    Context context = getApplicationContext();
                    CharSequence text = "Incorrect Information";
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("PasswordLog", "Error with request response.");
                Context context = getApplicationContext();
                CharSequence text = "Login Failed";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
        }) {
            protected Map<String, String> getParams() {
                //Format data that will make up the body of the post request (email and password)
                Map<String, String> MyData = new HashMap<String, String>();
                MyData.put("password", passwordString);
                MyData.put("email", emailString);
                return MyData;
            }
        };
        // Add the request to the RequestQueue.
        queue.add(stringRequest);

    }

    //Go to Create Account page when the create button is clicked
    public void ClickToSignUpPage(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }
}
