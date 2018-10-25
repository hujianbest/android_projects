package cn.hujianbest.iot_terminal;

import android.app.Application;
import android.os.Handler;
import java.util.UUID;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {

    Handler handler = new Handler();

    final String broker       = "ssl://whutapp.mqtt.iot.bj.baidubce.com:1884";
    final String clientId     = "test_mqtt_android"+ UUID.randomUUID().toString();
    final String username     = "whutapp/windows";
    final String password     = "TLi7eK2AkPmDqXNQs1rYIAz8vgbUvbYK2qbnzk9fOmc=";

    final String topic        = "temperature";

    final Data powerData = new Data();
    MqttClient mqttClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        SeekBar seekBar = (SeekBar)findViewById(R.id.powerset_bar);
        final TextView powerSetVal = (TextView) findViewById(R.id.powerset);
        final Button powerSetBut = (Button)findViewById(R.id.powerset_button);

        final TextView power = (TextView) findViewById(R.id.power_text);
        final TextView temperature = (TextView) findViewById(R.id.temperature_text);
        final TextView clientIdText = (TextView) findViewById(R.id.client_id);



        try {
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setUserName(username);
            connOpts.setPassword(password.toCharArray());

            System.out.println("Connecting to broker: " + broker);
            Toast.makeText(MainActivity.this,"Connecting...",Toast.LENGTH_SHORT).show();
            mqttClient = new MqttClient(broker, clientId,null);
            mqttClient.connect(connOpts);
            System.out.println("Connected. Client id is " + clientId);
            Toast.makeText(MainActivity.this,"Connected to:"+broker,Toast.LENGTH_SHORT).show();
            clientIdText.setText("ClientId:"+clientId);
            mqttClient.subscribe(topic, new IMqttMessageListener(){

                @Override
                public void messageArrived(String topic, final MqttMessage message) {
//                        Message msg = Message.obtain();
//                        Bundle bundle = new Bundle();
//                        bundle.putString("content",message.toString());
//                        msg.setData(bundle);
//                        handler.sendMessage(msg);
                    System.out.println("MQTT message received: " + message);
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            try{
                                JSONObject jsonObject = new JSONObject(message.toString());
                                String temperatureVal = jsonObject.getString("temperature");
                                temperature.setText(temperatureVal+" °C");
                                String powerVal = jsonObject.getString("power");
                                power.setText(powerVal+" W");
                            }catch (Exception e){
                                e.printStackTrace();
                                System.out.println("jsom解析异常" );
                            }


                        }
                    };
                    handler.post(runnable);
                }
            });
            System.out.println("Subscribed to topic: " + topic);
        } catch(Exception ex) {
            ex.printStackTrace();
        }



        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                powerData.setPower(progress);
                powerSetVal.setText(Integer.toString(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        powerSetBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                publishMessage();
            }
        });


    }

    public void publishMessage(){

        try {
            String content = "{\"set_power\":"+powerData.getPower()+"}";
            mqttClient.publish("powerSet",new MqttMessage(content.getBytes()));
            System.out.println("Publish Message: "+content);
        } catch (MqttException e) {
            System.err.println("Error Publishing: " + e.getMessage());
            e.printStackTrace();
        }
    }


}


class Data {
    private int power = 0;

    public int getPower(){
        return this.power;
    }
    public void setPower(int power){
        this.power=power;
    }
}