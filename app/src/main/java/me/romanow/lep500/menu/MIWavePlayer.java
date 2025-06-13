package me.romanow.lep500.menu;

import me.romanow.lep500.I_ArchiveSelector;
import me.romanow.lep500.MainActivity;
import me.romanow.lep500.service.AppData;
import romanow.lep500.FileDescription;
import romanow.lep500.FFTAudioTextFile;

public class MIWavePlayer extends MenuItem{
    public MIWavePlayer(MainActivity main0) {
        super(main0);
        main.addMenuList(new MenuItemAction("Слушать wave") {
            @Override
            public void onSelect() {
                main.selectFromArchive("Слушать wave",voiceSelector);
            }
        });
    }
    //-------------------------------------------- Воспроизведение ---------------------------------
    private I_ArchiveSelector voiceSelector = new I_ArchiveSelector() {
        @Override
        public void onSelect(FileDescription fd, boolean longClick) {
            new VoicePlayer(fd,main){
                @Override
                public void convert(String outFile, FileDescription fd) {
                    FFTAudioTextFile xx = new FFTAudioTextFile();
                    xx.setnPoints(AppData.ctx().set().nTrendPoints);
                    String pathName = AppData.ctx().androidFileDirectory()+"/"+fd.getOriginalFileName();
                    xx.convertToWave(fd,AppData.ctx().set().measureFreq, outFile, pathName, main);
                }
            };
        }
    };

}
