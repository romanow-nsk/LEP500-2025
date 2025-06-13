package me.romanow.lep500.menu;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import me.romanow.lep500.I_ArchiveMultiSelector2;
import me.romanow.lep500.MainActivity;
import me.romanow.lep500.service.AppData;
import romanow.lep500.FileDescription;
import romanow.lep500.FileDescriptionList;

public class MIFileCopy extends MIFileBrowser{
    //------------------------------------------------------------------------------------
    private I_ArchiveMultiSelector2 procFileCopy = new I_ArchiveMultiSelector2() {
        @Override
        public void onSelect(String path, FileDescriptionList fd, boolean longClick) {
            for (FileDescription ff : fd){
                try{
                    final InputStream is = new FileInputStream(path+"/"+ff.getOriginalFileName());
                    if (is==null){
                        main.errorMes("Файл "+ff.getOriginalFileName()+" не найден");
                        continue;
                        }
                    File gg = new File(AppData.ctx().androidFileDirectory());
                    if (!gg.exists()) {
                        gg.mkdir();
                        }
                    final FileOutputStream fos = new FileOutputStream(AppData.ctx().androidFileDirectory()+"/"+ff.getOriginalFileName());
                    final String zz = ff.getOriginalFileName();
                    Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            while (true) {
                                int vv = is.read();
                                if (vv == -1)
                                    break;
                                fos.write(vv);
                                }
                            fos.flush();
                            fos.close();
                            is.close();
                            main.addToLog("Файл "+zz+" скопирован");
                            } catch (final Exception ee) {
                                main.errorMes("Ошибка копирования: "+ee.toString());
                                }
                        }});
                    thread.start();
                    } catch (Throwable ee){
                        main.errorMes(ff.getOriginalFileName()+"\n"+ee.toString());
                        }
                    }
            }
        };
    public MIFileCopy(MainActivity main0) {
        super(main0,AppData.ColorArchive,"Файлы в архив");
        setProcSelector(procFileCopy);
        }
}
