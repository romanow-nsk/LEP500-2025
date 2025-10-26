package me.romanow.lep500.service;

import static romanow.abc.core.Utils.createFatalMessage;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import me.romanow.lep500.FFTAdapter;
import me.romanow.lep500.FFTCallBackPlus;
import me.romanow.lep500.I_ListBoxListener;
import me.romanow.lep500.LEP500Settings;
import me.romanow.lep500.ListBoxDialog;
import me.romanow.lep500.R;
import romanow.abc.core.Utils;
import romanow.abc.core.constants.ConstValue;
import romanow.abc.core.constants.Values;
import romanow.lep500.AnalyseResult;
import romanow.lep500.FileDescription;
import romanow.lep500.FileDescriptionList;
import romanow.lep500.I_FDComparator;
import romanow.lep500.fft.ExtremeList;
import romanow.lep500.fft.FFT;
import romanow.lep500.FFTAudioTextFile;
import romanow.lep500.fft.FFTArray;
import romanow.lep500.fft.FFTParams;
import romanow.lep500.fft.FFTStatistic;
import romanow.lep500.fft.I_Notify;

public abstract class BaseActivity extends AppCompatActivity implements I_Notify {
    //------------------------------------------------------------------------------
    public final static int EmoSet = 0x1F6E0;
    public final static int EmoErr = 0x1F4A3;
    public Thread guiThread;
    private AppData ctx;
    private LineGraphView multiGraph=null;
    private boolean fullInfo=false;             // Вывод полной информации о спектре
    public final static int greatTextSize=20;   // Крупный шрифт
    public final static int middleTextSize=16;
    public final static int smallTextSize=12;
    private final static int paintColors[]={0x00007000,0x000000FF,0x00A00000,0x000070C0,0x00C000C0,0x00206060};
    public abstract void clearLog();
    public abstract void addToLog(String ss, int textSize);
    public abstract void addToLogHide(String ss);
    public abstract void addToLog(boolean fullInfoMes, final String ss, final int textSize, final int textColor);
    public abstract void popupAndLog(String ss);
    public abstract void showStatisticFull(FFTStatistic inputStat, int idx);
    public void addToLog(String ss){
        addToLog(false,ss,0,AppData.ApplicationTextColor);
        }
    protected ListBoxDialog menuDialog=null;
    private ArrayList<FFTStatistic> deffered = new ArrayList<>();
    private FileDescriptionList defferedList = null;    // Описатели файлов (список) для deffered
    public void errorMes(int emoCode,String text){
        addToLog(false,(emoCode==0 ? "" : (new String(Character.toChars(emoCode)))+" ")+text,14,0x00FF0000);
        }
    public void errorMes(String text){
        errorMes(EmoErr,text);
        }
    //--------------------------------------------------------------------------
    public BufferedReader openReader(String fname) throws IOException {
        FileInputStream fis = new FileInputStream(ctx.androidFileDirectory()+"/"+fname);
        return new BufferedReader(new InputStreamReader(fis, "Windows-1251"));
        }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        guiThread = Thread.currentThread();
        ctx = AppData.ctx();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(@NonNull @NotNull Thread t, @NonNull @NotNull Throwable e) {
                if (menuDialog!=null)
                    menuDialog.cancel();
                String ss = Utils.createFatalMessage(e);
                ctx.addBugMessage(ss);
                ctx.toLog(true,ss);
                //bigPopup("Фатальная ошибка",ss);
                //-------------- Перезапуск с сообщением о сбое ----------------------------
                ctx.set().fatalMessage = "Фатальная ошибка\n"+ss;
                saveContext();
                overLoad(true);
                }
            });
        }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(receiver);
        ctx.setCanSendPopup(false);
        }
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int drawId = intent.getIntExtra("drawId",0);
            String mes = intent.getStringExtra("mes");
            boolean toLog = intent.getBooleanExtra("toLog",false);
            boolean popup = intent.getBooleanExtra("popup",true);
            boolean error = intent.getBooleanExtra("error",false);
            if (toLog){
                if (error)
                    addToLog(false,mes,14,0x00FF0000);
                else
                    addToLog(false,mes,14,AppData.ApplicationTextColor);
                }
            if (popup)
                popupToast(drawId,mes);
            }
        };
    @Override public void onStart(){
        super.onStart();
        IntentFilter filter = new IntentFilter(AppData.Event_Popup);
        this.registerReceiver(receiver, filter);
        ctx.setCanSendPopup(true);
        }
    public int getPaintColor(int idx){
        if (idx < paintColors.length)
            return paintColors[idx];
        idx -= paintColors.length;
        int color = 0x00808080;
        while(idx--!=0 && color!=0)
            color-=0x00202020;
        return color;
        }
    public void paintOneSpectrum(LineGraphView graphView, int noFirst, double dfreq, double data[], int color){
        GraphView.GraphViewData zz[] = new GraphView.GraphViewData[data.length-noFirst];
        for(int j=noFirst;j<data.length;j++){                    // Подпись значений факторов j-ой ячейки
            double cfreq = j*dfreq;
            zz[j-noFirst] = new GraphView.GraphViewData(cfreq,data[j]);
            }
        GraphViewSeries series = new GraphViewSeries(zz);
        series.getStyle().color = color | 0xFF000000;
        graphView.addSeries(series);
        }
    public void paintOneWave(LineGraphView graphView, double data[], int color, int noFirst, int noLast){
        GraphView.GraphViewData zz[] = new GraphView.GraphViewData[data.length-noFirst-noLast];
        for(int j=noFirst;j<data.length-noLast;j++){                    // Подпись значений факторов j-ой ячейки
            double cfreq = j/100.;
            zz[j-noFirst] = new GraphView.GraphViewData(cfreq,data[j]);
            }
        GraphViewSeries series = new GraphViewSeries(zz);
        series.getStyle().color = color | 0xFF000000;
        graphView.addSeries(series);
        }
    public LinearLayout createMultiGraph(int resId,double procHigh){
        LinearLayout lrr=(LinearLayout)getLayoutInflater().inflate(resId, null);
        LinearLayout panel = (LinearLayout)lrr.findViewById(R.id.viewPanel);
        if (procHigh!=0){
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)panel.getLayoutParams();
            params.height = (int)(getResources().getDisplayMetrics().widthPixels*procHigh);
            panel.setLayoutParams(params);
            }
        multiGraph = new LineGraphView(this,"");
        multiGraph.setScalable(true);
        multiGraph.setScrollable(true);
        multiGraph.getGraphViewStyle().setTextSize(15);
        panel.addView(multiGraph);
        return lrr;
        }
    public LineGraphView getMultiGraph() {
        return multiGraph;
        }
    //--------- Отложенная отрисовка с нормализацией --------------------------------------------------
    private class LayoutSave{
        LinearLayout layout;
        }
    public void defferedStart(){
        deffered.clear();
        }
    public void defferedFinish(){
        normalize();
        for(int i=0;i<deffered.size();i++){
            final FFTStatistic statistic = deffered.get(i);
            if (!statistic.isValid())
                continue;
            int color = getPaintColor(i);
            String title = Values.constMap().getGroupMapByValue("EState").get(statistic.getFileDescription().getExpertNote()).title();
            if (title == null)
                title = "...";
            final LayoutSave save = new LayoutSave();
            LinearLayout expertButton = addToLogButton(title, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final ArrayList<ConstValue> list = Values.constMap().getGroupList("EState");
                    ArrayList<String> out = new ArrayList<>();
                    for(ConstValue value :list)
                        out.add(value.title());
                    new ListBoxDialog(BaseActivity.this, out, "Состояние опоры", new I_ListBoxListener() {
                        @Override
                        public void onSelect(int index) {
                            ConstValue cc = list.get(index);
                            statistic.getFileDescription().setExpertNote(cc.value());
                            Button bb = (Button)save.layout.findViewById(R.id.ok_button);
                            bb.setText(cc.title());
                            String vv = statistic.getFileDescription().refreshExpertNoteInFile(ctx.androidFileDirectory());
                            if (vv!=null)
                                addToLog(vv);
                            }
                        @Override
                        public void onLongSelect(int index) {}
                        @Override
                        public void onCancel() {}
                        }).create();
                    }
                });
            save.layout = expertButton;
            addToLog(false,String.format(defferedList.get(i).toString()),greatTextSize,color);
            showStatisticFull(statistic,i);
            paintOneSpectrum(multiGraph,statistic.getnFirst(),statistic.getFreqStep(),statistic.getNormalized(),getPaintColor(i));
            }
        }
    public void defferedAdd(FFTStatistic inputStat){
        deffered.add(inputStat);
        }
    public void normalize(){
        if (ctx.settings.groupNormalize) {
            double max = 0;
            for (FFTStatistic statistic : deffered) {
                if (!statistic.isValid())
                    continue;
                double max2 = statistic.normalizeStart(ctx.settings);
                if (max2 > max)
                    max = max2;
                }
            for (FFTStatistic statistic : deffered) {
                if (!statistic.isValid())
                    continue;
                statistic.normalizeFinish((float) max);
                }
            }
        else{
            for (FFTStatistic statistic : deffered) {
                if (!statistic.isValid())
                    continue;
                statistic.normalizeFinish(statistic.normalizeStart(ctx.settings));
                }
            }
        }
    public ArrayList<AnalyseResult>   analyse(){
        ArrayList<AnalyseResult> out = new ArrayList<>();
        for(FFTStatistic statistic : deffered) {
            AnalyseResult result = new AnalyseResult(statistic,null);
            result.setTitle(statistic.getMessage());
            if (statistic.isValid()){
                for (int mode = 0; mode < Values.extremeFacade.length; mode++) {
                    ExtremeList elist = statistic.createExtrems(mode, ctx.set());
                    elist.testAlarm2(ctx.set(), statistic.getFreqStep());
                    result.data.add(elist);
                    }
                }
            }
        return  out;
        }
    //-----------------------------------------------------------------------------------------------------
    public LinearLayout addToLogButton(String ss, View.OnClickListener listener) {
        return null;
        }
    //-----------------------------------------------------------------------------------------------------
    public void procArchive(FileDescription fd){
        String fname = fd.getOriginalFileName();
        try {
            FileInputStream fis = new FileInputStream(ctx.androidFileDirectory()+"/"+fname);
            processInputStream(fd,fis,fd.toString());
           } catch (Throwable e) {
                errorMes("Файл не открыт: "+fname+"\n"+createFatalMessage(e,10));
                }
        }
    public static String createFatalMessage(Throwable ee, int stackSize) {
        String ss = ee.toString() + "\n";
        StackTraceElement dd[] = ee.getStackTrace();
        for (int i = 0; i < dd.length && i < stackSize; i++) {
            ss += dd[i].getClassName() + "." + dd[i].getMethodName() + ":" + dd[i].getLineNumber() + "\n";
            }
        String out = "Программная ошибка:\n" + ss;
        return out;
        }

    public void processInputStream(FileDescription fd, InputStream is, String title) throws Throwable{
        processInputStream(true,fd,is,title,new FFTAdapter(fd,this,title));
        }
    int deciBells(double vv){
        int maxInt = 0x7FFF;
        return (int)(20*Math.log10(vv/maxInt));
        }
    public void processInputStream(boolean toLog,FileDescription fd, InputStream is, String title, FFTCallBackPlus adapter) throws Throwable{
        LEP500Settings set = ctx.set();
        FFTAudioTextFile xx = new FFTAudioTextFile();
        xx.setnPoints(ctx.settings.nTrendPoints);
        xx.readData(fd,new BufferedReader(new InputStreamReader(is, "Windows-1251")),false);
        adapter.getStatistic().setFreq(fd.getFileFreq());
        adapter.getStatistic().setWave(new FFTArray(xx.getData()));
        addToLogHide(fd.measureMetaData());
        xx.removeTrend(ctx.settings.nTrendPoints);
        long lnt = xx.getFrameLength();
        //for(p_BlockSize=1;p_BlockSize*FFT.Size0<=lnt;p_BlockSize*=2);
        //if (p_BlockSize!=1) p_BlockSize/=2;
        FFTParams params = new FFTParams().W(set.p_BlockSize* FFT.Size0).procOver(set.p_OverProc).
                compressMode(false).winMode(set.winFun).freqHZ(fd.getFileFreq()).autoCorrelate(set.autoCorrelation);
        FFT fft = new FFT();
        fft.setFFTParams(params);
        fft.calcFFTParams();
        if (toLog){
            addToLogHide("Отсчетов: "+xx.getFrameLength());
            addToLogHide("Кадр: "+ctx.settings.p_BlockSize*FFT.Size0);
            addToLogHide("Перекрытие: "+ctx.settings.p_OverProc);
            addToLogHide("Дискретность: "+String.format("%5.4f",fft.getStepHZLinear())+" гц");
            }
        fft.fftDirect(xx,adapter);
        }
    //--------------------------------------------------------------------------------------------------------
    public void bigPopup(String title, String text){
        AlertDialog.Builder messageBox = new AlertDialog.Builder(ctx.getContext());
        messageBox.setTitle(title);
        messageBox.setMessage(text);
        messageBox.setCancelable(false);
        messageBox.setNeutralButton("OK", null);
        messageBox.show();
        }
    public void popupToast(int viewId, String ss) {
        Toast toast3 = Toast.makeText(getApplicationContext(), ss, Toast.LENGTH_LONG);
        LinearLayout toastContainer = (LinearLayout) toast3.getView();
        if (toastContainer==null){
            toast3.show();
            return;
            }
        ImageView catImageView = new ImageView(getApplicationContext());
        TextView txt = (TextView)toastContainer.getChildAt(0);
        txt.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        txt.setGravity(Gravity.CENTER);
        catImageView.setImageResource(viewId);
        //txt.setTextColor(0xFFFFFF);
        //toastContainer.setBackgroundColor(AppData.ApplicationColor);
        toastContainer.addView(catImageView, 0);
        toastContainer.setOrientation(LinearLayout.HORIZONTAL);
        toastContainer.setGravity(Gravity.CENTER);
        toastContainer.setVerticalGravity(5);
        //toastContainer.setBackgroundResource(R.color.status_almostFree);
        toast3.setGravity(Gravity.BOTTOM, 0, 200);
        toast3.show();
        }
    public void popupInfo(final String ss) {
        guiCall(new Runnable() {
            @Override
            public void run() {
                popupToast(R.drawable.info,ss);
            }
        });
        }
    public void guiCall(Runnable code){
        if (Thread.currentThread()==guiThread)
            code.run();
        else
            runOnUiThread(code);
        }
    public void onMessage(String mes){
        addToLog( mes);
        }
    public void onError(Exception ee){
        errorMes(ee.toString());
        }
    public boolean isFullInfo() {
        return fullInfo; }
    public void setFullInfo(boolean fullInfo) {
        this.fullInfo = fullInfo; }
    public ArrayList<FFTStatistic> deffered() {
        return deffered; }
    public FileDescriptionList getDefferedList() {
        return defferedList; }
    public void setDefferedList(FileDescriptionList defferedList) {
        this.defferedList = defferedList; }
    //----------------------------------------------------------------------------------------------
    public FileDescriptionList createArchiveFull(){
        String path = ctx.androidFileDirectory();
        return createArchivePath(path,false,true);
        }
    public FileDescriptionList createArchive(){
        return createArchive(null);
        }
    public FileDescriptionList createArchive(String subdir){
        String path = ctx.androidFileDirectory()+(subdir!=null ? "/"+subdir : "");
        return createArchivePath(path,false);
        }
    public FileDescriptionList createArchivePath(String path, boolean trace){
        return createArchivePath(path,trace,false);
        }
    public FileDescriptionList createArchivePath(String path, boolean trace,boolean full){
        File ff = new File(path);
        if (!ff.exists()) {
            ff.mkdir();
            }
        FileDescriptionList out = new FileDescriptionList();
        String vv[] = ff.list();
        if (vv==null)
            return out;
        for(String ss : vv){
            File file = new File(path+"/"+ss);
            if (file.isDirectory())
                continue;
            FileDescription dd = new FileDescription(ss);
            if (dd.getFormatError().length()!=0)
                continue;
            String zz = dd.validDescription();
            if (zz.length()!=0){
                if (trace)
                    addToLog("Файл: "+ss+"\n"+zz);
                }
            if (full){
                try {
                    FFTAudioTextFile xx = new FFTAudioTextFile();
                    FileInputStream fis = new FileInputStream(ctx.androidFileDirectory() + "/" + dd.getOriginalFileName());
                    xx.readData(dd, new BufferedReader(new InputStreamReader(fis, "Windows-1251")), false);
                    } catch (Exception ee){
                        addToLog("Файл: "+dd.getOriginalFileName()+"\n"+ee.toString());
                        }
                }
            out.add(dd);
            }
        out.sort(new I_FDComparator() {
            @Override
            public int compare(FileDescription o2, FileDescription o1) {
                return (int)(o2.getCreateDate().timeInMS() - o1.getCreateDate().timeInMS());
                }
            });
        return out;
        }
    public FileDescriptionList createDirArchive(){
        return createDirArchive(ctx.androidFileDirectory());
        }
    public FileDescriptionList createDirArchive(String path){
        File ff = new File(path);
        if (!ff.exists()) {
            ff.mkdir();
            }
        FileDescriptionList out = new FileDescriptionList();
        String zz[] = ff.list();
        if (zz==null)
            return out;
        for(String ss : zz){
            if (AppData.SubDirList.get(ss)!=null)
                continue;
            File file = new File(path+"/"+ss);
            if (file.isDirectory())
                out.add(new FileDescription(ss));
            }
        out.sort(new I_FDComparator() {
            @Override
            public int compare(FileDescription o2, FileDescription o1) {
                return o1.getOriginalFileName().compareTo(o2.getOriginalFileName());
                }
            });
        return out;
        }

    public void saveContext(){
        ctx.fileService.saveContext();
        }
    public void loadContext(){
        ctx.fileService.loadContext();
        }
    public void overLoad(boolean kill){
        //------------------------------- перезагрузка -------------------------
        Context context = getApplicationContext();
        /*
        Intent mStartActivity = new Intent(context, MainActivity.class);
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId,mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_MUTABLE);
        AlarmManager mgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 5000, mPendingIntent);
         */
        PackageManager packageManager = context.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(context.getPackageName());
        ComponentName componentName = intent.getComponent();
        Intent mainIntent = Intent.makeRestartActivityTask(componentName);
        context.startActivity(mainIntent);
        if (kill)
            android.os.Process.killProcess(android.os.Process.myPid());
        else
            Runtime.getRuntime().exit(0);               //finish();
        //-----------------------------------------------------------------------
        }

}
