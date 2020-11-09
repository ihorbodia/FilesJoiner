using FileJoiner.Models;
using ReactiveUI;
using System.Collections.Generic;
using FileJoiner.Logic;
using System.Collections.ObjectModel;
using System.Threading.Tasks;
using System.Diagnostics;
using FileJoiner.Helpers;

namespace FileJoiner.ViewModels
{
    public class MainDataViewModel : ViewModelBase
    {
        #region Private properties
        string status;
        ObservableCollection<ExtendedFile> items;
        bool canExecute;
        #endregion
        public MainDataViewModel()
        {
            Status = "Drag and drop files on header";
        }
        #region Public properties
        public ObservableCollection<ExtendedFile> Items 
        {
            get => items;
            set => this.RaiseAndSetIfChanged(ref items, value);
        }
        public string Status
        {
            get => status;
            set => this.RaiseAndSetIfChanged(ref status, value);
        }
        public bool CanExecute
        {
            get => canExecute;
            set => this.RaiseAndSetIfChanged(ref canExecute, value);
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
                    CanExecute = true;
                    Status = "Ready for start";
                }
            }
            Items = new ObservableCollection<ExtendedFile>(extendedFiles);
        }

        public void ProcessFiles()
        {
            Task.Factory.StartNew(() =>
            {
                CanExecute = false;
                Status = "Processing";
                Process();
            })
            .ContinueWith((parameter) =>
            {
                var buffer = new ObservableCollection<ExtendedFile>(Items);
                Items = new ObservableCollection<ExtendedFile>(buffer);
                CanExecute = true;
                Status = "Finished";
            });
        }

        void Process()
        {
            var fileProcessor = new FileProcessor();
            fileProcessor.ReadFilesContent(Items);
            fileProcessor.GetCommonHeaders(Items);
            var dataTable = fileProcessor.ComposeFiles(Items);
            Status = "Removing duplicates";
            dataTable = dataTable.RemoveDuplicates();
            try
            {
                dataTable.ToCSV("result_data.csv");
            }
            catch (System.Exception ex)
            {
                Debug.Write("Cannot save result file. " + ex.Message);
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
