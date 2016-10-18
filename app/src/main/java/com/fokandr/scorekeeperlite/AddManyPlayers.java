package com.fokandr.scorekeeperlite;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AddManyPlayers extends AppCompatActivity {

    EditText etPlayer0;
    TextView tvPlayer0;
    Button btAdds;
    Button btAddsOver;
    LinearLayout llPlayers;
    LinearLayout llForOnePlayer;
    int count;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_many_players);
        etPlayer0 = (EditText) findViewById(R.id.etPlayer0);
        tvPlayer0 = (TextView) findViewById(R.id.tvPlayer0);

        count=1;

        tvPlayer0.setText(getString(R.string.addManyPls_tv_player, count));
        etPlayer0.setHint(getString(R.string.addManyPls_edHint_name));

        btAdds = (Button) findViewById(R.id.btAdds);
        btAddsOver = (Button) findViewById(R.id.btAddsOver);
        llPlayers = (LinearLayout) findViewById(R.id.llPlayers);
        llForOnePlayer = (LinearLayout) findViewById(R.id.llForOnePlayer);

        btAdds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addForm();
            }
        });
        btAddsOver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addPlayersToDB();
            }
        });
    }

    private void addForm() {
        count++;
        TextView tv = new TextView(this);
        EditText et = new EditText(this);
        LinearLayout ll = new LinearLayout(this);


        tv.setLayoutParams(tvPlayer0.getLayoutParams());
        et.setLayoutParams(etPlayer0.getLayoutParams());
        ll.setLayoutParams(llForOnePlayer.getLayoutParams());

        tv.setText(getString(R.string.addManyPls_tv_player,count));
        et.setHint(getString(R.string.addManyPls_edHint_name));
        et.setId(count);


        ll.addView(tv);
        ll.addView(et);

        llPlayers.addView(ll);

        et.requestFocus();

    }


    private void addPlayersToDB() {

        DBHelper dbHelper = new DBHelper(this);
        SQLiteDatabase database = dbHelper.getWritableDatabase();


        ContentValues contentValues = new ContentValues();

        for (int plID = 1; plID <=this.count ; plID++) {
            String playerName;
            EditText et;
            if(plID==1){
                et= (EditText) findViewById(R.id.etPlayer0);
            }else{
                et= (EditText) findViewById(plID);//WTF!?
            }

            playerName=et.getText().toString().equals("")?getString(R.string.addManyPls_tv_player, plID):et.getText().toString();

            contentValues.put(dbHelper.KEY_ID_PlAYER,plID);
            contentValues.put(dbHelper.KEY_NAME_PlAYER,playerName);
            database.insert(dbHelper.TABLE_PLAYER,null,contentValues);
            contentValues.clear();
        }

        finish();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_cancel, menu);
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
