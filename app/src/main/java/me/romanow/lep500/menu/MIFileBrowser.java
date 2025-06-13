package me.romanow.lep500.menu;

import androidx.core.content.ContextCompat;

import java.io.File;
import java.util.ArrayList;

import me.romanow.lep500.I_ArchiveMultiSelector2;
import me.romanow.lep500.I_ListBoxListener;
import me.romanow.lep500.ListBoxDialog;
import me.romanow.lep500.MainActivity;
import me.romanow.lep500.MultiListBoxDialog;
import me.romanow.lep500.MultiListBoxListener;
import romanow.lep500.FileDescription;
import romanow.lep500.FileDescriptionList;

public class MIFileBrowser extends MenuItem{
    private I_ArchiveMultiSelector2 procSelector;
    private String title;
    private int color;
    private String basePath;
    private String subPath;
    private ArrayList<File> extRootPaths = new ArrayList<>();
    public MIFileBrowser(MainActivity main0,int color0,String title0) {
        this(main0,color0,title0,null);
        }
    public void setProcSelector(I_ArchiveMultiSelector2 procSelector) {
        this.procSelector = procSelector;
        }
    public MIFileBrowser(MainActivity main0, int color0,String title0, I_ArchiveMultiSelector2 procSelector0) {
        super(main0);
        procSelector = procSelector0;
        title = title0;
        color = color0;
        main.addMenuList(new MenuItemAction(color,title) {
            @Override
            public void onSelect() {
                //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                //    currentDir = main.getApplicationContext().getExternalFilesDir(null);
                //else
                //    currentDir = Environment.getExternalStorageDirectory();
                //currentPath = currentDir.getAbsolutePath();
                final File[] appsDir=ContextCompat.getExternalFilesDirs(main,null);
                extRootPaths.clear();
                for(final File file : appsDir)
                    extRootPaths.add(file.getParentFile().getParentFile().getParentFile().getParentFile());
                final ArrayList<String> storages = new ArrayList<>();
                storages.add("Внутренняя память");
                for(int i=1;i<extRootPaths.size();i++)
                    storages.add("SD-карта"+(i>1 ? ""+(i-1) : ""));
                new ListBoxDialog(main, storages, "Память", new I_ListBoxListener() {
                    @Override
                    public void onSelect(int index) {
                        basePath = extRootPaths.get(index).getAbsolutePath();
                        subPath="";
                        procOneDir();
                        }
                    @Override
                    public void onLongSelect(int index) {}
                    @Override
                    public void onCancel() {}
                    }).create();
                }
            });
        }
    //------------------------------------------------------------------------------------
    private void procOneDir(){
        ArrayList<String> fileList = new ArrayList<>();
        fileList.add("..");
        String path = basePath+"/"+subPath;
        final ArrayList<FileDescription> files = main.createArchivePath(path,false);
        fileList.add("Файлов данных: "+files.size());
        final ArrayList<FileDescription> dirs = main.createDirArchive(path);
        for(FileDescription zz : dirs){
            fileList.add(zz.getOriginalFileName());
            }
        new ListBoxDialog(main, fileList, subPath, new I_ListBoxListener() {
            @Override
            public void onSelect(int index) {
                if (index==0){
                    int idx = subPath.lastIndexOf("/");
                    if (idx==-1)
                        return;
                    subPath = subPath.substring(0,idx);
                    procOneDir();
                    }
                if (index>1){
                    subPath += (subPath.length()==0 ? "" : "/")+dirs.get(index-2).getOriginalFileName();
                    procOneDir();
                    }
                if(index==1){
                    ArrayList<String> list = new ArrayList<>();
                    for(FileDescription ss : files)
                        list.add(ss.toString());
                    new MultiListBoxDialog(main,title,list,new MultiListBoxListener(){
                        @Override
                        public void onSelect(boolean[] selected) {
                            FileDescriptionList out = new FileDescriptionList() ;
                            for(int i=0;i<files.size();i++)
                                if (selected[i])
                                    out.add(files.get(i));
                            main.setDefferedList(out);
                            if (procSelector!=null)
                                procSelector.onSelect(basePath+"/"+subPath,out,false);
                            }
                        });
                    }
                }
            @Override
            public void onLongSelect(int index) {}
            @Override
            public void onCancel() {}
            }).create();
        }

}
