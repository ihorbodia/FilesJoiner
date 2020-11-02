using FileJoiner.Models;
using ReactiveUI;
using System;
using System.Collections.Generic;
using System.Linq;
using Avalonia.Collections;
using IronXL;

namespace FileJoiner.ViewModels
{
    public class MainDataViewModel : ViewModelBase
    {
        #region Private properties
        string status;
        DataGridCollectionView dataGridItems;
        #endregion

        public MainDataViewModel()
        {
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
            List<WorkBook> wbs = new List<WorkBook>();
            foreach (ExtendedFile file in DataGridItems)
            {
                wbs.Add(WorkBook.Load(file.File.FullName));
            }

            var test = wbs.First();
            var defaultWorksheet = test.DefaultWorkSheet;

            foreach (var item in defaultWorksheet.Columns)
            {
                Console.WriteLine(item);
            }
            var header = defaultWorksheet.GetRow(0);
            var data = defaultWorksheet.GetRow(1);
            Console.WriteLine(test);
        }

        bool FileHasRightExtension(ExtendedFile file)
        {
            var result =
                file.File.Extension.Contains(".csv") ||
                file.File.Extension.Contains(".txt") ||
                file.File.Extension.Contains(".xlsx");

            return result;
        }
    }
}
