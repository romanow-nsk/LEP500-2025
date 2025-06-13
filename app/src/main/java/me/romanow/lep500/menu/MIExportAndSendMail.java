package me.romanow.lep500.menu;

import android.content.Intent;
import android.net.Uri;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

import me.romanow.lep500.BuildConfig;
import me.romanow.lep500.FFTExcelAdapter;
import me.romanow.lep500.I_ArchiveMultiSelector;
import me.romanow.lep500.MainActivity;
import me.romanow.lep500.service.AppData;
import romanow.lep500.FileDescription;
import romanow.lep500.FileDescriptionList;

public class MIExportAndSendMail extends MenuItem{
    public MIExportAndSendMail(MainActivity main0) {
        super(main0);
        main.addMenuList(new MenuItemAction("Отправить Excel в mail") {
            @Override
            public void onSelect() {
                main.selectMultiFromArchive("Отправить Excel в mail",exportAndSendMailSelector);
            }
        });
    }
    //--------------------------------------------------------------------------------------------
    private I_ArchiveMultiSelector exportAndSendMailSelector = new I_ArchiveMultiSelector() {
        @Override
        public void onSelect(FileDescriptionList fdlist, boolean longClick) {
            for(FileDescription fd : fdlist) {
                try {
                    String pathName = AppData.ctx().androidFileDirectory() + "/" + fd.getOriginalFileName();
                    FileInputStream fis = new FileInputStream(pathName);
                    main.processInputStream(false, fd, fis, "", new FFTExcelAdapter(main, "", fd));
                    } catch (Throwable e) {
                        main.errorMes("Файл не открыт: " + fd.getOriginalFileName() + "\n" + main.createFatalMessage(e, 10));
                        }
                }
            try {
                final Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                emailIntent.setType("plain/text");
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{AppData.ctx().set().mailToSend});
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Звенящие опоры России");
                emailIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                ArrayList<Uri> uris = new ArrayList<Uri>();
                for(FileDescription fd : fdlist){
                    emailIntent.putExtra(Intent.EXTRA_TEXT, "Датчик: " + fd.toString());
                    String filePath = new FFTExcelAdapter(main, "", fd).createOriginalExcelFileName();
                    File ff = new File(filePath);
                    Uri fileUri = FileProvider.getUriForFile(main, BuildConfig.APPLICATION_ID, ff);
                    uris.add(fileUri);
                    //--------------- Старое -------------------------------------------------------
                    //emailIntent.putExtra(android.content.Intent.EXTRA_STREAM,Uri.fromFile(ff));
                    //emailIntent.putExtra(android.content.Intent.EXTRA_STREAM,Uri.parse(filePath));
                    }
                emailIntent.putExtra(Intent.EXTRA_STREAM,uris);
                main.startActivity(Intent.createChooser(emailIntent, "Отправка письма..."));
                //----------------- Читстить каталог после отправки
                //for(FileDescription fd : fdlist){
                //    String filePath = new FFTExcelAdapter(main, "", fd).createOriginalExcelFileName();
                //    File ff = new File(filePath);
                //    ff.delete();
                //    }
                } catch (Exception ee){
                    main.errorMes("Ошибка mail: "+ee.toString());
                    }
        }
    };

}
