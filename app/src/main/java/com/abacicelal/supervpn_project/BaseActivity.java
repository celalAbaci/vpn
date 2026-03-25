package com.abacicelal.supervpn_project;

import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;

import com.abacicelal.supervpn_project.util.LocaleManager;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.applyLocale(newBase));
    }
}
