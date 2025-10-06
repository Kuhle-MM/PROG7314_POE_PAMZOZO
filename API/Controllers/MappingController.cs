using Microsoft.AspNetCore.Mvc;
using PROG7314_POE.Models;
using PROG7314_POE.Services;

namespace PROG7314_POE.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class MappingController : ControllerBase
    {
        private readonly NavigationService _navigationService;

        public MappingController(NavigationService navigationService)
        {
            _navigationService = navigationService;
        }

        [HttpPost("set")]
        public IActionResult SetReturnMapping([FromBody] Mapping mapping)
        {
            if (mapping == null || string.IsNullOrEmpty(mapping.RobotId))
                return BadRequest("Invalid mapping data.");

            _navigationService.SetMapping(mapping.RobotId, mapping.Coordinates);
            return Ok(new { message = "Mapping set successfully." });
        }

        [HttpGet("{robotId}")]
        public IActionResult GetCurrentMapping(string robotId)
        {
            var map = _navigationService.GetMapping(robotId);
            return Ok(new { robotId, coordinates = map });
        }

        [HttpPost("update")]
        public IActionResult UpdateLocation([FromBody] LocationUpdate update)
        {
            if (update == null || string.IsNullOrEmpty(update.RobotId))
                return BadRequest("Invalid update request.");

            _navigationService.UpdateLocation(update.RobotId, new Location { X = update.X, Y = update.Y });
            return Ok(new { message = "Location updated successfully." });
        }
    }

    public class LocationUpdate
    {
        public string RobotId { get; set; }
        public double X { get; set; }
        public double Y { get; set; }
    }
}
