package me.romanow.lep500.service;

import romanow.abc.core.utils.GPSPoint;

public interface GPSListener {
    public void onEvent(String ss);
    public void onGPS(GPSPoint gpsPoint);
}
