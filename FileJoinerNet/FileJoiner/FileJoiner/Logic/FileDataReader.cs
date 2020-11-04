using CsvHelper;
using CsvHelper.Configuration;
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
            if (file.Type.Equals(TXT))
            {
                result = ReadDataFromTextFile(file);
            }
            else if (file.Type.Equals(TSV))
            {
                result = ReadDataFromTextFile(file);
            }
            else if (file.Type.Equals(CSV))
            {
                try
                {
                    using (var reader = new StreamReader(file.File.FullName))
                    using (var csv = new CsvReader(reader, CultureInfo.InvariantCulture))
                    {
                        csv.Configuration.LineBreakInQuotedFieldIsBadData = false;
                        csv.Configuration.IgnoreQuotes = true;
                        var dt = new DataTable();
                        using (var dr = new CsvDataReader(csv))
                        {
                            dt.Load(dr);
                        }
                        DataSet dataSet = new DataSet();
                        dataSet.Tables.Add(dt);
                        result = dt;
                    }
                }
                catch (Exception ex)
                {
                    result = ReadDataFromTextFile(file);
                }
            }
            //else if (file.Type.Equals(XLS))
            //{
            //    result = WorkBook.LoadExcel(file.File.FullName);
            //}
            //else if (file.Type.Equals(XLSX))
            //{
            //    result = WorkBook.LoadExcel(file.File.FullName);
            //}
            return result;
        }

        DataTable ReadDataFromTextFile(ExtendedFile file)
        {
            var lines = File.ReadAllLines(file.File.FullName);
            var delimiter = getDelimiterByHeader(lines.First());
            var items = lines.Select(x => x.Split(delimiter)).ToList();
            var columns = items.First().Select(x => new DataColumn(x)).ToArray();
            DataTable table = new DataTable(file.Name);
            table.Columns.AddRange(columns);

            foreach (var row in items)
            {
                var dataRow = table.NewRow();
                for (int i = 0; i < table.Columns.Count; i++)
                {
                    dataRow[i] = row[i];
                }
                table.Rows.Add(dataRow);
            }
            return table;
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

            return delimiter;
        }
    }
}
