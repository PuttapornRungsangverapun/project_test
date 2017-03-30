package com.example.por.project_test;

import android.*;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class CallSingleActivity extends AppCompatActivity implements SocketCallback {
    public final int SAMPLE_RATE = 5000;
    public final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private String[] permissions = {android.Manifest.permission.RECORD_AUDIO};
    private boolean permissionToRecordAccepted = false;

    private Button btRecord;
    private RelativeLayout mainLayout;

    //audio
    private AudioTrack audioTrack;
    private AudioRecord audioRecorder;
    private int minBufferSize;

    //control
    private boolean doPlay = true;
    private boolean doRecord = true;
    private PlayThread playThread;
    private RecordThread recordThread;
    SocketTransmitter socketTransmitter;
    Button bt_call, bt_receive, bt_reject, bt_speaker;
    EditText et_call;
    String id, token, frienid, usernameFriend;
    int callId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_single);
        bt_call = (Button) findViewById(R.id.call);
        et_call = (EditText) findViewById(R.id.et_call);
        bt_reject = (Button) findViewById(R.id.reject);
        bt_receive = (Button) findViewById(R.id.receive);
        bt_speaker = (Button) findViewById(R.id.speaker);
        socketTransmitter = new SocketTransmitter("u3.punyapat.org", 1234);
        socketTransmitter.start();

        SharedPreferences sp = getSharedPreferences("MySetting", MODE_PRIVATE);
        id = sp.getString("user_id_current", "-1");
        token = sp.getString("token", "-1");

        Intent i = getIntent();
        frienid = i.getStringExtra("friendid");


        usernameFriend = getIntent().getStringExtra("frienduser");
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
        bt_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                socketTransmitter.send(1234, "request_call:" + id + ":" + frienid, CallSingleActivity.this);
            }
        });
        bt_reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                socketTransmitter.send(1234, "reject:" + id + ":" + usernameFriend + "", CallSingleActivity.this);
            }
        });
        bt_receive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                socketTransmitter.send(1234, "accept:" + id + ":" + usernameFriend + "", CallSingleActivity.this);
                startStreaming();
            }
        });
        bt_speaker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                audioTrack.stop();
                audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, ENCODING, minBufferSize, AudioTrack.MODE_STREAM);
                audioTrack.play();
            }
        });
    }

    private void init() {


        minBufferSize = AudioTrack.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, ENCODING);
        audioTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL, SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, ENCODING, minBufferSize, AudioTrack.MODE_STREAM);
        audioTrack.play();

        playThread = new PlayThread();
        audioRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, ENCODING, minBufferSize);
        recordThread = new RecordThread();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
        }

        if (!permissionToRecordAccepted) {
            Toast.makeText(this, "Need audio permission.", Toast.LENGTH_SHORT).show();
            finish();
        }

        init();
    }

    @Override
    protected void onStop() {
        if (audioTrack != null) {
            doPlay = false;
            doRecord = false;
            audioTrack.stop();
            audioTrack.release();
            audioRecorder.stop();
            audioRecorder.release();
        }
        super.onStop();
    }

    @Override
    public void onSocketResult(int requestId, String result) {
        if ((requestId == 1234) && (result.startsWith("waiting"))) {

            callId = Integer.parseInt(result.split(":")[2]);
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(CallSingleActivity.this, "Calling", Toast.LENGTH_SHORT).show();
                }
            });

            socketTransmitter.read(1, this);
        } else if (requestId == 1) {
            if (result.startsWith("reject")) {
                this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CallSingleActivity.this, "Reject calling", Toast.LENGTH_SHORT).show();
                    }
                });

            } else if (result.startsWith("start")) {
                this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CallSingleActivity.this, "Accept calling", Toast.LENGTH_SHORT).show();
                        startStreaming();
                    }
                });

            }
        }
        Log.i("123", requestId + "," + result);
    }

    private void startStreaming() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    playThread.start();
                    recordThread.start();
                    audioRecorder.startRecording();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    class RecordThread extends Thread {

        @Override
        public void run() {
            BufferedOutputStream bos = new BufferedOutputStream(socketTransmitter.getOutputstream());
            int n;
            byte[] data = new byte[minBufferSize];
            while (doRecord) {
                try {
                    n = audioRecorder.read(data, 0, data.length);
                    byte[] callId = ByteBuffer.allocate(4).putInt(CallSingleActivity.this.callId).array();
                    byte[] timeStamp = ByteBuffer.allocate(8).putLong(System.currentTimeMillis()).array();
                    byte[] type = ByteBuffer.allocate(1).put((byte) 1).array();
                    byte[] length = ByteBuffer.allocate(4).putInt(n).array();
                    byte[] payLoad = new byte[n];
                    System.arraycopy(data, 0, payLoad, 0, n);
                    byte[] packet = new byte[callId.length + 6 + 1+ 2 + n];

                    int count = 0;
                    System.arraycopy(callId, 0, packet, count, callId.length);
                    count += callId.length;
                    System.arraycopy(type, 0, packet, count, type.length);
                    count += 1;
                    System.arraycopy(timeStamp, 2, packet, count,6);
                    count += 6;
                    System.arraycopy(length, 2, packet, count, 2);
                    count += 2;
                    System.arraycopy(payLoad, 0, packet, count, payLoad.length);
                    bos.write(packet);
                    bos.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            try {
                bos.close();
            } catch (Exception e) {
            }
        }

    }

    class PlayThread extends Thread {

        @Override
        public void run() {
            BufferedInputStream bis = new BufferedInputStream(socketTransmitter.getInputstream());
            int n;
            byte data[] = new byte[minBufferSize];
            while (doPlay) {
                try {
                    n = bis.read(data);
                    audioTrack.write(data, 0, n);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            try {
                bis.close();
            } catch (IOException e) {
            }
        }

    }
}
