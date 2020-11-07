using FileJoiner.Models;
using ReactiveUI;
using System.Collections.Generic;
using FileJoiner.Logic;
using System.Collections.ObjectModel;
using IronXL;
using System.Linq;
using System.Threading.Tasks;
using System.Diagnostics;
using System.IO;
using System;
using System.Data;
using FileJoiner.Helpers;

namespace FileJoiner.ViewModels
{
    public class MainDataViewModel : ViewModelBase
    {
        #region Private properties
        string status;
        FileDataReader fileDataReader;
        FileProcessor fileProcessor;
        ObservableCollection<ExtendedFile> _items;
        #endregion

        #region Public properties
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
            //var worker = Task.Run(() => Process())
            //    .ContinueWith((parameter) =>
            //    {
            //        var buffer = new ObservableCollection<ExtendedFile>(Items);
            //        Items = new ObservableCollection<ExtendedFile>(buffer);
            //    });
            Process();
            var buffer = new ObservableCollection<ExtendedFile>(Items);
            Items = new ObservableCollection<ExtendedFile>(buffer);
        }

        void Process()
        {
            fileProcessor = new FileProcessor();
            fileDataReader = new FileDataReader();

            fileDataReader.ReadFilesContent(Items);
            fileProcessor.GetCommonHeaders(Items);

            var dataTable = fileProcessor.ComposeFiles(Items);

            try
            {
                dataTable.ToCSV("result_data.csv");
            }
            catch (System.Exception ex)
            {
                Debug.Write("Cannot save result file");
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
