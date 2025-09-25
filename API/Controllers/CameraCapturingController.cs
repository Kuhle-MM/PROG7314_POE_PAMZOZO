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

        private readonly CameraCapturingService _cameraService;

        public CameraCapturingController(CameraCapturingService cameraService)
        {
            _cameraService = cameraService;
        }

        //[HttpGet("stream")]
        //public IActionResult GetCameraFeed()
        //{
        //    var imageBytes = _cameraService.GetLiveFeed();
        //    return File(imageBytes, "image/jpeg");
        //}
    }
}
