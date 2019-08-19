
import com.univocity.parsers.common.processor.BatchedColumnProcessor;
import com.univocity.parsers.csv.CsvParserSettings;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.*;
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

    static CsvParserSettings getCsvParserSettings() {
        CsvParserSettings settings = new CsvParserSettings();
        settings.setNullValue("");
        settings.setEmptyValue("");
        settings.setDelimiterDetectionEnabled(true, '\t', ';', ',', '\n');
        settings.setLineSeparatorDetectionEnabled(true);
        settings.setIgnoreLeadingWhitespacesInQuotes(true);
        settings.setIgnoreTrailingWhitespacesInQuotes(true);
        settings.setKeepEscapeSequences(true);
        settings.setParseUnescapedQuotes(true);
        settings.setAutoConfigurationEnabled(true);
        settings.setMaxCharsPerColumn(500000);
        return settings;
    }

    static BatchedColumnProcessor getBatchedColumnProcessor(List<HeaderFileObject> columnsFiles) {
        return new BatchedColumnProcessor(100) {

            @Override
            public void batchProcessed(int rowsInThisBatch) {
                try {
                    Map<String, List<String>> columnValues = getColumnValuesAsMapOfNames();

                    HashSet<HeaderFileObject> unmatchedItems = new HashSet<>(columnsFiles);
                    columnValues.forEach((header, columnData) -> {
                        String inputHeader = header.replaceAll(":", "꞉");
                        HeaderFileObject item = null;

                        for (HeaderFileObject headerFileObject : columnsFiles) {
                            if (headerFileObject.getHeader().equalsIgnoreCase(inputHeader)) {
                                unmatchedItems.remove(headerFileObject);
                                item = headerFileObject;
                                break;
                            }
                        }
                        appendDataToTempFile(columnData, item);
                    });
                    unmatchedItems.forEach(unmatchedItem -> {
                        appendDataToTempFile(getEmptyDataList(rowsInThisBatch), unmatchedItem);
                    });
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        };
    }

    private static void appendDataToTempFile(List<String> strings, HeaderFileObject file) {
        try (FileWriter fw = new FileWriter(file, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            for (String string : strings) {
                out.println(string.replace("\"", "\"\""));
            }
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static ArrayList<String> getEmptyData(int count) {
        ArrayList<String> emptyData = new ArrayList<>();
        for (int i = 0; i < count - 1; i++) {
            emptyData.add("");
        }
        return emptyData;
    }

    public static ArrayList<String> getEmptyDataList(int count) {
        ArrayList<String> emptyData = new ArrayList<>();
        for (int i = 0; i < count; i++) {
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

    static String generateNameForFile(List<ExtendedFile> headerFileObjects) {
        StringBuilder sb = new StringBuilder();

        if (headerFileObjects.size() == 1) {
            sb.append(headerFileObjects.get(0).getHeader().replaceAll(":", "꞉"));
        }

        if (headerFileObjects.size() == 2) {
            sb.append(headerFileObjects.get(0).getHeader().replaceAll(":", "꞉")).append("+")
              .append(headerFileObjects.get(1).getHeader().replaceAll(":", "꞉"));
        }

        if (headerFileObjects.size() == 3) {
            sb.append(headerFileObjects.get(0).getHeader().replaceAll(":", "꞉")).append("+")
              .append(headerFileObjects.get(1).getHeader().replaceAll(":", "꞉")).append("+")
              .append(headerFileObjects.get(2).getHeader().replaceAll(":", "꞉"));
        }

        if (headerFileObjects.size() > 4) {
            sb.append(headerFileObjects.get(0).getHeader().replaceAll(":", "꞉")).append("+")
              .append(headerFileObjects.get(1).getHeader().replaceAll(":", "꞉")).append("+")
              .append(headerFileObjects.get(2).getHeader().replaceAll(":", "꞉"))
              .append("... and more");
        }

        return sb.toString();
    }
}

