using PROG7314_POE.Models;

namespace PROG7314_POE.Repository
{
    public class InMemoryRepository
    {
        private readonly Dictionary<string, List<GameItem>> _byCategory = new(StringComparer.OrdinalIgnoreCase);


        public InMemoryRepository()
        {
            Seed();
        }


        public IEnumerable<string> GetCategories() => _byCategory.Keys;


        public IEnumerable<GameItem> GetItems(string category)
        {
            if (!_byCategory.TryGetValue(category, out var list)) return Enumerable.Empty<GameItem>();
            return list;
        }

        public void AddItem(GameItem item)
        {
            if (!_byCategory.ContainsKey(item.Category)) _byCategory[item.Category] = new List<GameItem>();
            _byCategory[item.Category].Add(item);
        }


        public GameItem? GetRandomItem(string category, Random? rng = null)
        {
            rng ??= new Random();
            if (!_byCategory.TryGetValue(category, out var list) || list.Count == 0) return null;
            return list[rng.Next(list.Count)];
        }


        private void Seed()
        {
            // Categories: actors, movies, songs, food, people you know, animals, anime, sports
            AddCategory("actors", new[] { "Meryl Streep", "Denzel Washington", "Scarlett Johansson", "Tom Hanks", "Leonardo DiCaprio" });
            AddCategory("movies", new[] { "The Shawshank Redemption", "Inception", "Spirited Away", "The Matrix", "Casablanca" });
            AddCategory("songs", new[] { "Bohemian Rhapsody", "Imagine", "Billie Jean", "Shape of You", "Thriller" });
            AddCategory("food", new[] { "Sushi", "Spaghetti Carbonara", "Tacos", "Cheeseburger", "Pad Thai" });
            AddCategory("people you know", new[] { "Your neighbour", "Your teacher", "Your best friend", "A co-worker", "Your cousin" });
            AddCategory("animals", new[] { "Elephant", "Kangaroo", "Dolphin", "Penguin", "Giraffe" });
            AddCategory("anime", new[] { "Naruto", "Spirited Away", "One Piece", "Attack on Titan", "My Hero Academia" });
            AddCategory("sports", new[] { "Soccer", "Basketball", "Tennis", "Cricket", "Swimming" });
        }

        private void AddCategory(string cat, IEnumerable<string> items)
        {
            foreach (var text in items)
            {
                AddItem(new GameItem { Category = cat, Text = text });
            }
        }
    }
}
