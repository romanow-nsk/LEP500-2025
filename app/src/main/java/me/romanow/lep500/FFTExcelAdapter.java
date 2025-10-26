package me.romanow.lep500;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.PresetLineDash;
import org.apache.poi.xddf.usermodel.XDDFLineProperties;
import org.apache.poi.xddf.usermodel.XDDFPresetLineDash;
import org.apache.poi.xddf.usermodel.chart.AxisPosition;
import org.apache.poi.xddf.usermodel.chart.ChartTypes;
import org.apache.poi.xddf.usermodel.chart.LegendPosition;
import org.apache.poi.xddf.usermodel.chart.MarkerStyle;
import org.apache.poi.xddf.usermodel.chart.XDDFCategoryAxis;
import org.apache.poi.xddf.usermodel.chart.XDDFChartLegend;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory;
import org.apache.poi.xddf.usermodel.chart.XDDFLineChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFNumericalDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFValueAxis;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import me.romanow.lep500.service.AppData;
import me.romanow.lep500.service.BaseActivity;
import romanow.abc.core.constants.Values;
import romanow.abc.core.utils.Pair;
import romanow.lep500.FileDescription;
import romanow.lep500.fft.*;

import static me.romanow.lep500.MainActivity.createFatalMessage;

public class FFTExcelAdapter implements FFTCallBackPlus {
    private FFTStatistic inputStat;
    private MainActivity main;
    private FileDescription fd;
    public String createOriginalExcelFileName(){
        String dirName = AppData.ctx().androidFileDirectory()+"/"+ AppData.excelDir;
        File ff = new File(dirName);
        if (!ff.exists()){
            ff.mkdir();
            }
        String pathName = dirName+"/"+fd.getOriginalFileName();
        int k = pathName.lastIndexOf(".");
        pathName = pathName.substring(0, k) + ".xlsx";
        return pathName;
        }
    public FFTExcelAdapter(BaseActivity main0, String title, FileDescription fd0){
        inputStat = new FFTStatistic(title);
        inputStat.setFreq(fd0.getFileFreq());
        main = (MainActivity) main0;
        fd = fd0;
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
        XSSFWorkbook workbook = new XSSFWorkbook();
        //------------------------------------------------------------------------------------------
        XSSFSheet sheet = workbook.createSheet("Параметры");
        Row hd;
        hd = sheet.createRow(0);
        hd.createCell(0).setCellValue("Геолокация: "+fd.getGps().toString());
        hd = sheet.createRow(1);
        hd.createCell(0).setCellValue("Линия: "+fd.getPowerLine());
        hd = sheet.createRow(2);
        hd.createCell(0).setCellValue("Опора: "+fd.getSupport());
        hd = sheet.createRow(2);
        hd.createCell(0).setCellValue("Дата создания: "+fd.getCreateDate().dateTimeToString());
        hd = sheet.createRow(3);
        hd.createCell(0).setCellValue("Частота: "+String.format("%6.2f",fd.getFileFreq()));
        hd = sheet.createRow(4);
        hd.createCell(0).setCellValue("Датчик: "+fd.getSensor());
        hd = sheet.createRow(5);
        hd.createCell(0).setCellValue("Номер измерения: "+fd.getMeasureCounter());
        double dd[] = inputStat.getNormalized();
        hd = sheet.createRow(6);
        hd.createCell(0).setCellValue("Частот в спектре: "+dd.length);
        Pair<String,Integer> ss = list.testAlarm2(AppData.ctx().set(),inputStat.getFreqStep());
        if (ss.o1!=null){
            hd = sheet.createRow(7);
            hd.createCell(0).setCellValue(ss.o1);
            }
        //------------------------------------------------------------------------------------------
        sheet = workbook.createSheet("Спектр");
        double ff0 = AppData.ctx().set().FirstFreq;
        double ff1 = AppData.ctx().set().LastFreq;
        double step = inputStat.getFreqStep();
        int idx0=inputStat.getnFirst();
        for(int i=0;i<dd.length-idx0;i++){
            hd = sheet.createRow(i);
            hd.createCell(0).setCellValue(""+(i+1));
            hd.createCell(1).setCellValue(ff0+i*step);
            hd.createCell(2).setCellValue(dd[i+idx0]);
            }
        //------------------------------------------------------------------------------------------
        // Создаем холст
        XSSFDrawing drawing = sheet.createDrawingPatriarch();
        // Первые четыре значения по умолчанию: 0, [0,5]: начало с 0 столбца и 5 строк; [7,26]: ширина 7 ячеек, 26 расширяется до 26 строк
        // Ширина по умолчанию (14-8) * 12
        XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 4, 5, 20, 40);
        // Создаем объект диаграммы
        XSSFChart chart = drawing.createChart(anchor);
        //Заголовок
        chart.setTitleText ("Спектр");
        // перезапись заголовка
        chart.setTitleOverlay(false);
        // Положение легенды
        XDDFChartLegend legend = chart.getOrAddLegend();
        legend.setPosition(LegendPosition.TOP);
        // Метка оси классификации (ось X), позиция заголовка
        XDDFCategoryAxis bottomAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
        bottomAxis.setTitle ("Частота");
        // Ось значений (ось Y), позиция заголовка
        XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);
        leftAxis.setTitle ("Амплитуда");
        // CellRangeAddress (номер начальной строки, номер конечной строки, номер начального столбца, номер конечного столбца)
        // Данные оси классификации (ось X), положение диапазона ячеек от [0, 0] до [0, 6]
        //Double dd2[] = new Double[dd.length];
        //Double dd3[] = new Double[dd.length];
        //for(int i=0;i<dd.length;i++){
        //    dd2[i]=new Double(dd[i]);
        //    dd3[i]=new Double(ff0+i*step);
        //    }
        //XDDFNumericalDataSource<Double> ff2 = XDDFDataSourcesFactory.fromArray(dd2);
        //XDDFNumericalDataSource<Double> ff3 = XDDFDataSourcesFactory.fromArray(dd3);
        XDDFNumericalDataSource<Double> ff2 = XDDFDataSourcesFactory.fromNumericCellRange(sheet, new CellRangeAddress(0, dd.length-1-idx0, 2, 2));
        XDDFNumericalDataSource<Double> ff3 = XDDFDataSourcesFactory.fromNumericCellRange(sheet, new CellRangeAddress(0, dd.length-1-idx0, 1, 1));
        // LINE: линейный график,
        XDDFLineChartData data = (XDDFLineChartData) chart.createData(ChartTypes.LINE, bottomAxis, leftAxis);
        // Данные загрузки графика, пунктирная линия 1
        XDDFLineChartData.Series series1 = (XDDFLineChartData.Series) data.addSeries(ff3, ff2);
        series1.setTitle (fd.getOriginalFileName(), null);
        //Прямой
        series1.setSmooth(true);
        // Устанавливаем размер метки
        //series1.setMarkerSize((short) 6);
        // Устанавливаем стиль метки, звездочки
        series1.setMarkerStyle(MarkerStyle.CIRCLE);
        //Рисовать
        chart.plot(data);
        //------------------------------------------------------------------------------------------
        for(int i = 0; i< Values.extremeFacade.length; i++)
            writeExtrems(i,workbook);
        //------------------------------------------------------------------------------------------
        String pathName = createOriginalExcelFileName();
        try (FileOutputStream out = new FileOutputStream(new File(pathName))) {
            workbook.write(out);
            out.close();
            } catch (IOException e) { main.addToLog("Ошибка экспорта: "+e.toString());  }
        main.addToLog("Экспорт в Excel: "+fd.getOriginalFileName());
        }
    private void writeExtrems(int mode, XSSFWorkbook workbook){
        String ss="";
        try {
            ss=((ExtremeFacade)Values.extremeFacade[mode].newInstance()).getTitle();
            } catch (Exception ee){ }
        XSSFSheet sheet = workbook.createSheet(ss);
        int sz = inputStat.getMids().length;
        Row hd;
        int rowNum=0;
        hd = sheet.createRow(rowNum++);
        hd.createCell(0).setCellValue("Диапазон экстремумов:");
        hd.createCell(1).setCellValue(AppData.ctx().set().FirstFreq);
        hd.createCell(2).setCellValue(AppData.ctx().set().LastFreq);
        ExtremeList list = inputStat.createExtrems(mode,AppData.ctx().set());
        if (list.data().size()==0){
            hd = sheet.createRow(1);
            hd.createCell(0).setCellValue("Экстремумов не найдено");
            return;
            }
        //---------------------------------- Анализ -- TODO -------------------------
        Pair<String, Integer> res = list.testAlarm2(AppData.ctx().set(), inputStat.getFreqStep());
        int color2 = AppData.StateColors.get(res.o2);
        StringTokenizer tokenizer = new StringTokenizer(list.getTestComment(),"\n");
        ArrayList<String> resList = new ArrayList<>();
        while (tokenizer.hasMoreTokens())
            resList.add(tokenizer.nextToken());
        for(String str : resList){
            hd = sheet.createRow(rowNum++);
            hd.createCell(0).setCellValue(str);
            }
        Extreme extreme;
        extreme = list.data().get(0);
        hd = sheet.createRow(rowNum++);
        hd.createCell(0).setCellValue("Осн. частота:");
        hd.createCell(1).setCellValue( extreme.idx*inputStat.getFreqStep());
        if (extreme.decSize!=-1)
            hd.createCell(2).setCellValue( Math.PI*extreme.decSize/extreme.idx);
        int count = main.nFirstMax < list.data().size() ? main.nFirstMax : list.data().size();
        ExtremeFacade facade = list.getFacade();
        facade.setExtreme(list.data().get(0));
        double val0 = facade.getValue();
        extreme = facade.extreme();
        hd = sheet.createRow(rowNum++);
        hd.createCell(0).setCellValue("Ампл");
        hd.createCell(1).setCellValue("\u0394спад");
        hd.createCell(2).setCellValue("\u0394тренд");
        hd.createCell(3).setCellValue("f(гц)");
        hd.createCell(4).setCellValue("Декремент");
        hd = sheet.createRow(rowNum++);
        hd.createCell(0).setCellValue(extreme.value);
        hd.createCell(1).setCellValue(extreme.diff);
        hd.createCell(2).setCellValue(extreme.trend);
        hd.createCell(3).setCellValue(extreme.idx*inputStat.getFreqStep());
        if (extreme.decSize!=-1)
            hd.createCell(4).setCellValue(Math.PI*extreme.decSize/extreme.idx);
        double sum=0;
        for(int i=1; i<count;i++){
            facade = list.getFacade();
            facade.setExtreme(list.data().get(i));
            double proc = facade.getValue()*100/val0;
            sum+=proc;
            extreme = facade.extreme();
            hd = sheet.createRow(rowNum++);
            hd.createCell(0).setCellValue(extreme.value);
            hd.createCell(1).setCellValue(extreme.diff);
            hd.createCell(2).setCellValue(extreme.trend);
            hd.createCell(3).setCellValue(extreme.idx*inputStat.getFreqStep());
            if (extreme.decSize!=-1)
                hd.createCell(4).setCellValue(Math.PI*extreme.decSize/extreme.idx);
            }
        hd = sheet.createRow(5+count);
        hd.createCell(0).setCellValue(String.format("Средний - %d%% к первому",(int)(sum/(count-1))));
        }
    @Override
    public boolean onStep(int nBlock, int calcMS, double totalMS, FFT fft) {
        inputStat.setFreqStep(fft.getStepHZLinear());
        long tt = System.currentTimeMillis();
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
