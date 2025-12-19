package com.abacicelal.supervpn_project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import androidx.appcompat.app.AppCompatActivity;

public class LanguageSelectionActivity extends AppCompatActivity {
    private ListView languageListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_selection);

        languageListView = findViewById(R.id.languageListView);

        String[] languages = {"English", "Türkçe", "Français", "Deutsch"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, languages);
        languageListView.setAdapter(adapter);

        languageListView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(LanguageSelectionActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}