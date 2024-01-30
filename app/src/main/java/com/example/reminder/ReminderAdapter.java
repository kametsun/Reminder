package com.example.reminder;

import android.content.Context;
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

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder> {
    Reminder.ReminderDBHelper db;
    // 表示するリマインダーリスト
    private List<Reminder> reminders;
    private RecyclerView recyclerView;
    private TextView tvComplete;
    private int genreId;

    public ReminderAdapter(Context _context, RecyclerView _recyclerView, int _genreId, TextView _tvComplete) {
        // DBオブジェクトを生成し、保存されているリマインダーを取得する
        this.db = new Reminder.ReminderDBHelper(_context);
        this.genreId = _genreId;
        this.reminders = db.getRemindersByGenreId(this.genreId);
        this.recyclerView = _recyclerView;
        this.tvComplete = _tvComplete;
        checkReminders();
    }

    // RecyclerViewを初期化
    @Override
    public ReminderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // list_item.xmlを使用する
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        // ViewHolderオブジェクト生成
        return new ReminderViewHolder(v, this.reminders, this, this.db);
    }

    public void checkReminders() {
        if (reminders.isEmpty()) {
            tvComplete.setVisibility(View.VISIBLE);
        } else {
            tvComplete.setVisibility(View.GONE);
        }
    }

    // viewHolderの数 = reminders(リスト)のサイズ
    @Override
    public int getItemCount() {
        checkReminders();
        return this.reminders.size();
    }

    public void addNewReminderEditText() {
        // リマインダーオブジェクト作成
        Reminder newReminder = new Reminder(genreId);
        // ReminderAdapterのremindersに追加・長さ取得
        reminders.add(newReminder);
        int newPosition = reminders.size() - 1;
        // 指定した位置の項目が新しく挿入されたことを通知する -> https://learn.microsoft.com/ja-jp/xamarin/android/user-interface/layouts/recycler-view/parts-and-functionality
        notifyItemInserted(newPosition);
        // ビューに追加されたEditTextにカーソルを合わせる
        focusOnNewReminderEditText(newPosition);
    }

    @Override
    public void onBindViewHolder(ReminderViewHolder holder, int position) {
        // position(データの位置)から指定されたReminderを受け取る
        Reminder reminder = reminders.get(position);
        // それぞれにbindメソッドの特性を追加する
        holder.bind(reminder, db);
    }

    // 新規追加のEditTextにカーソルを合わせる
    private void focusOnNewReminderEditText(int position) {
        // postメソッド ->  キューに処理を追加
        new Handler().post(() -> {
            // recyclerViewからpositionの中身を取り出す
            RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(position);
            // ↑で生成したviewHolderがReminderViewHolderクラスか判定
            if (viewHolder instanceof ReminderViewHolder) {
                // ViewHolder型のviewHolderをReminderViewHolder型に変更し、editTextインスタンスの参照を受け渡す
                EditText editText = ((ReminderViewHolder) viewHolder).editText;
                // EditTextにカーソルを合わせる
                editText.requestFocus();
            }
        });
    }

    public static class ReminderViewHolder extends RecyclerView.ViewHolder {
        // オートセーブ用Handlerと削除用Handlerを生成
        /*
        Handler: 特定のスレッドにRunnable(実行可能なコード)を送る -> バックグラウンドスレッドからUIを更新するため
        参考 -> https://re-engines.com/2019/12/19/%E3%80%90java%E3%80%91handler%E3%82%AF%E3%83%A9%E3%82%B9%E3%81%AB%E3%81%A4%E3%81%84%E3%81%A6%E3%81%BE%E3%81%A8%E3%82%81%E3%81%A6%E3%81%BF%E3%81%BE%E3%81%97%E3%81%9F/
         */
        private final Handler autoSaveHandler = new Handler();
        private final Handler deleteHandler = new Handler();
        public RadioButton radioButton;
        public EditText editText;


        public ReminderViewHolder(View v, List<Reminder> reminders, ReminderAdapter adapter, Reminder.ReminderDBHelper db) {
            super(v);
            // ラジオボタン・EditTextを取得
            radioButton = v.findViewById(R.id.radioButton);
            editText = v.findViewById(R.id.etToDo);

            // ラジオボタンを押したとき
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
                        Reminder reminder = reminders.get(position);
                        db.deleteReminder(reminder.getId());  // reminderのIDでdbから削除する
                        reminders.remove(position);                 // remindersリストから削除する
                        adapter.notifyItemRemoved(position);        // リストの変更をadapterに通知する -> Viewから消える
                        adapter.checkReminders();
                    };
                    deleteHandler.postDelayed(deleteRunnable, 2000);    // 2秒後に削除する
                }
            });
        }

        public void bind(Reminder reminder, Reminder.ReminderDBHelper db) {
            // タイトルを表示する
            editText.setText(reminder.getTitle());
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
                            Log.d("保存をキャンセル", "autoSaveHandler 147");
                            autoSaveHandler.removeCallbacks(autoSaveRunnable);
                        }
                        // オートセーブを実装
                        autoSaveRunnable = () -> {
                            reminder.setTitle(s.toString().trim());
                            if (reminder.getId() == 0) {
                                int newId = db.insertNewReminder(reminder);
                                reminder.setId(newId);
                            } else {
                                db.updateReminder(reminder);
                            }
                        };
                        // 1秒後に実行
                        autoSaveHandler.postDelayed(autoSaveRunnable, 1500);
                        Log.d("保存します。", "autoSaveHandler 161");
                    }
                }
            };

            // EditTextのリスナーにtextWatcherをセット
            editText.addTextChangedListener(textWatcher);
        }
    }
}
