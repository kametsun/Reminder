package com.example.reminder;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ReminderActivity extends AppCompatActivity {
    private ReminderAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);

        // RecyclerViewのセットアップ -> https://developer.android.com/guide/topics/ui/layout/recyclerview?hl=ja
        // データを指定し、各アイテムの表示方法を定義すると、必要に応じてRecyclerViewに格納する
        RecyclerView recyclerView = findViewById(R.id.rvReminders);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // ReminderAdapterを生成
        adapter = new ReminderAdapter(this, recyclerView);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btAddToDo).setOnClickListener(v -> adapter.addNewReminderEditText());
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}