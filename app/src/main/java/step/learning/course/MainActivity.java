package step.learning.course;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = findViewById(R.id.button);
        button.setOnClickListener(this::buttonClick);

        Button button2 = findViewById(R.id.button2);
        button2.setOnClickListener(this::button2Click);
    }

    private void buttonClick(View view) {
        TextView textHello = findViewById(R.id.text_hello);
        String txt = textHello.getText().toString();
        txt += "!";
        textHello.setText(txt);
    }

    private void button2Click(View view) {
        TextView textHello = findViewById(R.id.text_hello);
        String txt = textHello.getText().toString();

        if(txt.charAt(txt.length() - 1) != '!'){
            return;
        }

        txt = txt.substring(0, txt.length() - 1);
        textHello.setText(txt);
    }
}