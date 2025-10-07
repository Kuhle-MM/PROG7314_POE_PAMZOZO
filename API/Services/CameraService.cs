namespace PROG7314_POE.Services
{
    public class CameraService
    {
        private byte[] _latestImage;

        public void SetLatestImage(byte[] imageBytes)
        {
            _latestImage = imageBytes;
        }

        public byte[] GetLatestImage()
        {
            return _latestImage;
        }
    }

}
