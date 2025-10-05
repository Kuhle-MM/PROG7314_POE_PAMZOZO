using Microsoft.AspNetCore.Mvc;
using PROG7314_POE.Models;
using PROG7314_POE.Services;

namespace Charades.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class GameController : ControllerBase
    {
        private readonly IGameService _game;


        public GameController(IGameService game)
        {
            _game = game;
        }


        [HttpGet("categories")]
        public IActionResult GetCategories() => Ok(_game.GetCategories());


        [HttpPost("start")]
        public IActionResult Start([FromBody] StartRequest req)
        {
            try
            {
                var session = _game.StartSession(req.Category ?? "movies", req.RoundSeconds <= 0 ? 60 : req.RoundSeconds);
                return Ok(new { sessionId = session.Id, session.Category, session.RoundSeconds, session.RoundEndsAt, session.IsActive });
            }
            catch (ArgumentException ex)
            {
                return BadRequest(new { error = ex.Message });
            }
        }

        [HttpGet("{sessionId}/current")]
        public IActionResult GetCurrent(string sessionId)
        {
            var s = _game.GetSession(sessionId);
            if (s == null) return NotFound();
            return Ok(new
            {
                sessionId = s.Id,
                current = s.CurrentItem != null ? new { s.CurrentItem.Id, s.CurrentItem.Text } : null,
                s.IsActive,
                s.RoundEndsAt,
                scores = s.Scores
            });
        }

        [HttpPost("{sessionId}/guess")]
        public IActionResult Guess(string sessionId, [FromBody] GuessRequest req)
        {
            if (string.IsNullOrWhiteSpace(req.Player) || string.IsNullOrWhiteSpace(req.Guess)) return BadRequest();
            if (!_game.SubmitGuess(sessionId, req.Player, req.Guess, out var correct)) return NotFound();
            return Ok(new { correct });
        }


        [HttpPost("{sessionId}/skip")]
        public IActionResult Skip(string sessionId)
        {
            if (!_game.Skip(sessionId, out var next)) return NotFound();
            return Ok(new { next = next != null ? new { next.Id, next.Text } : null });
        }


        [HttpPost("add-item")]
        public IActionResult AddItem([FromBody] GameItem item)
        {
            if (string.IsNullOrWhiteSpace(item.Category) || string.IsNullOrWhiteSpace(item.Text)) return BadRequest();
            _game.AddItem(item);
            return Ok(item);
        }
    }

    public class StartRequest
    {
        public string? Category { get; set; }
        public int RoundSeconds { get; set; } = 60;
    }


    public class GuessRequest
    {
        public string Player { get; set; } = string.Empty;
        public string Guess { get; set; } = string.Empty;
    }
}