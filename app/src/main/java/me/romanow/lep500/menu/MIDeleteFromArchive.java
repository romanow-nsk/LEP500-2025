package me.romanow.lep500.menu;

import java.io.File;

import me.romanow.lep500.I_ArchiveMultiSelector;
import me.romanow.lep500.MainActivity;
import me.romanow.lep500.R;
import me.romanow.lep500.service.AppData;
import romanow.lep500.FileDescription;
import romanow.lep500.FileDescriptionList;

public class MIDeleteFromArchive extends MenuItem{
    public MIDeleteFromArchive(MainActivity main0) {
        super(main0);
        main.addMenuList(new MenuItemAction(AppData.ColorArchive,"Удалить из архива") {
            @Override
            public void onSelect() {
                main.selectMultiFromArchive("Удалить из архива",deleteMultiSelector);
            }
        });
    }
    //-----------------------------------------------------------------------------------------
    private I_ArchiveMultiSelector deleteMultiSelector = new I_ArchiveMultiSelector() {
        @Override
        public void onSelect(FileDescriptionList fd, boolean longClick) {
            for (FileDescription ff : fd){
                File file = new File(AppData.ctx().androidFileDirectory()+"/"+ff.getOriginalFileName());
                file.delete();
                }
            }
    };
}
