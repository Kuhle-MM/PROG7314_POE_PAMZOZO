namespace PROG7314_POE.Models
{
    public class Motor
    {
        //sending to the raspberry pi
        public double x { get; set; } //this is the x axis of the motor
        public double y { get; set; } //this is the y axis of the motor
        public double leftMotorSpeed { get; set; }
        public double rightMotorSpeed { get; set; }

    }
}
