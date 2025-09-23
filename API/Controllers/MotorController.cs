using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using PROG7314_POE.Models;
using PROG7314_POE.Services;

namespace PROG7314_POE.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class MotorController : ControllerBase
    {
        //POST method to send the co-ordinates to the raspberry pi for the motor directions
        [HttpPost("moveMotors")]
        public IActionResult MoveMotors([FromBody] Motor motorCommand)
        {
            // Convert joystick vector (x,y) to motor PWM
            double left = (motorCommand.y) + (motorCommand.x);
            double right = (motorCommand.y) - (motorCommand.x);

            // Normalize to 0–100 range
            int leftPwm = (int)(Math.Clamp(left, -1, 1) * 100);
            int rightPwm = (int)(Math.Clamp(right, -1, 1) * 100);

            var response = new Motor
            {
                leftMotorSpeed = leftPwm,
                rightMotorSpeed = rightPwm
            };

            return Ok(response);
        }

        //POST method to send the co-ordinates to the raspberry pi for the motor speed
        [HttpPost("stopMotors")]
        public IActionResult StopMotors() //stops both wheels
        {
            return Ok(new Motor { leftMotorSpeed = 0, rightMotorSpeed = 0 });
        }
    }
}
