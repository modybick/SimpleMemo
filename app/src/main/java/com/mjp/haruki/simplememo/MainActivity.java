package com.mjp.haruki.simplememo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    //メンバ変数
    SimpleAdapter mAdapter = null;
    List<Map<String, String>> mList = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //ListView用アダプタのリストを生成
        mList = new ArrayList<Map<String, String>>();

        //ListView用アダプタを生成
        mAdapter = new SimpleAdapter(
                this,
                mList,
                android.R.layout.simple_list_item_2,
                new String[] {"title", "content"},
                new int[] {android.R.id.text1, android.R.id.text2}
        );

        //ListViewにアダプターをセット
        ListView list = (ListView) findViewById(R.id.listView);
        list.setAdapter(mAdapter);

        //ListViewのアイテム選択イベント
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //参照画面に渡すデータをセットし、表示
                Intent intent = new Intent(MainActivity.this, ViewerActivity.class);
                intent.putExtra("NAME", mList.get(position).get("filename"));
                startActivity(intent);
            }
        });

        //ListViewをコンテキストメニューに登録
        registerForContextMenu(list);
    }


    @Override
    protected void onResume() {
        super.onResume();

        //ListView用アダプタのクリア
        mList.clear();

        //ファイル読み込み処理
        //アプリの保存フォルダ内のファイル一覧を取得
        String savePath = this.getFilesDir().getPath().toString();
        File[] files = new File(savePath).listFiles();
        //ファイル名の降順でソート（新しいものほど上に）
        Arrays.sort (files, Collections.reverseOrder());
        //テキストファイルを取得し、ListView用アダプタのリストにセット
        for (int i=0; i < files.length; i++) {
            String fileName = files[i].getName();
            if (files[i].isFile() && fileName.endsWith(".txt")) {
                String title = "";
                String content = "";
                //ファイルを読み込み
                try {
                    //ファイルオープン
                    InputStream in = this.openFileInput(fileName);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                    //タイトル（ファイルの1行目）を読み込み
                    title = reader.readLine();
                    //内容（2行目~4行目）を読み込み
                    String con;
                    for (int j = 0; j < 3;) {
                        //一行ずつ取得し、空白でなければcontentにセット
                        con = reader.readLine();
                        if (con == null) {
                            //取得した行がnull（ファイルの終わり）ならばループ終了
                            break;
                        }
                        if (!con.isEmpty()) {
                            if (j != 0) {
                                content += "\n";
                            }
                            content += con;
                            j++;
                        }
                    }
                    //ファイルクローズ
                    reader.close();
                    in.close();
                } catch (Exception e) {
                    Toast.makeText(this, R.string.readError,Toast.LENGTH_LONG).show();
                }

                //ListView用のアダプタにデータをセット
                Map<String, String> map = new HashMap<String, String>();
                map.put("filename", fileName);
                map.put("title", title);
                map.put("content", content);
                mList.add(map);
            }
        }

        //ListViewのデータ変更を表示に反映
        mAdapter.notifyDataSetChanged();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        switch (item.getItemId()) {
            case R.id.context_del:
                //[削除]選択時の処理
                //削除するか確認
                //ファイル削除
                if (this.deleteFile(mList.get(info.position).get("filename"))) {
                    Toast.makeText(this, R.string.msg_del,Toast.LENGTH_SHORT).show();
                }
                //リストからアイテム処理
                mList.remove(info.position);
                //ListViewのデータ変更を表示に反映
                mAdapter.notifyDataSetChanged();
                break;
            case R.id.context_edit:
                //[編集]選択時の処理
                //編集画面を開く
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                intent.putExtra("NAME", mList.get(info.position).get("filename"));
                startActivity(intent);
            default:
                break;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                //編集画面への遷移処理
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo info) {
        super.onCreateContextMenu(menu, view, info);
        getMenuInflater().inflate(R.menu.menu_context, menu);
    }

}
