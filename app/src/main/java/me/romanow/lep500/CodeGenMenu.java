package me.romanow.lep500;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.LinearLayout;

import me.romanow.lep500.service.AppData;
import romanow.lep500.I_EventListener;

public class CodeGenMenu extends SettingsMenuBase{
    public CodeGenMenu(MainActivity base0){
        super(base0);
        }
    private AppData ctx;
    @Override
    public void settingsSave() {
        ctx.fileService().saveJSON(ctx.loginSettings());
        }

    @Override
    public void createDialog(LinearLayout trmain) {
        ctx = AppData.ctx();
        try {
            final LoginSettings set = AppData.ctx().loginSettings();
            LinearLayout layout = createItem("Свой код", "", false,true,new I_EventListener(){
                @Override
                public void onEvent(String zz) {
                    String ss = base.createRegistrationCode();
                    base.addToLogButton("Рег. код: "+ss);
                    set.setRegistrationCode(ss);
                    settingsSave();
                    base.addToLog(false, "Приложение зарегистрировано",18,0x00007020);
                    base.createMenuList();
                    cancel();
                }});
            trmain.addView(layout);
            layout = createItem("ID", "", true,true,new I_EventListener(){
                @Override
                public void onEvent(String zz) {
                    String ss = Registration.createRegistrationCode64(zz);
                    base.addToLogButton("Рег. код: "+ss,true,null,null);
                    base.addToLogButton("Для ID: "+zz, true,null,null);
                    cancel();
                }});
            trmain.addView(layout);
            layout = createItem("Сброс", "", false,true,new I_EventListener(){
                @Override
                public void onEvent(String zz) {
                    set.setRegistrationCode("");
                    settingsSave();
                    ctx.popupAndLog(false, "Регистрация отменена");
                    base.overLoad(false);
                }});
            trmain.addView(layout);
            } catch(Exception ee){
                int a=1;
                }
                catch(Error ee){
                int u=0;
                }
    }
}

