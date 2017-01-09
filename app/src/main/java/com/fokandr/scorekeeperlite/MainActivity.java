package com.fokandr.scorekeeperlite;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    DBHelper dbHelper;
    SQLiteDatabase database;
    TableLayout layName;
    TableLayout layScore;
    TableLayout layTotalScore;

    Button btAddPart;

    SharedPreferences settingOption;

    AlertDialog.Builder alertDialog;
    ArrayList<String> winsPlayer;

    TextView tvHeaderTotalScore;
    TextView tvHeaderScore;
    TextView tvHeaderName;
    TextView tvInfoRevers;
    Cursor curPlayer;

    LinearLayout llHeader;

    Menu menu;

    int maxRound;

    @Override
    protected void onResume() {

        super.onResume();
        drawTable();
        getWindow().
                getDecorView().
                setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.curPlayer.close();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(R.layout.activity_main);
         /*Определяем для работы с БД*/
        dbHelper = new DBHelper(this);
        database = dbHelper.getWritableDatabase();
        /*Определяем настройки игры*/
        settingOption = getSharedPreferences("Settings",MODE_PRIVATE);
        btAddPart = (Button)findViewById(R.id.btAddPart);
        llHeader = (LinearLayout) findViewById(R.id.llHeader);

        /*Игроки-победители*/
        winsPlayer = new ArrayList<String>();

        alertDialog = new AlertDialog.Builder(this);



    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);

        menu.findItem(R.id.addPlayer_settings).setTitle(getString(R.string.menu_addPlayers).toString());
        this.menu = menu;
        setVisibleMenu(menu, maxRound);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        ArrayList<String> execSQL=new ArrayList<String>();
        switch(item.getItemId()) {
            case R.id.play_settings:
                Intent intent = new Intent(this, SettingApp.class);
                startActivity(intent);
                return true;
            case R.id.addRound_setting:
            addRoundForAll();
            return true;
            case R.id.addPlayer_settings:
            if(this.curPlayer.getCount()==0){
                    addManyPlayers();
                }else{
                    addPlayer();
                }
            return true;
            case R.id.delPlayer_settings:
                Intent intentDel = new Intent(this, DeletePlayers.class);
                startActivity(intentDel);
                return true;
            case R.id.delLastRound_settings:
                execSQL.add("delete from "+dbHelper.TABLE_ROUND+" where "+dbHelper.KEY_ROUND_NUM+"= "+maxRound);
                createAlertDialogTwoButton(getString(R.string.main_delRound_Title),getString(R.string.main_delRound_Message),execSQL);
                return true;
            case R.id.clear_settings:
                execSQL.add("DELETE FROM "+dbHelper.TABLE_ROUND);
                createAlertDialogTwoButton(getString(R.string.main_clear_Title),getString(R.string.main_clear_Message),execSQL);
                return true;
            case R.id.drop_all:
                execSQL.add("DELETE FROM "+dbHelper.TABLE_ROUND+"; ");
                execSQL.add("DELETE FROM "+dbHelper.TABLE_PLAYER+";");
                createAlertDialogTwoButton(getString(R.string.main_dropAll_Title),getString(R.string.main_dropAll_Message),execSQL);
                return true;
            case R.id.about:
                Intent intentAbout = new Intent(this, About.class);
                startActivity(intentAbout);;
                return true;
            case R.id.exit_settings:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void drawTable(){
        winsPlayer.clear();
        layName = (TableLayout) findViewById(R.id.layName);
        layScore = (TableLayout) findViewById(R.id.layScore);
        layTotalScore = (TableLayout) findViewById(R.id.layTotalScore);


        layName.removeAllViews();
        layScore.removeAllViews();
        layTotalScore.removeAllViews();

        this.curPlayer = database.query(dbHelper.TABLE_PLAYER,null,null,null,null,null,dbHelper.KEY_ID_PlAYER);
        int liderScore=0;
        int loserScore=99999;
        getButtonPref(this.curPlayer.getCount());
        maxRound=0;
        if (this.curPlayer.moveToFirst()) {
            drawHeader();
            int idPlayerIndex = this.curPlayer.getColumnIndex(dbHelper.KEY_ID_PlAYER);
            int namePlayerIndex = this.curPlayer.getColumnIndex(dbHelper.KEY_NAME_PlAYER);
            do {
                /*Добавляем имя игрока на экран*/
                String playerName = this.curPlayer.getString(namePlayerIndex);
                createTVForName(playerName);

                /*Обрабатываем данные игрока*/
                int idPlayer = this.curPlayer.getInt(idPlayerIndex); //ID Игрока из верхнего уровня
                int totalScore=0;                               //Всего очков за все райнды для Игрока
                ArrayList<Integer> scoreArr= new ArrayList<Integer>();

                Cursor curPlRounds = database.query(dbHelper.TABLE_ROUND,null,dbHelper.KEY_ID_PLAYER_IN_ROUND+"="+idPlayer,null,null,null,dbHelper.KEY_ROUND_NUM);

                if (curPlRounds.moveToFirst()) {
                    int RoundNumIndex = curPlRounds.getColumnIndex(dbHelper.KEY_ROUND_NUM);
                    int RoundScoreIndex = curPlRounds.getColumnIndex(dbHelper.KEY_SCORE);
                    do {
                        scoreArr.add(curPlRounds.getInt(RoundScoreIndex));  //Формирование курсора
                        totalScore += curPlRounds.getInt(RoundScoreIndex);  //Расчет ИТОГО:
                        maxRound=curPlRounds.getInt(RoundNumIndex);//Расчет максимального раунда, который можно будет удалить
                    }
                    while (curPlRounds.moveToNext());
                    createTVForScore(scoreArr);
                    /*В зависимости от того в какую игру играем - по убыванию очков или по возрастанию*/
                    totalScore=getTotalScore(totalScore,playerName);
                    if (totalScore>liderScore){liderScore=totalScore;}
                    if (totalScore<loserScore){loserScore=totalScore;}
                    createTVForTotal(totalScore);
                }
                curPlRounds.close();

            }
            while (curPlayer.moveToNext());
        }else{
            llHeader.setVisibility(View.INVISIBLE);
        }
        if(winsPlayer.size()>0){createAlertDialogForWins();}

        if (this.menu!=null){
            setVisibleMenu(this.menu, maxRound);
        }

        tvInfoRevers = (TextView) findViewById(R.id.tvInfoRevers);
        if(settingOption.getBoolean(SettingApp.pef_KEY_RO_FLAG,false)){
            tvInfoRevers.setVisibility(View.VISIBLE);
        }else tvInfoRevers.setVisibility(View.INVISIBLE);

        if(liderScore!=0||loserScore!=99999){
            markerLider(liderScore,loserScore);
        }

    }

    private void markerLider(int liderScore, int loserScore) {
        ArrayList<View> tv = new ArrayList<View>();
        int liderColor=getResources().getColor(R.color.colorLiderScore);
        int looserColor=getResources().getColor(R.color.colorLoserScore);
        if(settingOption.getBoolean(SettingApp.pef_KEY_RO_FLAG,false)){
            /*Просто меняем местами переменные*/
            looserColor=getResources().getColor(R.color.colorLiderScore);
            liderColor=getResources().getColor(R.color.colorLoserScore);
        }

        layTotalScore.findViewsWithText(tv,String.valueOf(loserScore),View.FIND_VIEWS_WITH_TEXT);
        for (View v:tv ) {
            /*Устранение бага - нахождение вхождений меньшего количества очков в большее (например 20 в 320 или 10 в 1000)
            * используем дополнительную проверку, что нашли именно ту View*/
            if(  ((TextView)v).getText().equals(String.valueOf(loserScore))
                    ){((TextView)v).setTextColor(looserColor);}
        }
        tv.clear();
        layTotalScore.findViewsWithText(tv,String.valueOf(liderScore),View.FIND_VIEWS_WITH_TEXT);
        for (View v:tv ) {
            if(  ((TextView)v).getText().equals(String.valueOf(liderScore))
                    ){
                ((TextView)v).setTextColor(liderColor);}
        }
    }

    private void drawHeader() {

        llHeader.setVisibility(View.VISIBLE);
        tvHeaderName = (TextView) findViewById(R.id.tvHeaderName);
        tvHeaderName.setText(getString(R.string.main_tvHead_name).toString());

        tvHeaderScore = (TextView) findViewById(R.id.tvHeaderScore);
        tvHeaderScore.setText(getString(R.string.main_tvHead_Score).toString());

        tvHeaderTotalScore = (TextView) findViewById(R.id.tvHeaderTotalScore);

        if(settingOption.getBoolean(SettingApp.pef_KEY_RO_FLAG,false)){
        tvHeaderTotalScore.setText(getString(R.string.main_tvHead_total_desc).toString());
        }else{tvHeaderTotalScore.setText(getString(R.string.main_tvHead_total).toString());}

        tvHeaderName.setTextSize(17);
        tvHeaderScore.setTextSize(17);
        tvHeaderTotalScore.setTextSize(17);
    }

    private void getButtonPref(int countPlayers) {
        if (countPlayers==0)
        {/*Надо добавить игроков*/
            btAddPart.setText(getString(R.string.menu_addPlayers).toString());
            btAddPart.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    addManyPlayers();
                }
            });
        }
        else{
            /*Надо добавить раунды*/
            btAddPart.setText(getString(R.string.main_bt_newPart).toString());
            btAddPart.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    addRoundForAll();
                }
            });
        }
    }

    private void addRoundForAll(){

        if (this.curPlayer.moveToFirst()) {
            Intent intent = new Intent(this, AddRound.class);
            startActivity(intent);
        }else{
             createAlertDialogOneButton(getResources().getString(R.string.errADPleersTitle),getResources().getString(R.string.errADPleersMessage),getResources().getString(R.string.errOkButton));
        }

    }
    private void addPlayer(){
            Intent intent = new Intent(this, AddPlayer.class);
            startActivity(intent);
    }
    private void addManyPlayers(){
            Intent intent = new Intent(this, AddManyPlayers.class);
            startActivity(intent);
    }

    private void createTVForName(String namePlayer){

        TableRow rowName = new TableRow(this);
        TextView tvName = new TextView(this);
        TableRow.LayoutParams paramsExample = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,TableRow.LayoutParams.WRAP_CONTENT);
        paramsExample.rightMargin=20;
        tvName.setLayoutParams(paramsExample);
        tvName.setMaxLines(1);
        tvName.setText(namePlayer.toUpperCase());
       // tvName.setTypeface(null, Typeface.BOLD);
        tvName.setTextSize(15);
        rowName.addView(tvName);
        rowName.setMinimumHeight(80);
        rowName.setGravity(Gravity.CENTER_VERTICAL);
        layName.addView(rowName);
    }
    private void createTVForScore(ArrayList<Integer> scoreArr){
        TableRow rowScore = new TableRow(this);

        TableRow.LayoutParams paramsExample = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,TableRow.LayoutParams.WRAP_CONTENT);
        rowScore.setLayoutParams(paramsExample);
        rowScore.isScrollContainer();
        rowScore.setMinimumHeight(80);
        rowScore.setGravity(Gravity.CENTER_VERTICAL);


        for (int score:scoreArr
             ) {
            TextView tvScore = new TextView(this);
            tvScore.setMaxLines(1);
            tvScore.setGravity(Gravity.CENTER);
            tvScore.setText(score==0?"-":String.valueOf(score));
            tvScore.setPadding(20,0,20,0);
            rowScore.addView(tvScore);
        }

        layScore.addView(rowScore);
    }
    private void createTVForTotal(int TotalScore){
        TableRow rowTotalScore = new TableRow(this);
        TextView tvTotalScore = new TextView(this);
        TableRow.LayoutParams paramsExample = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,TableRow.LayoutParams.MATCH_PARENT);
        rowTotalScore.setLayoutParams(paramsExample);
        tvTotalScore.setLayoutParams(paramsExample);

        tvTotalScore.setGravity(Gravity.CENTER_VERTICAL);
        tvTotalScore.setMaxLines(1);
        tvTotalScore.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);

        tvTotalScore.setText(String.valueOf(TotalScore));
        //tvTotalScore.setTypeface(null, Typeface.BOLD);
        tvTotalScore.setTextSize(17);

        tvTotalScore.setTextColor(getResources().getColor(R.color.colorTotalScore));

        rowTotalScore.setGravity(Gravity.RIGHT);
        rowTotalScore.addView(tvTotalScore);
        rowTotalScore.setMinimumHeight(80);

        layTotalScore.addView(rowTotalScore);

    }

    private int getTotalScore(int totalScoreIn,String playerName){
        int res_value;
        if(settingOption.getBoolean(SettingApp.pef_KEY_RO_FLAG,false)){
            res_value = settingOption.getInt(SettingApp.pef_KEY_RO_VALUE,1000) - totalScoreIn;
            res_value = res_value<0?0:res_value;
            if (settingOption.getBoolean(SettingApp.pef_KEY_MAXSCORE_FLAG,false)&& //поднят флаг о завершении игры по достижению очков
                settingOption.getInt(SettingApp.pef_KEY_MAXSCORE_VALUE,0)>=res_value){
                winsPlayer.add(playerName);
            }
        }
         else{
            res_value=totalScoreIn;
            if (settingOption.getBoolean(SettingApp.pef_KEY_MAXSCORE_FLAG,false)&& //поднят флаг о завершении игры по достижению очков
                settingOption.getInt(SettingApp.pef_KEY_MAXSCORE_VALUE,0)<=res_value){
                winsPlayer.add(playerName);
            }
        }
        return res_value;
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
                )
                .setNegativeButton(null,null)

        ;
        alertDialog.show();
    }

    public void createAlertDialogForWins() {
        if(winsPlayer.size()==1){
            createAlertDialogOneButton(getString(R.string.errWinsPlayerOne)+winsPlayer.get(0),getString(R.string.errWinsPrizMessageOne),getString(R.string.txtOk));
        } else if(winsPlayer.size()>1){
            String winsPlayerName="";
            for (String a:winsPlayer) {
                winsPlayerName+=a+", ";
            }
            winsPlayerName=winsPlayerName.substring(0,winsPlayerName.length()-2);
            createAlertDialogOneButton(getString(R.string.errWinsPlayerMany)+winsPlayerName,getString(R.string.errWinsPrizMessageMany),getString(R.string.txtOk));
        }
    }

    private void createAlertDialogTwoButton(String title,String message, final ArrayList<String> dbexecSQL) {
        alertDialog.setTitle(title)
                .setMessage(message)
                //.setIcon()
                .setPositiveButton(getString(R.string.txtOk)
                        ,
                        new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                for (String execSQL:dbexecSQL
                                     ) {
                                    database.execSQL(execSQL);
                                }
                                drawTable();
                            }
                        }
                )
                .setNegativeButton(getString(R.string.txtCancel)
                                        ,
                                new DialogInterface.OnClickListener(){
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.cancel();
                                    }
                                });
        alertDialog.show();
    }

    public void setVisibleMenu(Menu menu, int flagHaveRound) {
        MenuItem clear_settings = menu.findItem(R.id.clear_settings);
        MenuItem drop_all = menu.findItem(R.id.drop_all);
        MenuItem delPlayer_settings = menu.findItem(R.id.delPlayer_settings);
        MenuItem delLastRound_settings = menu.findItem(R.id.delLastRound_settings);

        clear_settings.setVisible(false);
        drop_all.setVisible(false);
        delPlayer_settings.setVisible(false);
        delLastRound_settings.setVisible(false);

        if (curPlayer.moveToFirst()){
          if(flagHaveRound==0){
              drop_all.setVisible(true);
              delPlayer_settings.setVisible(true);
          }else{
            drop_all.setVisible(true);
            delPlayer_settings.setVisible(true);
            clear_settings.setVisible(true);
            delLastRound_settings.setVisible(true);
          }
        }


    }
}
