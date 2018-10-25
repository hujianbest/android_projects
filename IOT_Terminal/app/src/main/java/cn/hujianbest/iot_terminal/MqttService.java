package cn.hujianbest.iot_terminal;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MqttService extends Service {

    MqttConnectOptions connOpts;
    MqttClient mqttClient;

    //数据存储方式需要改进，下面仅做演示
    private List<String> temperatureList = new ArrayList<>();
    private List<String> powerList = new ArrayList<>();



    final String broker       = "ssl://whutapp.mqtt.iot.bj.baidubce.com:1884";
    final String clientId     = "test_mqtt_android"+ UUID.randomUUID().toString();
    final String topic        = "temperature";

    private MqttBinder mqttBinder = new MqttBinder();

    class MqttBinder extends Binder{
        public boolean connect(String username,String password){
            try{
                connOpts = new MqttConnectOptions();
                connOpts.setUserName(username);
                connOpts.setPassword(password.toCharArray());

                System.out.println("Connecting to broker: " + broker);
                mqttClient = new MqttClient(broker, clientId,null);
                mqttClient.connect(connOpts);
                System.out.println("Connected. Client id is " + clientId);

            }catch (Exception e){
                e.printStackTrace();
            }
            return true;
        }

        public void run(){
            try{
                mqttClient.subscribe(topic, new IMqttMessageListener(){

                    @Override
                    public void messageArrived(String topic, final MqttMessage message) {
                        System.out.println("MQTT message received: " + message);
                        try{
                            JSONObject jsonObject = new JSONObject(message.toString());
                            String temperatureVal = jsonObject.getString("temperature");
                            String powerVal = jsonObject.getString("power");
                            //存储数据的同时向activity发送广播
                            temperatureList.add(temperatureVal);
                            System.out.println("temperrature: "+temperatureVal);
                            powerList.add(powerVal);
                            System.out.println("power: "+powerVal);
                            getNotificationManager().notify(1,
                                    getNotification("监控进行中...",
                                            "当前温度："+temperatureVal+"°C"+"    "
                                                    +"当前功率："+powerVal+"W"));
                            Intent intent = new Intent("cn.hujianbest.iot_terminal.MQTT_BROADCAST");
                            intent.putExtra("mqttMsg",temperatureVal+" "+powerVal);
                            sendBroadcast(intent);
                        }catch (Exception e){
                            e.printStackTrace();
                            System.out.println("json解析异常!" );
                        }
                    }
                });
            }catch (Exception ex){
                ex.printStackTrace();
            }

        }
    }


    @Override
    public void onCreate(){

        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent,int flags, int startId){
        return super.onStartCommand(intent,flags,startId);
    }

    @Override
    public void onDestroy(){

        super.onDestroy();
        getNotificationManager().cancel(1);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return mqttBinder;
    }

    private NotificationManager getNotificationManager(){
        return (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
    }

    private Notification getNotification(String title,String content){
        Intent intent = new Intent(this,ContentActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this,0,intent,0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher));
        builder.setContentIntent(pi);
        builder.setContentTitle(title);
        builder.setContentText(content);
//        builder.setContentTitle("监控进行中...");
//        builder.setContentText("当前温度："+250+"°C"+"    "+"当前功率："+0+"W");
        return builder.build();
    }
}
