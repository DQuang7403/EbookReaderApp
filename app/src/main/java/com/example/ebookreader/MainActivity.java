package com.example.ebookreader;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.VolumeShaper;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.ebookreader.databinding.ActivityMainBinding;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Login
        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });

        //skipBtn
        binding.skipBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, DashboardUserActivity.class));
            }
        });
        binding.viLangBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Locale newLocale = new Locale("vi");
                Context context = MyContextWrapper.wrap(getApplicationContext(), newLocale);
                Resources resources = context.getResources();
                Configuration configuration = resources.getConfiguration();
                ContextCompat.getMainExecutor(context).execute(new Runnable() {
                    @Override
                    public void run() {
                        getResources().updateConfiguration(configuration, resources.getDisplayMetrics());
                        recreate();
                    }
                });
            }
        });
        binding.frLangBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Locale newLocale = new Locale("fr");
                Context context = MyContextWrapper.wrap(getApplicationContext(), newLocale);
                Resources resources = context.getResources();
                Configuration configuration = resources.getConfiguration();
                ContextCompat.getMainExecutor(context).execute(new Runnable() {
                    @Override
                    public void run() {
                        getResources().updateConfiguration(configuration, resources.getDisplayMetrics());
                        recreate();
                    }
                });
            }
        });
        binding.enLangBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Locale newLocale = new Locale("en");
                Context context = MyContextWrapper.wrap(getApplicationContext(), newLocale);
                Resources resources = context.getResources();
                Configuration configuration = resources.getConfiguration();
                ContextCompat.getMainExecutor(context).execute(new Runnable() {
                    @Override
                    public void run() {
                        getResources().updateConfiguration(configuration, resources.getDisplayMetrics());
                        recreate();
                    }
                });
            }
        });


    }
}