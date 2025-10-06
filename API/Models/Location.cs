namespace PROG7314_POE.Models
{
    public class Location
    {
        public double X {  get; set; }
        public double Y { get; set; }
        public double Latitude { get; internal set; }
        public double Longitude { get; internal set; }
    }

    public class Mapping
    {
        public string RobotId { get; set; }
        public List<Location> Coordinates { get; set; }
    }
}
