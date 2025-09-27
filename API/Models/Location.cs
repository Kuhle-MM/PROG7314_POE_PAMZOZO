namespace PROG7314_POE.Models
{
    public class Location
    {
        public double X {  get; set; }
        public double Y { get; set; }
    }

    public class Mapping
    {
        public string RobotId { get; set; }
        public List<Location> Coordinates { get; set; }
    }
}
