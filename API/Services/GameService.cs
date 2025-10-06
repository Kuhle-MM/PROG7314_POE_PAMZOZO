using PROG7314_POE.Models;
using PROG7314_POE.Repository;
using System.Collections.Concurrent;

namespace PROG7314_POE.Services
{
    public class GameService : IGameService
    {
        private readonly InMemoryRepository _repo;
        private readonly ConcurrentDictionary<string, GameSession> _sessions = new();
        private readonly Random _rng = new();

        public GameService(InMemoryRepository repo)
        {
            _repo = repo;
        }

        public IEnumerable<GameItem> GetItemsByCategory(string category)
        {
            return _repo.GetItems(category);
        }

        public IEnumerable<string> GetCategories() => _repo.GetCategories();

        public GameSession StartSession(string category, int roundSeconds = 60)
        {
            var items = _repo.GetItems(category).ToList();
            if (!items.Any()) throw new ArgumentException($"No items in category '{category}'");


            items = items.OrderBy(_ => _rng.Next()).ToList();


            var session = new GameSession
            {
                Category = category,
                Items = items,
                CurrentIndex = 0,
                IsActive = true,
                RoundSeconds = roundSeconds
            };


            session.RoundCancellation = new CancellationTokenSource();
            session.RoundEndsAt = DateTimeOffset.UtcNow.AddSeconds(roundSeconds);


            _ = Task.Run(async () =>
            {
                try
                {
                    await Task.Delay(TimeSpan.FromSeconds(roundSeconds), session.RoundCancellation.Token);
                }
                catch (TaskCanceledException) { return; }


                session.CurrentIndex++;
                if (session.CurrentIndex >= session.Items.Count)
                {
                    session.IsActive = false;
                    session.RoundEndsAt = null;
                }
                else
                {
                    session.RoundCancellation = new CancellationTokenSource();
                    session.RoundEndsAt = DateTimeOffset.UtcNow.AddSeconds(session.RoundSeconds);
                    _ = Task.Run(async () =>
                    {
                        try
                        {
                            await Task.Delay(TimeSpan.FromSeconds(session.RoundSeconds), session.RoundCancellation.Token);
                        }
                        catch { }
                    });
                }
            });


            _sessions[session.Id] = session;
            return session;
        }

        public GameSession? GetSession(string sessionId) => _sessions.TryGetValue(sessionId, out var s) ? s : null;


        public GameItem? NextItem(string sessionId)
        {
            if (!_sessions.TryGetValue(sessionId, out var session)) return null;
            if (!session.IsActive) return null;
            session.RoundCancellation?.Cancel();
            session.CurrentIndex++;
            if (session.CurrentIndex >= session.Items.Count)
            {
                session.IsActive = false;
                session.RoundEndsAt = null;
                return null;
            }


            session.RoundCancellation = new CancellationTokenSource();
            session.RoundEndsAt = DateTimeOffset.UtcNow.AddSeconds(session.RoundSeconds);
            _ = Task.Run(async () =>
            {
                try { await Task.Delay(TimeSpan.FromSeconds(session.RoundSeconds), session.RoundCancellation.Token); }
                catch { }
                session.CurrentIndex++;
                if (session.CurrentIndex >= session.Items.Count)
                {
                    session.IsActive = false;
                    session.RoundEndsAt = null;
                }
            });


            return session.CurrentItem;
        }

        public bool SubmitGuess(string sessionId, string player, string guess, out bool correct)
        {
            correct = false;
            if (!_sessions.TryGetValue(sessionId, out var session)) return false;
            if (!session.IsActive || session.CurrentItem == null) return false;
            var target = session.CurrentItem.Text.Trim();
            if (string.Equals(target, guess.Trim(), StringComparison.OrdinalIgnoreCase) ||
            target.IndexOf(guess.Trim(), StringComparison.OrdinalIgnoreCase) >= 0 ||
            guess.Trim().IndexOf(target, StringComparison.OrdinalIgnoreCase) >= 0)
            {
                correct = true;
                session.Scores.AddOrUpdate(player, 1, (_, old) => old + 1);
                session.RoundCancellation?.Cancel();
                session.CurrentIndex++;
                if (session.CurrentIndex >= session.Items.Count)
                {
                    session.IsActive = false;
                    session.RoundEndsAt = null;
                }
                else
                {
                    session.RoundCancellation = new CancellationTokenSource();
                    session.RoundEndsAt = DateTimeOffset.UtcNow.AddSeconds(session.RoundSeconds);
                    _ = Task.Run(async () =>
                    {
                        try { await Task.Delay(TimeSpan.FromSeconds(session.RoundSeconds), session.RoundCancellation.Token); } catch { }
                        session.CurrentIndex++;
                        if (session.CurrentIndex >= session.Items.Count)
                        {
                            session.IsActive = false;
                            session.RoundEndsAt = null;
                        }
                    });
                }


                return true;
            }


            return true;
        }

        public bool Skip(string sessionId, out GameItem? next)
        {
            next = null;
            if (!_sessions.TryGetValue(sessionId, out var session)) return false;
            if (!session.IsActive) return false;


            session.RoundCancellation?.Cancel();
            session.CurrentIndex++;
            if (session.CurrentIndex >= session.Items.Count)
            {
                session.IsActive = false;
                session.RoundEndsAt = null;
                next = null;
                return true;
            }


            session.RoundCancellation = new CancellationTokenSource();
            session.RoundEndsAt = DateTimeOffset.UtcNow.AddSeconds(session.RoundSeconds);
            next = session.CurrentItem;
            _ = Task.Run(async () =>
            {
                try { await Task.Delay(TimeSpan.FromSeconds(session.RoundSeconds), session.RoundCancellation.Token); } catch { }
                session.CurrentIndex++;
                if (session.CurrentIndex >= session.Items.Count)
                {
                    session.IsActive = false;
                    session.RoundEndsAt = null;
                }
            });


            return true;
        }


        public void AddItem(GameItem item) => _repo.AddItem(item);
    }


}
