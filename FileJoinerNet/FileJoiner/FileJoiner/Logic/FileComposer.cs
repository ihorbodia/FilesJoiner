using FileJoiner.Models;
using System;
using System.Collections.Generic;
using System.Data;
using System.Linq;
using System.Windows.Documents;

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
                    //TODO: Use ColumnNames clas here
                    // ...
                    
                    var headerExists = headers.
                        Any(header => header.Equals(column.ColumnName, StringComparison.InvariantCultureIgnoreCase));
                    if (!headerExists)
                    {
                        headers.Add(column.ColumnName.ToLowerInvariant());
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
                    foreach (DataColumn column in file.DataTable.Columns)
                    {
                        var dtColumn = dataTable.Columns[column.ColumnName].ColumnName;
                        newRow.SetField(dtColumn, fileDataRow.Field<string>(column.ColumnName));
                    }
                    dataTable.Rows.Add(newRow);
                }
            }
            return dataTable;
        }
    }
}
