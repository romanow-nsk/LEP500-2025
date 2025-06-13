package me.romanow.lep500.menu;

import android.content.Intent;

import java.io.BufferedReader;
import java.util.ArrayList;

import me.romanow.lep500.service.AppData;
import me.romanow.lep500.I_ArchiveMultiSelector;
import me.romanow.lep500.MainActivity;
import romanow.lep500.FileDescription;
import romanow.lep500.FileDescriptionList;
import romanow.lep500.FFTAudioTextFile;
import me.romanow.lep500.yandexmap.MapFilesActivity;

public class MIMap extends MenuItem{
    public MIMap(MainActivity main0) {
        super(main0);
        main.addMenuList(new MenuItemAction("Показать на карте") {
            @Override
            public void onSelect() {
                main.selectMultiFromArchive("Показать на карте",procMapMultiSelector);
                }
            });
        }
    //----------------------------------------------------------------------------------------------
    private I_ArchiveMultiSelector procMapMultiSelector = new I_ArchiveMultiSelector() {
        @Override
        public void onSelect(FileDescriptionList fd, boolean longClick) {
            try {
                ArrayList<FileDescription> list = AppData.ctx().getFileList();
                list.clear();
                for (FileDescription ff : fd){
                    BufferedReader reader = main.openReader(ff.getOriginalFileName());
                    FFTAudioTextFile.readHeader(ff,reader);
                    reader.close();
                    if (ff.getGps().gpsValid())
                        list.add(ff);
                }
                Intent intent = new Intent();
                intent.setClass(main.getApplicationContext(), MapFilesActivity.class);
                main.startActivity(intent);
            } catch (Exception ee){ main.errorMes(ee.toString());}
        }
    };

}
