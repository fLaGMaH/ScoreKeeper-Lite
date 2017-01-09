package com.fokandr.scorekeeperlite;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;

public class SettingApp extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    Switch swReversOrder;
    Switch swMaxScore;
    Switch swNegativNum;
    public final static String pef_KEY_RO_FLAG = "ReversOrderFlag";
    public final static String pef_KEY_RO_VALUE = "ReversOrderValue";
    public final static String pef_KEY_MAXSCORE_FLAG = "FinalScoreFlag";
    public final static String pef_KEY_MAXSCORE_VALUE = "FinalScoreValue";
    public final static String pef_KEY_NEGATIVE_NUM = "ApplyNegativNum";
    SharedPreferences settingOption;

    LinearLayout llMaxScore;
    EditText etSettingMaxValue;
    LinearLayout llStartValue;
    EditText etStartValue;
    Button btSetOk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_app);

        settingOption = getSharedPreferences("Settings", MODE_PRIVATE);

        swReversOrder = (Switch) findViewById(R.id.swReversOrder);
        swMaxScore = (Switch) findViewById(R.id.swMaxScore);
        swNegativNum = (Switch) findViewById(R.id.swNegativNum);
        llStartValue = (LinearLayout) findViewById(R.id.llStartValue);
        llMaxScore = (LinearLayout) findViewById(R.id.llMaxScore);
        etStartValue = (EditText) findViewById(R.id.etSettingStartValue);
        etSettingMaxValue = (EditText) findViewById(R.id.etSettingMaxValue);
        btSetOk = (Button) findViewById(R.id.btSetOk);
        etStartValue.setHint(String.valueOf(settingOption.getInt(pef_KEY_RO_VALUE, 0)));
        etSettingMaxValue.setHint(String.valueOf(settingOption.getInt(pef_KEY_MAXSCORE_VALUE, 0)));

        /*Первоначальные значения*/
        swReversOrder.setChecked(settingOption.getBoolean(pef_KEY_RO_FLAG, false));
        swMaxScore.setChecked(settingOption.getBoolean(pef_KEY_MAXSCORE_FLAG, false));
        swNegativNum.setChecked(settingOption.getBoolean(pef_KEY_NEGATIVE_NUM, false));
        llStartValue.setVisibility(swReversOrder.isChecked() ? View.VISIBLE : View.INVISIBLE);
        llMaxScore.setVisibility(swMaxScore.isChecked() ? View.VISIBLE : View.INVISIBLE);
        /*Назначаем Листнеры*/
        swReversOrder.setOnCheckedChangeListener(this);
        swMaxScore.setOnCheckedChangeListener(this);
        swNegativNum.setOnCheckedChangeListener(this);

        btSetOk.setOnClickListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch (compoundButton.getId()) {
            case R.id.swReversOrder:
                llStartValue.setVisibility(swReversOrder.isChecked() ? View.VISIBLE : View.INVISIBLE);
                swReversOrder.setTypeface(swReversOrder.isChecked() ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
                if (swReversOrder.isChecked()) {
                    etStartValue.requestFocus();
                } else etStartValue.clearFocus();
                break;
            case R.id.swMaxScore:
                llMaxScore.setVisibility(swMaxScore.isChecked() ? View.VISIBLE : View.INVISIBLE);
                swMaxScore.setTypeface(swMaxScore.isChecked() ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
                if (swMaxScore.isChecked()) {
                    etSettingMaxValue.requestFocus();
                } else etSettingMaxValue.clearFocus();
                break;
            case R.id.swNegativNum:
                swNegativNum.setTypeface(swNegativNum.isChecked() ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
                break;
        }
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.btSetOk:
                SharedPreferences.Editor prefEditor = settingOption.edit();
                prefEditor.putBoolean(pef_KEY_RO_FLAG, swReversOrder.isChecked());
                prefEditor.putBoolean(pef_KEY_MAXSCORE_FLAG, swMaxScore.isChecked());
                prefEditor.putBoolean(pef_KEY_NEGATIVE_NUM, swNegativNum.isChecked());
                if (swReversOrder.isChecked()) {
                    int roOver;
                    roOver = etStartValue.getText().length() > 0 ? Integer.parseInt(etStartValue.getText().toString()) : Integer.parseInt(etStartValue.getHint().toString());
                    prefEditor.putInt(pef_KEY_RO_VALUE, roOver);
                }
                if (swMaxScore.isChecked()) {
                    int maxScore;
                    maxScore = etSettingMaxValue.getText().length() > 0 ? Integer.parseInt(etSettingMaxValue.getText().toString()) : Integer.parseInt(etSettingMaxValue.getHint().toString());
                    prefEditor.putInt(pef_KEY_MAXSCORE_VALUE, maxScore);
                }
                prefEditor.apply();
                finish();
                break;
        }

    }
}
