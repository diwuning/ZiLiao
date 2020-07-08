package com.nmpa.nmpaapp.modules.huanxin.record;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import com.nmpa.nmpaapp.utils.FileLoadUtils;

public class AudioRecordImpl {
  private static final String TAG = "AudioRecordImpl";

    // 采样率
    private static final int SAMPLE_RATE_HZ = 16000;

    private static final int BUFFER_SIZE = 640;
    private AudioRecord audioRecord;
    private Thread recordThread;
  //采样频率
    private int mAudioSampleRate = SAMPLE_RATE_HZ;
    //音频源
    private int mAudioSource = MediaRecorder.AudioSource.MIC;
    //编码大小
    private int mAudioFormat = AudioFormat.ENCODING_PCM_16BIT;
    //选择声道
    private int mAudioChannel = AudioFormat.CHANNEL_IN_MONO;
    // 用于数据统计
    private volatile boolean isQueryLog;
    private volatile boolean isStartRecord = false;

    public AudioRecordImpl() {
        setAudio(SAMPLE_RATE_HZ, MediaRecorder.AudioSource.MIC, AudioFormat.ENCODING_PCM_16BIT,
                AudioFormat.CHANNEL_IN_MONO);
    }
    //默认设置
    public void setAudio(int sampleRate, int source, int format, int channel) {
        mAudioSampleRate = sampleRate;
        mAudioSource = source;
        mAudioFormat = format;
        mAudioChannel = channel;
    }
    //开始录音
    public void startRecord() {
        stopRecord();
        if (recordThread != null) {
            try {
                //t.join()方法阻塞调用此方法的线程(calling thread)，
                // 直到线程t完成，此线程再继续
                recordThread.join();
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
        }
        //设置线程优先级，android.os.Process.THREAD_PRIORITY_AUDIO-标准音乐播放使用的线程优先级
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);
        //获取最小buffer
        final int bufferSize = AudioRecord.getMinBufferSize(
                mAudioSampleRate, mAudioChannel, mAudioFormat);

        try {
            // 防止某些手机崩溃
            //创建audioRecord对象
            audioRecord = new AudioRecord(mAudioSource, mAudioSampleRate,
                    mAudioChannel, mAudioFormat, bufferSize * 10);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "new AudioRecord() IllegalStateException ", e);
        }
        isStartRecord = true;
        recordThread = new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    // 防止某些手机崩溃
                    if (audioRecord != null) {
                        audioRecord.startRecording();
                    }
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                    Log.e(TAG, "startRecording IllegalStateException ", e);
                    isStartRecord = false;
                }

                isQueryLog = true;
                while (isStartRecord) {
                    try {
                        //读取语音数据
                        byte[] buffer = new byte[BUFFER_SIZE];
                        int readBytes = audioRecord.read(buffer, 0, buffer.length);
                        if (readBytes > 0) {
                            //处理语音数据
//                            fireData(buffer);
                            FileLoadUtils.getFile(buffer,System.currentTimeMillis()+".amr");
                        }
                  } catch (Exception e) {
                      e.printStackTrace();
                      Log.e(TAG, "audio recorder exception," + e);
                      isStartRecord = false;
                  }
              }

              // 释放资源
              try {
                  if (audioRecord != null) {
                      audioRecord.release();
                      Log.e(TAG, "audioRecorder release ");
                  }
              } catch (Exception e) {
                  e.printStackTrace();
                  Log.e(TAG, "stop and release IllegalStateException ", e);
              } finally {
                  audioRecord = null;
              }
          }
      };

      recordThread.start();
  }
    //停止录音
    public void stopRecord() {
        Log.e(TAG, "stopRecord");
        isStartRecord = false;
    }
    //释放资源
    public void release() {
        //释放资源
        Log.e(TAG, "release");
        stopRecord();
    }
}
