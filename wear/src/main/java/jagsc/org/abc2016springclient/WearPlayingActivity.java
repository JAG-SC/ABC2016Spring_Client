package jagsc.org.abc2016springclient;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.support.wearable.view.WearableDialogActivity;
import android.util.Log;
import android.view.View;
import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.lang.*;
import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

/**
 * Created by yuuki on 2015/12/12.
 */
public class WearPlayingActivity extends WearableActivity implements  SensorEventListener,View.OnClickListener,GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,DataApi.DataListener{ //DataApi.DataListener  {

    //private GoogleApiClient mGoogleApiClient;
    private String scene;//dataAPIのシーン情報のkey
    //private String datapath;//データアクセスパス
    private Boolean Vibe=false;//バイブするかどうかの真偽を入れる変数
    private Vibrator vib;//バイブさせる変数

    private BoxInsetLayout mContainerView;
    private TextView mTextView;
    private TextView mClockView;
    private SensorManager manager;
    private AlertDialog.Builder alertdialog;
    private Button btn_exit;

    private float sensordata[]={0,0,0,0,0,0};

    private Handler _handler;

    private GlobalVariables globalv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playing_wear);//activity_playing_wearを表示

        /*mContainerView = (BoxInsetLayout) findViewById(R.id.container);
        mTextView = (TextView) findViewById(R.id.text);
        mClockView = (TextView) findViewById(R.id.clock);
        */
        manager = (SensorManager) getSystemService(SENSOR_SERVICE);

        btn_exit = (Button) findViewById(R.id.btn_win);
        btn_exit.hasOnClickListeners();

        globalv=(GlobalVariables) this.getApplication();
        //mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addApi(Wearable.API).build();
        //mGoogleApiClient.connect();//接続
        _handler = new Handler();

        _handler.postDelayed(new Runnable() {//Timer的な
            @Override
            public void run() {
                RefreshSensorNum();
                _handler.postDelayed(this, 50);
            }
        }, 50);

        PutDataRequest dataMapRequest = PutDataRequest.create(globalv.DATA_PATH);


    }

    @Override
    public void onClick(View v){//デバッグ用
        switch (v.getId()) {
            case R.id.btn_exit_tit:

                alertdialog = new AlertDialog.Builder(WearPlayingActivity.this);
                alertdialog.setTitle("終了確認");
                alertdialog.setMessage("終了しますか？");
                alertdialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        WearPlayingActivity.this.moveTaskToBack(true);
                    }
                });
                alertdialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                alertdialog.create().show();
                break;
        }
    }

    @Override
    protected void onStop(){
        super.onStop();
        manager.unregisterListener(this);
        _handler.removeCallbacksAndMessages(null);
        if(globalv.mGoogleApiClient != null && globalv.mGoogleApiClient.isConnected()){
            globalv.mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        if(globalv.mGoogleApiClient != null && globalv.mGoogleApiClient.isConnected()){
            globalv.mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();

        ArrayList<List<Sensor>> sensors = new ArrayList<List<Sensor>>();
        sensors.add( manager.getSensorList(Sensor.TYPE_ACCELEROMETER));
        sensors.add(manager.getSensorList(Sensor.TYPE_GYROSCOPE));

        for(List<Sensor> sensor : sensors){
            if(sensor.size()>0){
                manager.registerListener(this,sensor.get(0),SensorManager.SENSOR_DELAY_GAME);
            }
        }

        globalv.mGoogleApiClient.connect();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub
    }
    @Override
    public void onSensorChanged(SensorEvent sensor_e) {
        // TODO Auto-generated method stub
        if (sensor_e.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            sensordata[0] = sensor_e.values[SensorManager.DATA_X];
            sensordata[1] = sensor_e.values[SensorManager.DATA_Y];
            sensordata[2] = sensor_e.values[SensorManager.DATA_Z];
            //sensorchange=true;
        }
        if (sensor_e.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            sensordata[3] = sensor_e.values[SensorManager.DATA_X];
            sensordata[4] = sensor_e.values[SensorManager.DATA_Y];
            sensordata[5] = sensor_e.values[SensorManager.DATA_Z];
            //sensorchange=true;
        }
    }

    public void RefreshSensorNum(){
        long timestamp = System.nanoTime();
        StringBuilder str_builder = new StringBuilder();
        str_builder.append(Long.toString(timestamp));
        for(int element_num=0;element_num < sensordata.length;++element_num){
            str_builder.append(",");
            str_builder.append(Float.toString(sensordata[element_num]));
        }
        String send_data = new String(str_builder);
        mTextView.setText(send_data);
        //SendToHandheld(sensordata, "sensordata", "/datapath");
        SendToHandheld(send_data);

    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {//dataAPIが更新されたら自動で呼び出される
        for (DataEvent event : dataEventBuffer) {
            if (event.getType() == DataEvent.TYPE_DELETED) {
                Log.d("TAG", "DataItem deleted: " + event.getDataItem().getUri());
            } else if (event.getType() == DataEvent.TYPE_CHANGED) {
                Log.d("TAG", "DataItem changed: " + event.getDataItem().getUri());
                DataMap dataMap = DataMap.fromByteArray(event.getDataItem().getData());
                //variable = dataMap.get~("keyname"); で受け取る


                switch(event.getDataItem().getUri().toString()) {
                    case "scene":
                        scene = dataMap.getString("scene");
                        switch (scene) {
                            case "scene:title":
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        //受け取り後の処理をここに
                                        //resultview.setText(resultstr);
                                        Intent intenttitle = new Intent(WearPlayingActivity.this, MainActivity.class);//MainActivityへ遷移
                                        startActivity(intenttitle);
                                    }
                                });
                                break;
                            case "scene:result":
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        //受け取り後の処理をここに
                                        //resultview.setText(resultstr);
                                        Intent intentresult = new Intent(WearPlayingActivity.this, WearResultActivity.class);//WearResultActivityへ遷移
                                        startActivity(intentresult);
                                    }
                                });
                                break;
                        }
                    case "vibrator":
                        int num = dataMap.getInt("vibrator");
                        vib = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);//バイブさせるためのサービスをvibに入れる
                        vib.vibrate(num);
                        break;
                }

            }
        }
    }



    @Override
    public void onConnected(Bundle bundle){
        Log.d("TAG", "onConnected");
    }
    @Override
    public void onConnectionSuspended(int i){
        Log.d("TAG", "onConnectionSuspended");
    }
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult){
        Log.e("TAG", "onConnectionFailed");
    }

    public void SendToHandheld(String send_data){
        if(send_data == null) return;
        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                String tweet = params[0];
                byte[] bytes;
                try {
                    bytes = tweet.getBytes("UTF-8");
                }
                catch(Exception ex){
                    Log.e("sender_error", ex.getMessage());
                    return null;
                }
                for (Node node : Wearable.NodeApi.getConnectedNodes(globalv.mGoogleApiClient).await().getNodes()){
                    Log.v("senderer", "sending to node:" + node.getId());
                    Wearable.MessageApi.sendMessage(globalv.mGoogleApiClient, node.getId(), "/sensordata", bytes)
                            .setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                                @Override
                                public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                                    Log.v("SendMsgResult", sendMessageResult.toString());
                                }
                            });
                }
                return null;
            }
        }.execute(send_data);
    }



}