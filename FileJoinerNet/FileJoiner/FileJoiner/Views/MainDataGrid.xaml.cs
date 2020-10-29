using Avalonia;
using Avalonia.Collections;
using Avalonia.Controls;
using Avalonia.Input;
using Avalonia.Markup.Xaml;
using FileJoiner.Models;
using System.Collections.Generic;

namespace FileJoiner.Views
{
    public class MainDataGrid : UserControl
    {
        DataGrid _DataGrid;
        
        public MainDataGrid()
        {
            this.InitializeComponent();

            _DataGrid = this.FindControl<DataGrid>("mainDataGrid");
            _DataGrid.IsReadOnly = true;

            AddHandler(DragDrop.DropEvent, Drop);
        }


        private void Drop(object sender, DragEventArgs e)
        {
            var files = e.Data.GetFileNames();
            var extendedFiles = new List<ExtendedFile>();
            foreach (var file in files)
            {
                extendedFiles.Add(new ExtendedFile(file));
            }
            _DataGrid.Items = new DataGridCollectionView(extendedFiles);
        }

        private void InitializeComponent()
        {
            AvaloniaXamlLoader.Load(this);
        }
    }
}
