package me.romanow.lep500.service;

import me.romanow.lep500.MainActivity;
import romanow.abc.core.utils.GPSPoint;

public interface I_GPSService {
    public void startService(MainActivity main0);
    public void stopService();
    public GPSPoint lastGPS();
    }