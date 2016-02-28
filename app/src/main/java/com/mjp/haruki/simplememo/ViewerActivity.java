package com.mjp.haruki.simplememo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ViewerActivity extends AppCompatActivity {

    //メンバ変数の定義
    String mFileName = "";
    String Tag = "Test";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer);


    }

    @Override
    protected void onResume(){
        super.onResume();

        //EditTextの取得
        TextView textViewTitle = (TextView) findViewById(R.id.textViewTitle);
        TextView textViewContent = (TextView) findViewById(R.id.textViewContent);

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
                Log.d(Tag, mFileName + "：読み込み開始");
                InputStream in = this.openFileInput(mFileName);
                BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                //タイトル（ファイルの1行目）を読み込み
                title = reader.readLine();
                textViewTitle.setText(title);
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

                textViewContent.setText(content);

                //ファイルクローズ
                reader.close();
                in.close();
                Log.d(Tag, mFileName + "読み込み完了");
            } catch (Exception e) {
                //ファイルオープンに失敗したら（ファイルがなければ）Activityを終了。
                Log.d(Tag, "ファイルオープンエラー");
                finish();
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_viewer, menu);
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
                this.finish();
                break;
            case R.id.action_edit:
                //編集画面を開く
                Intent intent = new Intent(ViewerActivity.this, EditorActivity.class);
                intent.putExtra("NAME", mFileName);
                startActivity(intent);
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
