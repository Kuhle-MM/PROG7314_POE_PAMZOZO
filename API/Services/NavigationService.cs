using PROG7314_POE.Models;

namespace PROG7314_POE.Services
{
    public class NavigationService
    {
        private readonly Dictionary<string, List<Location>> _maps = new();

        public void SetMapping(string robotId, List<Location> coordinates)
        {
            _maps[robotId] = coordinates;
        }

        public List<Location> GetMapping(string robotId)
        {
            if (_maps.TryGetValue(robotId, out var coordinates))
            {
                return coordinates;
            }

            return new List<Location>();
        }

        public void UpdateLocation(string robotId, Location location)
        {
            if (!_maps.ContainsKey(robotId))
                _maps[robotId] = new List<Location>();

            _maps[robotId].Add(location);
        }
    }
}
