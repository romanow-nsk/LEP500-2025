package me.romanow.lep500.service;


import romanow.abc.core.UniException;
import romanow.abc.core.constants.Values;

public abstract class NetBackDefault implements NetBack {
    public NetBackDefault(){
        }
    @Override
    public void onError(int code, String mes) {
        if (code == Values.HTTPAuthorization){
            AppData.ctx().popupAndLog(true,"Сеанс прерван. Повторный логин");
            }
        else{
            String ss = "Ошибка сервера: "+code+":"+mes;
            AppData.ctx().addStoryMessage(ss);
            AppData.ctx().popupAndLog(true,ss);
            }
        }
    @Override
    public void onError(UniException ee) {
        AppData.ctx().addStoryMessage("Ошибка сети: "+ee.toString());
        AppData.ctx().popupAndLog(true,"Сеть недоступна");
        }
    }
