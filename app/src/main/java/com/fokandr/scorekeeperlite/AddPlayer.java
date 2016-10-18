package com.fokandr.scorekeeperlite;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AddPlayer extends AppCompatActivity {
    EditText etNameNewPlayer;
    EditText etScoreNewPlayer;
    TextView tvMaxScore;
    TextView tvMinScore;
    TextView tvCountParty;
    Button btAddPlayer;
    SQLiteDatabase database;
    DBHelper dbHelper;
    int countRound;

    AlertDialog.Builder alertDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_player);

        etNameNewPlayer=(EditText) findViewById(R.id.etNameNewPlayer);
        etScoreNewPlayer=(EditText) findViewById(R.id.etScoreNewPlayer);
        etScoreNewPlayer.setHint("0");
        etScoreNewPlayer.setImeOptions(EditorInfo.IME_ACTION_DONE);/*Кнопка DONE на клавиатуре*/

        tvMaxScore=(TextView) findViewById(R.id.tvMaxScore);
        tvMinScore=(TextView) findViewById(R.id.tvMinScore);
        tvCountParty=(TextView) findViewById(R.id.tvCountParty);

        btAddPlayer=(Button) findViewById(R.id.btAddPlayer);

        btAddPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addPlayerToDb();
            }
        });

        dbHelper = new DBHelper(this);
        database = dbHelper.getWritableDatabase();

        setTextParametr();
        alertDialog = new AlertDialog.Builder(this);


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

    private void setTextParametr(){
        Cursor curRound = database.rawQuery("SELECT MAX("+dbHelper.KEY_ROUND_NUM+") FROM "+dbHelper.TABLE_ROUND,null);
        if (curRound.moveToFirst()){
            countRound = curRound.getInt(0);
        } else {
            countRound = 0;//Такого быть не может. Сделано для инициализации переменной
        }
        curRound.close();
        String helpText;
        if(countRound==1){
            helpText=" "+countRound+" "+getString(R.string.MNFAddRound).toLowerCase()+".";
        } else if (countRound<5) {
            helpText=" "+countRound+" "+getString(R.string.MNFAddRound).toLowerCase()+getString(R.string.endOf_A)+".";
        } else {
            helpText=" "+countRound+" "+getString(R.string.MNFAddRound).toLowerCase()+getString(R.string.endOf_OV)+".";
        }
        tvCountParty.setText(getString(R.string.addPlayer_tv_countRnd)+helpText);

        Cursor curPlayer = database.rawQuery("SELECT "+dbHelper.KEY_ID_PLAYER_IN_ROUND+
                                                ", "+dbHelper.KEY_NAME_PlAYER+
                                                ", sum("+dbHelper.KEY_SCORE+") SUMSCORE" +
                                                    " FROM "+dbHelper.TABLE_ROUND+" r, "+dbHelper.TABLE_PLAYER+" p "+
                                                    " WHERE r."+dbHelper.KEY_ID_PLAYER_IN_ROUND+"=p."+dbHelper.KEY_ID_PlAYER+
                                                    " GROUP BY "+dbHelper.KEY_ID_PLAYER_IN_ROUND +", "+dbHelper.KEY_NAME_PlAYER+
                                                    " ORDER BY 3 DESC",  null);


        if (curPlayer.moveToFirst()){
            LinearLayout llAddPlInfGame = (LinearLayout) findViewById(R.id.llAddPlInfGame);
            LinearLayout llAddScore4Player = (LinearLayout) findViewById(R.id.llAddScore4Player);
            setViewVisible(llAddPlInfGame, View.VISIBLE);
            setViewVisible(llAddScore4Player, View.VISIBLE);


            int indexPlName = curPlayer.getColumnIndex(dbHelper.KEY_NAME_PlAYER);
            int indexSumScore = curPlayer.getColumnIndex("SUMSCORE");

            String maxPlname = curPlayer.getString(indexPlName);
            int maxScore = curPlayer.getInt(indexSumScore);

            curPlayer.moveToLast();
            String minPlname = curPlayer.getString(indexPlName);
            int minScore = curPlayer.getInt(indexSumScore);
            SharedPreferences settingOption = getSharedPreferences("Settings",MODE_PRIVATE);
            TextView addPlayer_tv_InfoRevers = (TextView) findViewById(R.id.addPlayer_tv_InfoRevers);
            if(!settingOption.getBoolean(SettingApp.pef_KEY_RO_FLAG,false)) {
                tvMaxScore.setText(String.format(getString(R.string.addPlayer_tv_maxScore), maxPlname, String.valueOf(maxScore)));
                tvMinScore.setText(String.format(getString(R.string.addPlayer_tv_minScore), minPlname, String.valueOf(minScore)));
                setViewVisible(addPlayer_tv_InfoRevers,View.INVISIBLE);
            }else{
                /*Тупо наоборот*/
                int roScore;
                roScore=settingOption.getInt(SettingApp.pef_KEY_RO_VALUE,1000)-maxScore;
                tvMinScore.setText(String.format(getString(R.string.addPlayer_tv_maxScore), maxPlname, String.valueOf(roScore<0?0:roScore)));
                roScore=settingOption.getInt(SettingApp.pef_KEY_RO_VALUE,1000)-minScore;
                tvMaxScore.setText(String.format(getString(R.string.addPlayer_tv_minScore), minPlname, String.valueOf(roScore<0?0:roScore)));
                setViewVisible(addPlayer_tv_InfoRevers,View.VISIBLE);
            }

        }

        curPlayer.close();

    }

    private void setViewVisible(View view, int visible) {
        if(view!=null){
            view.setVisibility(visible);
        }
    }

    public void addPlayerToDb(){
        ContentValues contentValues = new ContentValues();
        if(etNameNewPlayer.getText().length()==0){
            createAlertDialogOneButton(getResources().getString(R.string.errAddPlayerTitle),getResources().getString(R.string.errAddPlayerMessage),getResources().getString(R.string.errOkButton));
            return;
        }
        int plIDd;
        Cursor curPlayerID= database.rawQuery("SELECT MAX("+dbHelper.KEY_ID_PlAYER+") FROM "+dbHelper.TABLE_PLAYER,null);
        if(curPlayerID.moveToFirst())
        {
            plIDd=curPlayerID.getInt(0)+1;
        } else  {
            plIDd=1;
        }
        curPlayerID.close();
        contentValues.put(dbHelper.KEY_ID_PlAYER,plIDd);
        contentValues.put(dbHelper.KEY_NAME_PlAYER,etNameNewPlayer.getText().toString());
        database.insert(dbHelper.TABLE_PLAYER,null,contentValues);
        contentValues.clear();
        for (int i = 1; i <= countRound; i++) {

            contentValues.put(dbHelper.KEY_ID_PLAYER_IN_ROUND, plIDd);
            contentValues.put(dbHelper.KEY_ROUND_NUM, i);
            contentValues.put(dbHelper.KEY_SCORE, (etScoreNewPlayer.getText().length() == 0)||(i!=countRound) ? "0" : etScoreNewPlayer.getText().toString());
            database.insert(dbHelper.TABLE_ROUND, null, contentValues);
            contentValues.clear();
        }
        finish();
    }

    private void createAlertDialogOneButton(String Title, String Message, String txtButton) {
        alertDialog.setTitle(Title)
                .setMessage(Message)
                //.setIcon()
                .setPositiveButton(txtButton
                        ,
                        new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        }
                );
        alertDialog.show();
    }

}
