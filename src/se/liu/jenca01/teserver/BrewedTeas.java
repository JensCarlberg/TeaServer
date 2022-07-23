package se.liu.jenca01.teserver;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.ServletOutputStream;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import se.liu.jenca01.teserver.servlets.AddTea;


public class BrewedTeas {
	private static final int DAY_ENDS_AT_GMT = 4;
    private static final String DELIMITER = "\t";
    private static final String DATE_FORMAT = "yyyy-MM-dd HH.mm.ss";
    private static final String DAY_FORMAT = "yyyy-MM-dd";
    private static final BrewedTeas singleton = new BrewedTeas();
	private static final int DEFAULT_TEA_VOLUME = 3;
    public static BrewedTeas instance() { return singleton; }
    public File getlogFileDir() { return logFile.getParentFile(); }

    private static DateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
    private static DateFormat sdyf = new SimpleDateFormat(DAY_FORMAT);
    private TreeMap<Date, String> teas = new TreeMap<>();
    private TreeMap<Date, Double> volumes = new TreeMap<>();
    private File logFile = null;

    private BrewedTeas() {}

    public void setLogFile(File file) {
        logFile = file;
    }

    public void addTea(String tea, double volume) {
    	addTea(new Date(), tea, volume);
    }

    public void addTea(Tea tea) {
    	addTea(tea.start, tea.name, tea.volume);
	}

	private void addTea(Date brewTime, String tea, double volume) {
    	if (tea == null || "".equals(tea)) return;
    	cacheTea(brewTime, tea, volume);
    	writeTea(brewTime);
    }

	private void cacheTea(Date brewTime, String tea, double volume) {
		teas.put(brewTime, tea);
    	volumes.put(brewTime, volume);
	}

    private synchronized void writeTea(Date brewTime) {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(logFile, true), UTF_8)) {
            writer.write(format(brewTime, DELIMITER));
            writer.write(System.getProperty("line.separator"));
        } catch (IOException e) {
            System.err.println("Could not write tea to log file");
            e.printStackTrace();
        }
    }

    public String[] latestTeas(int noOfTeas) {
        return latestTeas(noOfTeas, DELIMITER);
    }

    private String[] latestTeas(int noOfTeas, String delimiter) {
    	Set<Date> latestTeaDates = latestTeaDates(noOfTeas);
        Set<String> latestTeas = new TreeSet<>();
        for(Date teaDate: latestTeaDates)
            latestTeas.add(format(teaDate, delimiter));
        return latestTeas.toArray(new String[] {});
    }

    public String[][] latestTeasArray(int noOfTeas) {
        TreeSet<Date> latestTeaDates = latestTeaDates(noOfTeas);
        String[][] latestTeas = new String[latestTeaDates.size()][];
        int pos = latestTeaDates.size();
        for (Date date : latestTeaDates)
        	latestTeas[--pos] = new String[] { sdf.format(date), teas.get(date), "" + volumes.get(date) };
        return latestTeas;
    }

    private TreeSet<Date> latestTeaDates(int noOfTeas) {
        int noOfDates = Math.min(noOfTeas, teas.size());
        TreeSet<Date> dates = new TreeSet<>();
        for (Date date : teas.descendingKeySet()) {
        	dates.add(date);
        	if (dates.size() == noOfDates) break;
        }
        return dates;
    }

    public String format(Date date, String delimiter) {
        return String.format("%s%s%s%s%s", sdf.format(date), delimiter, teas.get(date), delimiter, volumes.get(date));
    }

    public int noOfTeas() { return teas.size(); }
    public void clearTeas() { teas.clear(); }
    public void reloadTeas() { clearTeas(); load(); }

    public synchronized void load() {
    	System.out.println("Loading log file: "+logFile.getAbsolutePath());
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(logFile), UTF_8))) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(DELIMITER);
                Date date = sdf.parse(data[0]);
                if (data.length == 3)
                	cacheTea(date, data[1], Double.valueOf(data[2]));
                else
                	cacheTea(date, data[1], DEFAULT_TEA_VOLUME);
            }
            System.out.println("Number of loaded teas: " + teas.size());
        } catch (Exception e) {
            System.err.println("Failed reading log file, starting from scratch");
            e.printStackTrace();
            clearTeas();
            removeLogFile();
        }
    }

    public synchronized void removeLogFile() {
        logFile.delete();
        AddTea.removeLogFile();
    }

    public synchronized void copyLogFileToStream(ServletOutputStream out) {
        try (FileInputStream in = new FileInputStream(logFile)) {
            byte[] buffer = new byte[16384];
            int read = 0;
            while ((read = in.read(buffer)) > 0)
                out.write(buffer, 0, read);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class TeaDayKey implements Comparable<TeaDayKey> {
        private String day;
        private String tea;

        public TeaDayKey(String tea, String day) {
            this.tea = tea;
            this.day = day;
        }

        @Override
        public int compareTo(TeaDayKey o) {
            if (o == null) return 1;
            if (!o.day.equals(day)) return o.day.compareTo(day);
            if (!o.tea.equals(tea)) return o.tea.compareTo(tea);
            return 0;
        }
    }

    public String[][] sumTeaDay() {
        Map<TeaDayKey, Double> teaPerDay = new TreeMap<>();
        Set<String> brewTimes = new TreeSet<>();
        Set<String> brewedTeas = new TreeSet<>();
        Map<String, Double> sumPerDay = new TreeMap<>();
        Map<String, Double> sumPerTea = new TreeMap<>();

        for (Date date: teas.keySet())
            addData(sdyf.format(dayEndsAt(date, DAY_ENDS_AT_GMT)), teas.get(date), volumes.get(date),
            		teaPerDay, brewTimes, brewedTeas, sumPerDay, sumPerTea);

        String[][] result = new String[brewedTeas.size() + 2][brewTimes.size() + 2];
        addTeaRows(result, brewedTeas);
        addTeaSums(result, sumPerTea);
        addDayColumns(result, brewTimes);
        addDaySums(result, sumPerDay);
        addMatrix(result, teaPerDay, brewedTeas, brewTimes);
        addEmptyCorners(result);
        addGrandTotal(result, sumPerDay);
        return result;
    }

    public JSONObject jsonBrewedTeaNames() {
        Set<String> brewedTeas = new TreeSet<>(teas.values());
        JSONArray teaNamesJsonArray = new JSONArray();
        teaNamesJsonArray.addAll(brewedTeas);
        JSONObject result = new JSONObject();
		result.put("teaNames", teaNamesJsonArray);
        return result;
    }

    public JSONObject jsonSumTeaDay() {
        Map<TeaDayKey, Double> teaPerDay = new TreeMap<>();
        Set<String> brewTimes = new TreeSet<>();
        Set<String> brewedTeas = new TreeSet<>();
        Map<String, Double> sumPerDay = new TreeMap<>();
        Map<String, Double> sumPerTea = new TreeMap<>();

        for (Date date: teas.keySet())
            addData(sdyf.format(dayEndsAt(date, DAY_ENDS_AT_GMT)), teas.get(date), volumes.get(date),
            		teaPerDay, brewTimes, brewedTeas, sumPerDay, sumPerTea);

        JSONObject result = new JSONObject();
        result.put("total", calcGrandTotal(sumPerDay));
        result.put("per-day", addPerDay(brewTimes, sumPerDay));
        result.put("per-tea", addPerTea(brewedTeas, sumPerTea));
        return result;
    }

    private JSONObject addPerTea(Set<String> brewedTeas, Map<String, Double> sumPerTea) {
    	JSONObject rowData = new JSONObject();
    	for (String tea : brewedTeas) {
    		rowData.put(tea, sumPerTea.get(tea));
    	}
		return rowData;
	}

	private JSONObject addPerDay(Set<String> brewTimes, Map<String, Double> sumPerDay) {
    	JSONObject rowData = new JSONObject();
    	for (String day : brewTimes) {
    		rowData.put(parseTime(day), sumPerDay.get(day));
    	}
		return rowData;
	}

	private long parseTime(String day) {
		try {
			return sdyf.parse(day).getTime();
		} catch (ParseException e) {
			System.err.println("Failed parsing: " + day);
			e.printStackTrace();
			return 0;
		}
	}

	private Date dayEndsAt(Date originalDate, int dayEnd) {
        return new Date(originalDate.getTime() - dayEnd * 60 * 60 * 1000);
    }

    private void addGrandTotal(String[][] result, Map<String, Double> sumPerDay) {
        double sumSum = calcGrandTotal(sumPerDay);
        result[result.length - 1][result[0].length - 1] = sumSum + "";
    }

	private double calcGrandTotal(Map<String, Double> sumPerDay) {
		double sumSum = 0;
        for (Double sum: sumPerDay.values())
            sumSum += sum;
		return sumSum;
	}

    private void addMatrix(String[][] result, Map<TeaDayKey, Double> teaPerDay, Set<String> brewedTeas, Set<String> brewTimes) {
        int teaRow = 1;
        for (String tea : brewedTeas) {
            int dateCol = 1;
            for (String day : brewTimes)
                result[teaRow][dateCol++] = blankNull(teaPerDay.get(new TeaDayKey(tea, day)));
            teaRow++;
        }
    }

    private String blankNull(Double value) {
        if (value == null) return "";
        return formatDouble(value);
    }

    private String formatDouble(Double value) {
        return String.format(Locale.ROOT, "%.1f", value);
    }

    private void addEmptyCorners(String[][] result) {
        result[0][0] = "";
        result[0][0] = "Te";
        result[0][result[0].length - 1] = "Summa:";
        result[result.length - 1][0] = "Summa:";
    }

    private void addTeaSums(String[][] result, Map<String, Double> sumPerTea) {
        int pos = 1;
        for (String tea : sumPerTea.keySet())
            result[pos++][result[0].length - 1] = formatDouble(sumPerTea.get(tea));
    }

    private void addDaySums(String[][] result, Map<String, Double> sumPerDay) {
        int pos = 1;
        for (String tea : sumPerDay.keySet())
            result[result.length - 1][pos++] = formatDouble(sumPerDay.get(tea));
    }

    private void addTeaRows(String[][] result, Set<String> brewedTeas) {
        int pos = 1;
        for (String tea : brewedTeas)
            result[pos++][0] = tea;
    }

    private void addDayColumns(String[][] result, Set<String> brewTimes) {
        int pos = 1;
        for (String day : brewTimes)
            result[0][pos++] = day;
    }

    private void addData(
            String day, String tea, double volume,
            Map<TeaDayKey, Double> teaPerDay, Set<String> brewTimes, Set<String> brewedTeas,
            Map<String, Double> sumPerDay, Map<String, Double> sumPerTea) {
        TeaDayKey key = new TeaDayKey(tea, day);
        brewTimes.add(day);
        brewedTeas.add(tea);
        addVolume(teaPerDay, key, volume);
        addVolume(sumPerDay, day, volume);
        addVolume(sumPerTea, tea, volume);
    }

    private void addVolume(Map<TeaDayKey, Double> teaPerDay, TeaDayKey key, double volume) {
    	Double previouslyBrewed = teaPerDay.get(key);
        if (previouslyBrewed == null)
            teaPerDay.put(key, volume);
        else
            teaPerDay.put(key, previouslyBrewed + volume);
    }

    private void addVolume(Map<String, Double> teaPerDay, String key, double volume) {
    	Double previouslyBrewed = teaPerDay.get(key);
        if (previouslyBrewed == null)
            teaPerDay.put(key, volume);
        else
            teaPerDay.put(key, previouslyBrewed + volume);
    }
}
