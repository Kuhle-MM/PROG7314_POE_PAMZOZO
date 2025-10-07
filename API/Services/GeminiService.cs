using System.Net.Http;
using System.Net.Http.Json;
using System.Threading.Tasks;
using Microsoft.Extensions.Configuration;

namespace PROG7314_POE.Services
{
    public interface IGeminiService
    {
        Task<string> AskGeminiAsync(string prompt);
    }

    public class GeminiService : IGeminiService
    {
        private readonly HttpClient _httpClient;
        private readonly string _apiKey;

        public GeminiService(HttpClient httpClient, IConfiguration config)
        {
            _httpClient = httpClient;
            _apiKey = config["GeminiApiKey"];
        }

        public async Task<string> AskGeminiAsync(string prompt)
        {
            var request = new
            {
                contents = new[]
                {
                    new {
                        parts = new[]
                        {
                            new { text = prompt }
                        }
                    }
                }
            };

            var url = $"https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent?key={_apiKey}";

            var response = await _httpClient.PostAsJsonAsync(url, request);

            if (!response.IsSuccessStatusCode)
            {
                return $"Error: {response.StatusCode} - {await response.Content.ReadAsStringAsync()}";
            }

            var result = await response.Content.ReadFromJsonAsync<GeminiResponse>();

            return result?.Candidates?[0]?.Content?.Parts?[0]?.Text ?? "No response from Gemini.";
        }

        public class GeminiResponse
        {
            public Candidate[] Candidates { get; set; }

            public class Candidate
            {
                public Content Content { get; set; }
            }

            public class Content
            {
                public Part[] Parts { get; set; }
            }

            public class Part
            {
                public string Text { get; set; }
            }
        }
    }
}
