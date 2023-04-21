package step.learning.course;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class ChatActivity extends AppCompatActivity {

    private final String CHAT_URL = "https://diorama-chat.ew.r.appspot.com/story";
    private EditText etAuthor;
    private EditText etMessage;
    private TextView tvChat;

    private List<ChatMessage> chatMessages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        etAuthor = findViewById(R.id.chat_et_author);
        etMessage = findViewById(R.id.chat_et_message);
        tvChat = findViewById(R.id.tv_chat);
        findViewById(R.id.chat_button_send).setOnClickListener(this::sendMessageClick);

        new Thread(this::getChatMessages).start();
    }

    private void getChatMessages() {

        try(InputStream chatStream = new URL(CHAT_URL).openStream()) {

            ByteArrayOutputStream byteBuilder = new ByteArrayOutputStream();
            byte[] chunk = new byte[4096];
            int len;

            while((len = chatStream.read(chunk)) != -1) {
                byteBuilder.write(chunk, 0, len);
            }

            parseChatMessages(byteBuilder.toString());

            byteBuilder.close();
        }
        catch(android.os.NetworkOnMainThreadException ignored) {
            Log.d("getChatMessages", "NetworkOnMainThreadException");
        }
        catch (MalformedURLException ex) {
            Log.d("getChatMessages", "MalformedURLException " + ex.getMessage());
        }
        catch (IOException ex) {
            Log.d("getChatMessages", "IOException " + ex.getMessage());
        }
    }

    private void parseChatMessages(String loadedContent) {

        try {

            JSONObject content = new JSONObject(loadedContent);

            if(content.has("data")) {

                JSONArray data = content.getJSONArray("data");
                int len = data.length();

                for(int i = 0; i < len; i++) {

                   chatMessages.add(
                           new ChatMessage(data.getJSONObject(i))
                   );
                }

                chatMessages.sort(new ChatDateComparator());
            }
            else {
                Log.d("parseChatMessages", "Content has no 'data' " + loadedContent);
            }
        }
        catch (JSONException ex) {
            Log.d("parseChatMessages", ex.getMessage());
        }

        runOnUiThread(this::showChatMessages);
    }

    private void showChatMessages() {

        StringBuilder sb = new StringBuilder();

        for(ChatMessage message : chatMessages) {
            sb.append(message.getAuthor())
                    .append(" : ")
                    .append(message.getTxt())
                    .append(" : ")
                    .append(message.getMoment())
                    .append('\n');
        }

        tvChat.setText(sb.toString());
    }

    private void sendMessageClick(View view) {

    }

    private static class ChatMessage {

        private UUID id;
        private String author;
        private String txt;
        private Date moment;
        private UUID idReply;
        private String replyPreview;

        private static final SimpleDateFormat chatMomentFormat = new SimpleDateFormat("MMM dd, yyyy KK:mm:ss a", Locale.US);
        public ChatMessage() {
        }

        public ChatMessage(JSONObject jsonObject) throws JSONException {

            this.setId(UUID.fromString(jsonObject.getString("id")));
            this.setAuthor(jsonObject.getString("author"));
            this.setTxt(jsonObject.getString("txt"));

            try {
                this.setMoment(chatMomentFormat.parse(jsonObject.getString("moment")));
            }
            catch (ParseException ex) {
                throw new JSONException("Moment parse error: " + ex.getMessage());
            }

            if(jsonObject.has("idReply")) {
                this.setIdReply(UUID.fromString(jsonObject.getString("idReply")));
            }

            if(jsonObject.has("replyPreview")) {
                this.setReplyPreview(jsonObject.getString("replyPreview"));
            }
        }

        // region Accessors

        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public String getTxt() {
            return txt;
        }

        public void setTxt(String txt) {
            this.txt = txt;
        }

        public Date getMoment() {
            return moment;
        }

        public void setMoment(Date moment) {
            this.moment = moment;
        }

        public UUID getIdReply() {
            return idReply;
        }

        public void setIdReply(UUID idReply) {
            this.idReply = idReply;
        }

        public String getReplyPreview() {
            return replyPreview;
        }

        public void setReplyPreview(String replyPreview) {
            this.replyPreview = replyPreview;
        }

        // endregion
    }

    private static class ChatDateComparator implements Comparator<ChatMessage> {
        @Override
        public int compare(ChatMessage a, ChatMessage b) {
            return a.getMoment().compareTo(b.getMoment());
        }
    }
}
