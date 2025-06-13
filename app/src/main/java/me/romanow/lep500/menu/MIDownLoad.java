package me.romanow.lep500.menu;

import com.google.gson.Gson;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import me.romanow.lep500.I_ArchiveMultiSelector;
import me.romanow.lep500.I_ListBoxListener;
import me.romanow.lep500.ListBoxDialog;
import me.romanow.lep500.MainActivity;
import me.romanow.lep500.MultiListBoxDialog;
import me.romanow.lep500.MultiListBoxListener;
import me.romanow.lep500.R;
import me.romanow.lep500.service.AppData;
import me.romanow.lep500.service.NetBackDefault;
import me.romanow.lep500.service.NetCall;
import okhttp3.Response;
import okhttp3.ResponseBody;
import romanow.abc.core.DBRequest;
import romanow.abc.core.UniException;
import romanow.abc.core.Utils;
import romanow.abc.core.constants.Values;
import romanow.abc.core.entity.artifacts.Artifact;
import romanow.abc.core.entity.subjectarea.MeasureFile;
import romanow.abc.core.entity.subjectarea.PowerLine;
import romanow.abc.core.entity.subjectarea.Support;
import romanow.lep500.FileDescription;
import romanow.lep500.FileDescriptionList;

import static me.romanow.lep500.MainActivity.ViewProcHigh;
import static romanow.abc.core.Utils.httpError;

public class MIDownLoad extends MenuItem{
    private ArrayList<PowerLine> lines = new ArrayList<>();
    private ArrayList<Support> supports = new ArrayList<>();
    private ArrayList<MeasureFile> measureFiles = new ArrayList<>();
    private String token;
    private AppData ctx;
    private Gson gson = new Gson();
    public MIDownLoad(MainActivity main0) {
        super(main0);
        ctx = AppData.ctx();
        token = ctx.loginSettings().getSessionToken();
        main.addMenuList(new MenuItemAction(AppData.ColorServer,"Загрузка с сервера") {
            @Override
            public void onSelect() {
                new NetCall<ArrayList<DBRequest>>().call(main, ctx.getService().getEntityList(token,"PowerLine", Values.GetAllModeActual,3), new NetBackDefault(){
                    @Override
                    public void onSuccess(Object val) {
                        lines.clear();
                        ArrayList names = new ArrayList();
                        try {
                            for(DBRequest dd : (ArrayList<DBRequest>)val) {
                                PowerLine line = (PowerLine) dd.get(gson);
                                lines.add(line);
                                names.add(line.getName());
                                }
                            new ListBoxDialog(main, names, "Линии", new I_ListBoxListener() {
                                @Override
                                public void onSelect(int index) {
                                    main.runOnUiThread(new Runnable() {
                                    //-------------------------------------------------------------
                                    //ctx.getEvent().post(new Runnable() {
                                    @Override
                                        public void run() {
                                            procLine(lines.get(index));
                                            }
                                        });
                                    }
                                @Override
                                public void onLongSelect(int index) { }
                                @Override
                                public void onCancel() {}
                                }).create();
                            } catch (Exception e) {}
                    }
                });
            }
        });
    }
    //------------------------------------------------------------------------------------
    private void procLine(PowerLine line){
        supports = line.getGroup();
        ArrayList names = new ArrayList();
        for(Support support : supports)
            names.add(support.getName());
        new ListBoxDialog(main, names, "Опоры", new I_ListBoxListener() {
            @Override
            public void onSelect(int index) {
                main.runOnUiThread(new Runnable() {
                //-------------------------------------------------------------
                //ctx.getEvent().post(new Runnable() {
                    @Override
                    public void run() {
                        procSupport(supports.get(index));
                        }
                    });
                }
            @Override
            public void onLongSelect(int index) { }
            @Override
            public void onCancel() {}
        }).create();
    }
    //-----------------------------------------------------------------------------------
    private void procSupport(Support support){
        measureFiles = support.getFiles();
        AppData ctx = AppData.ctx();
        ArrayList names = new ArrayList();
        for(MeasureFile file : measureFiles)
            names.add(file.toString());
        new MultiListBoxDialog(main, "Файлы измерений", names,new MultiListBoxListener() {
            @Override
            public void onSelect(boolean[] selected) {
                main.runOnUiThread(new Runnable() {
                //-------------------------------------------------------------
                //ctx.getEvent().post(new Runnable() {
                    @Override
                    public void run() {
                        procSelection(selected);
                    }
                });
        }});
    }
    //-------------------------------------------------------------------------------------
    private void procSelection(boolean selected[]){
        for(int i=0;i<selected.length;i++){
            if (!selected[i]) continue;
            final MeasureFile measureFile = measureFiles.get(i);
            final Artifact artifact = measureFiles.get(i).getArtifact().getRef();
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
}
