using System.Collections.Generic;
using System.Data;
using System.IO;

namespace FileJoiner.Models
{
    public class ExtendedFile
    {
        public ExtendedFile(string path)
        {
            Headers = new List<Header>();
            File = new FileInfo(path);
            Name = File.Name;
            Type = File.Extension.Replace(".", "").ToLower();
            var fileSize = File.Length / 1000;
            Size = fileSize == 0 ? 1 : fileSize;
        }
        public DataTable DataTable { get; set; }
        public List<Header> Headers { get; set; }
        public FileInfo File { get; private set; }
        public string Name { get; private set; }
        public string Type { get; private set; }
        public long Size { get; private set; }
        public bool Processed { get; set; }
    }
}
