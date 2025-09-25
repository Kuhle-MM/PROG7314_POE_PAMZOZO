namespace PROG7314_POE.Models
{
    public class Camera
    {
        //sending to the raspberry pi
        public double pan {  get; set; } //this is the x axis of the camera servo; 0–180 degrees
        public double tilt { get; set; } //this is the y axis of the camera servo; 0–90 degrees

    }
}
