package me.romanow.lep500.menu;

import java.io.FileInputStream;

import me.romanow.lep500.FFTExcelAdapter;
import me.romanow.lep500.I_ArchiveMultiSelector;
import me.romanow.lep500.MainActivity;
import me.romanow.lep500.service.AppData;
import romanow.lep500.FileDescription;
import romanow.lep500.FileDescriptionList;

public class MIExportMeasureExcel extends MenuItem{
    public MIExportMeasureExcel(MainActivity main0) {
        super(main0);
        main.addMenuList(new MenuItemAction(AppData.ColorArchive,"Измерения -> Excel") {
            @Override
            public void onSelect() {
                main.selectMultiFromArchive("Измерения -> Excel",exportSelector);
                }
            });
        }
    //------------------------------------------------------------------------------------
    private I_ArchiveMultiSelector exportSelector = new I_ArchiveMultiSelector() {
        @Override
        public void onSelect(FileDescriptionList flist, boolean longClick) {
            FFTExcelAdapter adapter = new FFTExcelAdapter(main);
            for(FileDescription fd : flist) {
                try {
                    String pathName = AppData.ctx().androidFileDirectory() + "/" + fd.getOriginalFileName();
                    FileInputStream fis = new FileInputStream(pathName);
                    adapter.nextStep("",fd);
                    main.processInputStream(false, fd, fis, "", adapter);
                    } catch (Throwable e) {
                        main.errorMes("Файл не открыт: " + fd.getOriginalFileName() + "\n" + main.createFatalMessage(e, 10));
                        }
                }
            adapter.createExcel();
            }
        };
    }

