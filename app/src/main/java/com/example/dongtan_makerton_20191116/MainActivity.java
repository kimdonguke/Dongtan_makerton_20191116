package com.example.dongtan_makerton_20191116;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.telephony.SmsManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;

public class MainActivity extends AppCompatActivity {
    Button button;
    private static final String SETTINGS_PLAYER = "settings_player";
    private BluetoothSPP bt;
    String name, year, locate;
    TextView warning_textView;

    Intent intent;

    ConstraintLayout layout;
    AnimationDrawable animationDrawable;

    SharedPreferences pref1;
    SmsManager smsManager = SmsManager.getDefault();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        layout=(ConstraintLayout)findViewById(R.id.myLayout);
        animationDrawable=(AnimationDrawable)layout.getBackground();
        animationDrawable.setEnterFadeDuration(4500);
        animationDrawable.setExitFadeDuration(4500);
        animationDrawable.start();

        pref1=getSharedPreferences("image",MODE_PRIVATE);
        String image=pref1.getString("imagestrings","");
        Bitmap bitmap=StringToBitmap(image);
        warning_textView=findViewById(R.id.warning_txt);


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
            public void onDataReceived(byte[] data, String message) {// 형 이거 아두이노에서 값 받을때만 실행되는 코드라 저기 위애 다른곳으로 가야함
                SendMessage(getSettingItem("locate"),getSettingItem("year"),getSettingItem("name")," 사용자에게 충격 발생");
                warning_textView.setText("이 환자에게 이상이 생겼습니다");
                layout.setBackgroundColor(Color.parseColor("#FF0000"));
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
                if (bt.getServiceState() == BluetoothState.STATE_CONNECTED) {//빌드 가자이
                    bt.disconnect();
                } else {
                    Intent intent = new Intent(getApplicationContext(), DeviceList.class);
                    startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);//형 전송했어 받아짐?
                }
            }
        });
        year=getSettingItem("year");
        name=getSettingItem("name");
        locate=getSettingItem("locate");
        //Button testbtn;
//        testbtn=findViewById(R.id.testbtn);
//        testbtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                warning_textView.setText("이 환자에게 이상이 생겼습니다." +
//                        "가까운 기관에게 연결합니다.");
//                layout.setBackgroundColor(Color.parseColor("#FF0000"));
//            }
//        });// 이 리스너도 죽여버려야해
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode==6974){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                //퍼미션 허용 됐다 이말이야
            }
            else if(grantResults[0]==PackageManager.PERMISSION_DENIED){

            }
        }
    }//지려버렷다 이말입니다 형님
    //지금 내옆에서 자꾸 지렸다 얘기하지ㅜ 마라-찬희 진짜 크

    public void SendMessage(String where, String name, String year, String symptom){
        String phoneNum="01026510268"; //보내보셈  ㅇㅋㅇㅋ ㄱㄷㄱㄷ

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)!= PackageManager.PERMISSION_GRANTED){  //문자 보내는 권한이 없을때는 이 if문이 실행됌 밑에서 권한요청함
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.SEND_SMS},6974);

        }
        else{ //권한이 있으면 이쪽 엘스로 빠짐 여기다 문자 쳐보내면됌 히히 깃에다 처올려야징
            try {
                smsManager.sendTextMessage(phoneNum, null, "긴급상황입니다."+where+"에서 "+year+" 세 "+name+"가 "+symptom, null, null);
                Toast.makeText(getApplicationContext(), "전송 완료!", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "전송실패, 잠시 뒤에 시도하세요"+e, Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }

    }
}