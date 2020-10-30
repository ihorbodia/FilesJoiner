using System.IO;

namespace FileJoiner.Models
{
    public class ExtendedFile
    {
        public ExtendedFile(string path)
        {
            File = new FileInfo(path);
            Name = File.Name;
            Type = File.Extension.Replace(".", "");
            Size = File.Length / 1000;
        }
        public FileInfo File { get; private set; }

        public string Name { get; private set; }
        public string Type { get; private set; }
        public long Size { get; private set; }
    }
}
