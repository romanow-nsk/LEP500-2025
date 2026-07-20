package me.romanow.lep500.menu;

import static me.romanow.lep500.MainActivity.ViewProcHigh;

import me.romanow.lep500.I_ArchiveMultiSelector;
import me.romanow.lep500.MainActivity;
import me.romanow.lep500.R;
import romanow.lep500.FileDescription;
import romanow.lep500.FileDescriptionList;

public class MIBaseFreq extends MenuItem{
    public MIBaseFreq(MainActivity main0) {
        super(main0);
        main.addMenuList(new MenuItemAction("Базовая частота") {
            @Override
            public void onSelect() {
                main.selectMultiFromArchive("Проcмотр архива",procViewMultiSelector);
            }
        });
    }
    //------------------------------------------------------------------------------------
    private I_ArchiveMultiSelector procViewMultiSelector = new I_ArchiveMultiSelector() {
        @Override
        public void onSelect(FileDescriptionList fd, boolean longClick) {
            main.log().addView(main.createMultiGraph(R.layout.graphview,ViewProcHigh));
            main.defferedStart();
            for (FileDescription ff : fd){
                main.procArchive(ff,false);
                }
            main.defferedFinishBaseFreq();
            }
    };
}
