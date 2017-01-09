package com.fokandr.scorekeeperlite;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class AddRound extends AppCompatActivity {

    Button btAddToDB;
    int nextRoundNum;
    DBHelper dbHelper;
    TableLayout table4Add;
    Cursor curPlayer;
    SQLiteDatabase database;

    @Override
    protected void onPause() {
        super.onPause();
        this.curPlayer.close();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_round);

        table4Add=(TableLayout)findViewById(R.id.table4Add);
        dbHelper=new DBHelper(this);
        database=dbHelper.getWritableDatabase();

        curPlayer = database.query(dbHelper.TABLE_PLAYER,null,null,null,null,null,dbHelper.KEY_ID_PlAYER);
        if (curPlayer.moveToFirst()) {
            Cursor curRound = database.rawQuery("SELECT MAX("+dbHelper.KEY_ROUND_NUM+") FROM "+dbHelper.TABLE_ROUND,null);
            if(curRound!=null){
                curRound.moveToFirst();
                nextRoundNum=curRound.getInt(0)+1;
                curRound.close();
            }else{nextRoundNum=1;}

            drawTable4Add(curPlayer);

            btAddToDB = (Button) findViewById(R.id.btAddToDB);
            btAddToDB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    addToDB(curPlayer);
                    finish();
                }
            });
        }else{
            /*Создать AlertDialog*/
            finish();
        }


    }

    private void drawTable4Add(Cursor curPlayer){
        do{
            String playerName = curPlayer.getString(curPlayer.getColumnIndex(dbHelper.KEY_NAME_PlAYER));
            int playerID= curPlayer.getInt(curPlayer.getColumnIndex(dbHelper.KEY_ID_PlAYER));
            EditText etPlayerScore = new EditText(this);

            /*Очень хитрый план - присваивать EditText.ID - ID игрока
            * В дальнейшем вставлять в БД раунд для игрока с ID = Id.editText'а*/
            etPlayerScore.setId(playerID);
            etPlayerScore.setHint("0");
            etPlayerScore.setImeOptions(EditorInfo.IME_ACTION_DONE);/*Кнопка DONE на клавиатуре*/

            TextView tvPlayerName = new TextView(this);
            tvPlayerName.setText(playerName);


            TableRow.LayoutParams parTV = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,TableRow.LayoutParams.MATCH_PARENT);
            parTV.weight=2;
            parTV.rightMargin=20;
            tvPlayerName.setLayoutParams(parTV);
            tvPlayerName.setMaxLines(1);
            tvPlayerName.setGravity(Gravity.CENTER_VERTICAL);
            TableRow.LayoutParams parET = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,TableRow.LayoutParams.WRAP_CONTENT);
            parET.weight=1;
            etPlayerScore.setLayoutParams(parET);
            etPlayerScore.setMaxLines(1);

            if(!getSharedPreferences("Settings",MODE_PRIVATE).getBoolean(SettingApp.pef_KEY_NEGATIVE_NUM,false))
                //Вводим только положительные числа
                etPlayerScore.setInputType(InputType.TYPE_CLASS_NUMBER);
                else
                //Разрешаме вводить отрицательные числа
                etPlayerScore.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_SIGNED);




            TableRow rowAdd = new TableRow(this);
            rowAdd.addView(tvPlayerName);
            rowAdd.addView(etPlayerScore);

            table4Add.addView(rowAdd);

        } while(curPlayer.moveToNext());
    }

    void addToDB(Cursor curPlayer){
        ContentValues contentValues = new ContentValues();
        EditText etPlayerScore;
        curPlayer.moveToFirst();
        do{
            int playerID = curPlayer.getInt(curPlayer.getColumnIndex(DBHelper.KEY_ID_PlAYER));
            etPlayerScore = (EditText) findViewById(playerID);
            contentValues.put(DBHelper.KEY_ID_PLAYER_IN_ROUND,playerID);
            contentValues.put(DBHelper.KEY_ROUND_NUM, nextRoundNum);
            int score = etPlayerScore.getText().length()==0?0:Integer.parseInt(etPlayerScore.getText().toString());
            contentValues.put(DBHelper.KEY_SCORE,score);
            database.insert(DBHelper.TABLE_ROUND,null,contentValues);
            contentValues.clear();

        }
        while(curPlayer.moveToNext());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_cancel,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_cancel:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
