package FileJoiner;


import com.univocity.parsers.common.processor.BatchedColumnProcessor;
import com.univocity.parsers.csv.CsvParserSettings;
import com.univocity.parsers.tsv.TsvParser;
import com.univocity.parsers.tsv.TsvParserSettings;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataHelper {

    public static final Pattern VALID_EMAIL_ADDRESS_REGEX
            = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    public static final Pattern VALID_WEBSITE_ADDRESS_REGEX
            = Pattern.compile("^(http:\\/\\/www\\.|https:\\/\\/www\\.|http:\\/\\/|https:\\/\\/)?[a-z0-9]+([\\-\\.]{1}[a-z0-9]+)*\\.[a-z]{2,5}(:[0-9]{1,5})?(\\/.*)?$", Pattern.CASE_INSENSITIVE);


    public static boolean validateEmail(String emailStr) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);
        return matcher.find();
    }

    public static boolean validateURLs(String url) {
        Matcher matcher = VALID_WEBSITE_ADDRESS_REGEX.matcher(url);
        return matcher.find();
    }

    public static TsvParser getTsvParser(List<HeaderFileObject> columnsFiles) {
        TsvParserSettings settings = new TsvParserSettings();
        settings.getFormat().setLineSeparator("\r\n");
        settings.setProcessor(DataHelper.getBatchedColumnProcessor(columnsFiles));
        return new TsvParser(settings);
    }

    static CsvParserSettings getCsvParserSettings() {
        CsvParserSettings settings = new CsvParserSettings();
        settings.setNullValue("");
        settings.setEmptyValue("");
        settings.setDelimiterDetectionEnabled(true, '\t', ';', ',', '\n');
        settings.setLineSeparatorDetectionEnabled(true);
        settings.setIgnoreLeadingWhitespacesInQuotes(true);
        settings.setIgnoreTrailingWhitespacesInQuotes(true);
        settings.setMaxCharsPerColumn(500000);
        return settings;
    }

    static BatchedColumnProcessor getBatchedColumnProcessor(List<HeaderFileObject> columnsFiles) {
        return new BatchedColumnProcessor(100) {

            @Override
            public void batchProcessed(int rowsInThisBatch) {
                try {
                    List<List<String>> columnValues = getColumnValuesAsList();
                    System.out.println("Batch " + getBatchesProcessed() + ":");
                    for (int i = 0; i < columnsFiles.size(); i++) {
                        File f = columnsFiles.get(i);
                        if (i >= columnValues.size()) {
                            appendDataToTempFile(getEmptyData(columnValues.size()), f);
                        } else {
                            appendDataToTempFile(getEmptyDataIfNull(columnValues.get(i)), f);
                        }
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        };
    }

    public static String normalizeDataString(int beforeColumnCount, String data) {
        StringBuilder result = new StringBuilder();
        if (!StringUtils.isEmpty(data)) {
            for (int i = 0; i < beforeColumnCount; i++) {
                result.append("\"\",");
            }
            result.append(data);
        }
        return result.toString();
    }

    public static String echoAsCSV(Sheet sheet) {
        StringBuilder result = new StringBuilder();
        Row row;
        for (int i = 0; i <= sheet.getLastRowNum(); i++) {
            row = sheet.getRow(i);
            for (int j = 0; j < row.getLastCellNum(); j++) {
                result.append("\"").append(row.getCell(j, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).toString().replaceAll("\r", "").replaceAll("\n", "")).append("\",");
            }
            if(result.toString().endsWith(","))
            {
                result = new StringBuilder(result.substring(0, result.length() - 1));
            }
            result.append("\n");
        }
        return result.toString();
    }

    private static void appendDataToTempFile(List<String> strings, File file) {
        try (FileWriter fw = new FileWriter(file, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            for (String string : strings) {
                out.println(normalizeDataString(string));
            }
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static String removeAllQuotes(String value) {
        return value.replaceAll("\"", "");
    }

    public static String normalizeDataString(String dataRow) {
        String result = "";
        if (isContainsExtraChars(dataRow)) {
            result = dataRow.replaceAll(",\"\"\"", "\"")
                            .replaceAll("\"\"\",", "\"")
                            .replaceAll(",\"\"", "\"")
                            .replaceAll("\"\",", "\"");
        } else {
            result = dataRow.replaceAll(",\"\"\"", "\"\"")
                            .replaceAll("\"", "\"\"");
        }

        return result;
    }

    private static boolean isContainsExtraChars(String value) {
        return StringUtils.isEmpty(value.replaceAll("\n", "").replaceAll("\r", ""));
    }

    private static ArrayList<String> getEmptyData(int count) {
        ArrayList<String> emptyData = new ArrayList<>();
        for (int i = 0; i < count - 1; i++) {
            emptyData.add("");
        }
        return emptyData;
    }

    private static List<String> getEmptyDataIfNull(List<String> list) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) == null) {
                list.set(i, "");
            }
        }
        return list;
    }
}

