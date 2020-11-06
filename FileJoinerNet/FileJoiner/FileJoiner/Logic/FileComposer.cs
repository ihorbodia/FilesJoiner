using FileJoiner.Models;
using System;
using System.Collections.Generic;
using System.Data;
using System.Linq;

namespace FileJoiner.Logic
{
    public class FileComposer
    {
        List<string> headers;
        public FileComposer()
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

            var newHeaderRow = dataTable.NewRow();
            foreach (string header in headers)
            {
                dataTable.Columns.Add(header);
                newHeaderRow.SetField(header, header);
            }
            dataTable.Rows.Add(newHeaderRow);

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
    }
}
