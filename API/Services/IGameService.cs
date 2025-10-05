using PROG7314_POE.Models;

namespace PROG7314_POE.Services
{
    public interface IGameService
    {
        IEnumerable<string> GetCategories();
        GameSession StartSession(string category, int roundSeconds = 60);
        GameSession? GetSession(string sessionId);
        GameItem? NextItem(string sessionId);
        bool SubmitGuess(string sessionId, string player, string guess, out bool correct);
        bool Skip(string sessionId, out GameItem? next);
        void AddItem(GameItem item);
    }
}
