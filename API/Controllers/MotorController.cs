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
    private readonly string _piBaseUrl = "http://192.168.137.250:5000";

    public MotorController(HttpClient httpClient)
    {
        _httpClient = httpClient;
    }

    // POST method to calculate motor speeds and forward them to the Pi
    [HttpPost("moveMotors")]
    public async Task<IActionResult> MoveMotors([FromBody] Motor motorCommand)
    {
        // 1. Convert joystick vector (x,y) to motor PWM
        double left = (motorCommand.y) + (motorCommand.x);
        double right = (motorCommand.y) - (motorCommand.x);

        // Normalize to -100 to 100 range
        int leftPwm = (int)(Math.Clamp(left, -1, 1) * 100);
        int rightPwm = (int)(Math.Clamp(right, -1, 1) * 100);

        var response = new Motor
        {
            leftMotorSpeed = leftPwm,
            rightMotorSpeed = rightPwm
        };

        // 2. Forward command to Raspberry Pi
        var json = JsonSerializer.Serialize(response);
        var content = new StringContent(json, Encoding.UTF8, "application/json");

        try
        {
            var forwardResponse = await _httpClient.PostAsync($"{_piBaseUrl}/motor/move", content);

            if (!forwardResponse.IsSuccessStatusCode)
            {
                return StatusCode((int)forwardResponse.StatusCode,
                    $"Failed to forward command to Pi: {forwardResponse.ReasonPhrase}");
            }
        }
        catch (HttpRequestException ex)
        {
            return StatusCode(500, $"Error forwarding to Pi: {ex.Message}");
        }

        // 3. Return response to app
        return Ok(response);
    }

    // POST method to stop both motors
    [HttpPost("stopMotors")]
    public async Task<IActionResult> StopMotors()
    {
        var stopCommand = new Motor { leftMotorSpeed = 0, rightMotorSpeed = 0 };

        var json = JsonSerializer.Serialize(stopCommand);
        var content = new StringContent(json, Encoding.UTF8, "application/json");

        try
        {
            var forwardResponse = await _httpClient.PostAsync($"{_piBaseUrl}/motor/stop", content);

            if (!forwardResponse.IsSuccessStatusCode)
            {
                return StatusCode((int)forwardResponse.StatusCode,
                    $"Failed to forward stop command to Pi: {forwardResponse.ReasonPhrase}");
            }
        }
        catch (HttpRequestException ex)
        {
            return StatusCode(500, $"Error forwarding stop command to Pi: {ex.Message}");
        }

        return Ok(stopCommand);
    }
}
