using System.Collections.Generic;
using System.Text.RegularExpressions;

namespace FileJoiner.Helpers
{
    public static class Delimiters
    {
        public static readonly List<string> List;

        readonly static string Tab = "\t";
        readonly static string VerticalBar = "|";
        readonly static string Semicolon = ";";
        readonly static string Comma = ",";
        
        static Delimiters()
        {
            List = new List<string>()
            {
                Tab, VerticalBar, Semicolon, Comma
            };
        }

        public static string GetSplitPattern(string delimiter)
        {
            string commonPattern = "(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";
            return delimiter.Equals("|") ? $"\\{delimiter}{commonPattern}" : $"{delimiter}{commonPattern}";
        }

        public static string GetDelimiterByHeader(string headerRow)
        {
            int wordsCount = 0;
            string delimiter = string.Empty;

            foreach (var possibleDelimiter in List)
            {
                int currentWordsCount = Regex.Split(headerRow, GetSplitPattern(possibleDelimiter)).Length;
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
