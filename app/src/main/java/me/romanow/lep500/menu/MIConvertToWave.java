package me.romanow.lep500.menu;

import java.io.File;

import me.romanow.lep500.service.AppData;
import me.romanow.lep500.I_ArchiveSelector;
import me.romanow.lep500.MainActivity;
import romanow.lep500.FileDescription;
import romanow.lep500.FFTAudioTextFile;

public class MIConvertToWave extends MenuItem{
    public MIConvertToWave(MainActivity main0) {
        super(main0);
        main.addMenuList(new MenuItemAction("Конвертировать в wave") {
            @Override
            public void onSelect() {
                main.selectFromArchive("Конвертировать в wave",convertSelector);
            }
        });
    }
    //-------------------------------------------------------------------------------------------
    private I_ArchiveSelector convertSelector = new I_ArchiveSelector() {
        @Override
        public void onSelect(FileDescription fd, boolean longClick) {
            String pathName = AppData.ctx().androidFileDirectory()+"/"+fd.getOriginalFileName();
            FFTAudioTextFile xx = new FFTAudioTextFile();
            xx.setnPoints(AppData.ctx().set().nTrendPoints);
            main.hideFFTOutput=false;
            String dirName = AppData.ctx().androidFileDirectory()+"/"+ AppData.waveDir;
            File ff = new File(dirName);
            if (!ff.exists()){
                ff.mkdir();
                }
            xx.convertToWave(fd,AppData.ctx().set().measureFreq, dirName+"/"+fd.getOriginalFileName(),pathName, main);
        }
    };

}
