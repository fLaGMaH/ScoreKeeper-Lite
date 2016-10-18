package com.fokandr.scorekeeperlite;

import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

public class DeletePlayers extends AppCompatActivity implements View.OnClickListener{

    TableLayout table4Delete;
    SQLiteDatabase database;
    DBHelper dbHelper;
    int maxIdPlayer;
    int defaultTextColor;
    Button btOkDelete;
    Button btCancelDel;
    HashMap<Integer, String> playersForDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_players);

        /*Для определения цвета текста по-Default. бОльше не для чего :)*/
        TextView tvDefault = new TextView(this);
        defaultTextColor = tvDefault.getTextColors().getDefaultColor();

        playersForDelete= new HashMap<Integer, String>();

        table4Delete = (TableLayout) findViewById(R.id.table4Delete);
        dbHelper=new DBHelper(this);
        database=dbHelper.getWritableDatabase();
        Cursor curPlayer = database.query(dbHelper.TABLE_PLAYER,null,null,null,null,null,dbHelper.KEY_ID_PlAYER);
        Cursor curMaxId = database.rawQuery("select max("+dbHelper.KEY_ID_PlAYER+") from "+dbHelper.TABLE_PLAYER,null);
        if(curMaxId.moveToFirst()){
            maxIdPlayer=curMaxId.getInt(0);
        } else {maxIdPlayer=100;}
        if (curPlayer.moveToFirst()) {
        drawPlayerForDelete(curPlayer);
        } else {finish();}

        curMaxId.close();
        curPlayer.close();


        btOkDelete = (Button) findViewById(R.id.btOkDelete);
        btCancelDel = (Button) findViewById(R.id.btCancelDel);

        btOkDelete .setOnClickListener(this);
        btCancelDel.setOnClickListener(this);

    }

    private void drawPlayerForDelete(Cursor curPlayer) {
        int count=0;
        TableRow rowAdd = new TableRow(this);
        do {
            count++;
            String playerName = curPlayer.getString(curPlayer.getColumnIndex(dbHelper.KEY_NAME_PlAYER));
            int playerID = curPlayer.getInt(curPlayer.getColumnIndex(dbHelper.KEY_ID_PlAYER));
            CheckBox checkBoxForDelete = new CheckBox(this);

            /*Очень хитрый план - присваивать CheckBox.ID - ID игрока
            * В дальнейшем вставлять в БД раунд для игрока с ID = Id.editText'а*/
            checkBoxForDelete.setId(playerID);
            checkBoxForDelete.setChecked(false);
            checkBoxForDelete.setPadding(10,10,10,10);

            final TextView tvPlayerName = new TextView(this);
            tvPlayerName.setText(playerName);

            /*Для изменения текста соответствующей textView*/

            setClickListnerForTvCheck(tvPlayerName,checkBoxForDelete);

            TableRow.LayoutParams parTV = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);

            parTV.rightMargin = 20;
            parTV.weight=2;
            parTV.setMargins(0,0,0,10);
            tvPlayerName.setLayoutParams(parTV);
            tvPlayerName.setMaxLines(1);
            tvPlayerName.setGravity(Gravity.CENTER_VERTICAL);




            rowAdd.addView(checkBoxForDelete);
            rowAdd.addView(tvPlayerName);

            if (count % 2 ==0){
            table4Delete.addView(rowAdd);
                rowAdd = new TableRow(this);
                } else if (count == curPlayer.getCount()){
                rowAdd.addView(new TextView(this));
                rowAdd.addView(new TextView(this));
                table4Delete.addView(rowAdd);
            }

        } while (curPlayer.moveToNext());
    }

    private void setClickListnerForTvCheck(final TextView tvPlayerName, final CheckBox checkBoxForDelete) {

        checkBoxForDelete.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    tvPlayerName.setTextColor(Color.RED);
                    tvPlayerName.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                    playersForDelete.put(checkBoxForDelete.getId(), tvPlayerName.getText().toString());
                } else {
                    tvPlayerName.setTextColor(defaultTextColor);
                    tvPlayerName.setPaintFlags(0);
                    playersForDelete.remove(checkBoxForDelete.getId());
                }
            }
        });

        tvPlayerName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkBoxForDelete.setChecked(!checkBoxForDelete.isChecked());
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btOkDelete:
                deletePlayers();
                break;
            case R.id.btCancelDel:
                finish();
                break;
        }
    }


    public void deletePlayers() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        String message = "";
        String cond = "";
        for (Map.Entry<Integer, String> pl4Del : playersForDelete.entrySet()) {
            message += pl4Del.getValue() + ", ";
            cond += pl4Del.getKey() + ",";
        }
        message = message.substring(0, message.length() - 2);
        cond = "(" + cond.substring(0, cond.length() - 1) + ")";
        final String condition = cond;//Для использования при удалении
        alertDialog.setTitle(getString(R.string.menu_delPlayersTitleDialog))
                .setMessage(message)
                //.setIcon()
                .setPositiveButton(getString(R.string.txtDelete)
                        ,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                database.delete(dbHelper.TABLE_PLAYER, dbHelper.KEY_ID_PlAYER + " in " + condition, null);
                                database.delete(dbHelper.TABLE_ROUND, dbHelper.KEY_ID_PLAYER_IN_ROUND + " in " + condition, null);
                                finish();
                            }
                        }
                )
                .setNegativeButton(getString(R.string.txtCancel)
                        ,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });
        alertDialog.show();
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
