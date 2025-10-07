namespace PROG7314_POE.Services
{
    public interface ITranslationService
    {
        bool IsLanguageAllowed(string code);
        Task<string?> DetectLanguageAsync(string text);
        Task<(bool Success, string TranslatedText, string? Error)> TranslateAsync(string text, string? fromLang, string toLang);
    }
}
