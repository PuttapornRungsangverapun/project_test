package com.example.por.project_test;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

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
    ImageButton bt_reject, bt_speaker;
    String id, token, frienid, usernameFriend;
    int callId;
    Ringtone r;
    PowerManager.WakeLock mProximityWakeLock;
    byte type = 0;
    boolean speaker = false;
    TextView tv_call;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        setContentView(R.layout.activity_call_single);

        PowerManager powerManager = (PowerManager) getSystemService(CallSingleActivity.POWER_SERVICE);


        mProximityWakeLock = powerManager.newWakeLock(
                PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, "");
        mProximityWakeLock.acquire();


//        try {
//            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
//            r = RingtoneManager.getRingtone(getApplicationContext(), notification);
//            r.play();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        bt_reject = (ImageButton) findViewById(R.id.reject);

        bt_speaker = (ImageButton) findViewById(R.id.speaker);
        socketTransmitter = new SocketTransmitter("vps145.vpshispeed.net", 1234);
        socketTransmitter.start();

        SharedPreferences sp = getSharedPreferences("MySetting", MODE_PRIVATE);
        id = sp.getString("user_id_current", "-1");
        token = sp.getString("token", "-1");
        tv_call = (TextView) findViewById(R.id.tv_call);
        Intent i = getIntent();
        frienid = i.getStringExtra("friendid");
        tv_call.setText(i.getStringExtra("username_friend"));

        usernameFriend = getIntent().getStringExtra("frienduser");
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
//        Timer t = new Timer();
//        t.schedule(new TimerTask() {
//            @Override
//            public void run() {
//
//
//            }
//        }, 1000);

        bt_reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                socketTransmitter.send(1234, "reject:" + id + ":" + usernameFriend + "", CallSingleActivity.this);
//                r.stop();
                type = 123;
                try {
                    mProximityWakeLock.release();
                } catch (Throwable th) {
                }
                finish();
            }
        });

        bt_speaker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (audioTrack != null) {
                    if (!speaker) {
                        audioTrack.stop();
                        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, ENCODING, minBufferSize, AudioTrack.MODE_STREAM);
                        audioTrack.play();
                        speaker = true;
                    } else {
                        audioTrack.stop();
                        audioTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL, SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, ENCODING, minBufferSize, AudioTrack.MODE_STREAM);
                        audioTrack.play();
                        speaker = false;
                    }
                }
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

        socketTransmitter.send(1234, "request_call:" + id + ":" + frienid, CallSingleActivity.this);

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
    protected void onPause() {

        super.onPause();
    }

    @Override
    protected void onStop() {
//        if (audioTrack != null) {
//            doPlay = false;
//            doRecord = false;
//            audioTrack.stop();
//            audioTrack.release();
//            audioRecorder.stop();
//            audioRecorder.release();
//        }
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

                        if (mProximityWakeLock != null) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                try {
                                    mProximityWakeLock.release(0);
                                } catch (Throwable th) {
                                }
                            }
                        }
                        finish();
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

    private class RecordThread extends Thread {

        @Override
        public void run() {
            BufferedOutputStream bos = new BufferedOutputStream(socketTransmitter.getOutputstream());
            int n;
            byte[] data = new byte[minBufferSize*2];
            while (doRecord) {
                try {
                    n = audioRecorder.read(data, 0, data.length);
                    byte[] callId = ByteBuffer.allocate(4).putInt(CallSingleActivity.this.callId).array();
                    byte[] timeStamp = ByteBuffer.allocate(8).putLong(System.currentTimeMillis()).array();
                    byte[] type = ByteBuffer.allocate(1).put(CallSingleActivity.this.type).array();
                    byte[] length = ByteBuffer.allocate(4).putInt(n).array();
                    byte[] payLoad = new byte[n];
                    System.arraycopy(data, 0, payLoad, 0, n);
                    byte[] packet = new byte[callId.length + 6 + 1 + 2 + n];

                    int count = 0;
                    System.arraycopy(callId, 0, packet, count, callId.length);
                    count += callId.length;
                    System.arraycopy(type, 0, packet, count, type.length);
                    count += 1;
                    System.arraycopy(timeStamp, 2, packet, count, 6);
                    count += 6;
                    System.arraycopy(length, 2, packet, count, 2);
                    count += 2;
                    System.arraycopy(payLoad, 0, packet, count, payLoad.length);
                    bos.write(packet);
                    bos.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i("broken", e.getMessage());
                    if (e.getMessage().contains("Broken pipe")) {
                        break;
                    }
                }
            }

            try {
                if (audioTrack != null) {
                    doPlay = false;
                    doRecord = false;
                    audioTrack.stop();
                    audioTrack.release();
                    audioRecorder.stop();
                    audioRecorder.release();
                    mProximityWakeLock.release();
                }

                bos.close();

            } catch (Exception e) {

            }

            finish();
        }

    }

    private class PlayThread extends Thread {

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


    @Override
    public void onBackPressed() {
        return;
    }
}
