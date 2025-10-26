package me.romanow.lep500;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
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
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
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
        public Cell createCell(int rowIdx,int colIdx){
            Cell cc = get(rowIdx).createCell(colIdx);
            cc.setCellStyle(border);
            return cc;
            }
        }
    private final static int colOffset=4;
    private final static int MainSheetRows=30;
    private FFTStatistic inputStat;
    private MainActivity main;
    private FileDescription fd;
    private XSSFWorkbook workbook;
    private RowList  mainRows;
    private RowList  dataRows;
    private int callNum=-1;             // Для многократного вызова
    private CellStyle border;
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
        border = workbook.createCellStyle();
        border.setBorderTop(BorderStyle.THIN);
        border.setBorderBottom(BorderStyle.THIN);
        border.setBorderLeft(BorderStyle.THIN);
        border.setBorderRight(BorderStyle.THIN);
        border.setAlignment(HorizontalAlignment.LEFT);
        mainRows =new RowList(workbook.createSheet("Результаты"));
        mainRows.createCell(0,0).setCellValue("Измерение:");
        mainRows.createCell(1,0).setCellValue("Файл:");
        mainRows.createCell(2,0).setCellValue("Геолокация:");
        mainRows.createCell(3,0).setCellValue("Линия:");
        mainRows.createCell(4,0).setCellValue("Опора:");
        mainRows.createCell(5,0).setCellValue("Дата создания:");
        mainRows.createCell(6,0).setCellValue("Частота:");
        mainRows.createCell(7,0).setCellValue("Датчик:");
        mainRows.createCell(8,0).setCellValue("Номер измерения:");
        mainRows.createCell(9,0).setCellValue("Частот в спектре:");
        mainRows.createCell(10,0).setCellValue("Оценка:");
        mainRows.createCell(11,0).setCellValue("Анализ:");
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
        //------------------------------------------------------------------------------------------
        int colIdx = callNum+1;
        mainRows.createCell(0,colIdx).setCellValue(colIdx);
        mainRows.createCell(1,colIdx).setCellValue(fd.getOriginalFileName());
        mainRows.createCell(2,colIdx).setCellValue(fd.getGps().toString());
        mainRows.createCell(3,colIdx).setCellValue(fd.getPowerLine());
        mainRows.createCell(4,colIdx).setCellValue(fd.getSupport());
        mainRows.createCell(5,colIdx).setCellValue(fd.getCreateDate().dateTimeToString());
        mainRows.createCell(6,colIdx).setCellValue(String.format("%6.2f",fd.getFileFreq()));
        mainRows.createCell(7,colIdx).setCellValue(fd.getSensor());
        mainRows.createCell(8,colIdx).setCellValue(""+fd.getMeasureCounter());
        double dd[] = inputStat.getNormalized();
        mainRows.createCell(9,colIdx).setCellValue(dd.length);
        ExtremeList list = inputStat.createExtrems(FFTStatistic.ExtremeAbsMode,AppData.ctx().set());
                if (list.data().size()==0){
            mainRows.createCell(11,colIdx).setCellValue("Экстремумов не найдено");
            return;
            }
        Pair<String,Integer> ss = list.testAlarmBase(AppData.ctx().set(),inputStat.getFreqStep());
        mainRows.createCell(10,colIdx).setCellValue(ss.o2);
        if (ss.o1!=null){
            StringTokenizer tokenizer = new StringTokenizer(list.getTestComment(),"\n");
            ArrayList<String> resList = new ArrayList<>();
            int idx=0;
            while (tokenizer.hasMoreTokens()){
                mainRows.createCell(11+idx,colIdx).setCellValue(tokenizer.nextToken());
                idx++;
                }
            }
        //------------------------------------------------------------------------------------------
        dataRows = new RowList(workbook.createSheet("Измерение-"+(callNum+1)));
        double ff0 = AppData.ctx().set().FirstFreq;
        double ff1 = AppData.ctx().set().LastFreq;
        double step = inputStat.getFreqStep();
        int idx0=inputStat.getnFirst();
        Row hd;
        Cell cc;
        for(int i=0;i<dd.length-idx0;i++){
            hd = dataRows.get(i);
            cc = hd.createCell(0);
            cc.setCellStyle(border);
            cc.setCellValue(""+(i+1));
            cc = hd.createCell(1);
            cc.setCellStyle(border);
            cc.setCellValue(ff0+i*step);
            cc=hd.createCell(2);
            cc.setCellStyle(border);
            cc.setCellValue(dd[i+idx0]);
            }
        //------------------------------------------------------------------------------------------
        // Создаем холст
        XSSFDrawing drawing = dataRows.sheet.createDrawingPatriarch();
        // Первые четыре значения по умолчанию: 0, [0,5]: начало с 0 столбца и 5 строк; [7,26]: ширина 7 ячеек, 26 расширяется до 26 строк
        // Ширина по умолчанию (14-8) * 12
        XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 10, 5, 20, 40);
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
        XDDFNumericalDataSource<Double> ff2 = XDDFDataSourcesFactory.fromNumericCellRange(dataRows.sheet, new CellRangeAddress(0, dd.length-1-idx0, 2, 2));
        XDDFNumericalDataSource<Double> ff3 = XDDFDataSourcesFactory.fromNumericCellRange(dataRows.sheet, new CellRangeAddress(0, dd.length-1-idx0, 1, 1));
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
        rowNum=0;
        int weight = 0;
        for(int i = 0; i< Values.extremeFacade.length; i++){
            weight += writeExtrems(i,workbook);
            }
        mainRows.createCell(10,colIdx).setCellValue(weight);
        dataRows.sheet.setColumnWidth(colOffset-1,500);
        dataRows.sheet.setColumnWidth(colOffset+0,9000);
        dataRows.sheet.setColumnWidth(colOffset+4,4000);
        //------------------------------------------------------------------------------------------
        }
    public String createExcel(){
        mainRows.sheet.setColumnWidth(0,5000);
        for(int columnIndex = 0; columnIndex < callNum+1; columnIndex++)
            mainRows.sheet.setColumnWidth(columnIndex+1,10000);
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
    //----------------------------------------------------------------------------------------------
    private int rowNum=0;
    private int writeExtrems(int mode, XSSFWorkbook workbook){
        String ss="";
        try {
            ss=((ExtremeFacade)Values.extremeFacade[mode].newInstance()).getTitle();
            } catch (Exception ee){ }
        int sz = inputStat.getMids().length;
        Row hd;
        hd = dataRows.get(rowNum++);
        dataRows.createCell(rowNum,colOffset+0).setCellValue("Диапазон экстремумов:");
        dataRows.createCell(rowNum,colOffset+1).setCellValue(AppData.ctx().set().FirstFreq);
        dataRows.createCell(rowNum,colOffset+2).setCellValue(AppData.ctx().set().LastFreq);
        rowNum++;
        ExtremeList list = inputStat.createExtrems(mode,AppData.ctx().set());
        if (list.data().size()==0){
            dataRows.createCell(rowNum++,colOffset+0).setCellValue("Экстремумов не найдено");
            return 0;
            }
        //---------------------------------- Анализ -- TODO -------------------------
        Pair<String, Integer> res = list.testAlarm2(AppData.ctx().set(), inputStat.getFreqStep());
        int color2 = AppData.StateColors.get(res.o2);
        StringTokenizer tokenizer = new StringTokenizer(list.getTestComment(),"\n");
        ArrayList<String> resList = new ArrayList<>();
        while (tokenizer.hasMoreTokens())
            resList.add(tokenizer.nextToken());
        for(String str : resList){
            dataRows.createCell(rowNum++,colOffset+0).setCellValue(str);
            }
        Extreme extreme;
        extreme = list.data().get(0);
        dataRows.createCell(rowNum,colOffset+0).setCellValue("Осн. частота:");
        dataRows.createCell(rowNum,colOffset+1).setCellValue( extreme.idx*inputStat.getFreqStep());
        rowNum++;
        if (extreme.decSize!=-1)
            dataRows.createCell(rowNum,colOffset+2).setCellValue( Math.PI*extreme.decSize/extreme.idx);
        int count = main.nFirstMax < list.data().size() ? main.nFirstMax : list.data().size();
        ExtremeFacade facade = list.getFacade();
        facade.setExtreme(list.data().get(0));
        double val0 = facade.getValue();
        extreme = facade.extreme();
        dataRows.createCell(rowNum,colOffset+0).setCellValue("Ампл");
        dataRows.createCell(rowNum,colOffset+1).setCellValue("\u0394спад");
        dataRows.createCell(rowNum,colOffset+2).setCellValue("\u0394тренд");
        dataRows.createCell(rowNum,colOffset+3).setCellValue("f(гц)");
        dataRows.createCell(rowNum,colOffset+4).setCellValue("Декремент");
        rowNum++;
        dataRows.createCell(rowNum,colOffset+0).setCellValue(extreme.value);
        dataRows.createCell(rowNum,colOffset+1).setCellValue(extreme.diff);
        dataRows.createCell(rowNum,colOffset+2).setCellValue(extreme.trend);
        dataRows.createCell(rowNum,colOffset+3).setCellValue(extreme.idx*inputStat.getFreqStep());
        rowNum++;
        if (extreme.decSize!=-1)
            dataRows.createCell(rowNum,4).setCellValue(Math.PI*extreme.decSize/extreme.idx);
        double sum=0;
        for(int i=1; i<count;i++){
            facade = list.getFacade();
            facade.setExtreme(list.data().get(i));
            double proc = facade.getValue()*100/val0;
            sum+=proc;
            extreme = facade.extreme();
            dataRows.createCell(rowNum,colOffset+0).setCellValue(extreme.value);
            dataRows.createCell(rowNum,colOffset+1).setCellValue(extreme.diff);
            dataRows.createCell(rowNum,colOffset+2).setCellValue(extreme.trend);
            dataRows.createCell(rowNum,colOffset+3).setCellValue(extreme.idx*inputStat.getFreqStep());
            if (extreme.decSize!=-1)
                dataRows.createCell(rowNum,colOffset+4).setCellValue(Math.PI*extreme.decSize/extreme.idx);
            rowNum++;
            }
        dataRows.createCell(rowNum++,colOffset+0).setCellValue(String.format("Средний - %d%% к первому",(int)(sum/(count-1))));
        return AppData.StateWeight.get(color2);
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
