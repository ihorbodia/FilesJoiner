using Avalonia.Controls;
using ReactiveUI;

namespace FileJoiner.ViewModels
{
    public class ViewModelBase : ReactiveObject
    {
        public Control View { get; set; }
    }
}
