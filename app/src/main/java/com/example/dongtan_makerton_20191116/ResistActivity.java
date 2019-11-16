package com.example.dongtan_makerton_20191116;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;

public class ResistActivity extends AppCompatActivity {
    EditText name, year, locate;
    String image;
    private final int GET_GALLERY_IMAGE = 200;
    private ImageView imageview;
    Button button;
    private static final String SETTINGS_PLAYER = "settings_player";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resist);
        name=findViewById(R.id.res_nametxt);
        year=findViewById(R.id.res_yeartxt);
        locate=findViewById(R.id.res_locate);
        button=findViewById(R.id.res_btn);
        imageview=(ImageView)findViewById(R.id.resist_img);
        imageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent. setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent, GET_GALLERY_IMAGE);
            }
        });
        button.setOnClickListener(new View.OnClickListener() { // 이미지뷰 교채
            @Override
            public void onClick(View v) {
                imageview.buildDrawingCache();
                Bitmap bitmap=imageview.getDrawingCache();
                putSettingItem("name",name.getText().toString());
                putSettingItem("year",year.getText().toString());
                putSettingItem("locate",locate.getText().toString());
                image=BitMapToString(bitmap);
            }
        });
    }
    private void putSettingItem(String key, String value) { // 셰어드 프리페런스 씨발 아이템 저장 함수
        SharedPreferences preferences = getSharedPreferences(SETTINGS_PLAYER, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.apply();
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) { // 이미지뷰 이미지에 set
        if (requestCode == GET_GALLERY_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri selectedImageUri = data.getData();
            imageview.setImageURI(selectedImageUri);
        }

    }
    public String BitMapToString(Bitmap bitmap){ // 비트맵 문자열로 바꾸기
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,baos);
        byte [] b=baos.toByteArray();
        String temp= Base64.encodeToString(b,Base64.DEFAULT);
        return temp;
    }
}
