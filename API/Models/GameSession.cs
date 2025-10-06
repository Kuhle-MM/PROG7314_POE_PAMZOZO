using System.Collections.Concurrent;

namespace PROG7314_POE.Models
{
    public class GameSession
    {
        public string Id { get; set; } = Guid.NewGuid().ToString();
        public string Category { get; set; } = string.Empty;
        public List<GameItem> Items { get; set; } = new();
        public int CurrentIndex { get; set; } = -1; // -1 = not started
        public ConcurrentDictionary<string, int> Scores { get; set; } = new();
        public bool IsActive { get; set; } = false;
        public int RoundSeconds { get; set; } = 60;
        public CancellationTokenSource? RoundCancellation { get; set; }
        public DateTimeOffset? RoundEndsAt { get; set; }


        public GameItem? CurrentItem => (CurrentIndex >= 0 && CurrentIndex < Items.Count) ? Items[CurrentIndex] : null;
    }
}
