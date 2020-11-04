using FileJoiner.Models;
using ReactiveUI;
using System;
using System.Collections.Generic;
using System.Linq;
using Avalonia.Collections;
using IronXL;
using System.IO;
using System.Text;
using System.Net;
using System.Globalization;
using CsvHelper;
using CsvHelper.Configuration;
using System.Data;
using FileJoiner.Logic;
using System.Diagnostics;

namespace FileJoiner.ViewModels
{
    public class MainDataViewModel : ViewModelBase
    {
        #region Private properties
        string status;
        DataGridCollectionView dataGridItems;
        FileDataReader fileDataReader;
        #endregion

        public MainDataViewModel()
        {
            fileDataReader = new FileDataReader();
        }

        #region Public properties
        public DataGridCollectionView DataGridItems 
        {
            get => dataGridItems;
            set => this.RaiseAndSetIfChanged(ref dataGridItems, value);
        }
        
        public string Status
        {
            get => status;
            set => this.RaiseAndSetIfChanged(ref status, value);
        }
        #endregion

        public void DataGridFilesDragged(IEnumerable<string> files)
        {
            var extendedFiles = new List<ExtendedFile>();
            foreach (var file in files)
            {
                var extendedFile = new ExtendedFile(file);
                if (FileHasRightExtension(extendedFile))
                {
                    extendedFiles.Add(extendedFile);
                    
                }
            }
            DataGridItems = new DataGridCollectionView(extendedFiles);
        }

        public void ProcessFiles()
        {
            foreach (ExtendedFile file in DataGridItems)
            {
                file.DataTable = fileDataReader.ReadFileContent(file);
            }

            foreach (ExtendedFile file in DataGridItems)
            {
                var dataTable = file.DataTable;

                Debug.WriteLine(file.Name);
                foreach (DataColumn column in dataTable.Columns)
                {
                    Debug.Write(column.ColumnName);
                    Debug.Write(" ");
                }
                Debug.WriteLine(" ");
                Debug.WriteLine(dataTable.Rows.Count);
                Debug.WriteLine(" ");
            }
            
        }

        bool FileHasRightExtension(ExtendedFile file)
        {
            var result =
                file.Type.Contains("csv") ||
                file.Type.Contains("tsv") ||
                file.Type.Contains("txt") ||
                file.Type.Contains("xlsx");

            return result;
        }

        
    }
}
