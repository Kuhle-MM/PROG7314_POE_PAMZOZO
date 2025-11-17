using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using PROG7314_POE.Services;

namespace PROG7314_POE.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class CameraCapturingController : ControllerBase
    {
        ////GET /video-feed → StreamLiveFeed()
        //[HttpGet("VideoFeed")]
        ////GET /map → GetMapData()
        //[HttpGet("GetMapData")]

        private readonly CameraService _cameraService;

        public CameraCapturingController(CameraService cameraService)
        {
            _cameraService = cameraService;
        }

        [HttpPost("upload")]
        [Authorize]  // <-- requires valid JWT
        public async Task<IActionResult> UploadImage([FromForm] IFormFile file)
        {
            if (file == null || file.Length == 0)
                return BadRequest("No file uploaded.");

            using var ms = new MemoryStream();
            await file.CopyToAsync(ms);
            var imageBytes = ms.ToArray();

            _cameraService.SetLatestImage(imageBytes);

            return Ok("Image uploaded");
        }
    }
}
