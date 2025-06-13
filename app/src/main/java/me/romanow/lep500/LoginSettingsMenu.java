package me.romanow.lep500;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;

import me.romanow.lep500.service.AppData;
import me.romanow.lep500.service.NetBack;
import me.romanow.lep500.service.NetBackDefault;
import me.romanow.lep500.service.NetCall;
import romanow.abc.core.DBRequest;
import romanow.abc.core.UniException;
import romanow.abc.core.constants.Values;
import romanow.abc.core.entity.baseentityes.JEmpty;
import romanow.abc.core.entity.users.Account;
import romanow.abc.core.entity.users.User;
import romanow.lep500.I_EventListener;

public class LoginSettingsMenu extends SettingsMenuBase{
    public LoginSettingsMenu(MainActivity base0){
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
            LinearLayout layout = createItem("IP", set.getDataSetverIP(), true,true,new I_EventListener(){
                @Override
                public void onEvent(String ss) {
                    set.setDataSetverIP(ss);
                    settingsChanged();
                }});
            trmain.addView(layout);
            layout = createItem("Порт", ""+set.getDataServerPort(), new I_EventListener(){
                @Override
                public void onEvent(String ss) {
                    try {
                        set.setDataServerPort(Integer.parseInt(ss));
                        settingsChanged();
                        } catch (Exception ee){
                           base.popupInfo("Формат числа");}
                            }
                    });
            trmain.addView(layout);
            layout = createItem("Телефон", set.getUserPhone(),true,false,new I_EventListener(){
                @Override
                public void onEvent(String ss) {
                    set.setUserPhone(ss);
                    settingsChanged();
                    }});
            trmain.addView(layout);
            layout = createItem("Пароль", "******", true,true,new I_EventListener(){
                @Override
                public void onEvent(String ss) {
                    set.setUserPass(ss);
                    settingsChanged();
                }});
            trmain.addView(layout);
            final boolean isLogin = ctx.cState()==AppData.CStateGreen;
            layout = createItem((!isLogin ? "Войти" : "Выйти"), "",true,true,new I_EventListener(){
                @Override
                public void onEvent(String ss) {
                    final SettingsMenuBase pp = LoginSettingsMenu.this;
                    if (!isLogin){
                        base.retrofitConnect();
                        Account acc = new Account("",set.getUserPhone(), set.getUserPass());
                        new NetCall<User>().call(base,ctx.getService().login(acc), new NetBack(){
                            @Override
                            public void onError(int code, String mes) {
                                if (code == Values.HTTPAuthorization)
                                    ctx.toLog(false,"Ошибка авторизации: "+mes+"");
                                else if (code==Values.HTTPNotFound)
                                    ctx.toLog(false,"Ошибка соединения: "+mes+"");
                                else
                                    ctx.toLog(false,mes);
                                }
                            @Override
                            public void onError(UniException ee) {
                                ctx.popupToastFatal(ee);
                                }
                            @Override
                            public void onSuccess(Object val) {
                                base.sessionOn();
                                User user =(User)val;
                                final LoginSettings set = ctx.loginSettings();
                                set.setUserId(user.getOid());
                                set.setSessionToken(user.getSessionToken());
                                ctx.popup(false,"Вошли");
                                String serverSim = user.getSimCardICC();
                                String regCode = base.createRegistrationCode();
                                if (serverSim.length()==0 && !set.getUserPhone().equals(Values.env().superUser().getLoginPhone())){
                                    user.setSimCardICC(regCode);
                                    //--------------------------------------------------------------
                                    new NetCall<JEmpty>().call(base,ctx.getService().updateEntityField(set.getSessionToken(),"simCardICC",
                                            new DBRequest(user,new Gson())), new NetBack(){
                                        @Override
                                        public void onError(int code, String mes) {
                                            ctx.toLog(true,""+code+": "+mes);
                                            ctx.toLog(true,"Не зарегистрирован на сервере");
                                            ctx.setRegisteredOnServer(false);
                                            }
                                        @Override
                                        public void onError(UniException ee) {
                                            ctx.popupToastFatal(ee);
                                            ctx.toLog(true,"Не зарегистрирован на сервере");
                                            ctx.setRegisteredOnServer(false);
                                            }
                                        @Override
                                        public void onSuccess(Object val) {
                                            ctx.toLog(false,"Зарегистрирован на сервере");
                                            ctx.setRegisteredOnServer(true);
                                            }
                                        });
                                    //--------------------------------------------------------------
                                    }
                                else{
                                    if (serverSim.equals(regCode)){
                                        ctx.toLog(false,"Зарегистрирован на сервере");
                                        ctx.setRegisteredOnServer(true);
                                        }
                                    else{
                                        ctx.toLog(false,"Зарегистрирован с другого устройства");
                                        ctx.setRegisteredOnServer(false);
                                        }
                                    }
                               }
                            });
                        pp.cancel();
                        }
                    else{
                        new NetCall<JEmpty>().call(base,ctx.getService().logoff(ctx.loginSettings().getSessionToken()), new NetBackDefault(){
                            @Override
                            public void onSuccess(Object val) {
                                base.sessionOff();
                                AppData.ctx().popup(false,"Вышли");
                                ctx.cState(AppData.CStateGray);
                                }
                            });
                        }
                    pp.cancel();
                    }
                });
            trmain.addView(layout);
        } catch(Exception ee){
            int a=1;
            }
        catch(Error ee){
            int u=0;
        }
    }
}

