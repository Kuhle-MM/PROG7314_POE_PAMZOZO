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
            AddCategory("actors", new[]
            {
    // Original
    "Meryl Streep", "Denzel Washington", "Scarlett Johansson", "Tom Hanks", "Leonardo DiCaprio",

    // Hollywood Legends
    "Morgan Freeman", "Robert De Niro", "Al Pacino", "Brad Pitt", "Johnny Depp",
    "Will Smith", "Angelina Jolie", "Natalie Portman", "Julia Roberts", "Matt Damon",
    "George Clooney", "Samuel L. Jackson", "Harrison Ford", "Keanu Reeves", "Christian Bale",
    "Hugh Jackman", "Anne Hathaway", "Emma Stone", "Ryan Gosling", "Jennifer Lawrence",
    "Nicole Kidman", "Cate Blanchett", "Reese Witherspoon", "Charlize Theron", "Sandra Bullock",
    "Ben Affleck", "Mark Wahlberg", "Jake Gyllenhaal", "Tom Cruise", "Chris Hemsworth",
    "Chris Evans", "Robert Downey Jr.", "Jeremy Renner", "Zoe Saldana", "Brie Larson",

    // British Actors
    "Idris Elba", "Daniel Craig", "Benedict Cumberbatch", "Tom Hardy", "Emily Blunt",
    "Kate Winslet", "Michael Caine", "Tilda Swinton", "Helena Bonham Carter", "Gary Oldman",
    "Judi Dench", "Emma Watson", "Maggie Smith", "Colin Firth", "Keira Knightley",

    // South African 🇿🇦 Actors
    "Sharlto Copley", "Leleti Khumalo", "John Kani", "Atandwa Kani", "Terry Pheto",
    "Pearl Thusi", "Sello Maake Ka-Ncube", "Presley Chweneyagae", "Connie Ferguson", "Rapulana Seiphemo",
    "Moshidi Motshegwa", "Warren Masemola", "Thuso Mbedu", "Bonnie Mbuli", "Masasa Mbangeni",
    "Vusi Kunene", "Mapaseka Koetle", "Hlomla Dandala", "Nomzamo Mbatha", "Nandi Madida",

    // Bollywood Actors (for global flavor)
    "Shah Rukh Khan", "Priyanka Chopra", "Deepika Padukone", "Ranveer Singh", "Amitabh Bachchan",
    "Hrithik Roshan", "Kareena Kapoor", "Aishwarya Rai", "Salman Khan", "Irrfan Khan",

    // Contemporary & Diverse
    "Timothée Chalamet", "Florence Pugh", "Zendaya", "John Boyega", "Lupita Nyong’o",
    "Daniel Kaluuya", "Gal Gadot", "Jason Momoa", "Pedro Pascal", "Millie Bobby Brown"
});

            AddCategory("movies", new[]
            {
    "The Shawshank Redemption", "Inception", "Spirited Away", "The Matrix", "Casablanca",
    "Avatar", "The Godfather", "Titanic", "Star Wars: Episode V – The Empire Strikes Back",
    "Harry Potter and the Goblet of Fire", "Parasite", "Jurassic Park", "Logan", "Black Panther",
    "Toy Story 3", "The Revenant", "The Dark Knight", "Moana", "Frozen", "Joker", "Interstellar",
    "Dune", "Coco", "Life Is Beautiful", "The Lord of the Rings: The Return of the King",
    "The Godfather: Part II", "Up", "District 9", "Finding Nemo", "The Grand Budapest Hotel",
    "The Lion King", "Deadpool", "The Pianist", "Zootopia", "Gladiator", "Oldboy",
    "The Hunger Games", "Avatar: The Way of Water", "Harry Potter and the Half-Blood Prince",
    "Shutter Island", "Your Name", "Slumdog Millionaire", "Mockingjay Part 2", "Mandela: Long Walk to Freedom",
    "Harry Potter and the Prisoner of Azkaban", "Crouching Tiger, Hidden Dragon", "Frozen II",
    "Pan's Labyrinth", "Toy Story", "Finding Dory", "Life of Pi", "Howl's Moving Castle",
    "The Social Network", "Harry Potter and the Sorcerer's Stone", "Inglourious Basterds",
    "Five Fingers for Marseilles", "Gravity", "Whiplash", "Shrek", "The Wolf of Wall Street",
    "Princess Mononoke", "Harry Potter and the Order of the Phoenix", "Mandela: Long Walk to Freedom",
    "Django Unchained", "Mockingjay Part 1", "The Lord of the Rings: The Two Towers",
    "The Lord of the Rings: The Fellowship of the Ring", "Avatar", "Kalushi: The Story of Solomon Mahlangu",
    "Of Good Report", "The Gods Must Be Crazy", "The King's Speech", "Amélie", "Titanic",
    "Star Wars: Episode IV – A New Hope", "Star Wars: Episode VI – Return of the Jedi",
    "Shutter Island", "The Hateful Eight", "The Matrix", "Mad Max: Fury Road", "Spirited Away",
    "Your Name", "Weathering with You", "Catch a Fire", "Sarafina!", "White Lion", "Yesterday",
    "The River", "Coco", "Frozen", "Moana", "Inside Out", "Ratatouille"
});

            AddCategory("songs", new[]
            {
                "Jerusalema - Master KG", "African Queen - 2Baba", "Nomvula - Brenda Fassie", "Pata Pata - Miriam Makeba",
    "Asimbonanga - Johnny Clegg", "Homeless - Ladysmith Black Mambazo", "Ndihamba Nawe - Mafikizolo",
    "Umqombothi - Yvonne Chaka Chaka", "Greatest Love of All - Lira", "Spirit - Kwesta",
    "Ngiyaz'fela Ngawe - Sjava", "Soweto Blues - Hugh Masekela", "Rollercoaster - Amu", "Shake - DJ Cleo",
    "Bohemian Rhapsody", "Imagine", "Billie Jean", "Shape of You", "Thriller",
    "Hotel California", "Sweet Child O' Mine", "Stairway to Heaven", "Smells Like Teen Spirit",
    "Hey Jude", "Rolling in the Deep", "Someone Like You", "Uptown Funk", "Happy", "Shake It Off",
    "Blinding Lights", "Bad Guy", "Levitating", "Shallow", "Rolling Stone", "Poker Face",
    "Firework", "Royals", "All of Me", "Can't Stop the Feeling!", "Old Town Road",
    "Havana", "Senorita", "Thinking Out Loud", "Sorry", "Wake Me Up", "Counting Stars",
    "Call Me Maybe", "Roar", "Girls Like You", "Believer", "Faded", "Love Yourself",
    "Memories", "Perfect", "Watermelon Sugar", "Driver's License", "As It Was",
    "Stay", "Peaches", "Good 4 U", "Industry Baby", "Bad Romance", "Rolling in the Deep",
    "Hello", "Rehab", "Umbrella", "Toxic", "Since U Been Gone", "I Will Always Love You",
    "Creep", "Wonderwall", "Fix You", "Clocks", "Yellow", "Viva La Vida", "Every Breath You Take",
    "Livin' on a Prayer", "Eye of the Tiger", "We Will Rock You", "Another One Bites the Dust",
    "Radioactive", "Counting Stars", "Take Me to Church", "Shape of You", "Bad Guy", "Bohemian Like You",    
});

            AddCategory("food", new[]
            {
    "Sushi", "Spaghetti Carbonara", "Tacos", "Cheeseburger", "Pad Thai",
    "Pizza", "Ramen", "Burrito", "Lasagna", "Chicken Tikka Masala",
    "Falafel", "Dim Sum", "Fish and Chips", "Paella", "Ceviche",
    "Pho", "Gnocchi", "Moussaka", "Pancakes", "Waffles",
    "Croissant", "Baguette", "Hamburger", "Hot Dog", "Mac and Cheese",
    "Nachos", "Quesadilla", "Fajitas", "Empanadas", "Jollof Rice",
    "Bobotie", "Bunny Chow", "Pap and Wors", "Vetkoek", "Samosa",
    "Chow Mein", "Spring Rolls", "Dumplings", "Currywurst", "Bulgogi",
    "Kimchi", "Tempura", "Miso Soup", "Ratatouille", "Shakshuka",
    "Tiramisu", "Baklava", "Gelato", "Ice Cream", "Cheesecake",
    "Brownies", "Donuts", "Cupcakes", "Pavlova", "Banana Bread",
    "Hummus", "Guacamole", "Bruschetta", "Sashimi", "Kebabs",
    "Steak", "Lamb Chops", "Roast Chicken", "Grilled Salmon", "Seafood Paella",
    "Vegetable Stir Fry", "Stuffed Peppers", "Falooda", "Churros", "Poutine",
    "Clam Chowder", "Borscht", "Gumbo", "Fettuccine Alfredo", "Carbonara"
});

            AddCategory("people you know", new[]
            {
    "Your neighbour", "Your teacher", "Your best friend", "A co-worker", "Your cousin",
    "Your sibling", "Your parent", "Your grandparent", "Your classmate", "Your coach",
    "Your boss", "Your dentist", "Your barber", "Your delivery person", "Your mail carrier",
    "Your librarian", "Your favourite uncle", "Your favourite aunt", "Your cousin's friend", "Your neighbour's pet",
    "Your study partner", "Your gym buddy", "Your teammate", "Your mentor", "Your mentee",
    "Your partner", "Your spouse", "Your child", "Your niece", "Your nephew",
    "Your friend from school", "Your friend from work", "Your friend's sibling", "Your neighbour's child", "Your cousin's sibling",
    "Your roommate", "Your landlord", "Your chef", "Your favourite cousin", "Your favourite neighbour",
    "Your favourite teacher", "Your favourite colleague", "Your childhood friend", "Your childhood neighbour", "Your neighbour's parent",
    "Your favourite babysitter", "Your favourite cousin's friend", "Your friend's parent", "Your friend's teacher", "Your best friend's sibling",
    "Your co-worker's spouse", "Your co-worker's child", "Your uncle's friend", "Your aunt's friend", "Your cousin's child",
    "Your mentor's friend", "Your mentee's parent", "Your classmate's sibling", "Your neighbour's grandparent", "Your friend's cousin",
    "Your friend's colleague", "Your sibling's friend", "Your parent's friend", "Your grandparent's friend", "Your neighbour's sibling",
    "Your friend from camp", "Your friend from sports club", "Your friend from music class", "Your neighbour's cousin", "Your cousin's neighbour",
    "Your teammate's parent", "Your teammate's sibling", "Your study partner's friend", "Your mentor's colleague", "Your mentee's sibling"
});

            AddCategory("animals", new[]
            {
    "Elephant", "Kangaroo", "Dolphin", "Penguin", "Giraffe",
    "Lion", "Tiger", "Cheetah", "Leopard", "Zebra",
    "Rhinoceros", "Hippopotamus", "Crocodile", "Alligator", "Polar Bear",
    "Brown Bear", "Wolf", "Fox", "Rabbit", "Deer",
    "Moose", "Squirrel", "Koala", "Panda", "Sloth",
    "Otter", "Beaver", "Owl", "Eagle", "Hawk",
    "Falcon", "Parrot", "Crow", "Swan", "Duck",
    "Goose", "Chicken", "Turkey", "Sheep", "Goat",
    "Cow", "Pig", "Dog", "Cat", "Horse",
    "Camel", "Donkey", "Elephant Seal", "Walrus", "Seal",
    "Shark", "Whale", "Octopus", "Squid", "Jellyfish",
    "Starfish", "Sea Turtle", "Frog", "Toad", "Lizard",
    "Chameleon", "Snake", "Iguana", "Tarantula", "Scorpion",
    "Antelope", "Warthog", "Meerkat", "Springbok", "Honey Badger",
    "Cheetah", "Bushbaby", "Kudu", "Nyala", "Genet"
});

            AddCategory("anime", new[]
            {
    "Naruto", "Spirited Away", "One Piece", "Attack on Titan", "My Hero Academia",
    "Dragon Ball Z", "Death Note", "Fullmetal Alchemist", "One Punch Man", "Demon Slayer",
    "Jujutsu Kaisen", "Tokyo Ghoul", "Sword Art Online", "Bleach", "Hunter x Hunter",
    "Fairy Tail", "Black Clover", "Haikyuu!!", "Code Geass", "Neon Genesis Evangelion",
    "Steins;Gate", "Your Name", "Weathering With You", "Inuyasha", "Sailor Moon",
    "Pokémon", "Digimon", "Akira", "Ghost in the Shell", "Cowboy Bebop",
    "Samurai Champloo", "Kill la Kill", "Parasyte", "Mob Psycho 100", "The Seven Deadly Sins",
    "Fate/Stay Night", "Fate/Zero", "Blue Exorcist", "Toradora!", "Clannad",
    "Angel Beats!", "Vivy: Fluorite Eye’s Song", "Chainsaw Man", "Vivy", "The Rising of the Shield Hero",
    "Made in Abyss", "Black Butler", "Noragami", "Psycho-Pass", "Great Teacher Onizuka",
    "Boku no Pico", "KonoSuba", "No Game No Life", "Overlord", "Re:Zero",
    "Katekyo Hitman Reborn!", "Gintama", "Haibane Renmei", "Erased", "Angel Sanctuary",
    "Bakuman", "Beelzebub", "C: The Money of Soul and Possibility Control", "Durarara!!", "Elfen Lied",
    "Future Diary", "Guilty Crown", "Hellsing", "Highschool DxD", "Inuyashiki",
    "Kimi no Suizou wo Tabetai", "Little Witch Academia", "Magi", "Noragami Aragoto", "Owari no Seraph"
});

            AddCategory("sports", new[]
            {
    "Soccer", "Basketball", "Tennis", "Cricket", "Swimming",
    "Rugby", "Golf", "Baseball", "Volleyball", "Table Tennis",
    "Badminton", "Hockey", "Ice Hockey", "American Football", "Boxing",
    "MMA", "Wrestling", "Cycling", "Skiing", "Snowboarding",
    "Skateboarding", "Surfing", "Karate", "Judo", "Taekwondo",
    "Gymnastics", "Diving", "Archery", "Shooting", "Fencing",
    "Weightlifting", "Track and Field", "Marathon", "Triathlon", "Horse Racing",
    "Equestrian", "Bowling", "Curling", "Sailing", "Rowing",
    "Lacrosse", "Softball", "Handball", "Bobsleigh", "Skeleton",
    "Speed Skating", "Figure Skating", "Racing", "Motocross", "Rally",
    "Snooker", "Pool", "Chess", "eSports", "Yoga",
    "CrossFit", "Parkour", "Ultimate Frisbee", "Kickboxing", "Cricket T20",
    "Netball", "Beach Volleyball", "Water Polo", "Surf Lifesaving", "Freediving",
    "Skydiving", "Paragliding", "Mountain Biking", "Snowshoeing", "Rock Climbing"
});
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
