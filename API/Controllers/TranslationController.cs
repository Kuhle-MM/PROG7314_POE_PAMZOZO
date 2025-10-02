using Microsoft.AspNetCore.Mvc;
using PROG7314_POE.Models;
using PROG7314_POE.Services;

namespace PROG7314_POE.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class TranslationController : ControllerBase
    {
        private readonly TranslationService _service;

        public TranslationController(TranslationService service)
        {
            _service = service;
        }

        [HttpPost("convert")]
        public async Task<IActionResult> Convert([FromBody] TranslationRequest req)
        {
            if (req == null || string.IsNullOrWhiteSpace(req.Text)) return BadRequest(new { error = "Text is required." });

            var to = req.To?.ToLower() ?? "en";
            if (!_service.IsLanguageAllowed(to)) return BadRequest(new { error = "Target language not supported. Allowed: en, af, xh, zu" });

            string? source = req.From?.Trim();
            if (string.IsNullOrWhiteSpace(source) || source.ToLower() == "auto")
            {
                var detected = await _service.DetectLanguageAsync(req.Text);
                if (detected == null) return StatusCode(502, new { error = "Failed to detect language." });
                source = detected.ToLower();              
            }

            var (success, translated, error) = await _service.TranslateAsync(req.Text, source, to);
            if (!success) return StatusCode(502, new { error });

            return Ok(new { original = req.Text, detected = source, translated, from = source, to });
        }

        [HttpGet("convert")]
        public async Task<IActionResult> ConvertGet([FromQuery] string text, [FromQuery] string from, [FromQuery] string to)
        {
            var req = new TranslationRequest { Text = text, From = from, To = to };
            return await Convert(req);
        }
    }
}
