using FileJoiner.Models;
using ReactiveUI;
using System;
using System.Collections.Generic;
using System.Linq;
using Avalonia.Collections;
using System.Data;
using FileJoiner.Logic;
using System.Diagnostics;
using System.Collections.ObjectModel;
using IronXL;

namespace FileJoiner.ViewModels
{
    public class MainDataViewModel : ViewModelBase
    {
        #region Private properties
        string status;
        DataGridCollectionView dataGridItems;
        FileDataReader fileDataReader;
        ObservableCollection<ExtendedFile> _items;
        #endregion

        public MainDataViewModel()
        {
            fileDataReader = new FileDataReader();
        }

        #region Public properties
        //public DataGridCollectionView DataGridItems 
        //{
        //    get => dataGridItems;
        //    set => this.RaiseAndSetIfChanged(ref dataGridItems, value);
        //}

        public ObservableCollection<ExtendedFile> Items 
        {
            get => _items;
            set => this.RaiseAndSetIfChanged(ref _items, value);
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
            Items = new ObservableCollection<ExtendedFile>(extendedFiles);
        }

        public void ProcessFiles()
        {
            foreach (ExtendedFile file in Items)
            {
                file.DataTable = fileDataReader.ReadFileContent(file);
            }

            var fileComposer = new FileComposer();
            fileComposer.GetCommonHeaders(Items);
            var dataTable = fileComposer.ComposeFiles(Items);

            WorkBook wb = new WorkBook();
            wb.LoadWorkSheet(dataTable);
            wb.SaveAsCsv("result_data.csv", ",");

            var buffer = new ObservableCollection<ExtendedFile>(Items);
            Items = new ObservableCollection<ExtendedFile>(buffer);
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
