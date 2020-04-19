package com.vikramezhil.droidspeechexample;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mapzen.speakerbox.Speakerbox;
import com.vikramezhil.droidspeech.DroidSpeech;
import com.vikramezhil.droidspeech.OnDSListener;
import com.vikramezhil.droidspeech.OnDSPermissionsListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * Droid Speech Example Activity
 *
 * @author Vikram Ezhil
 */

public class ActivityDroidSpeech extends Activity implements OnClickListener, OnDSListener, OnDSPermissionsListener {
    public final String TAG = "ActivityDroidSpeech";
    public String data = "";
    private DroidSpeech droidSpeech;
    private TextView finalSpeechResult;
    private Speakerbox speakerbox;
    private ChatMessageAdapter mAdapter;
    private ListView mListView;

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
        String answer = "Okay here is your data: \n"+
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
        droidSpeech.retry.performClick();
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
}
