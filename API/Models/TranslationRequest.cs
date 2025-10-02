namespace PROG7314_POE.Models
{
    public class TranslationRequest
    {
        public string Text { get; set; } = string.Empty;
        public string From { get; set; } = "";
        public string To { get; set; } = "en";
        public string UserId { get; set; } = "";
    }
}
