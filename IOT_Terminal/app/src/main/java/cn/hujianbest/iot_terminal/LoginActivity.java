package cn.hujianbest.iot_terminal;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;


public class LoginActivity extends AppCompatActivity {

    private MqttService.MqttBinder mqttBinder;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mqttBinder = (MqttService.MqttBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };


    String username;
    String password;

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;


    private Button login;
    private EditText editUsername;
    private EditText editPassword;
    private CheckBox checkBox_username;
    private CheckBox checkBox_password;

    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        login = (Button) findViewById(R.id.user_sign_in_button);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
                if (checkBox_username.isChecked()) {
                    editor.putString("userName", username);
                    editor.commit();
                } else {
                    editor.remove("userName");
                    editor.commit();
                }
                if (checkBox_password.isChecked()) {
                    editor.putString("userPassword", password);
                    editor.commit();
                } else {
                    editor.remove("userPassword");
                    editor.commit();
                }
            }
        });

        editUsername = (EditText) findViewById(R.id.username);
        editPassword = (EditText) findViewById(R.id.password);
        checkBox_username = (CheckBox) findViewById(R.id.cb_username);
        checkBox_password = (CheckBox) findViewById(R.id.cb_password);


        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        preferences = getSharedPreferences("UserInfo", MODE_PRIVATE);
        editor = preferences.edit();

        String name = preferences.getString("userName", null);
        if (name == null) {
            checkBox_username.setChecked(false);
        } else {
            editUsername.setText(name);
            checkBox_username.setChecked(true);
        }
        String password = preferences.getString("userPassword", null);
        if (password == null) {
            checkBox_password.setChecked(false);
        } else {
            editPassword.setText(password);
            checkBox_password.setChecked(true);
        }

        Intent bindIntent = new Intent(this, MqttService.class);
        bindService(bindIntent, connection, BIND_AUTO_CREATE);


    }


    private void attemptLogin() {
        //在这里开启服务
        mProgressView.setVisibility(View.VISIBLE);
        username = editUsername.getText().toString();
        password = editPassword.getText().toString();

        if (mqttBinder.connect(username, password)) {
            mqttBinder.run();
            Intent intent = new Intent(LoginActivity.this, ContentActivity.class);
            startActivity(intent);
        }

    }

}


