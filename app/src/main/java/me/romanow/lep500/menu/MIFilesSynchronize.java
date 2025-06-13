package me.romanow.lep500.menu;

import com.google.gson.Gson;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import me.romanow.lep500.MainActivity;
import me.romanow.lep500.service.AppData;
import me.romanow.lep500.service.NetBackDefault;
import me.romanow.lep500.service.NetCall;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import romanow.abc.core.API.RestAPICommon;
import romanow.abc.core.DBRequest;
import romanow.abc.core.UniException;
import romanow.abc.core.Utils;
import romanow.abc.core.constants.Values;
import romanow.abc.core.entity.artifacts.Artifact;
import romanow.abc.core.entity.baseentityes.JEmpty;
import romanow.abc.core.entity.subjectarea.MeasureFile;
import romanow.abc.core.mongo.DBQueryBoolean;
import romanow.abc.core.mongo.DBQueryList;
import romanow.abc.core.mongo.DBQueryLong;
import romanow.abc.core.mongo.DBXStream;
import romanow.abc.core.utils.FileNameExt;
import romanow.lep500.FileDescription;
import romanow.lep500.FileDescriptionList;

public class MIFilesSynchronize extends MenuItem{
    private String token;
    private AppData ctx;
    private HashMap<String,MeasureFile> serverMap = new HashMap<>();
    private HashMap<String, FileDescription> clientMap = new HashMap<>();
    private ArrayList<MeasureFile> downLoadList = new ArrayList<>();
    private FileDescriptionList upLoadList = new FileDescriptionList();
    private FileDescriptionList upDateList = new FileDescriptionList();
    private ArrayList<MeasureFile> upDateList2 = new ArrayList<>();
    private Gson gson = new Gson();
    public MIFilesSynchronize(MainActivity main0) {
        super(main0);
        ctx = AppData.ctx();
        token = ctx.loginSettings().getSessionToken();
        main.addMenuList(new MenuItemAction(AppData.ColorServer,"Синхронизация с сервером") {
            @Override
            public void onSelect() {
                procFiles();
                }
            });
        }
        //-------------------------------------------------------------------------------------
        private void procFiles(){
            FileDescriptionList list = main.createArchiveFull();
            clientMap.clear();
            for(FileDescription ff : list)
                clientMap.put(ff.getOriginalFileName(),ff);
            // 1. Загрузка новых всех
            // 2. Загрузка существующих, если нет локальной оценки
            downLoadList.clear();
            DBQueryList query = new DBQueryList().
                    add(new DBQueryLong("userId", ctx.loginSettings().getUserId())).
                    add(new DBQueryBoolean("valid", true));
            final String xmlQuery = new DBXStream().toXML(query);
            new NetCall<ArrayList<DBRequest>>().call(main, ctx.getService().getEntityListByQuery(token, "MeasureFile", xmlQuery, 1), new NetBackDefault() {
                @Override
                public void onSuccess(Object val) {
                    int count1=0;
                    int count2=0;
                    for (DBRequest dd : (ArrayList<DBRequest>) val) {
                        try {
                            MeasureFile orig = (MeasureFile) dd.get(new Gson());
                            FileDescription ff = clientMap.get(orig.getOriginalFileName());
                            if (ff == null) {
                                downLoadList.add(orig);
                                count1++;
                            } else {
                                boolean bb1 = orig.getExpertResult() == Values.ESNotSupported || orig.getExpertResult() == Values.ESNotSet;
                                boolean bb2 = ff.getExpertNote() == Values.ESNotSupported || ff.getExpertNote() == Values.ESNotSet;
                                if (!bb1 && bb2) {
                                    downLoadList.add(orig);     // Только при условии отсутствия оценки при наличии на сервере
                                    count2++;
                                    }
                                }
                            } catch (UniException e) {
                            }
                        }
                    if (downLoadList.size()==0){
                        main.addToLog( "Нет файлов к загрузке");
                        upLoadAll();
                         return;
                         }
                    main.addToLog( "Новых "+count1+", с загрузкой оценки "+count2);
                    procFile(0);
                    }
                });
            }

        private void procFile(final int idx){
            if (idx>=downLoadList.size()){
                ctx.toLog(false,"Загрузка закончена");
                upLoadAll();
                return;
                }
            final MeasureFile measureFile = downLoadList.get(idx);
            final Artifact artifact = measureFile.getArtifact().getRef();
            try {
                new NetCall<ResponseBody>().call(main,ctx.getService().downLoad(token,artifact.getOid()), new NetBackDefault(){
                    @Override
                    public void onSuccess(Object val) {
                        final ResponseBody body = (ResponseBody)val;
                        new Thread(new Runnable() {             // НЕЛЬЗЯ ДЕЛАТЬ В ПОТОКЕ GUI???????
                            @Override
                            public void run() {
                                long fileSize = body.contentLength();
                                InputStream in = body.byteStream();
                                try {
                                    String fspec = ctx.androidFileDirectory()+"/"+artifact.getOriginalName();
                                    final FileOutputStream out = new FileOutputStream(fspec);
                                    while (fileSize-- != 0)
                                        out.write(in.read());
                                    in.close();
                                    out.flush();
                                    out.close();
                                    main.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            ctx.toLog(false,"Файл загружен: "+ measureFile.toString());
                                            procFile(idx+1);
                                            }
                                        });
                                    } catch (Exception ee) {
                                        main.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                ctx.toLog(true,"Ошибка загрузки: "+ measureFile.toString()+"\n"+Utils.createFatalMessage(ee));
                                            }
                                        });
                                        }
                                    }
                                }).start();
                            }
                        });
                    } catch (Exception e2){}
            }
    //-----------------------------------------------------------------------------------------------------
    private void upLoadAll(){
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
                            upDateList.add(ff);     // Только при условии наличия оценки при отсутствии на сервере
                            upDateList2.add(orig);
                            count2++;
                        }
                    }
                    else{
                        upLoadList.add(ff);
                        count1++;
                    }
                }
                if (upLoadList.size()==0 && upDateList.size()==0){
                    main.addToLog( "Нет файлов к выгрузке/обновлению");
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
            upDate(0);
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
    private void upDate(final int idx){
        if(idx>=upDateList.size()){
            main.addToLog("Обновление закончено");
            return;
            }
        final FileDescription ff2 = upDateList.get(idx);
        FileNameExt fname = new FileNameExt(ctx.androidFileDirectory(),ff2.getOriginalFileName());
        MultipartBody.Part body2 = RestAPICommon.createMultipartBody(fname);
        String name = "Выгружен: "+ctx.loginSettings().getUserPhone();
        new NetCall<Artifact>().call(main,ctx.getService().upload(token,name,fname.fileName(),body2), new NetBackDefault(){
            @Override
            public void onSuccess(Object val) {
                final Artifact artifact = (Artifact)val;
                final MeasureFile file = upDateList2.get(idx);
                new NetCall<JEmpty>().call(main,ctx.getService().removeArtifact(token, file.getArtifact().getOid()), new NetBackDefault() {
                    @Override
                    public void onSuccess(Object val) {
                        main.addToLog( "Файл обновлен: "+ff2.toString());
                        file.getArtifact().setOidRef(artifact);
                        file.setExpertResult(ff2.getExpertNote());
                        new NetCall<JEmpty>().call(main,ctx.getService().updateEntity(token, new DBRequest(file,new Gson())), new NetBackDefault() {
                            @Override
                            public void onSuccess(Object val) {
                                upDate(idx+1);
                                }
                            });
                        }
                    });
                }
            });
        }
    }
