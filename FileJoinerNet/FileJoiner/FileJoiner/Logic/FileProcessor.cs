﻿using FileJoiner.Helpers;
using FileJoiner.Models;
using IronXL;
using System;
using System.Collections.Generic;
using System.Data;
using System.IO;
using System.Linq;

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
                catch (Exception)
                {
                    file.Processed = false;
                }
                file.DataTable = result;
            }
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
            var fileRows = lines.Select(x => x.Split(delimiter)).ToList();
            var headers = fileRows.First();
            var columns = fileRows.First().Select(x => new DataColumn(x.TrimQuotes())).ToArray();
            table.Columns.AddRange(columns);

            foreach (var fileRow in fileRows.Skip(1))
            {
                if (headers.Length == fileRow.Length)
                {
                    var newTableRow = table.NewRow();
                    for (int i = 0; i < table.Columns.Count; i++)
                    {
                        newTableRow[i] = fileRow[i].TrimQuotes();
                    }
                    table.Rows.Add(newTableRow);
                }
            }
            return table;
        }
    }
}
