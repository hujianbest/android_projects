package cn.hujianbest.iot_terminal;

import android.content.DialogInterface;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ContentActivity extends AppCompatActivity implements HomeFragment.OnFragmentInteractionListener,
        ChartFragment.OnFragmentInteractionListener,NoticeFragment.OnFragmentInteractionListener{

    private HomeFragment homeFragment;
    private ChartFragment chartFragment;
    private NoticeFragment noticeFragment;

    private TextView temperature;
    private TextView power;
    private Button setTemperature;
    private TextView temperatureSetText;
    private String temperatureSetVal;

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


    private IntentFilter intentFilter;
    private MqttReceiver mqttReceiver;


    private TextView mTextMessage;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    //mTextMessage.setText(R.string.title_home);
                    if (null == homeFragment) {
                        homeFragment = new HomeFragment();
                    }
                    replaceFragment(homeFragment);
                    return true;
                case R.id.navigation_dashboard:
                    if (null == chartFragment) {
                        chartFragment = new ChartFragment();
                    }
                    replaceFragment(chartFragment);
                    return true;
                case R.id.navigation_notifications:
                    if (null == noticeFragment) {
                        noticeFragment = new NoticeFragment();
                    }
                    replaceFragment(noticeFragment);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        homeFragment = new HomeFragment();
        replaceFragment(homeFragment);

//        setTemperature = (Button)homeFragment.getView().findViewById(R.id.temperature_set_button);
//        temperatureSetText = (TextView) homeFragment.getView().findViewById(R.id.temperature_set_text_frg);
//        setTemperature.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                LayoutInflater factory = LayoutInflater.from(ContentActivity.this);
//                View view = factory.inflate(R.layout.editdialog, null);
//                final EditText edit=(EditText)view.findViewById(R.id.edit_dialog);
//
//                AlertDialog.Builder inputdialog = new AlertDialog.Builder(ContentActivity.this);
//                inputdialog.setTitle("请输入");
//                inputdialog.setView(edit);
//                inputdialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        temperatureSetVal=edit.getText().toString();
//                        temperatureSetText.setText(temperatureSetVal);
//                    }
//                });
//                inputdialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//
//                    }
//                });
//            }
//        });

        intentFilter = new IntentFilter();
        intentFilter.addAction("cn.hujianbest.iot_terminal.MQTT_BROADCAST");
        mqttReceiver = new MqttReceiver(new Handler());
        registerReceiver(mqttReceiver,intentFilter);

        Intent bindIntent = new Intent(this, MqttService.class);
        bindService(bindIntent, connection, BIND_AUTO_CREATE);

        //mqttBinder.run();//有问题

    }

    public void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.frame_layout, fragment);
        transaction.commit();
    }

    @Override
    public void onFragmentInteraction(Uri uri){

    }


    public class MqttReceiver extends BroadcastReceiver{

        private Handler handler;

        public MqttReceiver(Handler handler){
            this.handler = handler;
        }

        @Override
        public void onReceive(Context context, Intent intent){
            String msg = intent.getStringExtra("mqttMsg");
            String[] msgs = msg.split(" ");
            final String temperatureVal = msgs[0];
            final String powerVal = msgs[1];

            //在这里更新UI
            System.out.println("更新UI-temperrature: "+temperatureVal);
            System.out.println("更新UI-power: "+powerVal);

            handler.post(new Runnable() {
                @Override
                public void run() {
                    //UI
                    temperature = (TextView) homeFragment.getView().findViewById(R.id.temperature_text_frg);
                    power = (TextView)homeFragment.getView().findViewById(R.id.power_text_frg);
                    temperature.setText(temperatureVal+"°C");
                    power.setText(powerVal+"W");
                }
            });

        }
    }

}





