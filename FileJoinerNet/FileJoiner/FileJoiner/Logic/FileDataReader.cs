using CsvHelper;
using FileJoiner.Models;
using IronXL;
using System;
using System.Collections.Generic;
using System.Data;
using System.Diagnostics;
using System.Globalization;
using System.IO;
using System.Linq;

namespace FileJoiner.Logic
{
    public class FileDataReader
    {
        const string TXT = "txt";
        const string TSV = "tsv";
        const string CSV = "csv";
        const string XLS = "xls";
        const string XLSX = "xlsx";

        List<string> delimiters;

        public FileDataReader()
        {
            delimiters = new List<string>()
            {
                "\",\"", "\t", "|", ";", ","
            };
        }

        public DataTable ReadFileContent(ExtendedFile file)
        {
            DataTable result = null;
            if (isTextFile(file))
            {
                try
                {
                    result = ReadDataFromTextFile(file);
                }
                catch (Exception ex)
                {
                    Debug.Write(ex.Message);
                    //result = ReadDataFromTextFileManually(file);
                    file.Processed = false;
                    return result;
                }
            }
            else if (isExcelWorksheet(file))
            {
                var wb = WorkBook.LoadExcel(file.File.FullName);
                result = wb.DefaultWorkSheet.ToDataTable(true);
            }
            file.Processed = true;

            foreach (DataColumn item in result.Columns)
            {
                item.ColumnName = item.ColumnName.Replace("\"", "");
            }
            return result;
        }

        bool isExcelWorksheet(ExtendedFile file) => file.Type.Equals(XLS) || file.Type.Equals(XLSX);
        bool isTextFile(ExtendedFile file) => file.Type.Equals(TXT) || file.Type.Equals(CSV) || file.Type.Equals(TSV);

        DataTable ReadDataFromTextFileManually(ExtendedFile file)
        {
            var lines = File.ReadAllLines(file.File.FullName);
            var delimiter = getDelimiterByHeader(lines.First());
            var fileRows = lines.Select(x => x.Split(delimiter)).ToList();
            var columns = fileRows.First().Select(x => new DataColumn(x)).ToArray();
            DataTable table = new DataTable(file.Name);
            table.Columns.AddRange(columns);

            foreach (var fileRow in fileRows)
            {
                var newTableRow = table.NewRow();
                for (int i = 0; i < table.Columns.Count; i++)
                {
                    newTableRow[i] = fileRow[i];
                }
                table.Rows.Add(newTableRow);
            }
            return table;
        }

        DataTable ReadDataFromTextFile(ExtendedFile file)
        {
            string header = File.ReadLines(file.File.FullName).First();

            using (var reader = new StreamReader(file.File.FullName))
            using (var csv = new CsvReader(reader, CultureInfo.InvariantCulture))
            {
                csv.Configuration.LineBreakInQuotedFieldIsBadData = false;
                csv.Configuration.Delimiter = getDelimiterByHeader(header);
                //csv.Configuration.MissingFieldFound = null;
                csv.Configuration.IgnoreQuotes = true;
                var dt = new DataTable();
                using (var dr = new CsvDataReader(csv))
                {
                    dt.Load(dr);
                }
                return dt;
            }
        }

        string getDelimiterByHeader(string header)
        {
            int wordsCount = 0;
            string delimiter = "";

            foreach (var item in delimiters)
            {
                int currentWordsCount = header.Split(item).Length;
                if (currentWordsCount > wordsCount)
                {
                    wordsCount = currentWordsCount;
                    delimiter = item;
                }
            }

            Debug.Write("Delimiter: " + delimiter);
            return delimiter;
        }
    }
}
