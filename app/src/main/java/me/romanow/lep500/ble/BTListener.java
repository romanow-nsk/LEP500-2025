package me.romanow.lep500.ble;


import romanow.lep500.FFTAudioTextFile;

public interface BTListener {
    public void notify(BTReceiver sensor, boolean fullInfo, String ss);
    public void onReceive(BTReceiver sensor, FFTAudioTextFile file);
    public void onState(BTReceiver sensor, int state);
    public void onStateText(BTReceiver sensor, String text);
    public void onPopup(BTReceiver sensor, String text);
}
