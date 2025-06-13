package me.romanow.lep500.menu;

import android.content.Intent;

import me.romanow.lep500.service.AppData;
import me.romanow.lep500.FullScreenWave;
import me.romanow.lep500.I_ArchiveMultiSelector;
import me.romanow.lep500.MainActivity;
import romanow.lep500.FileDescriptionList;

public class MIViewWave extends MenuItem{
    public MIViewWave(MainActivity main0) {
        super(main0);
        main.addMenuList(new MenuItemAction("Просмотр волны") {
            @Override
            public void onSelect() {
                main.selectMultiFromArchive("Просмотр волны",procWaveSelectorFull);
                //showWaveForm();
            }
        });
    }
    //-----------------------------------------------------------------------------------
    private I_ArchiveMultiSelector procWaveSelectorFull = new I_ArchiveMultiSelector() {
        @Override
        public void onSelect(FileDescriptionList fd, boolean longClick) {
            Intent intent = new Intent();
            intent.setClass(main.getApplicationContext(), FullScreenWave.class);
            AppData.ctx().setFileList(fd);
            main.startActivity(intent);
        }
    };

}
