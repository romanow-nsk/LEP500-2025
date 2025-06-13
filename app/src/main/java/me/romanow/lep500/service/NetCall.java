package me.romanow.lep500.service;

import retrofit2.Call;
import retrofit2.Response;
import romanow.abc.core.UniException;
import romanow.abc.core.Utils;

/** Контейнер вызова сервера с синхронизацией и обработкой ошибок*/
public class NetCall<T> {
    private Response res = null;
    private AppData gbl;
    public NetCall(){ }
    public void call(final BaseActivity base, final Call<T> cl, final NetBack back){
        gbl = AppData.ctx();
        new Thread() {
            public void run() {
                //Блокирует GUI ----------------------------------------------------------------------------------------
                //synchronized (gbl){
                try {
                    gbl.cState(AppData.CStateYellow);
                    res = cl.execute();
                    if (!gbl.isApplicationOn())
                        return;
                    if (res.isSuccessful())
                        gbl.cState(AppData.CStateGreen);
                    if (back==null) return;
                    base.runOnUiThread(new Runnable() {
                    //---------------------------------------------------------------------------
                    //gbl.getEvent().post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (!res.isSuccessful()) {
                                    AppData.ctx().cState(AppData.CStateRed);
                                    back.onError(res.code(), res.errorBody().string());
                                } else{
                                    try {
                                        AppData.ctx().cState(AppData.CStateGreen);
                                        back.onSuccess((T) res.body());
                                        } catch (Exception ee){
                                            AppData.ctx().cState(AppData.CStateRed);
                                            gbl.addBugMessage(Utils.createFatalMessage(ee));
                                            gbl.popupToastFatal(ee);
                                            }
                                    }
                                }
                                catch (final Exception e) {
                                    if (back==null) return;
                                    base.runOnUiThread(new Runnable() {
                                    //--------------------------------------------------------
                                    //AppData.ctx().getEvent().post(new Runnable() {
                                        @Override
                                        public void run() {
                                            AppData.ctx().cState(AppData.CStateRed);
                                            back.onError(UniException.net(e));
                                            }
                                    });
                            }}
                        });
                } catch (final Exception e) {
                    AppData.ctx().cState(AppData.CStateRed);
                    if (back==null) return;
                    base.runOnUiThread(new Runnable() {
                    //-----------------------------------------------------------------------------
                    //AppData.ctx().getEvent().post(new Runnable() {
                        @Override
                        public void run() {
                            back.onError(UniException.net(e));
                            }
                        });
                }
            }
        }.start();
    }
}
