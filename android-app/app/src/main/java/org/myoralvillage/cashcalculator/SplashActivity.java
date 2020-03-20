package org.myoralvillage.cashcalculator;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.content.Intent;

import android.widget.Button;
import android.widget.ImageView;

import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    boolean exit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);
        mainActivityButtonListener();
        settingButtonListener();
    }

    private void mainActivityButtonListener() {
        ImageView setting = findViewById(R.id.main);
        setting.setOnClickListener((e) -> {
            exit = true;
            switchToMain();
        });
    }

    private void switchToMain(){
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private static void setDefaultImage(Button setting){
        String systemLangauge = Locale.getDefault().getCountry();
        switch (systemLangauge){
            case "KE":
                setting.setBackgroundResource(R.drawable.kes);
                break;
            default:
                setting.setBackgroundResource(R.drawable.pkr);
                break;
        }
    }

    private void settingButtonListener() {
        Button setting = findViewById(R.id.setting);

        setDefaultImage(setting);
        setting.setOnClickListener((e) -> {
            exit = true;
            switchToSetting();
        });
    }

    private void switchToSetting(){
        startActivity(new Intent(this, SettingActivity.class));
        finish();
    }
}
