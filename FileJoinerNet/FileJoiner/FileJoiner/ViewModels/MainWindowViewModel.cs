using ReactiveUI;
using System;

namespace FileJoiner.ViewModels
{
    class MainWindowViewModel : ViewModelBase
    {
        ViewModelBase content;

        public MainWindowViewModel()
        {
            Content = MainDataViewModel = new MainDataViewModel();
        }

        public ViewModelBase Content
        {
            get => content;
            private set => this.RaiseAndSetIfChanged(ref content, value);
        }

        public MainDataViewModel MainDataViewModel { get; }
    }
}