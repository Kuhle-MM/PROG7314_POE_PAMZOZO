using Microsoft.AspNetCore.Mvc;
using PROG7314_POE.Models; // assuming you have a Location model

namespace PROG7314_POE.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class LocationController : ControllerBase
    {
        // POST /location → SetReturnLocation
        [HttpPost("Location")]
        public IActionResult SetReturnLocation([FromBody] Location location)
        {
            // TODO: implement logic to save or update location
            return Ok(new { message = "Location received", location });
        }

        // GET /location → GetCurrentLocation
        [HttpGet("Location")]
        public IActionResult GetCurrentLocation()
        {
            // TODO: implement logic to return current location
            var dummyLocation = new Location
            {
                Latitude = 0.0,
                Longitude = 0.0
            };
            return Ok(dummyLocation);
        }
    }
}
