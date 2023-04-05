package step.learning.course;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class CalcActivity extends AppCompatActivity {

    private TextView tvHistory;
    private TextView tvResult;

    String minusSign;
    String zeroSign;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calc);

        minusSign = getString(R.string.calc_minus_sign);
        zeroSign = getString(R.string.calc_btn_0_text);

        tvHistory = findViewById(R.id.tv_history);
        tvResult = findViewById(R.id.tv_result);

        tvHistory.setText("");
        displayResult("");

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
        String result = tvResult.getText().toString();
        result = result.substring(0, result.length() - 1);
        displayResult(result);
    }

    private void digitClick(View view){

        String result = tvResult.getText().toString();

        if(result.length() >= 10){
            return;
        }

        String digit = ((Button)view).getText().toString();

        if( result.equals("0") ) {
            result = "";
        }

        result += digit;
        displayResult(result);
    }

    private void displayResult(String result){

        if("".equals(result) || minusSign.equals(result)){
            result = getString(R.string.calc_btn_0_text);
        }

        tvResult.setText(result);
    }
}