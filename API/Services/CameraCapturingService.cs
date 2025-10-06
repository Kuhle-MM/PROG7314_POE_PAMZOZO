
    namespace PROG7314_POE.Services
    {
        public class CameraService
        {
            public byte[] GetLiveFeed()
            {
                // Simulate a camera snapshot (replace with actual camera integration)
                return File.ReadAllBytes("path/to/sample.jpg");
            }

            public async Task<byte[]> CaptureImageAsync()
            {
                return await File.ReadAllBytesAsync("path/to/sample.jpg");
            }

            public string ConvertToBase64(byte[] imageData)
            {
                return Convert.ToBase64String(imageData);
            }
        }
    }


