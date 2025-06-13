package me.romanow.lep500.menu;

import com.google.gson.Gson;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import me.romanow.lep500.I_ListBoxListener;
import me.romanow.lep500.ListBoxDialog;
import me.romanow.lep500.MainActivity;
import me.romanow.lep500.MultiListBoxDialog;
import me.romanow.lep500.MultiListBoxListener;
import me.romanow.lep500.service.AppData;
import me.romanow.lep500.service.NetBackDefault;
import me.romanow.lep500.service.NetCall;
import okhttp3.ResponseBody;
import retrofit2.Call;
import romanow.abc.core.DBRequest;
import romanow.abc.core.UniException;
import romanow.abc.core.Utils;
import romanow.abc.core.constants.Values;
import romanow.abc.core.entity.EntityList;
import romanow.abc.core.entity.artifacts.Artifact;
import romanow.abc.core.entity.subjectarea.MeasureFile;
import romanow.abc.core.entity.subjectarea.PowerLine;
import romanow.abc.core.entity.subjectarea.Support;
import romanow.abc.core.entity.users.User;
import romanow.abc.core.mongo.DBQueryBoolean;
import romanow.abc.core.mongo.DBQueryInt;
import romanow.abc.core.mongo.DBQueryList;
import romanow.abc.core.mongo.DBQueryLong;
import romanow.abc.core.mongo.DBXStream;
import romanow.abc.core.mongo.I_DBQuery;
import romanow.lep500.FileDescription;
import romanow.lep500.FileDescriptionList;

public class MIDownLoadAll extends MenuItem{
    private String token;
    private AppData ctx;
    private HashMap<String,MeasureFile> serverMap = new HashMap<>();
    private HashMap<String, FileDescription> clientMap = new HashMap<>();
    private ArrayList<MeasureFile> downLoadList = new ArrayList<>();
    private Gson gson = new Gson();
    public MIDownLoadAll(MainActivity main0) {
        super(main0);
        ctx = AppData.ctx();
        token = ctx.loginSettings().getSessionToken();
        main.addMenuList(new MenuItemAction(AppData.ColorServer,"Загрузка новых") {
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
                            if (ff==null){
                                downLoadList.add(orig);
                                count1++;
                                }
                            else{
                                boolean bb1=orig.getExpertResult()== Values.ESNotSupported || orig.getExpertResult()==Values.ESNotSet;
                                boolean bb2=ff.getExpertNote()== Values.ESNotSupported || ff.getExpertNote()==Values.ESNotSet;
                                if (!bb1 && bb2) {
                                    downLoadList.add(orig);     // Только при условии отсутствия оценки при наличии на сервере
                                    count2++;
                                    }
                                }
                            } catch (UniException e) {}
                        if (downLoadList.size()==0){
                            main.addToLog( "Нет файлов к загрузке");
                            return;
                            }
                        main.addToLog( "Новых "+count1+", с загрузкой оценки "+count2);
                        procFile(0);
                        }
                    }
                });
            }

        private void procFile(final int idx){
            if (idx>=downLoadList.size()){
                ctx.toLog(false,"Загрузка закончена");
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
        }
