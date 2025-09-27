namespace PROG7314_POE.Models
{
    public class Motor
    {
        //sending to the raspberry pi
        public double x { get; set; } //this is the x axis of the motor
        public double y { get; set; } //this is the y axis of the motor
        public int leftMotorSpeed { get; set; } //0-100%
        public int rightMotorSpeed { get; set; } //0-100%

    }
}
