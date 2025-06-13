package me.romanow.lep500.service;

import static androidx.core.content.ContextCompat.checkSelfPermission;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;

import org.joda.time.DateTime;

import me.romanow.lep500.MainActivity;
import romanow.abc.core.utils.GPSPoint;

public class GPSService11 implements I_GPSService{
    public final static int GPSInterval = 10;                 // Интервал опроса GPS-координат (сек)
    public final static int GPSDistance = 20;                 // Интервал изменения координат (м)
    public final static int GPSValidDelay = 10;               // Интервал валидности GPS (мин)
    private boolean gpsOn = false;
    private LocationManager mLocationManager = null;
    GnssStatus.Callback mGnssStatusCallback;
    private GPSPoint lastGPSGeo = new GPSPoint();
    private GPSPoint lastGPSNet = new GPSPoint();
    private Context context;
    private int satCount=0;
    private DateTime lastGPSTime = new DateTime();
    private AppData ctx;
    private Handler event = new Handler();
    private MainActivity main;
    public void startService(MainActivity main0) {
        main = main0;
        context = main;
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (mLocationManager == null) {
            AppData.ctx().popupAndLog(true, "Недоступен менеджер местоположения/навигации");
            return;
        }
        mGnssStatusCallback = new GnssStatus.Callback() {};
        try {
            mLocationManager.registerGnssStatusCallback(mGnssStatusCallback);
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPSInterval*1000, 0, locationListener);
            main.setDelay(GPSInterval, gpsClock);
            gpsOn = true;
        } catch (Exception ee) {
            AppData.ctx().popupAndLog(true, "Ошибка GPS-сервиса: " + ee.toString());
        }
    }
    public void stopService() {
        gpsOn = false;
        if (mLocationManager != null)
            mLocationManager.removeUpdates(locationListener);
        mLocationManager.unregisterGnssStatusCallback(mGnssStatusCallback);
        mLocationManager = null;
    }

    public GPSPoint lastGPS() {
        GPSPoint notValid = new GPSPoint();
        if (!lastGPSGeo.gpsValid() && !lastGPSNet.gpsValid())
            return notValid;
        long delay = lastGPSGeo.elapsedTimeInSec()/60;
        if (lastGPSGeo.gpsValid() && delay  <GPSValidDelay)
            return lastGPSGeo;
        return lastGPSNet;          //38 - если нет от сети
    }

    Runnable gpsClock = new Runnable() {
        public void run() {
            procLocation(null);
            if (gpsOn)
                main.setDelay(GPSInterval,gpsClock);
        }
    };

    public void procLocation(Location loc) {
        try {
            if (mLocationManager == null) {
                return;
            }
            Location cL;
            if (checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    Activity#requestPermissions
                AppData.ctx().popupAndLog(true,"Установите разрешения GPS");
                return;
            }
            GPSPoint old = lastGPS();
            cL = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (cL != null){
                lastGPSGeo = new GPSPoint(cL.getLatitude(),cL.getLongitude(),true,cL.getTime());
                lastGPSTime = new DateTime();
            }
            else
                lastGPSGeo = new GPSPoint();
            cL= mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (cL==null)
                lastGPSNet = new GPSPoint();
            else{
                lastGPSNet = new GPSPoint(cL.getLatitude(),cL.getLongitude(),false,cL.getTime());
                lastGPSTime = new DateTime();
            }
            GPSPoint gpsPoint = lastGPS();
            AppData.ctx().sendGPS(gpsPoint);
            AppData.ctx().setLastGPS(gpsPoint);
        }
        catch(Throwable ee){
            AppData.ctx().popupAndLog(true,"Ошибка GPS-сервиса: "+ee.toString());
            stopService();
        }
    }

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            //main.addToLog(location.toString());
            procLocation(location);
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            //main.addToLog(provider+" "+status);
            }
        @Override
        public void onProviderEnabled(String provider) {
            //main.addToLog(provider+" enabled");
            }
        @Override
        public void onProviderDisabled(String provider) {
            //main.addToLog(provider+" disabled");
        }
    };

}
