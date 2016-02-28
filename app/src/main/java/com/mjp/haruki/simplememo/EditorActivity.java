package com.mjp.haruki.simplememo;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class EditorActivity extends AppCompatActivity {

    private boolean mNotSave = false;
    private String mFileName = "";
    private String tmp = "";
    String Tag = "Test";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        //ファイルを読み込む処理
        //EditTextの取得
        EditText eTxtTitle = (EditText) findViewById(R.id.eTxtTitle);
        EditText eTxtContent = (EditText) findViewById(R.id.eTxtContent);

        //メイン画面からファイル名の受け取り
        Intent intent = getIntent();
        String name = intent.getStringExtra("NAME");
        //ファイル名があれば、ファイルを開く
        // （"追加"で編集画面を開いた場合はnullが届く）
        if (name != null) {
            mFileName = name;
            String title;
            String content = "";

            //ファイルを読み込み
            try {
                //ファイルオープン
                InputStream in = this.openFileInput(mFileName);
                BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                //タイトル（ファイルの1行目）を読み込み
                title = reader.readLine();
                Log.d(Tag, title);
                if (!title.equals(getString(R.string.non_title))) {
                    //"無題"でなければテキストセット
                    eTxtTitle.setText(title);
                }
                //内容（2行目~4行目）を読み込み
                String con;
                int j = 0;
                while (true) {
                    //一行ずつ取得する無限ループ
                    con = reader.readLine();
                    if (con == null) {
                        //取得した行がnull（ファイルの終わり）ならばループ終了
                        break;
                    }
                    if (j != 0) {
                        content += "\n";
                    }
                    content += con;
                    j++;
                }

                eTxtContent.setText(content);


                //ファイルクローズ
                reader.close();
                in.close();
            } catch (Exception e) {
                Toast.makeText(this, R.string.readError,Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //ファイル保存処理
        if (mNotSave) {
            return;
        }
        //ファイル保存処理メソッド
        Log.d(Tag, "保存処理メソッド開始");

        //タイトル・内容を取得
        EditText eTxtTitle = (EditText) findViewById(R.id.eTxtTitle);
        EditText eTxtContent = (EditText) findViewById(R.id.eTxtContent);
        String title = eTxtTitle.getText().toString();
        String content = eTxtContent.getText().toString();

        if (title.isEmpty() && content.isEmpty()) {
            //タイトル・内容が空白の場合、保存しない
            return;
        } else if (title.isEmpty()) {
            //タイトルのみ空白の場合、タイトル："無題"として保存
            title = getString(R.string.non_title);
        } else if (content.isEmpty()) {
            //内容のみ空白の場合、そのまま保存
        }

        //ファイル名を生成　ファイル名：yyyyMMdd_HHmmssSSS.txt
        //既に保存されているファイルは、ファイル名を書き換えて元のファイルを消す
        if (!mFileName.isEmpty()){
            //元のファイル名をtmpに代入しておく
            tmp = mFileName.toString();
        }
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.JAPAN);
        mFileName = sdf.format(date) + ".txt";

        //保存
        OutputStream out = null;
        PrintWriter writer = null;
        try {
            out = this.openFileOutput(mFileName, Context.MODE_PRIVATE);
            writer = new PrintWriter(new OutputStreamWriter(out, "UTF-8"));
            //タイトル
            writer.println(title);
            //内容
            writer.print(content);
            writer.close();
            out.close();
            //元のファイルを削除
            if (!tmp.isEmpty()) {
                this.deleteFile(tmp);
            }
        } catch (Exception e) {
            Toast.makeText(this, R.string.saveError, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_del:
                //ファイル削除
                if (!mFileName.isEmpty()) {
                    this.deleteFile(mFileName);
                }
                Toast.makeText(this, R.string.msg_del, Toast.LENGTH_SHORT).show();
                //保存せずに画面を閉じる
                mNotSave = true;
                this.finish();
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

}
