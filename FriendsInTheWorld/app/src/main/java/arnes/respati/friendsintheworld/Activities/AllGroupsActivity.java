package arnes.respati.friendsintheworld.Activities;

import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import arnes.respati.friendsintheworld.Communications;
import arnes.respati.friendsintheworld.Controller;
import arnes.respati.friendsintheworld.Network.ReceiveListener;
import arnes.respati.friendsintheworld.R;

public class AllGroupsActivity extends AppCompatActivity {
    private ListView lvGroups;
    private Button buttonaddGroup, buttonReg;
    private EditText inputGroupName;
    private TextView tv_add, tv_groups;

    private ArrayAdapter<String> listAdapter;
    private Controller controller;

    private String groupName, userName, groupID;
    private boolean joined_a_group = false;
    private boolean english = true;
    private ArrayList<String> names = new ArrayList<>();

    Observer<ArrayList<String>> updateListView = new Observer<ArrayList<String>>() {
        @Override
        public void onChanged(@Nullable final ArrayList<String> newNames) {
            // Update the UI
            if (newNames!= null) {
                Log.d("Observe update listview", "update groups");
                names = newNames;
                listAdapter = new ArrayAdapter<>(AllGroupsActivity.this, R.layout.list_items_groups, R.id.tvGroupName, names);
                lvGroups.setAdapter(listAdapter);
            }
        }
    };

    Observer<Boolean> joined = new Observer<Boolean>() {
        @Override
        public void onChanged(@Nullable final Boolean joined) {
            // Update the UI
            joined_a_group = joined;
        }
    };

    Observer<Boolean> isEnglish = new Observer<Boolean>() {
        @Override
        public void onChanged(@Nullable final Boolean isEnglish) {
            // Update the UI
            english = isEnglish;
            setLanguage(isEnglish);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_groups);

        controller = (Controller) getApplication();

        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        controller.isEnglish().observe(this, isEnglish);
        controller.getCurrentGroupNames().observe(this, updateListView);
        controller.getCurrentStatus().observe(this, joined);

        userName = controller.getCurrentUserName().getValue();

        initData();
        registerListeners();
        getGroups();
    }

    private void registerListeners() {
        lvGroups.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                groupName = lvGroups.getItemAtPosition(position).toString();
                lvGroups.setSelection(position);
                lvGroups.setSelector(R.color.colorBackground);

                Toast.makeText(AllGroupsActivity.this, "You clicked " + groupName, Toast.LENGTH_SHORT).show();
                buttonReg.setEnabled(true);

//                if (joined_a_group && controller.getCurrentGroupName().getValue() != null ){
//                    if (controller.getCurrentGroupName().getValue().equals(groupName) ){
//                        Toast.makeText(AllGroupsActivity.this, "You are already registered to group " + groupName, Toast.LENGTH_SHORT).show();
//                        buttonReg.setEnabled(false);
//                    }
//                    else {
//                        Toast.makeText(AllGroupsActivity.this, "You clicked " + groupName, Toast.LENGTH_SHORT).show();
//                        buttonReg.setEnabled(true);
//                    }
//                }

            }
        });

        buttonaddGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = inputGroupName.getText().toString();

                if (groupAtMax()){
                    Toast.makeText(AllGroupsActivity.this, "Maximum number of groups in the server is reached", Toast.LENGTH_SHORT).show();
                }

                else {
                    registerGroup(name, userName);
                }
                getGroups();
                inputGroupName.getText().clear();
            }
        });

        buttonReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                String curGroupID = controller.getCurrentGroupID().getValue();
//                controller.sendRequest(Communications.unregister(curGroupID));

                registerGroup(groupName, userName);
                buttonReg.setEnabled(false);
            }
        });
    }

    private void initData() {
        lvGroups = (ListView) findViewById(R.id.lvAllGroups);
        buttonaddGroup = (Button) findViewById(R.id.buttonaddGroup);
        inputGroupName = (EditText) findViewById(R.id.inputGroupName);
        tv_add = (TextView) findViewById(R.id.tv_add);
        tv_groups = (TextView) findViewById(R.id.tv_groups);
        buttonReg = (Button) findViewById(R.id.buttonReg);
        buttonReg.setEnabled(false);

    }

    private void getGroups() {
        controller.sendRequest(Communications.allGroups());
    }

    private boolean groupAtMax (){
        boolean atMax = false;

        ArrayList<String> checkGroups = new ArrayList<>();
        checkGroups = controller.getCurrentGroupNames().getValue();

        if (checkGroups != null){
            if (checkGroups.size() >= 20) {
             atMax = true;
            }
        }

        return atMax;
    }

    private void registerGroup(String groupName, String userName){
        controller.sendRequest(Communications.register(groupName, userName));
    }

    public void setLanguage (boolean english) {
        if (english){
            tv_add.setText(getString(R.string.add_a_new_group));
            inputGroupName.setHint(getString(R.string.group_name));
            buttonaddGroup.setText(getString(R.string.add_group));
            tv_groups.setText(getString(R.string.group_name));
            buttonReg.setText(R.string.register);

        }
        else {
            tv_add.setText(getString(R.string.add_a_new_group2));
            inputGroupName.setHint(getString(R.string.group_name2));
            buttonaddGroup.setText(getString(R.string.add_group2));
            tv_groups.setText(getString(R.string.group_name2));
            buttonReg.setText(R.string.register2);
        }
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
                Toast.makeText(AllGroupsActivity.this,
                        getString(R.string.home),
                        Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(AllGroupsActivity.this,
                        getString(R.string.home2),
                        Toast.LENGTH_SHORT).show();
            }

            Intent intent = new Intent(AllGroupsActivity.this, MainActivity.class);
            AllGroupsActivity.this.startActivity(intent);
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
                        Toast.makeText(AllGroupsActivity.this,
                                getString(R.string.map),
                                Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(AllGroupsActivity.this,
                                getString(R.string.map2),
                                Toast.LENGTH_SHORT).show();
                    }
                    Intent intent = new Intent(AllGroupsActivity.this, MapsActivity.class);
                    AllGroupsActivity.this.startActivity(intent);
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
                            Toast.makeText(AllGroupsActivity.this,
                                    getString(R.string.mygroup),
                                    Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(AllGroupsActivity.this,
                                    getString(R.string.mygroup2),
                                    Toast.LENGTH_SHORT).show();
                        }
                        Intent intent = new Intent(AllGroupsActivity.this, MyGroupActivity.class);
                        AllGroupsActivity.this.startActivity(intent);
                    }
                    if (id == R.id.pop_allGroups) {
                        if (english){
                            Toast.makeText(AllGroupsActivity.this,
                                    getString(R.string.allgroup),
                                    Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(AllGroupsActivity.this,
                                    getString(R.string.allgroup2),
                                    Toast.LENGTH_SHORT).show();
                        }
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
                        controller.isEnglish().setValue(false);
                        Toast.makeText(AllGroupsActivity.this,
                                getString(R.string.changeSwedish),
                                Toast.LENGTH_SHORT).show();
                    }
                    if (id == R.id.pop_changeEnglish) {
                        controller.isEnglish().setValue(true);
                        Toast.makeText(AllGroupsActivity.this,
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
