package me.romanow.lep500.ble;

import romanow.abc.core.utils.GPSPoint;
import romanow.lep500.FFTAudioTextFile;
import romanow.lep500.LEP500Params;

public class BTTextFile extends FFTAudioTextFile {
    @Override
    public void setData(short dd[]){
        data = new double[dd.length];
        for(int i=0;i<dd.length;i++)
            data[i] = dd[i];
        }
    public BTTextFile(LEP500Params settings0, String sensorName0, GPSPoint gps0){
        super(settings0,sensorName0,gps0);
    }
}
