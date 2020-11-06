using FileJoiner.Models;
using ReactiveUI;
using System.Collections.Generic;
using FileJoiner.Logic;
using System.Collections.ObjectModel;
using IronXL;
using System.Linq;

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
            fileProcessor = new FileProcessor();
            fileDataReader = new FileDataReader();

            fileDataReader.ReadFilesContent(Items);
            fileProcessor.GetCommonHeaders(Items);

            var dataTable = fileProcessor.ComposeFiles(Items);

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
