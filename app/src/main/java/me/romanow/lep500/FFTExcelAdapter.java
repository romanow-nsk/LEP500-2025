package me.romanow.lep500;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellUtil;
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
import romanow.abc.core.utils.OwnDateTime;
import romanow.abc.core.utils.Pair;
import romanow.lep500.FileDescription;
import romanow.lep500.fft.*;

import static me.romanow.lep500.MainActivity.createFatalMessage;

public class FFTExcelAdapter implements FFTCallBackPlus {
    public class RowList {
        public final XSSFSheet sheet;
        private ArrayList<Row> rows = new ArrayList<>();
        public  RowList(XSSFSheet sheet0){
            sheet = sheet0;
            }
        public Row get(int idx){
            int sz = rows.size();
            while (idx>=sz)
                rows.add(sheet.createRow(sz++));
            return rows.get(idx);
            }
        }
    private final static int MainSheetRows=30;
    private FFTStatistic inputStat;
    private MainActivity main;
    private FileDescription fd;
    private XSSFWorkbook workbook;
    private RowList  mainRows;
    private RowList  dataRows;
    private int callNum=-1;             // Для многократного вызова
    public void nextStep(String title, FileDescription fd0){
        inputStat = new FFTStatistic(title);
        inputStat.setFreq(fd0.getFileFreq());
        fd = fd0;
        callNum++;
        }
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
    public FFTExcelAdapter(BaseActivity main0){
        main = (MainActivity) main0;
        workbook = new XSSFWorkbook();
        mainRows =new RowList(workbook.createSheet("Результаты"));
        mainRows.get(0).createCell(0).setCellValue("Файл:");
        mainRows.get(1).createCell(0).setCellValue("Геолокация:");
        mainRows.get(2).createCell(0).setCellValue("Линия:");
        mainRows.get(3).createCell(0).setCellValue("Опора:");
        mainRows.get(4).createCell(0).setCellValue("Дата создания:");
        mainRows.get(5).createCell(0).setCellValue("Частота:");
        mainRows.get(6).createCell(0).setCellValue("Датчик:");
        mainRows.get(7).createCell(0).setCellValue("Номер измерения:");
        mainRows.get(8).createCell(0).setCellValue("Частот в спектре:");
        mainRows.get(9).createCell(0).setCellValue("Оценка:");
        mainRows.get(10).createCell(0).setCellValue("Анализ:");
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
        //------------------------------------------------------------------------------------------
        int colIdx = callNum+1;
        mainRows.get(0).createCell(colIdx).setCellValue(fd.getOriginalFileName());
        mainRows.get(1).createCell(colIdx).setCellValue(fd.getGps().toString());
        mainRows.get(2).createCell(colIdx).setCellValue(fd.getPowerLine());
        mainRows.get(3).createCell(colIdx).setCellValue(fd.getSupport());
        mainRows.get(4).createCell(colIdx).setCellValue(fd.getCreateDate().dateTimeToString());
        mainRows.get(5).createCell(colIdx).setCellValue(String.format("%6.2f",fd.getFileFreq()));
        mainRows.get(6).createCell(colIdx).setCellValue(fd.getSensor());
        mainRows.get(7).createCell(colIdx).setCellValue(fd.getMeasureCounter());
        double dd[] = inputStat.getNormalized();
        mainRows.get(8).createCell(colIdx).setCellValue(dd.length);
        Pair<String,Integer> ss = list.testAlarm2(AppData.ctx().set(),inputStat.getFreqStep());
        if (ss.o1!=null){
            StringTokenizer tokenizer = new StringTokenizer(list.getTestComment(),"\n");
            ArrayList<String> resList = new ArrayList<>();
            int idx=0;
            while (tokenizer.hasMoreTokens()){
                mainRows.get(10+idx).createCell(colIdx).setCellValue(tokenizer.nextToken());
                idx++;
                }
            mainRows.get(9).createCell(colIdx).setCellValue(ss.o2);
            }
        //------------------------------------------------------------------------------------------
        XSSFSheet sheet = workbook.createSheet("Спектр-"+(callNum+1));
        double ff0 = AppData.ctx().set().FirstFreq;
        double ff1 = AppData.ctx().set().LastFreq;
        double step = inputStat.getFreqStep();
        int idx0=inputStat.getnFirst();
        Row hd;
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
        dataRows = new RowList(workbook.createSheet("Пики-"+(callNum+1)));
        rowNum=0;
        for(int i = 0; i< Values.extremeFacade.length; i++){
            writeExtrems(i,workbook);
            }
        //------------------------------------------------------------------------------------------
        }
    public String createExcel(){
        String dirName = AppData.ctx().androidFileDirectory()+"/"+ AppData.excelDir;
        File ff = new File(dirName);
        if (!ff.exists()){
            ff.mkdir();
            }
        String pathName = "Измерения ("+(callNum+1)+") "+new OwnDateTime().dateTimeToString()+".xlsx";
        try (FileOutputStream out = new FileOutputStream(new File(dirName+"/"+pathName))) {
            workbook.write(out);
            out.close();
        } catch (IOException e) { main.addToLog("Ошибка экспорта: "+e.toString());  }
        main.addToLog("Экспорт в Excel: "+fd.getOriginalFileName());
        return dirName+"/"+pathName;
        }
    private int rowNum=0;
    private void writeExtrems(int mode, XSSFWorkbook workbook){
        String ss="";
        try {
            ss=((ExtremeFacade)Values.extremeFacade[mode].newInstance()).getTitle();
            } catch (Exception ee){ }
        int sz = inputStat.getMids().length;
        Row hd;
        hd = dataRows.get(rowNum++);
        hd.createCell(0).setCellValue("Диапазон экстремумов:");
        hd.createCell(1).setCellValue(AppData.ctx().set().FirstFreq);
        hd.createCell(2).setCellValue(AppData.ctx().set().LastFreq);
        ExtremeList list = inputStat.createExtrems(mode,AppData.ctx().set());
        if (list.data().size()==0){
            hd = dataRows.get(rowNum++);
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
            hd = dataRows.get(rowNum++);
            hd.createCell(0).setCellValue(str);
            }
        Extreme extreme;
        extreme = list.data().get(0);
        hd = dataRows.get(rowNum++);
        hd.createCell(0).setCellValue("Осн. частота:");
        hd.createCell(1).setCellValue( extreme.idx*inputStat.getFreqStep());
        if (extreme.decSize!=-1)
            hd.createCell(2).setCellValue( Math.PI*extreme.decSize/extreme.idx);
        int count = main.nFirstMax < list.data().size() ? main.nFirstMax : list.data().size();
        ExtremeFacade facade = list.getFacade();
        facade.setExtreme(list.data().get(0));
        double val0 = facade.getValue();
        extreme = facade.extreme();
        hd = dataRows.get(rowNum++);
        hd.createCell(0).setCellValue("Ампл");
        hd.createCell(1).setCellValue("\u0394спад");
        hd.createCell(2).setCellValue("\u0394тренд");
        hd.createCell(3).setCellValue("f(гц)");
        hd.createCell(4).setCellValue("Декремент");
        hd = dataRows.get(rowNum++);
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
            hd = dataRows.get(rowNum++);
            hd.createCell(0).setCellValue(extreme.value);
            hd.createCell(1).setCellValue(extreme.diff);
            hd.createCell(2).setCellValue(extreme.trend);
            hd.createCell(3).setCellValue(extreme.idx*inputStat.getFreqStep());
            if (extreme.decSize!=-1)
                hd.createCell(4).setCellValue(Math.PI*extreme.decSize/extreme.idx);
            }
        hd = dataRows.get(rowNum++);
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
