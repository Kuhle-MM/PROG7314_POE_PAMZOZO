using Microsoft.AspNetCore.Mvc;
using PROG7314_POE.Models;
using System.Net.Http;
using System.Text;
using System.Text.Json;
using System.Threading.Tasks;

[ApiController]
[Route("api/[controller]")]
public class MotorController : ControllerBase
{
    private readonly HttpClient _httpClient;
    private readonly string _piBaseUrl = "http://192.168.137.250:5000/api/command"; // Your Pi endpoint

    public MotorController(IHttpClientFactory factory)
    {
        _httpClient = factory.CreateClient();
    }

    // Move robot based on joystick or direction
    [HttpPost("move")]
    public async Task<IActionResult> MoveRobot([FromBody] MoveRequest request)
    {
        if (string.IsNullOrEmpty(request.Cmd))
            return BadRequest("Command is required (e.g., forward, backward, left, right, stop)");

        try
        {
            var response = await _httpClient.PostAsJsonAsync(_piBaseUrl, request);

            if (!response.IsSuccessStatusCode)
            {
                var msg = await response.Content.ReadAsStringAsync();
                return StatusCode((int)response.StatusCode, $"Pi returned error: {msg}");
            }

            return Ok(await response.Content.ReadAsStringAsync());
        }
        catch (HttpRequestException ex)
        {
            return StatusCode(500, $"Error reaching Raspberry Pi: {ex.Message}");
        }
    }

    // Stop robot immediately
    [HttpPost("stop")]
    public async Task<IActionResult> StopRobot()
    {
        var stopCommand = new MoveRequest { Cmd = "stop", Speed = 0 };

        try
        {
            var response = await _httpClient.PostAsJsonAsync(_piBaseUrl, stopCommand);
            if (!response.IsSuccessStatusCode)
            {
                var msg = await response.Content.ReadAsStringAsync();
                return StatusCode((int)response.StatusCode, $"Pi returned error: {msg}");
            }

            return Ok("Robot stopped successfully.");
        }
        catch (HttpRequestException ex)
        {
            return StatusCode(500, $"Error sending stop command: {ex.Message}");
        }
    }
}

// DTO for communication
public class MoveRequest
{
    public string Cmd { get; set; }   // forward, backward, left, right, stop
    public int Speed { get; set; } = 50;
}
