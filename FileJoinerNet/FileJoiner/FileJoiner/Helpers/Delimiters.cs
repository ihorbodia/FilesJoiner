using System.Collections.Generic;

namespace FileJoiner.Helpers
{
    public static class Delimiters
    {
        public static readonly List<string> List;
        static Delimiters()
        {
            List = new List<string>()
            {
                "\",\"", "\t", "|", ";", ","
            };
        }
    }
}
