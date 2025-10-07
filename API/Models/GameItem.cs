namespace PROG7314_POE.Models
{
    public class GameItem
    {
        public string Id { get; set; } = Guid.NewGuid().ToString();
        public string Category { get; set; } = string.Empty; 
        public string Text { get; set; } = string.Empty; 
    }
}
