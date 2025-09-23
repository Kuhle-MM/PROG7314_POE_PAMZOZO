using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using PROG7314_POE.Models;
using PROG7314_POE.Services;

namespace PROG7314_POE.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class CameraController : ControllerBase
    {
        //POST method to send the co-ordinates to the raspberry pi for the camera servo directions
        [HttpPost ("moveCamera")]
        public IActionResult MoveCameraServo([FromBody] Camera command)
        {
           // Validate angles
           double Pan = Math.Clamp(command.pan, 0, 180);
           double Tilt = Math.Clamp(command.tilt, 0, 90);

           return Ok(new { pan = Pan, tilt = Tilt });
            
        }

        //POST method to send the co-ordinates to the raspberry pi for the camera servo speed
        [HttpPost("resetCamera")]
        public IActionResult ResetCameraServo() //resets the camera to a neutral position
        {
            return Ok(new { Pan = 90, Tilt = 45 }); // Neutral position
        }
    }
}
