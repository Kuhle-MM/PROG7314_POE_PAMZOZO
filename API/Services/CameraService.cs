using System;
using System.Threading;

namespace PROG7314_POE.Services
{
    public class CameraService
    {
        private byte[]? _latestImage;
        private readonly ReaderWriterLockSlim _lock = new ReaderWriterLockSlim();

        public void SetLatestImage(byte[] imageBytes)
        {
            _lock.EnterWriteLock();
            try
            {
                _latestImage = imageBytes;
            }
            finally
            {
                _lock.ExitWriteLock();
            }
        }
        public byte[]? GetLatestImage()
        {
            _lock.EnterReadLock();
            try
            {
                return _latestImage;
            }
            finally
            {
                _lock.ExitReadLock();
            }
        }

        public void ClearImage()
        {
            _lock.EnterWriteLock();
            try
            {
                _latestImage = null;
            }
            finally
            {
                _lock.ExitWriteLock();
            }
        }

        public bool HasImage()
        {
            _lock.EnterReadLock();
            try
            {
                return _latestImage != null && _latestImage.Length > 0;
            }
            finally
            {
                _lock.ExitReadLock();
            }
        }
    }
}
