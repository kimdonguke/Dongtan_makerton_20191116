package com.example.dongtan_makerton_20191116;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.telephony.SmsManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;

public class MainActivity extends AppCompatActivity {
    Button button;
    private static final String SETTINGS_PLAYER = "settings_player";
    private BluetoothSPP bt;
    String name, year, locate;

    Intent intent;
    Button testbtn;
    SharedPreferences pref1;
    SmsManager smsManager = SmsManager.getDefault();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pref1=getSharedPreferences("image",MODE_PRIVATE);
        String image=pref1.getString("imagestrings","");
        Bitmap bitmap=StringToBitmap(image);

        button=findViewById(R.id.testintent);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent=new Intent(MainActivity.this,ResistActivity.class);
                startActivity(intent);
            }
        });
        bt = new BluetoothSPP(this); //Initializing

        if (!bt.isBluetoothAvailable()) { //블루투스 사용 불가
            Toast.makeText(getApplicationContext()
                    , "Bluetooth is not available"
                    , Toast.LENGTH_SHORT).show();
            finish();
        }

        bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() { //데이터 수신
            public void onDataReceived(byte[] data, String message) {

                switch (Integer.parseInt(message)){
                    case 0:
                        SendMessage(getSettingItem("locate"),getSettingItem("year"),getSettingItem("name")," 사용자 심박수 하강");
                        break;
                    case 1:
                        SendMessage(getSettingItem("locate"),getSettingItem("year"),getSettingItem("name")," 사용자 심박수 상승");
                        break;
                    case 2:
                        SendMessage(getSettingItem("locate"),getSettingItem("year"),getSettingItem("name")," 사용자에게 충격 발생");
                        break;
                }
                Log.e("test",message+" "+bt.getConnectedDeviceName());
            }
        });

        bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() { //연결됐을 때
            public void onDeviceConnected(String name, String address) {
                Toast.makeText(getApplicationContext()
                        , "Connected to " + name + "\n" + address
                        , Toast.LENGTH_SHORT).show();
            }

            public void onDeviceDisconnected() { //연결해제
                Toast.makeText(getApplicationContext()
                        , "Connection lost", Toast.LENGTH_SHORT).show();
            }

            public void onDeviceConnectionFailed() { //연결실패
                Toast.makeText(getApplicationContext()
                        , "Unable to connect", Toast.LENGTH_SHORT).show();
            }
        });

        Button btnConnect = findViewById(R.id.btnConnect); //연결시도
        btnConnect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (bt.getServiceState() == BluetoothState.STATE_CONNECTED) {
                    bt.disconnect();
                } else {
                    Intent intent = new Intent(getApplicationContext(), DeviceList.class);
                    startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
                }
            }
        });
        year=getSettingItem("year");
        name=getSettingItem("name");
        locate=getSettingItem("locate");
        testbtn=findViewById(R.id.testbtn);
        testbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMessage("동탄","김동욱","17","나는 이신우다");
            }
        });
    }

    public void onDestroy() {
        super.onDestroy();
        bt.stopService(); //블루투스 중지
    }

    public void onStart() {
        super.onStart();
        if (!bt.isBluetoothEnabled()) { //
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
        } else {
            if (!bt.isServiceAvailable()) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER); //DEVICE_ANDROID는 안드로이드 기기 끼리
//                setup();
            }
        }
    }

//    public void setup() {
//        Button btnSend = findViewById(R.id.btnSend); //데이터 전송
//        btnSend.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//               // hakbun=hak_edit.getText().toString();
//                //bt.send(hakbun, true);
//            }
//        });
//    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if (resultCode == Activity.RESULT_OK)
                bt.connect(data);
        }
        else if (requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER);
                //setup();
            } else {
                Toast.makeText(getApplicationContext()
                        , "Bluetooth was not enabled."
                        , Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
    private String getSettingItem(String key) {
        return getSharedPreferences(SETTINGS_PLAYER, 0).getString(key, null);
    }
    public Bitmap StringToBitmap(String encodedString){
        try{
            byte[] encodedByte= Base64.decode(encodedString,Base64.DEFAULT);
            Bitmap bitmap= BitmapFactory.decodeByteArray(encodedByte,0,encodedByte.length);
            return  bitmap;
        }
        catch (Exception e){
            e.getMessage();
            return null;
        }
    }
    public void SendMessage(String where, String name,String year,String symptom){
        String phoneNum="01021836707";
        try {
            smsManager.sendTextMessage(phoneNum, null, "긴급상황입니다."+where+"에서 "+year+" 세 "+name+"가 "+symptom, null, null);
            Toast.makeText(getApplicationContext(), "전송 완료!", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "전송실패, 잠시 뒤에 시도하세요"+e, Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
}