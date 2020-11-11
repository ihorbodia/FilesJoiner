using DynamicData;
using FileJoiner.Helpers;
using FileJoiner.Models;
using IronXL;
using System;
using System.Collections.Generic;
using System.Data;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Text.RegularExpressions;

namespace FileJoiner.Logic
{
    public class FileProcessor
    {
        List<string> headers;
        public FileProcessor()
        {
            headers = new List<string>();
        }
        public void GetCommonHeaders(IEnumerable<ExtendedFile> files)
        {
            foreach (var file in files)
            {
                foreach (DataColumn column in file.DataTable.Columns)
                {
                    var fileHeader = new Header(column);
                    file.Headers.Add(fileHeader);

                    var headerExists = headers.
                        Any(header => header.Equals(fileHeader.NewHeader, StringComparison.InvariantCultureIgnoreCase));
                    if (!headerExists)
                    {
                        headers.Add(fileHeader.NewHeader);
                    }
                }
            }
        }

        public DataTable ComposeFiles(IEnumerable<ExtendedFile> files)
        {
            DataTable dataTable = new DataTable("defaultTable");

            foreach (string header in headers)
            {
                dataTable.Columns.Add(header);
            }

            foreach (ExtendedFile file in files)
            {
                foreach (DataRow fileDataRow in file.DataTable.Rows)
                {
                    var newRow = dataTable.NewRow();
                    foreach (Header header in file.Headers)
                    {
                        var dtColumn = dataTable.Columns[header.NewHeader].ColumnName;
                        newRow.SetField(dtColumn, fileDataRow.Field<string>(header.OriginalHeader));
                    }
                    dataTable.Rows.Add(newRow);
                }
            }
            return dataTable;
        }

        public void ReadFilesContent(IEnumerable<ExtendedFile> files)
        {
            foreach (var file in files)
            {
                DataTable result = null;
                try
                {
                    if (FileTypes.isTextFile(file))
                    {
                        result = ReadDataFromTextFileManually(file);
                    }
                    else if (FileTypes.isExcelWorksheet(file))
                    {
                        var wb = WorkBook.LoadExcel(file.File.FullName);
                        result = wb.DefaultWorkSheet.ToDataTable(true);
                    }
                    file.Processed = true;
                }
                catch (Exception ex )
                {
                    file.Processed = false;
                }
                file.DataTable = result;
            }
        }

        List<string[]> SplitLines(string[] lines, string delimiter)
        {
            string regexPattern = Delimiters.GetSplitPattern(delimiter);
            var rows = lines.Select(line => Regex.Split(line, regexPattern)).ToList();
            return rows;
        }

        DataTable ReadDataFromTextFileManually(ExtendedFile file)
        {
            DataTable table = new DataTable(file.Name);
            if (!FileTypes.isTextFile(file))
            {
                return table;
            }

            var lines = File.ReadAllLines(file.File.FullName);
            var delimiter = Delimiters.GetDelimiterByHeader(lines.First());
            var fileRows = SplitLines(lines, delimiter);
            var columns = fileRows.First()
                .Where(x => !string.IsNullOrWhiteSpace(x))
                .Select(x => new DataColumn(x.TrimQuotes())).ToArray();
            table.Columns.AddRange(columns);

            foreach (var fileRow in fileRows.Skip(1))
            {
                var updatedRow = fileRow.Where(x => !string.IsNullOrWhiteSpace(x)).ToArray();
                if (columns.Length == updatedRow.Length)
                {
                    var newTableRow = table.NewRow();
                    for (int i = 0; i < table.Columns.Count; i++)
                    {
                        newTableRow[i] = updatedRow[i].TrimQuotes();
                    }
                    table.Rows.Add(newTableRow);
                }
            }
            return table;
        }
    }
}
