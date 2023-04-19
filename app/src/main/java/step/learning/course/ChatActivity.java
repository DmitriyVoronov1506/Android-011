package step.learning.course;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class ChatActivity extends AppCompatActivity {

    private EditText etAuthor ;
    private EditText etMessage ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        etAuthor = findViewById(R.id.chat_et_author);
        etMessage = findViewById(R.id.chat_et_message);
        findViewById(R.id.chat_button_send).setOnClickListener(this::sendMessageClick);
    }

    private void sendMessageClick(View view) {

    }
}