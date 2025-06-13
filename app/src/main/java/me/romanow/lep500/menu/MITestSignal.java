package me.romanow.lep500.menu;

import me.romanow.lep500.FFTAdapter;
import me.romanow.lep500.LEP500Settings;
import me.romanow.lep500.MainActivity;
import me.romanow.lep500.R;
import me.romanow.lep500.service.AppData;
import romanow.abc.core.constants.Values;
import romanow.lep500.AnalyseResult;
import romanow.lep500.AnalyseResultList;
import romanow.lep500.FileDescription;
import romanow.lep500.fft.*;

import static me.romanow.lep500.MainActivity.ViewProcHigh;

import java.util.ArrayList;

public class MITestSignal extends MenuItem{
    public MITestSignal(MainActivity main0) {
        super(main0);
        //if (!AppData.ctx().set().fullInfo)
        //    return;
        main.addMenuList(new MenuItemAction("Тестовый сигнал") {
            @Override
            public void onSelect() {
                LEP500Settings set = AppData.ctx().set();
                main.log().addView(main.createMultiGraph(R.layout.graphview,ViewProcHigh));
                main.defferedStart();
                FFTParams params = new FFTParams().W(set.p_BlockSize* FFT.Size0).procOver(set.p_OverProc).
                            compressMode(false).winMode(set.winFun).freqHZ(set.measureFreq).autoCorrelate(set.autoCorrelation);
                FFT fft = new FFT();
                fft.setFFTParams(params);
                fft.calcFFTParams();
                double hz[]={3,5,8,13,21,34,48};
                double ampl[]={1,1,1,1,1,1,1};
                double dHz = set.measureFreq/(set.p_BlockSize*FFT.Size0);
                FFTAudioSource source = new FFTHarmonic(set.measureFreq,hz,ampl,set.measureDuration, dHz);
                main.addToLogHide("Отсчетов: "+source.getFrameLength());
                main.addToLogHide("Кадр: "+set.p_BlockSize*FFT.Size0);
                main.addToLogHide("Перекрытие: "+set.p_OverProc);
                main.addToLogHide("Дискретность: "+String.format("%5.4f",fft.getStepHZLinear())+" гц");
                FileDescription fd = new FileDescription("");
                FFTAdapter adapter = new FFTAdapter(fd,main,title);
                fft.fftDirect(source,adapter);
                FFTStatistic statistic = adapter.getStatistic();
                statistic.setFreq(set.measureFreq);
                //ArrayList<AnalyseResult> results = main.analyse();
                main.setFullInfo(true);
                main.normalize();
                main.showStatistic(main.deffered().get(0),0);
                main.paintOneSpectrum(main.getMultiGraph(),0,fft.getStepHZLinear(),main.deffered().get(0).getNormalized(), main.getPaintColor(0));
                }
            });
        }

}
