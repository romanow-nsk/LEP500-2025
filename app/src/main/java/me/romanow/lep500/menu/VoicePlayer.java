package me.romanow.lep500.menu;

import android.media.MediaPlayer;
import android.net.Uri;
import android.view.View;
import android.widget.LinearLayout;

import java.io.File;

import me.romanow.lep500.MainActivity;
import me.romanow.lep500.service.AppData;
import romanow.lep500.FileDescription;

import static me.romanow.lep500.MainActivity.VoiceFile;

public abstract class VoicePlayer{
    private  LinearLayout voiceButton;
    public abstract void convert(String outFile, FileDescription fd);
    public VoicePlayer(FileDescription fd, final MainActivity main) {
        main.hideFFTOutput=false;
        final String outFile = AppData.ctx().androidFileDirectory()+"/"+VoiceFile;
        convert(outFile,fd);
        //FFTAudioTextFile xx = new FFTAudioTextFile();
        //xx.setnPoints(set.nTrendPoints);
        //xx.convertToWave(fd,set.measureFreq, outFile, pathName, new FFTAdapter(MainActivity.this,fd.toString()));
        voiceButton = main.addToLogButton("Остановите музыку", new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                main.voiceRun=false;
                main.log().removeView(voiceButton);
                //zz.setVisibility(View.INVISIBLE);
                }
            });
        Thread voice = new Thread(){
            public void run() {
                File file = new File(outFile);
                main.voiceRun = true;
                try {
                    MediaPlayer mp = MediaPlayer.create(main, Uri.fromFile(file));
                    while(main.voiceRun & ! main.shutDown){
                        mp.start();
                        while (mp.isPlaying() && main.voiceRun && !main.shutDown);
                        }
                    } catch (Exception ee){
                        main.errorMes(ee.toString());
                        }
                }
        };
        voice.start();
    }
}
