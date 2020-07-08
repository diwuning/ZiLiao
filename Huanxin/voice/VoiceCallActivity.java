package com.nmpa.nmpaapp.modules.huanxin.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.RingtoneManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.hyphenate.EMError;
import com.hyphenate.chat.EMCallSession;
import com.hyphenate.chat.EMCallStateChangeListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;
import com.hyphenate.util.EMLog;
import com.hyphenate.util.PathUtil;
import com.nmpa.nmpaapp.R;
import com.nmpa.nmpaapp.modules.huanxin.record.AudioRecordImpl;
import com.nmpa.nmpaapp.modules.huanxin.record.AudioUtils;
import com.nmpa.nmpaapp.modules.huanxin.record.RecordingService;
import com.nmpa.nmpaapp.modules.huanxin.utils.DemoHelper;
import com.nmpa.nmpaapp.modules.huanxin.utils.PhoneStateManager;
import com.nmpa.nmpaapp.modules.huanxin.utils.PreferenceManager;
import com.nmpa.nmpaapp.utils.FileLoadUtils;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.UUID;

/**
 * 语音通话页面
 *
 */
 public class VoiceCallActivity extends CallActivity implements OnClickListener {
	private LinearLayout comingBtnContainer;
	private Button hangupBtn;
	private Button refuseBtn;
	private Button answerBtn;
	private ImageView muteImage;
	private ImageView handsFreeImage;
	private ImageView recordImage;
  private boolean isMuteState;
	private boolean isHandsfreeState;

	private TextView callStateTextView;
	private boolean endCallTriggerByMe = false;
	private Chronometer chronometer;
	String st1;
	private LinearLayout voiceContronlLayout;
    private TextView netwrokStatusVeiw;
    private boolean monitor = false;
    private Context mContext;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(savedInstanceState != null){
        	finish();
        	return;
        }
		setContentView(R.layout.em_activity_voice_call);
    mContext = VoiceCallActivity.this;
		DemoHelper.getInstance().isVoiceCalling = true;
		callType = 0;

        comingBtnContainer = (LinearLayout) findViewById(R.id.ll_coming_call);
		refuseBtn = (Button) findViewById(R.id.btn_refuse_call);
		answerBtn = (Button) findViewById(R.id.btn_answer_call);
		hangupBtn = (Button) findViewById(R.id.btn_hangup_call);
		muteImage = (ImageView) findViewById(R.id.iv_mute);
    recordImage = findViewById(R.id.iv_record);
		handsFreeImage = (ImageView) findViewById(R.id.iv_handsfree);
		callStateTextView = (TextView) findViewById(R.id.tv_call_state);
        TextView nickTextView = (TextView) findViewById(R.id.tv_nick);
        TextView durationTextView = (TextView) findViewById(R.id.tv_calling_duration);
		chronometer = (Chronometer) findViewById(R.id.chronometer);
    voiceContronlLayout = (LinearLayout) findViewById(R.id.ll_voice_control);
		netwrokStatusVeiw = (TextView) findViewById(R.id.tv_network_status);

		refuseBtn.setOnClickListener(this);
		answerBtn.setOnClickListener(this);
		hangupBtn.setOnClickListener(this);
		muteImage.setOnClickListener(this);
		handsFreeImage.setOnClickListener(this);
		recordImage.setOnClickListener(this);
    getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
						| WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

		addCallStateListener();
		msgid = UUID.randomUUID().toString();
    username = getIntent().getStringExtra("username");
		isInComingCall = getIntent().getBooleanExtra("isComingCall", false);
        Log.e(TAG,"isInComingCall="+isInComingCall);
		nickTextView.setText(username);
		if (!isInComingCall) {// outgoing call
			soundPool = new SoundPool(1, AudioManager.STREAM_RING, 0);
			outgoing = soundPool.load(this, R.raw.em_outgoing, 1);
      comingBtnContainer.setVisibility(View.INVISIBLE);
			hangupBtn.setVisibility(View.VISIBLE);
			st1 = getResources().getString(R.string.Are_connected_to_each_other);
			callStateTextView.setText(st1);
			handler.sendEmptyMessage(MSG_CALL_MAKE_VOICE);
            handler.postDelayed(new Runnable() {
                public void run() {
                    streamID = playMakeCallSounds();
                }
            }, 300);
  } else { // incoming call
			voiceContronlLayout.setVisibility(View.INVISIBLE);
			Uri ringUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
			audioManager.setMode(AudioManager.MODE_RINGTONE);
			audioManager.setSpeakerphoneOn(true);
			ringtone = RingtoneManager.getRingtone(this, ringUri);
			ringtone.play();
		}
    
    final int MAKE_CALL_TIMEOUT = 50 * 1000;
        handler.removeCallbacks(timeoutHangup);
        handler.postDelayed(timeoutHangup, MAKE_CALL_TIMEOUT);
        audioUtils.setOnStatusListener(new AudioUtils.OnStatusListener() {
            @Override
            public void onUpdate(double db, long time) {
                Log.e(TAG,"audioUtils.setOnStatusListener   onUpdate");
            }
            @Override
            public void onStop(String filePath, long time) {
                Log.e(TAG,"audioUtils.setOnStatusListener   onStop");
            }
        });
	}

	/**
	 * set call state listener
	 */
   void addCallStateListener() {
	    callStateListener = new EMCallStateChangeListener() {

            @Override
            public void onCallStateChanged(CallState callState, final CallError error) {
                // Message msg = handler.obtainMessage();
                EMLog.d("EMCallManager", "onCallStateChanged:" + callState);
                switch (callState) {

                case CONNECTING:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callStateTextView.setText(st1);
                        }
                    });
                    break;
                case CONNECTED:
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            String st3 = getResources().getString(R.string.have_connected_with);
                            callStateTextView.setText(st3);
                            }
                        });
                        break;

                    case ACCEPTED:
                        handler.removeCallbacks(timeoutHangup);
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                try {
                                    if (soundPool != null)
                                      soundPool.stop(streamID);
                                } catch (Exception e) {
                                }
                                if (!isHandsfreeState)
                                    closeSpeakerOn();
                                //show relay or direct call, for testing purpose
                                ((TextView) findViewById(R.id.tv_is_p2p)).setText(EMClient.getInstance().callManager().isDirectCall()
                                        ? R.string.direct_call : R.string.relay_call);
                                chronometer.setVisibility(View.VISIBLE);
                                chronometer.setBase(SystemClock.elapsedRealtime());
                                // duration start
                                chronometer.start();
                                String str4 = getResources().getString(R.string.In_the_call);
                                callStateTextView.setText(str4);
                                callingState = CallingState.NORMAL;
                                startMonitor();
                                // Start to watch the phone call state.
                                PhoneStateManager.get(VoiceCallActivity.this).addStateCallback(phoneStateCallback);
                            }
                        });
                        break;
                    case NETWORK_UNSTABLE:
                        runOnUiThread(new Runnable() {
                            public void run() {
                                netwrokStatusVeiw.setVisibility(View.VISIBLE);
                                if (error == CallError.ERROR_NO_DATA) {
                                    netwrokStatusVeiw.setText(R.string.no_call_data);
                                } else {
                                    netwrokStatusVeiw.setText(R.string.network_unstable);
                                }
                            }
                        });
                        break;
                    case NETWORK_NORMAL:
                        runOnUiThread(new Runnable() {
                          public void run() {
                                netwrokStatusVeiw.setVisibility(View.INVISIBLE);
                            }
                        });
                        break;
                    case VOICE_PAUSE:
                        runOnUiThread(new Runnable() {
                            public void run() {
                              Toast.makeText(getApplicationContext(), "VOICE_PAUSE", Toast.LENGTH_SHORT).show();
                            }
                        });
                        break;
                    case VOICE_RESUME:
                        runOnUiThread(new Runnable() {
                            public void run() {
                              Toast.makeText(getApplicationContext(), "VOICE_RESUME", Toast.LENGTH_SHORT).show();
                            }
                        });
                        break;
                    case DISCONNECTED:
                        handler.removeCallbacks(timeoutHangup);
                        final CallError fError = error;
                        runOnUiThread(new Runnable() {
                            private void postDelayedCloseMsg() {
                                handler.postDelayed(new Runnable() {

                                    @Override
                                    public void run() {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Log.d("AAA", "CALL DISCONNETED");
                                                removeCallStateListener();

                                                // Stop to watch the phone call state.
                                                PhoneStateManager.get(VoiceCallActivity.this).removeStateCallback(phoneStateCallback);
                                                saveCallRecord();
                                                Animation animation = new AlphaAnimation(1.0f, 0.0f);
                                                animation.setDuration(800);
                                                findViewById(R.id.root_layout).startAnimation(animation);
                                                finish();
                                            }
                                        });
                                    }
                                }, 200);
                            }

                            @Override
                            public void run() {
                                chronometer.stop();
                                callDruationText = chronometer.getText().toString();
                                String st1 = "已拒绝";
                                String st2 = "对方拒绝接受！……";
                                String st3 = "连接建立失败！……";
                                String st4 = getResources().getString(R.string.The_other_party_is_not_online);
                                String st5 = getResources().getString(R.string.The_other_is_on_the_phone_please);

                                String st6 = getResources().getString(R.string.The_other_party_did_not_answer_new);
                                String st7 = getResources().getString(R.string.hang_up);
                                String st8 = getResources().getString(R.string.The_other_is_hang_up);

                                String st9 = getResources().getString(R.string.did_not_answer);
                                String st10 = getResources().getString(R.string.Has_been_cancelled);
                                String st11 = getResources().getString(R.string.hang_up);
                                String st12 = "service not enable";
                                String st13 = "service arrearages";
                                String st14 = "service forbidden";

                                if (fError == CallError.REJECTED) {
                                    callingState = CallingState.BEREFUSED;
                                    callStateTextView.setText(st2);
                                } else if (fError == CallError.ERROR_TRANSPORT) {
                                    callStateTextView.setText(st3);
                                } else if (fError == CallError.ERROR_UNAVAILABLE) {
                                    callingState = CallingState.OFFLINE;
                                    callStateTextView.setText(st4);
                                } else if (fError == CallError.ERROR_BUSY) {
                                    callingState = CallingState.BUSY;
                                    callStateTextView.setText(st5);
                                } else if (fError == CallError.ERROR_NORESPONSE) {
                                    callingState = CallingState.NO_RESPONSE;
                                    callStateTextView.setText(st6);
                                } else if (fError == CallError.ERROR_LOCAL_SDK_VERSION_OUTDATED || fError == CallError.ERROR_REMOTE_SDK_VERSION_OUTDATED) {
                                    callingState = CallingState.VERSION_NOT_SAME;
                                    callStateTextView.setText(R.string.call_version_inconsistent);
                                } else if (fError == CallError.ERROR_SERVICE_NOT_ENABLE) {
                                    callingState = CallingState.SERVICE_NOT_ENABLE;
                                    callStateTextView.setText(st12);
                                } else if (fError == CallError.ERROR_SERVICE_ARREARAGES) {
                                    callingState = CallingState.SERVICE_ARREARAGES;
                                    callStateTextView.setText(st13);
                                } else if (fError == CallError.ERROR_SERVICE_FORBIDDEN) {
                                    callingState = CallingState.SERVICE_NOT_ENABLE;
                                    callStateTextView.setText(st14);
                                } else {
                                    if (isRefused) {
                                        callingState = CallingState.REFUSED;
                                        callStateTextView.setText(st1);
                                    } else if (isAnswered) {
                                        callingState = CallingState.NORMAL;
                                        if (endCallTriggerByMe) {
//                                        callStateTextView.setText(st7);
                                        } else {
                                            callStateTextView.setText(st8);
                                        }
                                    } else {
                                        if (isInComingCall) {
                                            callingState = CallingState.UNANSWERED;
                                            callStateTextView.setText(st9);
                                        } else {
                                            if (callingState != CallingState.NORMAL) {
                                                callingState = CallingState.CANCELLED;
                                                callStateTextView.setText(st10);
                                            } else {
                                                callStateTextView.setText(st11);
                                            }
                                        }
                                    }
                                }
                                postDelayedCloseMsg();
                            }

                        });
                        break;

                    default:
                        break;
                }

            }
        };
        EMClient.getInstance().callManager().addCallStateChangeListener(callStateListener);
    }
    
    void removeCallStateListener() {
        EMClient.getInstance().callManager().removeCallStateChangeListener(callStateListener);
    }

    PhoneStateManager.PhoneStateCallback phoneStateCallback = new PhoneStateManager.PhoneStateCallback() {
