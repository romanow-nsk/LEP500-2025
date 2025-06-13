package me.romanow.lep500;

import romanow.lep500.fft.FFTCallBack;
import romanow.lep500.fft.FFTStatistic;

public interface FFTCallBackPlus extends FFTCallBack {
    public FFTStatistic getStatistic();
}
