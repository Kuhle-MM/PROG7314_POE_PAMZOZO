using Microsoft.AspNetCore.Mvc;
using PROG7314_POE.Models;
using System.Net.Http;
using System.Text;
using System.Text.Json;
using System.Threading.Tasks;

[ApiController]
[Route("api/[controller]")]
public class CameraController : ControllerBase
{
    private readonly HttpClient _httpClient;
    private readonly string _piBaseUrl = "http://192.168.137.250:5000";
 
    public CameraController(HttpClient httpClient)
    {
        _httpClient = httpClient;
    }

    // POST method to send coordinates to the Pi for the camera servo directions
    [HttpPost("moveCamera")]
    public async Task<IActionResult> MoveCameraServo([FromBody] Camera command)
    {
        // 1. Validate angles
        double pan = Math.Clamp(command.pan, 0, 180);
        double tilt = Math.Clamp(command.tilt, 0, 90);

        var payload = new { Pan = pan, Tilt = tilt };

        // 2. Forward to Raspberry Pi
        var json = JsonSerializer.Serialize(payload);
        var content = new StringContent(json, Encoding.UTF8, "application/json");

        try
        {
            var forwardResponse = await _httpClient.PostAsync($"{_piBaseUrl}/camera/move", content);

            if (!forwardResponse.IsSuccessStatusCode)
            {
                return StatusCode((int)forwardResponse.StatusCode,
                    $"Failed to forward camera command to Pi: {forwardResponse.ReasonPhrase}");
            }
        }
        catch (HttpRequestException ex)
        {
            return StatusCode(500, $"Error forwarding camera command to Pi: {ex.Message}");
        }

        // 3. Return confirmation
        return Ok(payload);
    }

    // POST method to reset the camera to a neutral position
    [HttpPost("resetCamera")]
    public async Task<IActionResult> ResetCameraServo()
    {
        var neutral = new { Pan = 90, Tilt = 45 }; // neutral position

        var json = JsonSerializer.Serialize(neutral);
        var content = new StringContent(json, Encoding.UTF8, "application/json");

        try
        {
            var forwardResponse = await _httpClient.PostAsync($"{_piBaseUrl}/camera/reset", content);

            if (!forwardResponse.IsSuccessStatusCode)
            {
                return StatusCode((int)forwardResponse.StatusCode,
                    $"Failed to forward reset command to Pi: {forwardResponse.ReasonPhrase}");
            }
        }
        catch (HttpRequestException ex)
        {
            return StatusCode(500, $"Error forwarding reset command to Pi: {ex.Message}");
        }

        return Ok(neutral);
    }
}
