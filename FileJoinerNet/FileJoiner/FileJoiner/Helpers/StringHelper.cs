using System.Data;

namespace FileJoiner.Helpers
{
    public static class StringHelper
    {
        public static string TrimQuotes(this string value)
        {
            return value.TrimStart('"').TrimEnd('"');
        }

        public static DataColumn TrimQuotes(this DataColumn column)
        {
            column.ColumnName = column.ColumnName.TrimQuotes();
            return column;
        }
    }
}
