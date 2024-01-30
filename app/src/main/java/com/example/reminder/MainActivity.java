package com.example.reminder;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btGenre).setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, ReminderActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt("genre_id", 1);
            intent.putExtras(bundle);
            startActivity(intent);
        });
    }
}
