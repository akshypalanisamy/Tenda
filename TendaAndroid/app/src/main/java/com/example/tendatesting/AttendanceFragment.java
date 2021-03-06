package com.example.tendatesting;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class AttendanceFragment extends Fragment implements AttendeeAdapter.OnNoteListener {
    private Button mButton;
    private JSONObject eventString;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private ArrayList<Attendee> attendanceArrayList;
    private JSONArray attendeeArray;
    String fragementIdentifier = "AttendeeLog";
    Timer timer;
    String startTimeTest, dateTest, timeTest;
    TextView numOfUsers;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){

        final View v = inflater.inflate(R.layout.fragment_attendance, container, false);
//        View load =  ((AppCompatActivity)getActivity()).findViewById(R.id.loadingPanel);
//        load.setVisibility(View.GONE);
        mButton = (Button) v.findViewById(R.id.sendAlert);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater layoutInflaterAndroid = LayoutInflater.from(v.getContext());
                View mView = layoutInflaterAndroid.inflate(R.layout.send_alert_dialog, null);
                AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(v.getContext(), R.style.sendAlertDialog);
                alertDialogBuilderUserInput.setView(mView);

                final EditText userInputDialogEditText = (EditText) mView.findViewById(R.id.userInputDialog);
                alertDialogBuilderUserInput
                        .setCancelable(false)
                        .setPositiveButton("Send", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {
                                Context context = v.getContext();
                                CharSequence text = "Alert Sent";
                                int duration = Toast.LENGTH_SHORT;

                                Toast toast = Toast.makeText(context, text, duration);
                                toast.setGravity(Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL,0,300);
                                toast.show();
                            }
                        })

                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialogBox, int id) {
                                        dialogBox.cancel();
                                    }
                                });

                AlertDialog alertDialogAndroid = alertDialogBuilderUserInput.create();
                alertDialogAndroid.show();
            }
        });

        Bundle extras = getActivity().getIntent().getExtras();
        String eventID = extras.getString("event_id");
        String user_id = extras.getString("user_id");
        Log.d("UserID_for_session", "event_id: " + eventID + ", user_id: " + user_id);

        //Format what is needed for request: place to go if verified, a request queue to send a request to the server, and url for server.
        RequestQueue queueDet = Volley.newRequestQueue(v.getContext());
        String serverURL = "http://ec2-18-218-174-216.us-east-2.compute.amazonaws.com";
        String urlDet =  serverURL + "/event/" + eventID + "/";
        //Create request
        final StringRequest stringRequestDet = new StringRequest(Request.Method.GET, urlDet, new Response.Listener<String>() {
            //When the request is recieved:
            @Override
            public void onResponse(String response) {
                try {
                    //Convert response to a json
                    JSONObject jsonObj = new JSONObject(response.toString());
                    String result = jsonObj.getString("result");
                    jsonObj = new JSONObject(result);

                    Log.d(fragementIdentifier, result);

                    String name = jsonObj.getString("name");
                    String description = jsonObj.getString("description");
                    String time = jsonObj.getString("time");
                    String date = jsonObj.getString("date");
                    String duration = jsonObj.getString("duration");

                    /////TIMER/////////
                    String startTimeTest = date + " "  + time;
                    String endTimeTest = date + " " + duration;

                    Log.d("startTimeLog", startTimeTest);
                    Timestamp startTime = java.sql.Timestamp.valueOf(startTimeTest);
                    Timestamp endTime = java.sql.Timestamp.valueOf(endTimeTest);

                    Date startdate = new Date(startTime.getTime());

                    timer = new Timer();
                    TimerTask task = new getAttendeeList();
                    Timestamp curTime = new Timestamp(System.currentTimeMillis());
                    if((curTime.getTime()<=endTime.getTime())&&(curTime.getTime()>=startTime.getTime())){
                        timer.schedule(task,startdate,30000);
                        Log.d("Timer :","started");
                        Log.d("END TIME :",String.valueOf(curTime.toString()));
                        Log.d("END TIME :",String.valueOf(endTime.toString()));
                        Log.d("START TIME :",String.valueOf(endTime.toString()));


                    }

                    //Getting the current time and then checking if the time has passed the end time
                    //if so then the task is cancelled

                    if(curTime.getTime()>=endTime.getTime()){
                        Log.d("Timer :","CANCELLED");
                        Log.d("CUR TIME :",String.valueOf(curTime.toString()));
                        Log.d("END TIME :",String.valueOf(endTime.toString()));
                        Log.d("START TIME :",String.valueOf(endTime.toString()));
                        timer.cancel();
                        timer.purge();
                    }
                    ///////TIMER END//////

                    //////FORMAT START TIME////
                    SimpleDateFormat twentyFourTimeFormat = new SimpleDateFormat("HH:mm");
                    SimpleDateFormat twelveHourFormat = new SimpleDateFormat("hh:mm a");
                    Date twentyFourHourDate = null;
                    try {
                        twentyFourHourDate = twentyFourTimeFormat.parse(time);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    String eventTimeAMPM = twelveHourFormat.format(twentyFourHourDate);
                    ///////FORMAT START TIME/////

                    //////FORMAT END TIME////
                    SimpleDateFormat twentyFourTimeFormatE = new SimpleDateFormat("HH:mm");
                    SimpleDateFormat twelveHourFormatE = new SimpleDateFormat("hh:mm a");
                    Date twentyFourHourDateE = null;
                    try {
                        twentyFourHourDateE = twentyFourTimeFormatE.parse(duration);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    String eventETimeAMPM = twelveHourFormatE.format(twentyFourHourDateE);
                    ///////FORMAT END TIME/////

                    //FORMAT DATE///
                    LocalDate eventDateF = LocalDate.parse(date);
                    String dateFormatted = eventDateF.format(DateTimeFormatter.ofPattern( "MMM d yyyy"));
                    //FORMAT DATE//



                    //startTimeTest = date + " " + time;

                    TextView eventTitle = v.findViewById(R.id.page_textTitle);
                    TextView eventDescription = v.findViewById(R.id.page_textDescription);
                    TextView eventTime = v.findViewById(R.id.page_textTime);
                    TextView eventDate = v.findViewById(R.id.page_textDate);
                    //TextView eventDuration = v.findViewById(R.id.page_textDuration);

                    eventTitle.setText(name);
                    eventDescription.setText(description);
                    eventTime.setText(eventTimeAMPM + " - " + eventETimeAMPM);
                    eventDate.setText(dateFormatted);
                   // eventDuration.setText(duration);

                } catch (JSONException e) {
                    e.printStackTrace();

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(fragementIdentifier, "Error with request response.");
                Context context = getContext();
                CharSequence text = "Couldn't Get Event Details";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
        });

        // Add the request to the RequestQueue.
        queueDet.add(stringRequestDet);

        try {
            TimeUnit.SECONDS.sleep(4);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Format what is needed for request: place to go if verified, a request queue to send a request to the server, and url for server.
        RequestQueue queue = Volley.newRequestQueue(v.getContext());
        String url = "http://ec2-18-218-174-216.us-east-2.compute.amazonaws.com/attendenceData/"+eventID+"/";
        //Create request
        final StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            //When the request is recieved:
            @Override
            public void onResponse(String response) {
                try {
                    //Convert response to a json
                    JSONObject jsonObject = new JSONObject(response.toString());
                    String result = jsonObject.getString("result");
                    attendeeArray = new JSONArray(result);
                    Log.d("Result",result);
                    createListData(attendeeArray);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(fragementIdentifier, "Error with request response.");
                Context context = getContext();
                CharSequence text = "Couldn't Get Attendee Data";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);


//        TextView eventTime = v.findViewById(R.id.page_textTime);
//        TextView eventDate = v.findViewById(R.id.page_textDate);
//
//        dateTest = eventDate.getText().toString();
//        timeTest = eventTime.getText().toString();

        startTimeTest = dateTest+" "+timeTest;





        recyclerView = v.findViewById(R.id.recyclerViewAttend);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        attendanceArrayList = new ArrayList<>();
        adapter = new AttendeeAdapter(attendanceArrayList,this);
        recyclerView.setAdapter(adapter);






        return v;
    }
    @Override
    public void onStart(){
        super.onStart();
        View load =  getActivity().findViewById(R.id.loadingPanelA);
        load.setVisibility(View.GONE);


    }

    @Override
    public void onStop() {
        super.onStop();
        if(timer != null){
            timer.cancel();
            timer.purge();
            //cancel timer task and assign null
        }
    }

    class getAttendeeList extends TimerTask {
        Bundle extras = getActivity().getIntent().getExtras();
        String eventID = extras.getString("event_id");

        @Override
        public void run() {
            //Format what is needed for request: place to go if verified, a request queue to send a request to the server, and url for server.
            RequestQueue queue = Volley.newRequestQueue(getActivity());
            String url ="http://ec2-18-218-174-216.us-east-2.compute.amazonaws.com/attendenceData/"+eventID+"/";
            //Create request

            final StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                //When the request is recieved:
                @Override
                public void onResponse(String response) {
                    try {
                        //Convert response to a json
                        JSONObject jsonObject = new JSONObject(response.toString());
                        String result = jsonObject.getString("result");
                        attendeeArray = new JSONArray(result);
                        Log.d("Result",result);
                        createListData(attendeeArray);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d(fragementIdentifier, "Error with request response.");
                    Context context = getContext();
                    CharSequence text = "Couldn't Get Attendee Data";
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
            });
            // Add the request to the RequestQueue.
            queue.add(stringRequest);
            Log.d("Refreshing","IT IS WORKING");



        }



    }



    private void createListData(JSONArray attendeeArray) {
        int length = attendeeArray.length();
        TextView numOfUsers = getActivity().findViewById(R.id.page_attendNum);
        numOfUsers.setText("IN ATTENDANCE: "+String.valueOf(length));
        Log.d("NUM OF USER: ",String.valueOf(length));
        attendanceArrayList.clear();

        for(int i = 0; i < length; i++) {
            try {
                JSONObject full = (JSONObject) attendeeArray.get(i);
                JSONObject test = new JSONObject(full.getString("user"));
                String name = test.getString("name");
                //String lastName = test.getString("lastName");
                String duration = full.getString("duration");
                String endTime = full.getString("last_response_time");
                Log.d("increment",duration);


                Attendee attendee = new Attendee(name, duration, endTime);
                Log.d(fragementIdentifier, "Adding user");
                attendanceArrayList.add(attendee);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        adapter.notifyDataSetChanged();
        Log.d("Changing Adapter", "True");
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onNoteClick(int position) {

    }
}
