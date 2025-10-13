using Microsoft.AspNetCore.Mvc;
using PROG7314_POE.Models;
using System.Net.Http;
using System.Threading.Tasks;

[ApiController]
[Route("api/[controller]")]
public class CameraController : ControllerBase
{
    private readonly HttpClient _httpClient;
    private readonly string _piBaseUrl = "http://192.168.137.250:5000";

    public CameraController(IHttpClientFactory factory)
    {
        _httpClient = factory.CreateClient();
    }

    [HttpPost("move")]
    public async Task<IActionResult> MoveCamera([FromBody] CameraRequest request)
    {
        try
        {
            var response = await _httpClient.PostAsJsonAsync($"{_piBaseUrl}/camera/move", request);
            if (!response.IsSuccessStatusCode)
                return StatusCode((int)response.StatusCode, await response.Content.ReadAsStringAsync());
            return Ok(await response.Content.ReadAsStringAsync());
        }
        catch (HttpRequestException ex)
        {
            return StatusCode(500, $"Error reaching Pi: {ex.Message}");
        }
    }

    [HttpPost("joystick")]
    public async Task<IActionResult> CameraJoystick([FromBody] CameraRequest request)
    {
        try
        {
            var response = await _httpClient.PostAsJsonAsync($"{_piBaseUrl}/camera/joystick", request);
            if (!response.IsSuccessStatusCode)
                return StatusCode((int)response.StatusCode, await response.Content.ReadAsStringAsync());
            return Ok(await response.Content.ReadAsStringAsync());
        }
        catch (HttpRequestException ex)
        {
            return StatusCode(500, $"Error reaching Pi: {ex.Message}");
        }
    }

    [HttpPost("reset")]
    public async Task<IActionResult> ResetCamera()
    {
        try
        {
            var response = await _httpClient.PostAsync($"{_piBaseUrl}/camera/reset", null);
            if (!response.IsSuccessStatusCode)
                return StatusCode((int)response.StatusCode, await response.Content.ReadAsStringAsync());
            return Ok(await response.Content.ReadAsStringAsync());
        }
        catch (HttpRequestException ex)
        {
            return StatusCode(500, $"Error reaching Pi: {ex.Message}");
        }
    }
}

// DTO
public class CameraRequest
{
    public float Pan { get; set; } = 90;
    public float Tilt { get; set; } = 45;
    public float Dx { get; set; } = 0;  // for joystick
    public float Dy { get; set; } = 0;  // for joystick
    public float Speed { get; set; } = 1;
}
