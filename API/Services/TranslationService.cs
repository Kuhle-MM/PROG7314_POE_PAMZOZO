using System.Net.Http;
using System.Net.Http.Json;
using System.Threading.Tasks;
using Microsoft.Extensions.Configuration;
using PROG7314_POE.Models;

namespace PROG7314_POE.Services
{
    public class TranslationService : ITranslationService
    {
        private readonly HttpClient _httpClient;
        private readonly string _apiKey;
        private static readonly HashSet<string> Allowed = new() { "en", "af", "xh", "zu" };

        public TranslationService(HttpClient httpClient, IConfiguration config)
        {
            _httpClient = httpClient;
            _apiKey = config["GoogleTranslateApiKey"] ?? throw new ArgumentNullException("GoogleTranslateApiKey not configured in appsettings.json");
            _httpClient.DefaultRequestHeaders.Accept.Clear();
            _httpClient.DefaultRequestHeaders.Accept.Add(new System.Net.Http.Headers.MediaTypeWithQualityHeaderValue("application/json"));
        }

        public bool IsLanguageAllowed(string code)
            => !string.IsNullOrWhiteSpace(code) && Allowed.Contains(code.ToLower());


        public async Task<string?> DetectLanguageAsync(string text)
        {
            var url = $"https://translation.googleapis.com/language/translate/v2/detect?key={_apiKey}";
            var body = new { q = text };

            var resp = await _httpClient.PostAsJsonAsync(url, body);
            if (!resp.IsSuccessStatusCode) return null;

            var payload = await resp.Content.ReadFromJsonAsync<DetectResponse>();
            var lang = payload?.Data?.Detections?.FirstOrDefault()?.FirstOrDefault()?.Language;
            return lang;
        }

        public async Task<(bool Success, string TranslatedText, string? Error)> TranslateAsync(string text, string? fromLang, string toLang)
        {
            if (string.IsNullOrWhiteSpace(text)) return (false, string.Empty, "Text is required.");
            if (string.IsNullOrWhiteSpace(toLang) || !IsLanguageAllowed(toLang)) return (false, string.Empty, $"Target language '{toLang}' not supported.");

            if (!string.IsNullOrWhiteSpace(fromLang) && fromLang.ToLower() != "auto" && !IsLanguageAllowed(fromLang))
                return (false, string.Empty, $"Source language '{fromLang}' not supported.");

            var url = $"https://translation.googleapis.com/language/translate/v2?key={_apiKey}";

            object body;
            if (string.IsNullOrWhiteSpace(fromLang) || fromLang.ToLower() == "auto")
            {
                body = new
                {
                    q = text,
                    target = toLang,
                    format = "text"
                };
            }
            else
            {
                body = new
                {
                    q = text,
                    source = fromLang,
                    target = toLang,
                    format = "text"
                };
            }

            try
            {
                var resp = await _httpClient.PostAsJsonAsync(url, body);
                if (!resp.IsSuccessStatusCode)
                {
                    var err = await resp.Content.ReadAsStringAsync();
                    return (false, string.Empty, $"Google Translate error: {resp.StatusCode} - {err}");
                }

                var payload = await resp.Content.ReadFromJsonAsync<TranslateResponse>();
                var translated = payload?.Data?.Translations?.FirstOrDefault()?.TranslatedText;
                if (string.IsNullOrWhiteSpace(translated))
                    return (false, string.Empty, "No translation returned.");

                return (true, translated, null);
            }
            catch (Exception ex)
            {
                return (false, string.Empty, $"Exception: {ex.Message}");
            }
        }

        private class TranslateResponse
        {
            public TranslateData? Data { get; set; }
        }

        private class TranslateData
        {
            public List<TranslationItem>? Translations { get; set; }
        }

        private class TranslationItem
        {
            public string? TranslatedText { get; set; }
            public string? DetectedSourceLanguage { get; set; }
        }

        private class DetectResponse
        {
            public DetectData? Data { get; set; }
        }

        private class DetectData
        {
            public List<List<DetectItem>>? Detections { get; set; }
        }

        private class DetectItem
        {
            public string? Language { get; set; }
            public bool? IsReliable { get; set; }
            public float? Confidence { get; set; }
        }
    }
}
