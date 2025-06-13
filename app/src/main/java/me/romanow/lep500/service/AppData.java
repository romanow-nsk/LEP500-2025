
package me.romanow.lep500.service;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

import me.romanow.lep500.LEP500Settings;
import me.romanow.lep500.LoginSettings;
import me.romanow.lep500.R;
import me.romanow.lep500.StoryList;
import romanow.abc.core.API.RestAPIBase;
import romanow.abc.core.API.RestAPILEP500;
import romanow.abc.core.Utils;
import romanow.abc.core.constants.Values;
import romanow.abc.core.entity.base.BugList;
import romanow.abc.core.entity.base.BugMessage;
import romanow.abc.core.entity.subjectarea.WorkSettings;
import romanow.abc.core.utils.GPSPoint;
import romanow.abc.core.utils.OwnDateTime;
import romanow.lep500.FileDescriptionList;

public class AppData extends Application {
    public final static String apkVersion = "3.0.01, 13.06.2025";
    private final static String codeGenPassword="pi31415926";
    public final static String MAPKIT_API_KEY = "fda3e521-bbc6-4c75-9ec7-ccd4fdaa34d3";
    public final static int PopupShortDelay=4;              // Время короткого popup
    public final static int PopupMiddleDelay=7;             // Время длинного popup
    public final static int PopupLongDelay=10;              // Время длинного popup
    public final static int CKeepALiveTime=10;     // Интервал времени проверки соединения
    public final static int ApplicationBackColor=0xFF008577;
    public final static int ApplicationTextColor=0x00007020;
    public final static String NeuralNetworkFile="expert.model";
    public final static int ColorCommon = 0xFFEEE8AA;           // PaleGoldenrod
    public final static int ColorArchive = 0xFFFFD700;
    public final static int ColorServer = 0xFF87CEFA;
    public final static int ColorMeasure = 0xFFE0FFFF;
    //---------------------------------------------------------------------------------------------
    public final static int CStateGray=0;          // Состояние соединения не определено
    public final static int CStateRed=1;           // Нет соединения
    public final static int CStateYellow=2;        // Восстановление
    public final static int CStateGreen=3;         // Есть соединение
    public final static int CStatesRes[]=new int[]{
            R.drawable.status_gray,
            R.drawable.status_red,
            R.drawable.status_yellow,
            R.drawable.status_green,
        };
    public final static int CNetRes[]=new int[]{
            R.drawable.ballgray,
            R.drawable.ballred,
            R.drawable.ballyellow,
            R.drawable.ballgreen,
    };
    public final static int StorySize=50;           // Размерность сохраненных событий
    public final static int HTTPTimeOut=30;
    public final static int FatalExceptionStackSize=20;
    //-------------------------------------------- События -------------------------------------------------------------
    public final static String Event_CState="me.romanow.CState";           // Изменнеие состояния сети
    public final static String Event_Clock="me.romanow.Clock";             // Таймер
    public final static String Event_GPS="me.romanow.GPS";                 // Изменение состояния GPS
    public final static String Event_Popup="me.romanow.Popup";             // Всплывающее сообщение, параметры header, text
    //----------------------------------------------------------------------------
    public final static ArrayList<String> WinFuncList=new ArrayList<>();{
        WinFuncList.add("Прямоугольник");
        WinFuncList.add("Треугольник");
        WinFuncList.add("Синус");
        WinFuncList.add("Парабола");
        }
    public final static String mapkitDir = "mapkit";
    public final static String waveDir = "wave";
    public final static String excelDir = "xls";
    public final static HashMap<String,Object> SubDirList=new HashMap<>();{
        SubDirList.put(mapkitDir,mapkitDir);
        SubDirList.put(excelDir,excelDir);
        SubDirList.put(waveDir,waveDir);
        }
    public final static HashMap<Integer,Integer> StateColors=new HashMap<>();{
        StateColors.put(Values.MSUndefined,R.drawable.status_gray);
        StateColors.put(Values.MSNormal,R.drawable.status_green);
        StateColors.put(Values.MSNormalMinus,R.drawable.status_light_green);
        StateColors.put(Values.MSNoise,R.drawable.status_gray);
        StateColors.put(Values.MSLowLevel,R.drawable.status_gray);
        StateColors.put(Values.MSNoPeak,R.drawable.status_gray);
        StateColors.put(Values.MSSecond1,R.drawable.status_red);
        StateColors.put(Values.MSSecond2,R.drawable.status_yellow);
        StateColors.put(Values.MSSumPeak1,R.drawable.status_light_red);
        StateColors.put(Values.MSSumPeak2,R.drawable.status_light_yellow);
        StateColors.put(Values.MSFail,R.drawable.status_red);
        }
    public final static HashMap<Integer,Integer> StateWeight=new HashMap<>();{
        StateWeight.put(R.drawable.status_gray,0);
        StateWeight.put(R.drawable.status_green,4);
        StateWeight.put(R.drawable.status_light_green,3);
        StateWeight.put(R.drawable.status_yellow,-2);
        StateWeight.put(R.drawable.status_light_yellow,-1);
        StateWeight.put(R.drawable.status_light_red,-3);
        StateWeight.put(R.drawable.status_red,-4);
        }
    /*
    public final static int FuncNoSim=0;        // Нет данных сим-карты
    public final static int FuncNoServerSim=1;  // сим-карта не зарегистрирована
    public final static int FuncAnotherSim=2;   // данные сим-карт не совпадают
    public final static int FuncNormal=3;       // сим-карта зарегистрирована
    public final String FuncStates[]={
        "Нет данных сим-карты",
        "Сим-карта не зарегистрирована",
        "Посторонняя сим-карта",
        "Cим-карта зарегистрирована"
        };
    */
    //------------------------------------------------------------------------------
    BugList fatalMessages = new BugList();
    WorkSettings workSettings = new WorkSettings();
    StoryList storyList = new StoryList();
    LEP500Settings settings = new LEP500Settings();
    LoginSettings loginSettings = new LoginSettings();
    Object appSynch = new Object();
    RestAPIBase service = null;
    RestAPILEP500 service2 = null;
    private boolean canSendPopup=false;
    //---------------------------------------------------------------------------------
    private int cState = AppData.CStateGray;               // Состояние соединения
    public synchronized int cState() { return cState; }
    public synchronized void cState(int cState) {
        this.cState = cState;
        Intent intent = new Intent();
        intent.setAction(Event_CState);
        context.sendBroadcast(intent);
        }
    //------------------------------------------------------------------------------
    FileService fileService = new FileService(this);
    private String registrationCode="";         // Хэш-код регистрации приложения
    private boolean registeredOnServer=false;   //
    //-----------------------------------------------------------------------------
    private AppData(){}
    private static AppData ctx = null;          // ГОРЯЧИЙ РЕСТАРТ - обнулить stopApplication
    public static AppData ctx(){
        if (ctx==null){
            ctx = new AppData();
            ctx.startApplication();
            }
        return ctx;
        }
    public void setCanSendPopup(boolean canSendPopup) {
        this.canSendPopup = canSendPopup; }
    public boolean isCanSendPopup() {
        return  canSendPopup; }
    //----------------------------------------------------------------------------
    private FileDescriptionList fileList = new FileDescriptionList();   // Мультивыборка
    private volatile boolean busy = false;              // Признак выполнения фоновой операции
    private String actualVersion = "";
    private int satCount = 0;                           // Количество спутников
    private GPSPoint lastGPS=new GPSPoint();
    private Thread.UncaughtExceptionHandler oldHandler;
    private boolean applicationOn = false;              // Приложение работает
    private Context context;
    //-----------------------------------------------------------------------------------
    public GPSPoint getLastGPS() {
        return lastGPS; }
    public void setLastGPS(GPSPoint lastGPS) {
        this.lastGPS = lastGPS; }
    public Context getContext() {
        return context; }
    public void setContext(Context context) {
        this.context = context; }
    public final String androidFileDirectory(){
        return context.getExternalFilesDir(null).getAbsolutePath();
        }
    public FileDescriptionList getFileList() {
        return fileList; }
    public void setFileList(FileDescriptionList fileList) {
        this.fileList = fileList; }
    public boolean isApplicationOn() {
        return applicationOn;
    }
    public synchronized boolean isBusy() { return busy; }
    public synchronized void setBusy(boolean busy) { this.busy = busy; }
    public int getSatCount() { return satCount; }
    public String getActualVersion() { return actualVersion; }
    public void setActualVersion(String actualVersion) { this.actualVersion = actualVersion; }
    public Thread.UncaughtExceptionHandler getUncaughtExceptionHandler() { return oldHandler; }
    public LEP500Settings set(){ return settings; }
    public LoginSettings loginSettings(){ return loginSettings; }
    public FileService getFileService() { return fileService; }
    public RestAPIBase getService() { return service; }
    public RestAPILEP500 getService2() { return service2; }
    public void service(RestAPIBase service) { this.service = service; }
    public void service2(RestAPILEP500 service2) { this.service2 = service2; }
    public String getCodeGenPassword() { return codeGenPassword; }
    public boolean isRegisteredOnServer() { return registeredOnServer; }
    public void setRegisteredOnServer(boolean registeredOnServer) { this.registeredOnServer = registeredOnServer; }

    @Override
    public void onTerminate() {
        super.onTerminate();
        stopApplication();
        }

    @SuppressLint("MissingPermission")
    public void startApplication() {
        applicationOn=true;
        }
    public FileService fileService(){ return fileService; }
    //============================================================================================================================
    public void clearSettings(){
        settings = new LEP500Settings();
        fileService.saveJSON(settings);
        }
    public void stopApplication() {
        applicationOn = false;
        ctx = null;                     // ГОРЯЧИЙ РЕСТАРТ - повторная инициализация синглетона
        }

    public void sendPopup(int drawId, boolean error, boolean popup, boolean tolog, String mes){
        if (!canSendPopup)
            return;
        Intent intent = new Intent();
        intent.setAction(Event_Popup);
        intent.putExtra("error",error);
        intent.putExtra("drawId",drawId);
        intent.putExtra("toLog",tolog);
        intent.putExtra("popup",popup);
        intent.putExtra("mes",mes);
        context.sendBroadcast(intent);
        }
    public void sendGPS(GPSPoint gpsPoint){
        Intent intent = new Intent();
        intent.setAction(Event_GPS);
        intent.putExtra("gpsX",gpsPoint.geox());
        intent.putExtra("gpsY",gpsPoint.geoy());
        intent.putExtra("state",gpsPoint.state());
        context.sendBroadcast(intent);
        }
    //-------------------------------------------------------------------------------------
    public void addBugMessage(String ss) {
        BugMessage bug = new BugMessage(AppData.ctx().loginSettings.getUserId(), new OwnDateTime(), ss);
        while (fatalMessages.size() > StorySize)
            fatalMessages.remove(0);
        fatalMessages.add(bug);
        fileService.saveJSON(fatalMessages);
        }
    public void addStoryMessage(String mes) {
        while (storyList.size() > StorySize)
            storyList.remove(0);
        storyList.add(new BugMessage(mes));
        fileService.saveJSON(mes);
        }
    public String createBugMessage(String mes) {
        String ss = "Фиксация ошибки: "+mes + "\n";
        StackTraceElement dd[] = Thread.currentThread().getStackTrace();
        for (int i = 0; i < dd.length && i < AppData.FatalExceptionStackSize; i++) {
            ss += dd[i].getClassName() + "." + dd[i].getMethodName() + ":" + dd[i].getLineNumber() + "\n";
            }
        addBugMessage(ss);
        return ss;
        }
    public String createFatalMessage(Throwable ee) {
        String ss = ee.toString() + "\n";
        StackTraceElement dd[] = ee.getStackTrace();
        for (int i = 0; i < dd.length && i < AppData.FatalExceptionStackSize; i++) {
            ss += dd[i].getClassName() + "." + dd[i].getMethodName() + ":" + dd[i].getLineNumber() + "\n";
            }
        String out = "Необработанное исключение:\n" + ss;
        addBugMessage(out);
        return out;
        }
    public void popupToastFatal(Exception ee){
        String ss = Utils.createFatalMessage(ee);
        toLog(true,"Ошибка: "+ee.toString()+"\n"+ss);
        addBugMessage(ss);
        }
    public void popupAndLog(boolean fatal,String ss){
        sendPopup(fatal ? R.drawable.problem : R.drawable.info,false,true,true,ss);
        }
    public void popup(boolean fatal,String ss){
        sendPopup(fatal ? R.drawable.problem : R.drawable.info,fatal,true,false,ss);
        }
    public void toLog(boolean fatal,String ss){
        sendPopup(fatal ? R.drawable.problem : R.drawable.info,fatal,false,true,ss);
        }
    //---------------------------------------------------------------------------------------
}
