namespace PROG7314_POE.Models
{
    public class Camera
    {
        //sending to the raspberry pi
        public double pan {  get; set; } //this is the x axis of the camera servo
        public double tilt { get; set; } //this is the y axis of the camera servo
        public double panSpeed { get; set; }
        public double tiltSpeed { get; set; }

    }
}
