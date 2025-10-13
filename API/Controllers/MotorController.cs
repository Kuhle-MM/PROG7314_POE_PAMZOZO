using Microsoft.AspNetCore.Mvc;
using PROG7314_POE.Models;
using System.Net.Http;
using System.Net.Http.Json;
using System.Threading.Tasks;

[ApiController]
[Route("api/[controller]")]
public class MotorController : ControllerBase
{
    private readonly HttpClient _httpClient;

    // <-- Make sure this is the Pi's actual IP on your network
    private readonly string _piBaseUrl = "http://192.168.137.250:5000";

    public MotorController(IHttpClientFactory factory)
    {
        _httpClient = factory.CreateClient();
    }

    [HttpPost("move")]
    public async Task<IActionResult> MoveRobot([FromBody] MoveRequest request)
    {
        var endpoint = "/api/joystick";

        try
        {
            var response = await _httpClient.PostAsJsonAsync($"{_piBaseUrl}{endpoint}", request);

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

    [HttpPost("stop")]
    public async Task<IActionResult> StopRobot()
    {
        var stopCommand = new MoveRequest { X = 0, Y = 0, Speed = 0 };

        try
        {
            var response = await _httpClient.PostAsJsonAsync($"{_piBaseUrl}/api/joystick", stopCommand);

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

// Updated DTO
public class MoveRequest
{
    public float X { get; set; } = 0;    // -1 to 1
    public float Y { get; set; } = 0;    // -1 to 1
    public int Speed { get; set; } = 50;
}
