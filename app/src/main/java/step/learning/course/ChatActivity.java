package step.learning.course;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public class ChatActivity extends AppCompatActivity {

    private final String CHAT_URL = "https://diorama-chat.ew.r.appspot.com/story";
    private final String CHANNEL_ID = "chat_notification_channel";
    private final int POST_NOTIFICATION_REQUEST_CODE = 234;
    private EditText etAuthor;
    private EditText etMessage;
    private LinearLayout chatContainer;

    private ScrollView svContainer;
    private List<ChatMessage> chatMessages = new ArrayList<>();
    private MediaPlayer incomingMessagePlayer;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        handler = new Handler();

        etAuthor = findViewById(R.id.chat_et_author);
        etMessage = findViewById(R.id.chat_et_message);
        chatContainer = findViewById(R.id.chat_container);
        svContainer = findViewById(R.id.sv_Container);

        incomingMessagePlayer = MediaPlayer.create(this, R.raw.sound_1);

        findViewById(R.id.chat_button_send).setOnClickListener(this::sendMessageClick);

        handler.post(this::updateChat);
    }

    private void updateChat() {

        new Thread(this::getChatMessages).start();

        handler.postDelayed(this::updateChat, 3000);
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
            boolean wasNewMessage = false;

            if(content.has("data")) {

                JSONArray data = content.getJSONArray("data");
                int len = data.length();

                for(int i = 0; i < len; i++) {

                    ChatMessage tmp = new ChatMessage(data.getJSONObject(i));

                    if(this.chatMessages.stream().noneMatch(msg -> msg.getId().equals(tmp.getId()))) {

                        chatMessages.add(tmp);
                        wasNewMessage = true;
                    }
                }

                if(wasNewMessage) {
                    //chatMessages.sort(new ChatDateComparator());
                    //chatMessages.sort((m1, m2) -> m1.getMoment().compareTo(m2.getMoment()));
                    chatMessages.sort(Comparator.comparing(ChatMessage::getMoment));
                    runOnUiThread(this::showChatMessages);
                }
            }
            else {
                Log.d("parseChatMessages", "Content has no 'data' " + loadedContent);
            }
        }
        catch (JSONException ex) {
            Log.d("parseChatMessages", ex.getMessage());
        }
    }

    private void showChatMessages() {

        Drawable otherBg = AppCompatResources.getDrawable(getApplicationContext(), R.drawable.chat_msg_bg_other);
        Drawable myBg = AppCompatResources.getDrawable(getApplicationContext(), R.drawable.chat_msg_bg_my);

        LinearLayout.LayoutParams marginOther = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        marginOther.setMargins(10, 10, 10, 10);
        marginOther.gravity = Gravity.START;

        LinearLayout.LayoutParams marginMy = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        marginMy.setMargins(10, 10, 10, 10);
        marginMy.gravity = Gravity.END;

        boolean wasNewMessage = false;

        for(ChatMessage message : chatMessages) {

            if(message.getView() != null) {
                continue;
            }

            TextView tvMessage = new TextView(ChatActivity.this);
            tvMessage.setText(message.toViewString());
            tvMessage.setTag(message);      // связываем View с ChatMessage
            message.setView(tvMessage);

            if(message.getAuthor().equals(etAuthor.getText().toString())) {
                tvMessage.setBackground(myBg);
                tvMessage.setLayoutParams(marginMy);
            }
            else {
                tvMessage.setBackground(otherBg);
                tvMessage.setLayoutParams(marginOther);
            }

            tvMessage.setTextSize(18);
            tvMessage.setPadding(10, 7, 10, 7);

            chatContainer.addView(tvMessage);

            wasNewMessage = true;
        }

        if(wasNewMessage) {

            svContainer.post(() -> svContainer.fullScroll(View.FOCUS_DOWN));

            AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

            if(am.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
                incomingMessagePlayer.start();
            }

            showNotification();
        }
    }

    private void sendMessageClick(View view) {

        new Thread(this::postChatMessage).start();
    }

    private void postChatMessage() {

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setTxt(etMessage.getText().toString());
        chatMessage.setAuthor(etAuthor.getText().toString());

        try {

            URL chatUrl = new URL(CHAT_URL);

            HttpURLConnection connection = (HttpURLConnection)chatUrl.openConnection();
            connection.setDoOutput(true);   // будет иметь тело (output)
            connection.setDoInput(true);    // будет иметь возврат (response)
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "*/*");
            connection.setChunkedStreamingMode(0); // не разделять на чанки (фрагменты)

            OutputStream body = connection.getOutputStream();
            body.write(chatMessage.toJsonString().getBytes());
            body.close();

            int responseCode = connection.getResponseCode();

            if(responseCode >= 400) {
                Log.e("postChatMessage", "responseCode = " + responseCode);
                return;
            }

            InputStream response = connection.getInputStream();

            ByteArrayOutputStream byteBuilder = new ByteArrayOutputStream();
            byte[] chunk = new byte[4096];
            int len;

            while((len = response.read(chunk)) != -1) {
                byteBuilder.write(chunk, 0, len);
            }

            String responseText = byteBuilder.toString();
            Log.d("postChatMessage", responseText);

            byteBuilder.close();
            response.close();
            connection.disconnect();

            new Thread(this::getChatMessages).start();
        }
        catch (Exception ex) {
            Log.e("postChatMessage", ex.getMessage());
        }
    }

    private void showNotification() {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.chat_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            channel.setDescription(getString(R.string.chat_channel_description));

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(ChatActivity.this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.btn_star_big_on)
                .setContentTitle("Chat")
                .setContentText("New incoming message")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        Notification notification = notificationBuilder.build();

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        //managerCompat.notify(105, notification);  // Missing permission

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        ChatActivity.this,
                        new String[] {
                                android.Manifest.permission.POST_NOTIFICATIONS
                        },
                        POST_NOTIFICATION_REQUEST_CODE
                );

                return;
            }
        }

        managerCompat.notify(105, notification);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == POST_NOTIFICATION_REQUEST_CODE) {

        }
    }

    private static class ChatMessage {

        private UUID id;
        private String author;
        private String txt;
        private Date moment;
        private UUID idReply;
        private String replyPreview;

        private View view;

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

        public String toJsonString() {

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("{\"author\": \"%s\", \"txt\": \"%s\"", this.getAuthor(), this.getTxt()));

            if(idReply != null) {
                sb.append(String.format(", \"idReply\": \"%s\"", this.getIdReply()));
            }

            sb.append("}");

            return sb.toString();
        }

        public String toViewString() {

            return String.format("%s: %s", this.getAuthor(), this.getTxt());
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

        public View getView() {
            return view;
        }

        public void setView(View view) {
            this.view = view;
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
