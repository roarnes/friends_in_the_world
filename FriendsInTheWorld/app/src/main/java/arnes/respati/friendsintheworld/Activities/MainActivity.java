package arnes.respati.friendsintheworld.Activities;

import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;

import arnes.respati.friendsintheworld.Communications;
import arnes.respati.friendsintheworld.Controller;
import arnes.respati.friendsintheworld.R;

public class MainActivity extends AppCompatActivity {
    private Controller controller;

    private static final String TAG = "MainActivity" ;
    private static final int ERROR_DIALOG_REQUEST = 9001;

    private TextView tvHello, tvResult, tvGroupStatus, tvUser;
    private Button buttonLeave;

    private String userName, groupName;
    private Boolean join = false, english = true;

    // Create the observer which updates the UI.
    Observer<Boolean> joined = new Observer<Boolean>() {
        @Override
        public void onChanged(@Nullable final Boolean joined) {
            // Update the UI
            join = joined;
            setTvGroupStatus(joined, english, groupName);
        }
    };

    Observer<Boolean> isEnglish = new Observer<Boolean>() {
        @Override
        public void onChanged(@Nullable final Boolean isEnglish) {
            Log.d("Observe update english", isEnglish.toString());
            // Update the UI
            english = isEnglish;
            setTvGroupStatus(join, isEnglish, groupName);

            if (english){
                tvHello.setText(getString(R.string.hello));
                buttonLeave.setText(R.string.leave_group);
            }
            else {
                tvHello.setText(getString(R.string.hello2));
                buttonLeave.setText(R.string.leave_group2);
            }
        }
    };

    Observer<String> currentGroupName = new Observer<String>() {
        @Override
        public void onChanged(@Nullable final String newName) {
            // Update the UI
            groupName = newName;
            setTvGroupStatus(join, english, newName);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        controller = (Controller) getApplication();


        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        controller.getCurrentStatus().observe(this, joined);
        controller.isEnglish().observe(this, isEnglish);
        controller.getCurrentGroupName().observe(this, currentGroupName);

        initComponent();
        registerListeners();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void initComponent() {
        tvHello = (TextView) findViewById(R.id.tvHello);
        tvResult = (TextView) findViewById(R.id.tvResult);
        tvGroupStatus = (TextView) findViewById(R.id.tvGroupStatus);
        tvUser = (TextView) findViewById(R.id.tvUserName);
        buttonLeave = (Button) findViewById(R.id.buttonLeaveGroup);

        tvHello.setText(getString(R.string.hello));
        tvUser.setText(controller.getCurrentUserName().getValue());
        buttonLeave.setVisibility(View.INVISIBLE);
    }

    public void registerListeners(){
        buttonLeave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unregisterGroup();
            }
        });
    }

    public void setTvGroupStatus (boolean joined, boolean isEnglish, String name){
        if (joined) {
            String temp;
            if (isEnglish){
                temp = getString(R.string.regTo) + name;
                tvGroupStatus.setText(temp);
            }
            else {
                temp = getString(R.string.regTo2) + name;
                tvGroupStatus.setText(temp);
            }
            buttonLeave.setVisibility(View.VISIBLE);
        }
        else {
            if (isEnglish){
                tvGroupStatus.setText(getString(R.string.createNew));
            }
            else {
                tvGroupStatus.setText(getString(R.string.createNew2));
            }
            buttonLeave.setVisibility(View.INVISIBLE);
        }

    }

    private void unregisterGroup(){
        String groupID = controller.getCurrentGroupID().getValue();
        controller.sendRequest(Communications.unregister(groupID));
    }

    public void setResult(String message) {
        tvResult.setText(message);
    }




    // create an action bar button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mymenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.btnHome) {
            if (english) {
                Toast.makeText(MainActivity.this,
                        getString(R.string.home),
                        Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(MainActivity.this,
                        getString(R.string.home2),
                        Toast.LENGTH_SHORT).show();
            }
        }

        if (id == R.id.btnMap) {
            View popupMap = findViewById(R.id.btnMap); // SAME ID AS MENU ID
            PopupMenu popupMenu = new PopupMenu(this, popupMap);
            popupMenu.inflate(R.menu.popmenu_map);

            if (english) {
                popupMenu.getMenu().getItem(0).setTitle(R.string.show_map);
            }
            else {
                popupMenu.getMenu().getItem(0).setTitle(R.string.show_map2);
            }

            //registering popup with OnMenuItemClickListener
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    if (english){
                        Toast.makeText(MainActivity.this,
                                getString(R.string.map),
                                Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(MainActivity.this,
                                getString(R.string.map2),
                                Toast.LENGTH_SHORT).show();
                    }
                    Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                    MainActivity.this.startActivity(intent);
                    return true;
                    }
                });

            popupMenu.show();

            return true;
        }

        if (id == R.id.btnGroups) {
            View popupGroups = findViewById(R.id.btnGroups); // SAME ID AS MENU ID
            PopupMenu popupMenu = new PopupMenu(this, popupGroups);
            popupMenu.inflate(R.menu.popmenu_group);

            if (english) {
                popupMenu.getMenu().getItem(0).setTitle(R.string.show_mygroup);
                popupMenu.getMenu().getItem(1).setTitle(R.string.show_allgroup);
            }
            else {
                popupMenu.getMenu().getItem(0).setTitle(R.string.show_mygroup2);
                popupMenu.getMenu().getItem(1).setTitle(R.string.show_allgroup2);
            }

            //registering popup with OnMenuItemClickListener
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                public boolean onMenuItemClick(MenuItem item) {
                    int id = item.getItemId();
                    if (id == R.id.pop_myGroup) {
                        if (english){
                            Toast.makeText(MainActivity.this,
                                    getString(R.string.mygroup),
                                    Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(MainActivity.this,
                                    getString(R.string.mygroup2),
                                    Toast.LENGTH_SHORT).show();
                        }
                        Intent intent = new Intent(MainActivity.this, MyGroupActivity.class);
                        MainActivity.this.startActivity(intent);
                    }
                    if (id == R.id.pop_allGroups) {
                        if (english){
                            Toast.makeText(MainActivity.this,
                                    getString(R.string.allgroup),
                                    Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(MainActivity.this,
                                    getString(R.string.allgroup2),
                                    Toast.LENGTH_SHORT).show();
                        }
                        Intent intent = new Intent(MainActivity.this, AllGroupsActivity.class);
                        MainActivity.this.startActivity(intent);
                    }
                    return true;
                }
            });

            popupMenu.show();
        }

        if (id == R.id.btnSettings) {
            View popupSettings = findViewById(R.id.btnSettings); // SAME ID AS MENU ID
            PopupMenu popupMenu = new PopupMenu(this, popupSettings);
            popupMenu.inflate(R.menu.popmenu_setting);

            if (english) {
                popupMenu.getMenu().getItem(0).setTitle(R.string.swedish);
                popupMenu.getMenu().getItem(1).setTitle(R.string.english);
            }
            else {
                popupMenu.getMenu().getItem(0).setTitle(R.string.swedish2);
                popupMenu.getMenu().getItem(1).setTitle(R.string.english2);
            }

            //registering popup with OnMenuItemClickListener
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    int id = item.getItemId();
                    if (id == R.id.pop_changeSwedish) {
                        controller.isEnglish().postValue(false);
                        Toast.makeText(MainActivity.this,
                                getString(R.string.changeSwedish),
                                Toast.LENGTH_SHORT).show();
                    }
                    if (id == R.id.pop_changeEnglish) {
                        controller.isEnglish().postValue(true);
                        Toast.makeText(MainActivity.this,
                                getString(R.string.changeEnglish),
                                Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
            });

            popupMenu.show();
        }

        return super.onOptionsItemSelected(item);
    }

}
