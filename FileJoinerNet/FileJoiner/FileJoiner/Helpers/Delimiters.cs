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

        public static string GetDelimiterByHeader(string headerRow)
        {
            int wordsCount = 0;
            string delimiter = string.Empty;

            foreach (var possibleDelimiter in List)
            {
                int currentWordsCount = headerRow.Split(possibleDelimiter).Length;
                if (currentWordsCount > wordsCount)
                {
                    wordsCount = currentWordsCount;
                    delimiter = possibleDelimiter;
                }
            }
            return delimiter;
        }
    }
}
