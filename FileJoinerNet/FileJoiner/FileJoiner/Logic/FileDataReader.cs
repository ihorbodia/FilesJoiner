using CsvHelper;
using FileJoiner.Helpers;
using FileJoiner.Models;
using IronXL;
using System;
using System.Collections.Generic;
using System.Data;
using System.Globalization;
using System.IO;
using System.Linq;

namespace FileJoiner.Logic
{
    public class FileDataReader
    {
        public void ReadFilesContent(IEnumerable<ExtendedFile> files)
        {
            foreach (var file in files)
            {
                DataTable result = null;
                try
                {
                    if (isTextFile(file))
                    {
                        result = ReadDataFromTextFile(file);
                    }
                    else if (isExcelWorksheet(file))
                    {
                        var wb = WorkBook.LoadExcel(file.File.FullName);
                        result = wb.DefaultWorkSheet.ToDataTable(true);
                    }
                    file.Processed = true;
                    file.DataTable = result;
                }
                catch (Exception)
                {
                    file.Processed = false;
                }
            }
        }
        public DataTable ReadFileContentToDataTable(ExtendedFile file)
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

            RemoveQuotesFromHeaderIfExists(result);
            return result;
        }

        void RemoveQuotesFromHeaderIfExists(DataTable dataTable)
        {
            foreach (DataColumn column in dataTable.Columns)
            {
                column.ColumnName = column.ColumnName.Replace("\"", "");
            }
        }

        bool isExcelWorksheet(ExtendedFile file) => file.Type.Equals(FileTypes.XLS) || file.Type.Equals(FileTypes.XLSX);
        bool isTextFile(ExtendedFile file) => file.Type.Equals(FileTypes.TXT) || file.Type.Equals(FileTypes.CSV) || file.Type.Equals(FileTypes.TSV);

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
                csv.Configuration.MissingFieldFound = null;
                csv.Configuration.IgnoreQuotes = true;
                var dt = new DataTable();
                using (var dr = new CsvDataReader(csv))
                {
                    dt.Load(dr);
                }
                RemoveQuotesFromHeaderIfExists(dt);
                return dt;
            }
        }

        string getDelimiterByHeader(string headerRow)
        {
            int wordsCount = 0;
            string delimiter = string.Empty;

            foreach (var possibleDelimiter in Delimiters.List)
            {
                int currentWordsCount = headerRow.Split(possibleDelimiter).Length;
                if (currentWordsCount > wordsCount)
                {
                    wordsCount = currentWordsCount;
                    delimiter = possibleDelimiter;
                }
            }
            return delimiter;
        }
    }
}
