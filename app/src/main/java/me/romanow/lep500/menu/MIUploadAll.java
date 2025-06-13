package me.romanow.lep500.menu;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;

import me.romanow.lep500.MainActivity;
import me.romanow.lep500.service.AppData;
import me.romanow.lep500.service.NetBackDefault;
import me.romanow.lep500.service.NetCall;
import okhttp3.MultipartBody;
import romanow.abc.core.API.RestAPICommon;
import romanow.abc.core.DBRequest;
import romanow.abc.core.UniException;
import romanow.abc.core.constants.Values;
import romanow.abc.core.entity.artifacts.Artifact;
import romanow.abc.core.entity.subjectarea.MeasureFile;
import romanow.abc.core.mongo.DBQueryBoolean;
import romanow.abc.core.mongo.DBQueryList;
import romanow.abc.core.mongo.DBQueryLong;
import romanow.abc.core.mongo.DBXStream;
import romanow.abc.core.utils.FileNameExt;
import romanow.lep500.FileDescription;
import romanow.lep500.FileDescriptionList;

public class MIUploadAll extends MenuItem{
    private AppData ctx;
    private HashMap<String,MeasureFile> serverMap = new HashMap<>();
    private HashMap<String,FileDescription> clientMap = new HashMap<>();
    private FileDescriptionList upLoadList = new FileDescriptionList();
    private String token;
    private int count=0;
    public MIUploadAll(MainActivity main0) {
        super(main0);
        ctx = AppData.ctx();
        token = ctx.loginSettings().getSessionToken();
        main.addMenuList(new MenuItemAction(AppData.ColorServer,"Выгрузка новых") {
            @Override
            public void onSelect() {
                processOK();
                }
            });
        }
    //-----------------------------------------------------------------------------------------------------
    private void processOK(){
        DBQueryList query =  new DBQueryList().
                add(new DBQueryLong("userId",ctx.loginSettings().getUserId())).
                add(new DBQueryBoolean("valid",true));
        final String xmlQuery = new DBXStream().toXML(query);
        new NetCall<ArrayList<DBRequest>>().call(main,ctx.getService().getEntityListByQuery(token,"MeasureFile",xmlQuery,1), new NetBackDefault(){
            @Override
            public void onSuccess(Object val) {
                ArrayList<DBRequest> cc = (ArrayList<DBRequest>)val;
                main.addToLog("На сервере "+cc.size()+" файлов от "+ctx.loginSettings().getUserPhone());
                for(DBRequest dd : cc){
                    try {
                        MeasureFile ss = (MeasureFile) dd.get(new Gson());
                        serverMap.put(ss.getOriginalFileName(),ss);
                        } catch (UniException e) {
                            main.errorMes(e.toString());
                            }
                        }
                FileDescriptionList list = main.createArchiveFull();
                clientMap.clear();
                for(FileDescription ff : list)
                    clientMap.put(ff.getOriginalFileName(),ff);
                // 1. Выгрузка новых без оценки
                // 2. Выгрузка новых с оценкой (нет или есть без оценки)
                int count1=0;
                int count2=0;
                upLoadList.clear();
                for(FileDescription ff : list){
                    MeasureFile orig = serverMap.get(ff.getOriginalFileName());
                    if (orig!=null){
                        boolean bb1=orig.getExpertResult()== Values.ESNotSupported || orig.getExpertResult()==Values.ESNotSet;
                        boolean bb2=ff.getExpertNote()== Values.ESNotSupported || ff.getExpertNote()==Values.ESNotSet;
                        if (bb1 && !bb2){
                            upLoadList.add(ff);     // Только при условии наличия оценки при отсутствии на сервере
                            count2++;
                            }
                        }
                    else{
                        upLoadList.add(ff);
                        count1++;
                        }
                    }
                if (upLoadList.size()==0){
                    main.addToLog( "Нет файлов к выгрузке");
                    return;
                    }
                main.addToLog( "Новых "+count1+", с замещением оценки "+count2);
                upLoad(0);
                }
        });
    }
    private void upLoad(final int idx){
        if(idx>=upLoadList.size()){
            main.addToLog("Выгрузка закончена");
            return;
            }
        FileDescription ff2 = upLoadList.get(idx);
        FileNameExt fname = new FileNameExt(ctx.androidFileDirectory(),ff2.getOriginalFileName());
        MultipartBody.Part body2 = RestAPICommon.createMultipartBody(fname);
        String name = "Выгружен: "+ctx.loginSettings().getUserPhone();
        new NetCall<Artifact>().call(main,ctx.getService().upload(token,name,fname.fileName(),body2), new NetBackDefault(){
            @Override
            public void onSuccess(Object val) {
                new NetCall<MeasureFile>().call(main,ctx.getService2().addMeasure(token, ((Artifact) val).getOid()), new NetBackDefault() {
                    @Override
                    public void onSuccess(Object val) {
                        main.addToLog( "Файл выгружен: "+ff2.toString());
                        upLoad(idx+1);
                    }
                });
            }
        });
    }
}
