package step.learning.course;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
public class CalcActivity extends AppCompatActivity {

    private TextView tvHistory;
    private TextView tvResult;

    String minusSign;
    String zeroSign;
    String commaSign;
    private boolean needClear;
    int lengthCountWithoutComa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calc);

        minusSign = getString(R.string.calc_minus_sign);
        zeroSign = getString(R.string.calc_btn_0_text);
        commaSign = getString(R.string.calc_comma_sign);

        tvHistory = findViewById(R.id.tv_history);
        tvResult = findViewById(R.id.tv_result);

        lengthCountWithoutComa = 0;

        clearClick(null);

        for(int i = 0; i < 10; i++){

            @SuppressLint("DiscouragedApi")
            int buttonId = getResources().getIdentifier(
                    "calc_btn_" + i,
                    "id",
                    getPackageName()
            );

            findViewById(buttonId).setOnClickListener(this::digitClick);
        }

        findViewById(R.id.calc_btn_backspace).setOnClickListener(this::backspaceClick);
        findViewById(R.id.calc_btn_plus_minus).setOnClickListener(this::plusMinusClick);
        findViewById(R.id.calc_btn_comma).setOnClickListener(this::commaClick);
        findViewById(R.id.calc_btn_clear).setOnClickListener(this::clearClick);
        findViewById(R.id.calc_btn_ce).setOnClickListener(this::clearEditClick);
        findViewById(R.id.calc_btn_square).setOnClickListener(this::squareClick);
        findViewById(R.id.calc_btn_sqrt).setOnClickListener(this::sqrtClick);
        findViewById(R.id.calc_btn_inverse).setOnClickListener(this::inverseClick);
    }

    private void sqrtClick(View view) {

        String result = tvResult.getText().toString();

        double arg;

        try {

            arg = Double.parseDouble(
                    result
                            .replace( minusSign, "-" )
                            .replaceAll( zeroSign, "0" )
            ) ;
        }
        catch(NumberFormatException | NullPointerException ignored) {

            Toast.makeText(
                            this,
                            R.string.calc_error_parse,
                            Toast.LENGTH_SHORT )
                    .show();
            return;
        }

        Vibrator vibrator;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            VibratorManager vibratorManager = (VibratorManager)getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            vibrator = vibratorManager.getDefaultVibrator();
        }
        else {
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        }

        long[] vibratePattern = { 0, 200, 100, 200 };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            vibrator.vibrate(
                    VibrationEffect.createWaveform(
                            vibratePattern, -1 // индекс повтора, -1 - без повторов, один раз
                    )
            );
        }
        else {
            //vibrator.vibrate(250); // вибрация 250 мс
            vibrator.vibrate(vibratePattern, -1);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle savingState) {

        // savingState - ~словарь сохраняющихся данных
        super.onSaveInstanceState(savingState);  // Оставить, нужно обязательно
        Log.d("CalcActivity", "onSaveInstanceState");

        // добавляем к сохраняемым данным свои значения
        savingState.putCharSequence( "history", tvHistory.getText() ) ;
        savingState.putCharSequence( "result", tvResult.getText() ) ;
    }


    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedState) {

        super.onRestoreInstanceState(savedState);
        Log.d("CalcActivity", "onRestoreInstanceState");

        tvHistory.setText( savedState.getCharSequence("history"));
        tvResult.setText( savedState.getCharSequence("result" ));
    }

    private void inverseClick(View view) {

        String result = tvResult.getText().toString();
        double arg;
        try {
            arg = Double.parseDouble(
                    result
                            .replace(minusSign, "-")
                            .replaceAll(zeroSign, "0")
                            .replace(commaSign, ".")
            );
        }
        catch (NumberFormatException | NullPointerException ignored) {
            Toast.makeText(
                            this,
                            R.string.calc_error_parse,
                            Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        tvHistory.setText("1/" + result + " =");
        arg = 1 / arg;

        displayResult(arg);
        needClear = true;
    }

    private void squareClick(View view) {

        String result = tvResult.getText().toString();
        double arg;

        try {

            arg = Double.parseDouble(
                    result
                            .replace( minusSign, "-" )
                            .replaceAll( zeroSign, "0" )
            ) ;
        }
        catch(NumberFormatException | NullPointerException ignored) {

            Toast.makeText(
                            this,
                            R.string.calc_error_parse,
                            Toast.LENGTH_SHORT )
                    .show();
            return;
        }

        tvHistory.setText(getString(R.string.calc_square_history, result));

        arg *= arg ;
        displayResult(arg) ;

        needClear = true ;
    }

    private void clearClick(View view) {

        tvHistory.setText( "" ) ;
        clearEditClick(view);
    }

    private void clearEditClick(View view) {

        lengthCountWithoutComa = 0;
        displayResult("") ;
    }

    private void commaClick(View view) {

        String result = tvResult.getText().toString();

        if (result.contains(commaSign) || lengthCountWithoutComa >= 10 ) {
            return;
        }

        result += commaSign;
        displayResult(result);
    }

    private void plusMinusClick(View view){

        String result = tvResult.getText().toString();
        String minusSign = getString(R.string.calc_minus_sign);

        if(result.startsWith(zeroSign)){
            return;
        }

        if (result.startsWith(minusSign)){

            result = result.substring(1);
        }
        else{
            result = minusSign + result;
        }

        displayResult(result);
    }

    private void backspaceClick(View view){

        if (needClear) {
            needClear = false;
            clearClick(view);
            return;
        }

        String result = tvResult.getText().toString();

        if(!result.endsWith(commaSign)) {
            lengthCountWithoutComa--;
        }

        result = result.substring(0, result.length() - 1);

        displayResult(result);
    }

    private void digitClick(View view){

        String result = tvResult.getText().toString();

        if(result.equals(zeroSign) || needClear) {

            result = "" ;
            needClear = false;
            lengthCountWithoutComa = 0;
        }

        if(lengthCountWithoutComa >= 10){
            return;
        }

        String digit = ((Button)view).getText().toString();

        lengthCountWithoutComa += 1;
        result += digit;

        displayResult(result);
    }

    private void displayResult(String result){

        if("".equals(result) || minusSign.equals(result)){
            result = getString(R.string.calc_btn_0_text);
        }

        tvResult.setText(result);
    }

    private void displayResult(double arg) {

        long argInt = (long)arg ;
        String result = argInt == arg ? "" + argInt : "" + arg ;

        result = result
                .replace( "-", minusSign )
                .replaceAll( "0", zeroSign ) ;

        displayResult( result ) ;
    }
}