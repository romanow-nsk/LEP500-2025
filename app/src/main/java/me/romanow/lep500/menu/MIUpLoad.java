package me.romanow.lep500.menu;

import me.romanow.lep500.I_ArchiveMultiSelector;
import me.romanow.lep500.MainActivity;
import me.romanow.lep500.R;
import me.romanow.lep500.service.AppData;
import me.romanow.lep500.service.NetBack;
import me.romanow.lep500.service.NetBackDefault;
import me.romanow.lep500.service.NetCall;
import okhttp3.MultipartBody;
import retrofit2.Call;
import romanow.abc.core.API.RestAPICommon;
import romanow.abc.core.UniException;
import romanow.abc.core.entity.artifacts.Artifact;
import romanow.abc.core.entity.subjectarea.MeasureFile;
import romanow.abc.core.entity.users.User;
import romanow.abc.core.utils.FileNameExt;
import romanow.lep500.FileDescription;
import romanow.lep500.FileDescriptionList;

import static me.romanow.lep500.MainActivity.ViewProcHigh;

public class MIUpLoad extends MenuItem{
    public MIUpLoad(MainActivity main0) {
        super(main0);
        main.addMenuList(new MenuItemAction(AppData.ColorServer,"Выгрузка на сервер") {
            @Override
            public void onSelect() {
                main.selectMultiFromArchive("Выгрузка файлов",procViewMultiSelector);
            }
        });
    }
    //------------------------------------------------------------------------------------
    private I_ArchiveMultiSelector procViewMultiSelector = new I_ArchiveMultiSelector() {
        @Override
        public void onSelect(FileDescriptionList fd, boolean longClick) {
            final AppData ctx = AppData.ctx();
            final String token = ctx.loginSettings().getSessionToken();
            for (FileDescription ff : fd){
                final FileDescription ff2 = ff;
                FileNameExt fname = new FileNameExt(ctx.androidFileDirectory(),ff.getOriginalFileName());
                MultipartBody.Part body2 = RestAPICommon.createMultipartBody(fname);
                String name = "Выгружен: "+ctx.loginSettings().getUserPhone();
                new NetCall<Artifact>().call(main,ctx.getService().upload(token,name,fname.fileName(),body2), new NetBackDefault(){
                    @Override
                    public void onSuccess(Object val) {
                        new NetCall<MeasureFile>().call(main,ctx.getService2().addMeasure(token, ((Artifact) val).getOid()), new NetBackDefault() {
                            @Override
                            public void onSuccess(Object val) {
                                    ctx.popupAndLog(false, "Файл выгружен: "+ff2.toString());
                                    }
                                });
                            }
                        });
                }
            }
    };
}
