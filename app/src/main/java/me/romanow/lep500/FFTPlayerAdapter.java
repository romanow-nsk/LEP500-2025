package me.romanow.lep500;

import me.romanow.lep500.service.AppData;
import me.romanow.lep500.service.BaseActivity;
import romanow.lep500.FFTAudioTextFile;
import romanow.lep500.fft.*;

import static me.romanow.lep500.MainActivity.createFatalMessage;

public class FFTPlayerAdapter implements FFTCallBackPlus {
    private FFTStatistic inputStat;
    private MainActivity main;
    private String outFile;
    public FFTPlayerAdapter(BaseActivity main0, String title, String outFile0){
        inputStat = new FFTStatistic(title);
        main = (MainActivity) main0;
        outFile = outFile0;
        }
    @Override
    public void onStart(double msOnStep) {}
    @Override
    public void onFinish() {
        if (inputStat.getCount()==0){
            main.popupAndLog("Настройки: короткий период измерений/много блоков");
            return;
            }
        inputStat.smooth(AppData.ctx().set().kSmooth);
        double max = inputStat.normalizeStart(AppData.ctx().set());
        inputStat.normalizeFinish(max);
        int sz = inputStat.getMids().length;
        ExtremeList list = inputStat.createExtrems(FFTStatistic.ExtremeAbsMode,AppData.ctx().set());
        if (list.data().size()==0){
            main.addToLog("Экстремумов не найдено");
            return;
            }
        Extreme extreme;
        int nFirstMax=10;
        int count = nFirstMax < list.data().size() ? nFirstMax : list.data().size();
        double ampl[] = new double[count];
        double freq[] = new double[count];
        for(int i=0;i<count;i++){
            ampl[i]=list.data().get(i).value*10000;
            freq[i]=list.data().get(i).idx*inputStat.getFreqStep();
            }
        FFTAudioSource harmonic = new FFTHarmonic(AppData.ctx().set().measureFreq,freq,ampl,200,0);
        FFTAudioTextFile xx = new FFTAudioTextFile();
        xx.setnPoints(AppData.ctx().set().nTrendPoints);
        xx.convertToWave(harmonic, outFile, main);
        }
    @Override
    public boolean onStep(int nBlock, int calcMS, double totalMS, FFT fft) {
        long tt = System.currentTimeMillis();
        inputStat.setFreqStep(fft.getStepHZLinear());
        double lineSpectrum[] = fft.getSpectrum();
        boolean xx;
        try {
            inputStat.addStatistic(lineSpectrum);
            } catch (Exception ex) {
                main.addToLog(createFatalMessage(ex,10));
                return false;
                }
        return true;
        }
    @Override
    public void onError(Exception ee) {
        main.errorMes(createFatalMessage(ee,10));
    }
    @Override
    public void onMessage(String mes) {
        main.addToLogHide(mes);
        }

    @Override
    public FFTStatistic getStatistic() {
        return inputStat;
    }
}
