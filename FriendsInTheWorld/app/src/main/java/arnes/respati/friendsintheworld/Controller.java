package arnes.respati.friendsintheworld;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.arch.lifecycle.MutableLiveData;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import arnes.respati.friendsintheworld.Activities.AllGroupsActivity;
import arnes.respati.friendsintheworld.Activities.MainActivity;
import arnes.respati.friendsintheworld.Activities.MapsActivity;
import arnes.respati.friendsintheworld.Activities.MyGroupActivity;
import arnes.respati.friendsintheworld.Network.ReceiveListener;
import arnes.respati.friendsintheworld.Network.TCPConnection;

import static android.content.ContentValues.TAG;

public class Controller extends Application {
    private static final int MAX_GROUP = 20;
    private static final int MAX_USER = 20;

    public TCPConnection connection;
    public boolean connected = false;
    private boolean bound = false;
    private ServiceConnection serviceConn;
    private Listener listener;

    private MutableLiveData<String> userName;
    private MutableLiveData<Double> userLat;
    private MutableLiveData<Double> userLong;

    private MutableLiveData<String> currentGroupName;
    private MutableLiveData<String> currentGroupID;

    private MutableLiveData<ArrayList<String>> groupNames;

    private MutableLiveData<ArrayList<String>> memberNames;
    private MutableLiveData<ArrayList<Double>> memberLat;
    private MutableLiveData<ArrayList<Double>> memberLong;

    private MutableLiveData<Boolean> joined_a_group;
    private MutableLiveData<Boolean> english;

    @Override
    public void onCreate() {
        super.onCreate();

        Intent intent = new Intent(this, TCPConnection.class);
        intent.putExtra(TCPConnection.IP, "195.178.227.53");
        intent.putExtra(TCPConnection.PORT, "7117");
        startService(intent);

        serviceConn = new ServiceConn();
        boolean result = bindService(intent, serviceConn, 0);
        if (!result)
            Log.d("Controller-constructor", "No binding");
    }


    public void connect(TCPConnection connection) {
        connection.connect();
        this.connection = connection;
    }

    public void disconnect() {
        if (connected) {
            connection.disconnect();
        }
    }

    public void sendRequest(String communications) {
        connection.send(communications);
    }

    public void onDestroy() {
        if (bound) {
            unbindService(serviceConn);
            listener.stopListener();
            bound = false;
        }
    }

    public class ServiceConn implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder binder) {
            TCPConnection.LocalService ls = (TCPConnection.LocalService) binder;
            connection = ls.getService();
            connect(connection);
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    }

    public void receiving (String message) {
        Thread t = new Listener(message);
        t.start();
    }

    ///// LIVE DATA /////
    public MutableLiveData<Boolean> getCurrentStatus() {
        if (joined_a_group == null) {
            joined_a_group = new MutableLiveData<Boolean>();
            joined_a_group.postValue(false);
        }
        return joined_a_group;
    }

    public MutableLiveData<Boolean> isEnglish() {
        if (english == null) {
            english = new MutableLiveData<Boolean>();
            english.postValue(true);
        }
        return english;
    }

    public MutableLiveData<String> getCurrentUserName() {
        if (userName == null) {
            userName = new MutableLiveData<String>();
        }
        return userName;
    }

    public MutableLiveData<Double> getCurrentUserLat() {
        if (userLat == null) {
            userLat = new MutableLiveData<Double>();
        }
        return userLat;
    }

    public MutableLiveData<Double> getCurrentUserLong() {
        if (userLong == null) {
            userLong = new MutableLiveData<Double>();
        }
        return userLong;
    }

    public MutableLiveData<String> getCurrentGroupName() {
        if (currentGroupName == null) {
            currentGroupName = new MutableLiveData<String>();
        }
        return currentGroupName;
    }

    public MutableLiveData<String> getCurrentGroupID() {
        if (currentGroupID == null) {
            currentGroupID = new MutableLiveData<String>();
        }
        return currentGroupID;
    }

    public MutableLiveData<ArrayList<String>> getCurrentGroupNames() {
        if (groupNames == null) {
            groupNames = new MutableLiveData<ArrayList<String>>();
        }
        return groupNames;
    }

    public MutableLiveData<ArrayList<String>> getCurrentMemberNames() {
        if (memberNames == null) {
            memberNames = new MutableLiveData<ArrayList<String>>();
        }
        return memberNames;
    }

    public MutableLiveData<ArrayList<Double>> getCurrentMemberLat() {
        if (memberLat == null) {
            memberLat = new MutableLiveData<ArrayList<Double>>();
        }
        return memberLat;
    }

    public MutableLiveData<ArrayList<Double>> getCurrentMemberLong() {
        if (memberLong == null) {
            memberLong = new MutableLiveData<ArrayList<Double>>();
        }
        return memberLong;
    }



    private class Listener extends Thread {
        private String message;
        Handler h = new Handler(Looper.getMainLooper());

        public Listener (String message) {
            this.message = message;
        }

        public void stopListener() {
            interrupt();
            listener = null;
        }

        public void run() {
            try {
                JSONObject jsonObject = new JSONObject(message);
                receiving(jsonObject);

            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, "Error processing message ", e);
            }

        }

        private void receiving(JSONObject jsonObject) throws JSONException {
            String requestType = jsonObject.getString("type");
            Log.d("Message type" , requestType);
            String groupName, groupID;
            switch (requestType) {
                case "groups": {
                    ArrayList<String> groupNames = new ArrayList<>();

                    JSONArray jsonArray = jsonObject.getJSONArray("groups");

                    for (int index = 0; index < jsonArray.length(); index++) {
                        groupNames.add(jsonArray.getJSONObject(index).getString("group"));
                    }
                    getCurrentGroupNames().postValue(groupNames);
                    break;
                }
                case "register": {
                    groupName = jsonObject.getString("group");
                    getCurrentGroupName().postValue(groupName);
                    groupID = jsonObject.getString("id");
                    getCurrentGroupID().postValue(groupID);

                    Log.d("Group is " , groupName + " id is: " + groupID);

                    String finalGroupName = groupName;
                    h.post(() -> {
                        Toast.makeText(Controller.this, "Registered to group " + finalGroupName, Toast.LENGTH_SHORT).show();
                    });
                    getCurrentStatus().postValue(true);
                    break;
                }
                case "unregister": {
                    groupID = jsonObject.getString("id");
                    getCurrentGroupID().postValue(groupID);
                    if (getCurrentGroupName() != null && (groupID.equals(getCurrentGroupID().toString()))) {
                        getCurrentGroupName().postValue(null);
                        getCurrentGroupID().postValue(null);
                    }
                    h.post(() -> {
                        Toast.makeText(Controller.this, "Successfully left group" , Toast.LENGTH_SHORT).show();
                    });
                    getCurrentStatus().postValue(false);
                    break;
                }
                case "locations": {
                    groupName = jsonObject.getString("group");
                    getCurrentGroupName().postValue(groupName);

                    JSONArray jsonArray = jsonObject.getJSONArray("location");
                    ArrayList<String> memberNames = new ArrayList<>();
                    ArrayList<Double> longitudes = new ArrayList<>();
                    ArrayList<Double> latitudes = new ArrayList<>();

                    for (int index = 0; index < jsonArray.length(); index++) {
                        String lat = jsonArray.getJSONObject(index).getString("latitude");
                        String lng = jsonArray.getJSONObject(index).getString("longitude");
                        if (!lng.equals("NaN") && !lat.equals("NaN")) {
                            memberNames.add(jsonArray.getJSONObject(index).getString("member"));
                            longitudes.add(Double.parseDouble(lng));
                            latitudes.add(Double.parseDouble(lat));
                            Log.d(TAG, "receiving: " + memberNames + " " + longitudes + " " + latitudes);
                        }
                        else {
                            memberNames.add(jsonArray.getJSONObject(index).getString("member"));
                            longitudes.add(-1.0);
                            latitudes.add(-1.0);
                        }

                    }
                    getCurrentMemberNames().postValue(memberNames);
                    getCurrentMemberLat().postValue(latitudes);
                    getCurrentMemberLong().postValue(longitudes);
                    break;
                }
                case "location": {
                    groupID = jsonObject.getString("id");
                    getCurrentGroupID().postValue(groupID);
                    Double longitude = Double.parseDouble(jsonObject.getString("longitude"));
                    getCurrentUserLong().postValue(longitude);
                    Double latitude = Double.parseDouble(jsonObject.getString("latitude"));
                    getCurrentUserLat().postValue(latitude);
                    break;
                }
                case "members": {
                    groupName = jsonObject.getString("group");
                    getCurrentGroupName().postValue(groupName);

                    JSONArray jsonArray = jsonObject.getJSONArray("members");

                    ArrayList<String> memberNames = new ArrayList<>();

                    for (int index = 0; index < jsonArray.length(); index++) {
                        memberNames.add(jsonArray.getJSONObject(index).getString("member"));
                    }

                    getCurrentMemberNames().postValue(memberNames);
                    break;
                }
            }
        }

    }
}