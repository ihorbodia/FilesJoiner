using Avalonia.Controls;
using Avalonia.Input;
using Avalonia.Markup.Xaml;
using Avalonia.ReactiveUI;
using FileJoiner.ViewModels;
using System;

namespace FileJoiner.Views
{
    public class MainDataView : UserControl
    {
        public MainDataViewModel _model;
        
        public MainDataView()
        {
            this.InitializeComponent();

            DataGrid _DataGrid = this.FindControl<DataGrid>("mainDataGrid");
            _DataGrid.IsReadOnly = true;
            _DataGrid.AddHandler(DragDrop.DropEvent, Drop);
        }

        protected override void OnDataContextChanged(EventArgs e)
        {
            if (_model != null)
                _model.View = null;
            _model = DataContext as MainDataViewModel;
            if (_model != null)
                _model.View = this;

            base.OnDataContextChanged(e);
        }

        private void Drop(object sender, DragEventArgs e)
        {
            var files = e.Data.GetFileNames();
            _model.DataGridFilesDragged(files);
        }

        private void InitializeComponent()
        {
            AvaloniaXamlLoader.Load(this);
        }
    }
}
