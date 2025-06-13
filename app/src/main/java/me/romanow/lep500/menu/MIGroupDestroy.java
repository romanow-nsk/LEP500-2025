package me.romanow.lep500.menu;

import java.io.File;
import java.util.ArrayList;

import me.romanow.lep500.I_ArchiveMultiSelector;
import me.romanow.lep500.MainActivity;
import me.romanow.lep500.R;
import me.romanow.lep500.service.AppData;
import romanow.lep500.FileDescription;
import romanow.lep500.FileDescriptionList;

public class MIGroupDestroy extends MenuItem{
    public MIGroupDestroy(MainActivity main0) {
        super(main0);
        main.addMenuList(new MenuItemAction(AppData.ColorArchive,"Разгруппировать") {
            @Override
            public void onSelect() {
                main.selectMultiFromArchive(true,"Разгруппировать",false,fromGroupSelector);
            }
        });
    }
    //---------------------------------------------------------------------------------------------
    private I_ArchiveMultiSelector fromGroupSelector = new I_ArchiveMultiSelector() {
        @Override
        public void onSelect(FileDescriptionList fd, boolean longClick) {
            for (FileDescription ff : fd){
                ArrayList<FileDescription> dirList = main.createArchive(ff.getOriginalFileName());
                for (FileDescription ff2 : dirList){
                    try {
                        main.moveFile(AppData.ctx().androidFileDirectory()+"/"+ff.getOriginalFileName()+"/"+ff2.getOriginalFileName(),
                                AppData.ctx().androidFileDirectory()+"/"+ff2.getOriginalFileName());
                    }catch (Exception ee){ main.errorMes(main.createFatalMessage(ee,5)); }
                }
                try {
                    File gg = new File(AppData.ctx().androidFileDirectory()+"/"+ff.getOriginalFileName());
                    gg.delete();
                    }catch (Exception ee){ main.errorMes(main.createFatalMessage(ee,5)); }
                main.popupAndLog("Разгруппировано");
            }
        }
    };

}
