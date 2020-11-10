using System.Collections.Generic;

namespace FileJoiner.Helpers
{
    public static class ColumnNames
    {
        public static Dictionary<string, List<string>> SuggestedHeaders;

        static ColumnNames()
        {
            SuggestedHeaders = new Dictionary<string, List<string>>();

            SuggestedHeaders.Add(nameof(Instagram), Instagram);
            SuggestedHeaders.Add(nameof(Email), Email);
            SuggestedHeaders.Add(nameof(Bio), Bio);
            SuggestedHeaders.Add(nameof(Link), Link);
            SuggestedHeaders.Add(nameof(Website), Website);
        }
        static List<string> Instagram = new List<string>()
        {
            "instagram", "inst"
        };
        static List<string> Email = new List<string>()
        {
            "email", "e-mail"
        };
        static List<string> Bio = new List<string>()
        {
            "bio"
        };

        static List<string> Link = new List<string>()
        {
            "link", "url"
        };

        static List<string> Website = new List<string>()
        {
            "website", "web"
        };
    }
}
