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
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import arnes.respati.friendsintheworld.Communications;
import arnes.respati.friendsintheworld.Controller;
import arnes.respati.friendsintheworld.R;

public class MyGroupActivity extends AppCompatActivity {
    private ListView lvMembers;
    private Controller controller;
    private boolean english;
    private ArrayAdapter<String> listAdapter;
    private ArrayList<String> names;
    private TextView tv_member;


    Observer<Boolean> isEnglish = new Observer<Boolean>() {
        @Override
        public void onChanged(@Nullable final Boolean isEnglish) {
            Log.d("Observe update english", isEnglish.toString());
            // Update the UI
            english = isEnglish;
            setLanguage(english);
        }
    };

    Observer<ArrayList<String>> updateListView = new Observer<ArrayList<String>>() {
        @Override
        public void onChanged(@Nullable final ArrayList<String> newNames) {
            // Update the UI
            if (newNames!= null) {
                Log.d("Observe update listview", "member names");
                names = newNames;
                listAdapter = new ArrayAdapter<>(MyGroupActivity.this, R.layout.list_items_members, R.id.tvMemberName, names);
                lvMembers.setAdapter(listAdapter);
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_group);
        controller = (Controller) getApplication();
        controller.isEnglish().observe(this, isEnglish);
        controller.getCurrentMemberNames().observe(this, updateListView);

        initData();
        getMemberNames(controller.getCurrentGroupName().getValue());
    }

    private void initData() {
        lvMembers = (ListView) findViewById(R.id.lvMembers);
        tv_member = (TextView) findViewById(R.id.tv_member);
    }

    private void getMemberNames(String groupName) {
        controller.sendRequest(Communications.groupMembers(groupName));
    }

    private void setLanguage(boolean english) {
        if (english){
            tv_member.setText(getString(R.string.members));
        }
        else {
            tv_member.setText(getString(R.string.members2));
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
                Toast.makeText(MyGroupActivity.this,
                        getString(R.string.home),
                        Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(MyGroupActivity.this,
                        getString(R.string.home2),
                        Toast.LENGTH_SHORT).show();
            }
            Intent intent = new Intent(MyGroupActivity.this, MainActivity.class);
            MyGroupActivity.this.startActivity(intent);
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
                        Toast.makeText(MyGroupActivity.this,
                                getString(R.string.map),
                                Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(MyGroupActivity.this,
                                getString(R.string.map2),
                                Toast.LENGTH_SHORT).show();
                    }
                    Intent intent = new Intent(MyGroupActivity.this, MapsActivity.class);
                    MyGroupActivity.this.startActivity(intent);
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
                            Toast.makeText(MyGroupActivity.this,
                                    getString(R.string.mygroup),
                                    Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(MyGroupActivity.this,
                                    getString(R.string.mygroup2),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                    if (id == R.id.pop_allGroups) {
                        if (english){
                            Toast.makeText(MyGroupActivity.this,
                                    getString(R.string.allgroup),
                                    Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(MyGroupActivity.this,
                                    getString(R.string.allgroup2),
                                    Toast.LENGTH_SHORT).show();
                        }
                        Intent intent = new Intent(MyGroupActivity.this, AllGroupsActivity.class);
                        MyGroupActivity.this.startActivity(intent);
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
                        Toast.makeText(MyGroupActivity.this,
                                getString(R.string.changeSwedish),
                                Toast.LENGTH_SHORT).show();
                    }
                    if (id == R.id.pop_changeEnglish) {
                        controller.isEnglish().postValue(true);
                        Toast.makeText(MyGroupActivity.this,
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
