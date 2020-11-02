using Avalonia.Controls;
using FileJoiner.Models;
using ReactiveUI;
using System;
using System.Collections;
using System.Collections.Generic;
using System.Linq;
using System.Reactive;
using System.Threading.Tasks;
using System.Windows;
using Avalonia.Interactivity;
using Avalonia.Collections;

namespace FileJoiner.ViewModels
{
    public class MainDataViewModel : ViewModelBase
    {
        string status;
        //public List<ExtendedFile> files;
        public Control View { get; set; }
        public DataGrid _DataGrid { get; set; }

        public DataGridCollectionView DataGridItems { get; set; }
        public MainDataViewModel()
        {
        }

        public string Status
        {
            get => status;
            set => this.RaiseAndSetIfChanged(ref status, value);
        }

        public void DataGridFilesDragged(IEnumerable<string> files)
        {
            var extendedFiles = new List<ExtendedFile>();
            foreach (var file in files)
            {
                extendedFiles.Add(new ExtendedFile(file));
            }
            DataGridItems = new DataGridCollectionView(extendedFiles);
        }

        //private void Drop(object sender, DragEventArgs e)
        //{
        //    var files = e.Data.GetFileNames();
        //    var extendedFiles = new List<ExtendedFile>();
        //    foreach (var file in files)
        //    {
        //        extendedFiles.Add(new ExtendedFile(file));
        //    }
        //    _DataGrid.Items = new DataGridCollectionView(extendedFiles);
        //}
    }
}
