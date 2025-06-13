package me.romanow.lep500;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.LinearLayout;

import me.romanow.lep500.service.AppData;
import romanow.lep500.I_EventListener;

public class RegistrationMenu extends SettingsMenuBase{
    public RegistrationMenu(MainActivity base0){
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
            final LoginSettings set = ctx.loginSettings();
            LinearLayout layout = createItem("Код", "", true,true,new I_EventListener(){
                @Override
                public void onEvent(String ss) {
                    if (!ss.equals(ctx.getCodeGenPassword())) {
                        String s2 = base.createRegistrationCode();
                        if (!ss.equals(s2)) {
                            base.popupAndLog("Неправильный регистрационный код",18,0x00A00000);
                            cancel();
                        } else {
                            set.setRegistrationCode(ss);
                            settingsSave();
                            base.popupAndLog( "Приложение зарегистрировано",18,0x00007020);
                            cancel();
                            base.overLoad(false);
                            }
                        }
                    else{
                        new CodeGenMenu(base);
                        cancel();
                        }
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

