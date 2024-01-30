package com.example.reminder;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GenreAdapter extends RecyclerView.Adapter<GenreAdapter.GenreViewHolder> {
    Genre.GenreDBHelper db;
    // 表示するリマインダーリスト
    private List<Genre> genres;
    private RecyclerView recyclerView;
    private Context context;

    public GenreAdapter(Context context, RecyclerView recyclerView) {
        this.context = context;
        // DBオブジェクトを生成し、保存されているリマインダーを取得する
        db = new Genre.GenreDBHelper(context);
        this.genres = db.getAllGenres();
        this.recyclerView = recyclerView;
    }

    // RecyclerViewを初期化
    @Override
    public GenreViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // genre_list.xmlを使用する
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.genre_list, parent, false);
        // ViewHolderオブジェクト生成
        return new GenreViewHolder(v, this.genres, this, this.db);
    }

    // viewHolderの数 = genres(リスト)のサイズ
    @Override
    public int getItemCount() {
        return this.genres.size();
    }

    public void addNewGenreEditText() {
        // リマインダーオブジェクト作成
        Genre newGenre = new Genre();
        // GenreAdapterのgenresに追加・長さ取得
        genres.add(newGenre);
        int newPosition = genres.size() - 1;
        // 指定した位置の項目が新しく挿入されたことを通知する -> https://learn.microsoft.com/ja-jp/xamarin/android/user-interface/layouts/recycler-view/parts-and-functionality
        notifyItemInserted(newPosition);
        // ビューに追加されたEditTextにカーソルを合わせる
        focusOnNewGenreEditText(newPosition);
    }

    @Override
    public void onBindViewHolder(GenreViewHolder holder, int position) {
        // position(データの位置)から指定されたGenreを受け取る
        Genre genre = genres.get(position);
        // それぞれにbindメソッドの特性を追加する
        holder.bind(genre, db);
    }

    // 新規追加のEditTextにカーソルを合わせる
    private void focusOnNewGenreEditText(int position) {
        // postメソッド ->  キューに処理を追加
        new Handler().post(() -> {
            // recyclerViewからpositionの中身を取り出す
            RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(position);
            // ↑で生成したviewHolderがGenreViewHolderクラスか判定
            if (viewHolder instanceof GenreViewHolder) {
                // ViewHolder型のviewHolderをGenreViewHolder型に変更し、editTextインスタンスの参照を受け渡す
                EditText editText = ((GenreViewHolder) viewHolder).editText;
                // EditTextにカーソルを合わせる
                editText.requestFocus();
            }
        });
    }

    public static class GenreViewHolder extends RecyclerView.ViewHolder {
        // オートセーブ用Handlerと削除用Handlerを生成
        /*
        Handler: 特定のスレッドにRunnable(実行可能なコード)を送る -> バックグラウンドスレッドからUIを更新するため
        参考 -> https://re-engines.com/2019/12/19/%E3%80%90java%E3%80%91handler%E3%82%AF%E3%83%A9%E3%82%B9%E3%81%AB%E3%81%A4%E3%81%84%E3%81%A6%E3%81%BE%E3%81%A8%E3%82%81%E3%81%A6%E3%81%BF%E3%81%BE%E3%81%97%E3%81%9F/
         */
        private final Handler autoSaveHandler = new Handler();
        private final Handler deleteHandler = new Handler();
        public RadioButton radioButton;
        public EditText editText;
        public TextView textView;
        private View view;


        public GenreViewHolder(View v, List<Genre> genres, GenreAdapter adapter, Genre.GenreDBHelper db) {
            super(v);
            this.view = v;
            //EditTextを取得
            editText = v.findViewById(R.id.etGenre);
            textView = v.findViewById(R.id.tvGenre);

                /*textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // TextViewがクリックされた時の処理をここに追加
                        Log.d("Text", "onClick: ");
                    }
                });*/

            if (radioButton != null) {  // Null チェックを追加
                radioButton.setOnClickListener(view -> {
                    Runnable deleteRunnable = null;
                    // 連続で押されたときのHandlerをキャンセル(リムーブ)
                    if (deleteRunnable != null) {
                        deleteHandler.removeCallbacks(deleteRunnable);
                    } else {
                        // 削除用Runnableを定義
                        deleteRunnable = () -> {
                            // 押されたラジオボタンのpositionを取得
                            int position = getAdapterPosition();
                            // positionからReminderを取得
                            Genre genre = genres.get(position);
                            db.deleteGenre(genre.getId());  // reminderのIDでdbから削除する
                            genres.remove(position);                 // remindersリストから削除する
                            adapter.notifyItemRemoved(position);        // リストの変更をadapterに通知する -> Viewから消える
                        };
                        deleteHandler.postDelayed(deleteRunnable, 2000);    // 2秒後に削除する
                    }
                });
            }
        }

        public void bind(Genre genre, Genre.GenreDBHelper db) {
            // タイトルを表示する
            editText.setText(genre.getTitle());
            textView.setText(genre.getTitle());
            // TextWatcherを実装する
            TextWatcher textWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                // 変更中・変更後処理を実装
                @Override
                public void afterTextChanged(Editable s) {
                    Runnable autoSaveRunnable = null;
                    // 入力された文字から空か判断
                    if (!s.toString().trim().isEmpty()) {
                        // 直前のautoSaveRunnableが存在していればautoSaveRunnableをキャンセル
                        if (autoSaveRunnable != null) {
                            autoSaveHandler.removeCallbacks(autoSaveRunnable);
                        }
                        // オートセーブを実装
                        autoSaveRunnable = () -> {
                            genre.setTitle(s.toString().trim());
                            if (genre.getId() == 0) {
                                int newId = db.insertNewGenre(genre);
                                genre.setId(newId);
                            } else {
                                db.updateGenre(genre);
                            }
                        };

                        textView.setText(s.toString().trim()); // 入力が完了したらTextViewに反映

                        // 3秒後に実行
                        autoSaveHandler.postDelayed(autoSaveRunnable, 3000);
                    }
                    textView.setText(s.toString().trim());
                }
            };


            // EditTextのリスナーにtextWatcherをセット
            editText.addTextChangedListener(textWatcher);


            textView.setOnClickListener(v -> {
                // TextViewがクリックされた時の処理をここに追加
                // 例: クリックされたGenreの情報を表示する、編集画面に遷移するなど

                /*
                Bundle bundle = new Bundle();
            bundle.putInt("genre_id", 1);
            intent.putExtras(bundle);
                 */

                Log.d("View", "onClick: ");
                Context context = v.getContext();
                Intent intent = new Intent(context, ReminderActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("genre_id", genre.getId());
                intent.putExtras(bundle);
                context.startActivity(intent);
            });

        }
    }
}
