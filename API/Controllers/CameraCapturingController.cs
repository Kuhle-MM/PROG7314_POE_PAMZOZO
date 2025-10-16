using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using PROG7314_POE.Services;
using Microsoft.AspNetCore.Http;

namespace PROG7314_POE.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class CameraCapturingController : ControllerBase
    {
        private readonly CameraService _cameraService;

        public CameraCapturingController(CameraService cameraService)
        {
            _cameraService = cameraService;
        }

        //[HttpPost("upload")]
        //[Authorize(Policy = "DeviceOnly")]
        //public async Task<IActionResult> UploadImage([FromForm] IFormFile file)
        //{
        //    if (file == null || file.Length == 0)
        //        return BadRequest("No file uploaded.");

        //    if (!file.ContentType.StartsWith("image/"))
        //        return BadRequest("Upload must be an image.");

        //    using var ms = new MemoryStream();
        //    await file.CopyToAsync(ms);
        //    var imageBytes = ms.ToArray();

        //    _cameraService.SetLatestImage(imageBytes);

        //    return Ok(new { message = "Image uploaded", size = imageBytes.Length });
        //}

        private static byte[] _latestFrame; // ✅ Stored in memory

        // POST /api/CameraCapturing/upload
        [HttpPost("upload")]
        public IActionResult UploadFrame(IFormFile file)
        {
            if (file == null || file.Length == 0)
                return BadRequest("No file uploaded");

            using (var ms = new MemoryStream())
            {
                file.CopyTo(ms);
                _latestFrame = ms.ToArray(); // ✅ Update latest image
            }

            return Ok("Frame received");
        }

        // GET /api/CameraCapturing/latest
        [HttpGet("latest")]
        public IActionResult GetLatestFrame()
        {
            if (_latestFrame == null)
                return NotFound("No frame yet");

            return File(_latestFrame, "image/jpeg");
        }
    


        //[HttpGet("latest")]
        //[Authorize(Policy = "UserOnly")]  // only users should fetch frames (or AllowAnonymous if you want)
        //public IActionResult GetLatestImage()
        //{
        //    var img = _cameraService.GetLatestImage();
        //    if (img == null || img.Length == 0)
        //        return NotFound("No image available.");

        //    return File(img, "image/jpeg");
        //}

        // optional: mjpeg stream - only for users
        [HttpGet("mjpeg")]
        [Authorize(Policy = "UserOnly")]
        public async Task MjpegStream()
        {
            Response.ContentType = "multipart/x-mixed-replace; boundary=--myboundary";
            var boundary = "\r\n--myboundary\r\n";
            while (!HttpContext.RequestAborted.IsCancellationRequested)
            {
                var img = _cameraService.GetLatestImage();
                if (img != null)
                {
                    await Response.Body.WriteAsync(System.Text.Encoding.ASCII.GetBytes(boundary));
                    var headers = $"Content-Type: image/jpeg\r\nContent-Length: {img.Length}\r\n\r\n";
                    await Response.Body.WriteAsync(System.Text.Encoding.ASCII.GetBytes(headers));
                    await Response.Body.WriteAsync(img, 0, img.Length);
                    await Response.Body.FlushAsync();
                }

                await Task.Delay(200);
            }
        }
    }
}
