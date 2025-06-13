package me.romanow.lep500.menu;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

import me.romanow.lep500.MainActivity;
import me.romanow.lep500.service.AppData;
import romanow.abc.core.utils.Base64Coder;

public class MITestCase extends MenuItem{
    public MITestCase(MainActivity main0) {
        super(main0);
        main.addMenuList(new MenuItemAction("Отладка") {
            @Override
            public void onSelect() {
                Thread thread = Thread.currentThread();
                Integer aaa=null;
                aaa.intValue();
            }
        });
    }

}
