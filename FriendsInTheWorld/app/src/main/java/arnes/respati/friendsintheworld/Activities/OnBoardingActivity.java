package arnes.respati.friendsintheworld.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import arnes.respati.friendsintheworld.R;

public class OnBoardingActivity extends AppCompatActivity {
    private EditText inputName;
    private Button btnFinish;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor preferencesEditor;
    private static final String PREF_NAME = "prefs";
    private static final String KEY_NAME = "name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_boarding);
        initializeComponents();
        registerListeners();
    }

    private void initializeComponents() {
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        preferencesEditor = sharedPreferences.edit();

        inputName = (EditText) findViewById(R.id.inputName);

        btnFinish = (Button) findViewById(R.id.btnFinish);
    }

    private void registerListeners () {
        btnFinish.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String name = inputName.getText().toString();
                if (name.isEmpty()) {
                    Toast.makeText(OnBoardingActivity.this, "Name field can not be empty!", Toast.LENGTH_LONG).show();
                }
                else {
                    saveUserName();
                    getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit()
                            .putBoolean("isFirstRun", false).commit();
                    Intent intent = new Intent(OnBoardingActivity.this, MapsActivity.class);
                    startActivity(intent);
                }
            }
        });
    }

    public void saveUserName(){
        String name = inputName.getText().toString();

        preferencesEditor.putString(KEY_NAME, name);
        preferencesEditor.commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        String name = inputName.getText().toString();

        super.onSaveInstanceState(outState);
        outState.putString("onboard_name", name);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        inputName.setText(savedInstanceState.getString("onboard_name"));
    }
}
