package com.example.reminder;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class GenreActivity extends AppCompatActivity {

    private GenreAdapter genreAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_genre);

        // RecyclerViewをレイアウトから取得
        // RecyclerView recyclerView = findViewById(R.id.rvReminders);
        RecyclerView recyclerView = findViewById(R.id.genre_recycle);

        // RecyclerViewのレイアウトマネージャーを設定
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // GenreAdapterを初期化
        genreAdapter = new GenreAdapter(this, recyclerView);

        // RecyclerViewにAdapterをセット
        recyclerView.setAdapter(genreAdapter);

        // addButtonのClickListenerを設定
        setAddButtonClickListener();
    }

    // addButtonのClickListenerを設定するメソッド
    private void setAddButtonClickListener() {
        // 追加ボタンを取得
        Button addButton = findViewById(R.id.genre_button);

        // 追加ボタンがクリックされたときの処理
        addButton.setOnClickListener(view -> {
            // GenreAdapterに新しいGenreを追加
            genreAdapter.addNewGenreEditText();
        });
    }
}
