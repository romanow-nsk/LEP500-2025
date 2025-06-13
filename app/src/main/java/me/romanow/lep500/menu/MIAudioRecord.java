package me.romanow.lep500.menu;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.view.View;
import android.widget.LinearLayout;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;

import me.romanow.lep500.I_ArchiveSelector;
import me.romanow.lep500.MainActivity;
import me.romanow.lep500.service.AppData;
import romanow.lep500.FFTAudioTextFile;
import romanow.lep500.FileDescription;
import romanow.lep500.I_EventListener;

public class MIAudioRecord extends MenuItem{
    private LinearLayout voiceButton;
    private short buff[];
    private AudioRecord record;
    private volatile boolean audioFinish=false;
    private ByteArrayOutputStream out;
    private  FFTAudioTextFile file;
    public MIAudioRecord(MainActivity main0) {
        super(main0);
        main.addMenuList(new MenuItemAction("Демо с микрофона") {
            @Override
            public void onSelect() {
                try {
                voiceButton = main.addToLogButton("Остановить запись", new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        main.voiceRun=false;
                        main.log().removeView(voiceButton);
                        record.stop();
                        while (!audioFinish && !main.shutDown);
                        file.save(AppData.ctx().androidFileDirectory(), new I_EventListener() {
                            @Override
                            public void onEvent(String s) {
                                main.addToLog(false,s,14,0x00A00000);
                                }
                            });
                        main.addToLog(false,"Файл сохранен: "+file.createOriginalFileName(),14,0x00007020);
                        }
                    });
                file = new FFTAudioTextFile(AppData.ctx().set(), "Micro",main.getGpsService().lastGPS());
                out = new ByteArrayOutputStream();
                int nchan= AudioFormat.CHANNEL_IN_MONO;
                int mode=AudioFormat.ENCODING_PCM_16BIT;
                int rate = 11025;
                int bsz= AudioRecord.getMinBufferSize(rate, nchan, mode);
                buff=new short[bsz];
                record = new AudioRecord(MediaRecorder.AudioSource.MIC,rate,nchan,mode,bsz);
                record.startRecording();
                if (record.getState()==0) {
                    main.addToLog(false,"Ошибка инициализации recorder",14,0x00A00000);
                    main.voiceRun=false;
                    main.log().removeView(voiceButton);
                    return;
                    }
                Thread voice = new Thread(){
                    public void run() {
                        main.voiceRun = true;
                        try {
                            while (main.voiceRun && !main.shutDown){
                                int nb = record.read(buff, 0, bsz);
                                for(int i=0;i<nb;i++){
                                    out.write(buff[i]);
                                    out.write(buff[i]>>8);
                                    }
                                }
                            out.flush();
                            byte bb[] = out.toByteArray();
                            short cc[] = new short[bb.length/2];
                            for(int i=0;i<cc.length;i++)
                                cc[i] = (short) ((bb[2*i] & 0x0FF) | (bb[2*i+1]<<8 & 0x0FF00));
                            file.setData(cc);
                            audioFinish=true;
                            } catch (Exception ee){
                                main.addToLog(false,"Ошибка recorder\n"+ee.toString(),14,0x00A00000);
                                main.voiceRun=false;
                                main.log().removeView(voiceButton);
                                }
                        }
                    };
                voice.start();
                }catch (Exception ee){
                    main.addToLog(false,"Ошибка recorder\n"+ee.toString(),14,0x00A00000);
                    }
                }
            });
        }
}
