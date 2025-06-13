package me.romanow.lep500.menu;

import java.io.File;

import me.romanow.lep500.R;
import me.romanow.lep500.service.AppData;
import me.romanow.lep500.I_ArchiveMultiSelector;
import me.romanow.lep500.MainActivity;
import me.romanow.lep500.SetOneParameter;
import romanow.lep500.FileDescription;
import romanow.lep500.FileDescriptionList;
import romanow.lep500.I_EventListener;

public class MIGroupCreate extends MenuItem{
    public MIGroupCreate(MainActivity main0) {
        super(main0);
        main.addMenuList(new MenuItemAction(AppData.ColorArchive,"Группировать") {
            @Override
            public void onSelect() {
                main.selectMultiFromArchive("Группировать",toGroupSelector);
            }
        });
    }
    //----------------------------------------------------------------------------------------------
    private I_ArchiveMultiSelector toGroupSelector = new I_ArchiveMultiSelector() {
        @Override
        public void onSelect(final FileDescriptionList fd, boolean longClick) {
            new SetOneParameter(main,"Группа","",true, new I_EventListener() {
                @Override
                public void onEvent(String subdir) {
                    if (AppData.SubDirList.get(subdir)!=null){
                        main.popupAndLog(subdir+" зарезервировано для программы");
                        return;
                        }
                    File dd = new File(AppData.ctx().androidFileDirectory()+"/"+subdir);
                    if (dd.exists()){
                        main.popupAndLog(subdir+" уже существует");
                        return;
                        }
                    dd.mkdir();
                    for (FileDescription ff : fd){
                        try {
                            String src = AppData.ctx().androidFileDirectory()+"/"+ff.getOriginalFileName();
                            main.moveFile(src, AppData.ctx().androidFileDirectory()+"/"+subdir+"/"+ff.getOriginalFileName());
                            }
                        catch (Exception ee){ main.errorMes(main.createFatalMessage(ee,5)); }
                        }
                    main.popupAndLog("Сгруппировано в "+subdir);
                }
            });
        }
    };
}
