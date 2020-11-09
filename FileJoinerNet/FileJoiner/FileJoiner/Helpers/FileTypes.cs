using FileJoiner.Models;

namespace FileJoiner.Helpers
{
    public static class FileTypes
    {
        public static readonly string TXT = "txt";
        public static readonly string TSV = "tsv";
        public static readonly string CSV = "csv";
        public static readonly string XLS = "xls";
        public static readonly string XLSX = "xlsx";

        public static bool isExcelWorksheet(ExtendedFile file) => file.Type.Equals(XLS) || file.Type.Equals(XLSX);
        public static bool isTextFile(ExtendedFile file) => file.Type.Equals(TXT) || file.Type.Equals(CSV) || file.Type.Equals(TSV);
    }
}
