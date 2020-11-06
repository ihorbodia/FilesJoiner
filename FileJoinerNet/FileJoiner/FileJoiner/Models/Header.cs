using FileJoiner.Helpers;
using System.Data;
using System.Linq;

namespace FileJoiner.Models
{
    public class Header
    {
        public Header(DataColumn dataColumn)
        {
            OriginalHeader = dataColumn.ColumnName;
            InitNewHeader();
        }
        public string OriginalHeader { get; }
        public string NewHeader { get; private set; }

        void InitNewHeader()
        {
            foreach (var headers in ColumnNames.SuggestedHeaders)
            {
                var headerExists = headers.Value
                    .Any(header => OriginalHeader.Contains(header, System.StringComparison.InvariantCultureIgnoreCase));
                if (headerExists)
                {
                    NewHeader = headers.Key;
                    return;
                }
            }
            NewHeader = OriginalHeader;
        }
    }
}
