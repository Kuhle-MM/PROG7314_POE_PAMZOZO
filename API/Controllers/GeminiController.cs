using Microsoft.AspNetCore.Mvc;
using PROG7314_POE.Services;

namespace PROG7314_POE.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class GeminiController : ControllerBase
    {
        private readonly GeminiService _geminiService;

        public GeminiController(GeminiService geminiService)
        {
            _geminiService = geminiService;
        }

        public class GeminiQuery
        {
            public string Question { get; set; }
        }

        [HttpPost("ask")]
        public async Task<IActionResult> AskGemini([FromBody] GeminiQuery query)
        {
            if (string.IsNullOrWhiteSpace(query.Question))
                return BadRequest("Question cannot be empty.");

            var answer = await _geminiService.AskGeminiAsync(query.Question);
            return Ok(new { answer });
        }

        [HttpGet("models")]
        public async Task<IActionResult> ListModels([FromServices] IConfiguration config, [FromServices] HttpClient httpClient)
        {
            var apiKey = config["GeminiApiKey"];
            var url = $"https://generativelanguage.googleapis.com/v1/models?key={apiKey}";

            var response = await httpClient.GetAsync(url);
            var content = await response.Content.ReadAsStringAsync();

            if (!response.IsSuccessStatusCode)
            {
                return BadRequest(content);
            }

            return Ok(content);
        }

    }
}
