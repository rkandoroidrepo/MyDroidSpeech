package com.vikramezhil.droidspeechexample;

import android.app.Activity;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mapzen.speakerbox.Speakerbox;
import com.vikramezhil.droidspeech.DroidSpeech;
import com.vikramezhil.droidspeech.OnDSListener;
import com.vikramezhil.droidspeech.OnDSPermissionsListener;

import org.alicebot.ab.AIMLProcessor;
import org.alicebot.ab.Bot;
import org.alicebot.ab.Chat;
import org.alicebot.ab.Graphmaster;
import org.alicebot.ab.MagicBooleans;
import org.alicebot.ab.MagicStrings;
import org.alicebot.ab.PCAIMLProcessorExtension;
import org.alicebot.ab.Timer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivityWithBot extends Activity implements View.OnClickListener, OnDSListener, OnDSPermissionsListener {
    public final String TAG = "ActivityDroidSpeech";
    private DroidSpeech droidSpeech;
    private Speakerbox speakerbox;
    private ChatMessageAdapter mAdapter;
    private ListView mListView;
    public Bot bot;
    public static Chat chat;
    // MARK: Activity Methods
    private ImageView start, stop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setting the layout;[.
        setContentView(R.layout.activity_main);
        speakerbox = new Speakerbox(getApplication());
        // Initializing the droid speech and setting the listener
        droidSpeech = new DroidSpeech(this, getFragmentManager(), R.id.container);
        droidSpeech.setOnDroidSpeechListener(this);
        droidSpeech.setShowRecognitionProgressView(true);
        droidSpeech.setOneStepResultVerify(true);
        droidSpeech.setRecognitionProgressMsgColor(Color.WHITE);
        droidSpeech.setOneStepVerifyConfirmTextColor(Color.WHITE);
        droidSpeech.setOneStepVerifyRetryTextColor(Color.WHITE);

        //finalSpeechResult = findViewById(R.id.finalSpeechResult);

        start = findViewById(R.id.start);
        start.setOnClickListener(this);

        stop = findViewById(R.id.stop);
        stop.setOnClickListener(this);

        mListView = findViewById(R.id.listView);
        mAdapter = new ChatMessageAdapter(this, new ArrayList<ChatMessage>(), getApplication());
        mListView.setAdapter(mAdapter);
        initBot();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (stop.getVisibility() == View.VISIBLE) {
            stop.performClick();
        }
    }

    // MARK: OnClickListener Method

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (stop.getVisibility() == View.VISIBLE) {
            stop.performClick();
        }
    }

    // MARK: DroidSpeechListener Methods

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.start:

                // Starting droid speech
                droidSpeech.startDroidSpeechRecognition();

                // Setting the view visibilities when droid speech is running
                start.setVisibility(View.GONE);
                stop.setVisibility(View.VISIBLE);

                break;

            case R.id.stop:

                // Closing droid speech
                droidSpeech.closeDroidSpeechOperations();

                stop.setVisibility(View.GONE);
                start.setVisibility(View.VISIBLE);

                break;
        }
    }

    @Override
    public void onDroidSpeechSupportedLanguages(String currentSpeechLanguage, List<String> supportedSpeechLanguages) {
        Log.i(TAG, "Current speech language = " + currentSpeechLanguage);
        Log.i(TAG, "Supported speech languages = " + supportedSpeechLanguages.toString());

        if (supportedSpeechLanguages.contains("en-IN")) {
            // Setting the droid speech preferred language as tamil if found
            droidSpeech.setPreferredLanguage("en-IN");

            // Setting the confirm and retry text in tamil
            droidSpeech.setOneStepVerifyConfirmText("confirm");
            droidSpeech.setOneStepVerifyRetryText("try again");
        }
    }

    @Override
    public void onDroidSpeechRmsChanged(float rmsChangedValue) {
        // Log.i(TAG, "Rms change value = " + rmsChangedValue);
    }

    @Override
    public void onDroidSpeechLiveResult(String liveSpeechResult) {
        Log.i(TAG, "Live speech result = " + liveSpeechResult);
    }

    @Override
    public void onDroidSpeechFinalResult(String finalSpeechResult) {
        // Setting the final speech result
        //data = data + finalSpeechResult + "\n";
        //this.finalSpeechResult.setText(data);

        if (droidSpeech.getContinuousSpeechRecognition()) {
            int[] colorPallets1 = new int[]{Color.RED, Color.GREEN, Color.BLUE, Color.CYAN, Color.MAGENTA};
            int[] colorPallets2 = new int[]{Color.YELLOW, Color.RED, Color.CYAN, Color.BLUE, Color.GREEN};

            // Setting random color pallets to the recognition progress view
            droidSpeech.setRecognitionProgressViewColors(new Random().nextInt(2) == 0 ? colorPallets1 : colorPallets2);
        } else {
            stop.setVisibility(View.GONE);
            start.setVisibility(View.VISIBLE);
        }

        mListView.setSelection(mAdapter.getCount() - 1);
        ChatMessage chatMessage = new ChatMessage(finalSpeechResult, true, false);
        mAdapter.add(chatMessage);
        mListView.setSelection(mAdapter.getCount() - 1);
        answer(finalSpeechResult);

    }

    private void answer(String command){
        String str = "";
        if(command.contains("last month")){
            str = "last month average ";
        }else if(command.contains("last week")){
            str = "last week average ";
        }
        String answer = "Okay here is your "+str +"data: \n"+
                "Calories 150\n"+
                "Distance 5 kilometer\n"+
                "Average speed: 25 kilometer per hour";

        speakerbox.play(answer);
        speakerbox.play(answer, null, new Runnable() {
            @Override
            public void run() {
                droidSpeech.startDroidSpeechRecognition();
            }
        }, null);

        ChatMessage chatMessage = new ChatMessage(answer, false, false);
        mAdapter.add(chatMessage);
        mListView.setSelection(mAdapter.getCount() - 1);
    }


    @Override
    public void onDroidSpeechClosedByUser() {
        stop.setVisibility(View.GONE);
        start.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDroidSpeechError(String errorMsg) {
        // Speech error
        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();

        stop.post(new Runnable() {
            @Override
            public void run() {
                // Stop listening
                stop.performClick();
            }
        });
    }

    @Override
    public void onDroidInvalidCommand(String invalidCommand) {
        String response = chat.multisentenceRespond(invalidCommand);
        if (TextUtils.isEmpty(invalidCommand)) {
            return;
        }
        sendMessage(invalidCommand);
        mimicOtherMessage(response);
        mListView.setSelection(mAdapter.getCount() - 1);
    }

    // MARK: DroidSpeechPermissionsListener Method

    @Override
    public void onDroidSpeechAudioPermissionStatus(boolean audioPermissionGiven, String errorMsgIfAny) {
        if (audioPermissionGiven) {
            start.post(new Runnable() {
                @Override
                public void run() {
                    // Start listening
                    start.performClick();
                }
            });
        } else {
            if (errorMsgIfAny != null) {
                // Permissions error
                Toast.makeText(this, errorMsgIfAny, Toast.LENGTH_LONG).show();
            }

            stop.post(new Runnable() {
                @Override
                public void run() {
                    // Stop listening
                    stop.performClick();
                }
            });
        }
    }

    private void initBot() {
        //checking SD card availablility
        boolean a = isSDCARDAvailable();
        //receiving the assets from the app directory
        AssetManager assets = getResources().getAssets();
        File jayDir = new File(Environment.getExternalStorageDirectory().toString() + "/hari/bots/Hari");
        boolean b = jayDir.mkdirs();
        if (jayDir.exists()) {
            //Reading the file
            try {
                for (String dir : assets.list("Hari")) {
                    File subdir = new File(jayDir.getPath() + "/" + dir);
                    boolean subdir_check = subdir.mkdirs();
                    for (String file : assets.list("Hari/" + dir)) {
                        File f = new File(jayDir.getPath() + "/" + dir + "/" + file);
                        if (f.exists()) {
                            continue;
                        }
                        InputStream in = null;
                        OutputStream out = null;
                        in = assets.open("Hari/" + dir + "/" + file);
                        out = new FileOutputStream(jayDir.getPath() + "/" + dir + "/" + file);
                        //copy file from assets to the mobile's SD card or any secondary memory
                        copyFile(in, out);
                        in.close();
                        in = null;
                        out.flush();
                        out.close();
                        out = null;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //get the working directory
        MagicStrings.root_path = Environment.getExternalStorageDirectory().toString() + "/hari";
        System.out.println("Working Directory = " + MagicStrings.root_path);
        AIMLProcessor.extension =  new PCAIMLProcessorExtension();
        //Assign the AIML files to bot for processing
        bot = new Bot("Hari", MagicStrings.root_path, "chat");
        chat = new Chat(bot);
        String[] args = null;
        mainFunction(args);
    }

    private void sendMessage(String message) {
        ChatMessage chatMessage = new ChatMessage(message, true, false);
        mAdapter.add(chatMessage);
        mListView.setSelection(mAdapter.getCount() - 1);
        //mimicOtherMessage(message);
    }

    private void mimicOtherMessage(String message) {
        ChatMessage chatMessage = new ChatMessage(message, false, false);
        mAdapter.add(chatMessage);
        speakerbox.play(message, null, new Runnable() {
            @Override
            public void run() {
                droidSpeech.retry.performClick();
            }
        }, null);
        mListView.setSelection(mAdapter.getCount() - 1);
    }

    private void sendMessage() {
        ChatMessage chatMessage = new ChatMessage(null, true, true);
        mAdapter.add(chatMessage);

        mimicOtherMessage();
    }

    private void mimicOtherMessage() {
        ChatMessage chatMessage = new ChatMessage(null, false, true);
        mAdapter.add(chatMessage);
    }
    //check SD card availability
    public static boolean isSDCARDAvailable(){
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)? true :false;
    }
    //copying the file
    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }
    //Request and response of user and the bot
    public static void mainFunction (String[] args) {
        MagicBooleans.trace_mode = false;
        System.out.println("trace mode = " + MagicBooleans.trace_mode);
        Graphmaster.enableShortCuts = true;
        Timer timer = new Timer();
        String request = "Hello.";
        String response = chat.multisentenceRespond(request);

        System.out.println("Human: "+request);
        System.out.println("Robot: " + response);
    }
}
