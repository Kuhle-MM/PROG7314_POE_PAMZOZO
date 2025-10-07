using Microsoft.VisualStudio.TestTools.UnitTesting;
using PROG7314_POE.Services;
using PROG7314_POE.Repository;
using PROG7314_POE.Models;
using System.Linq;

namespace Charades.Tests
{
    [TestClass]
    public class GameServiceTests
    {
        private GameService _gameService;
        private InMemoryRepository _repo;

        [TestInitialize]
        public void Setup()
        {
            _repo = new InMemoryRepository();
            _repo.AddItem(new GameItem { Category = "movies", Text = "Inception" });
            _repo.AddItem(new GameItem { Category = "movies", Text = "Titanic" });
            _repo.AddItem(new GameItem { Category = "actors", Text = "Tom Hanks" });
            _repo.AddItem(new GameItem { Category = "food", Text = "Pizza" });

            _gameService = new GameService(_repo);
        }

        [TestMethod]
        public void GetCategories_ShouldReturnCategories()
        {
            var categories = _gameService.GetCategories();

            Assert.IsNotNull(categories);
            Assert.IsTrue(categories.Contains("movies"));
            Assert.IsTrue(categories.Contains("actors"));
        }

        [TestMethod]
        public void StartSession_ShouldCreateNewSession()
        {
            var session = _gameService.StartSession("movies", 10);

            Assert.IsNotNull(session);
            Assert.AreEqual("movies", session.Category);
            Assert.IsTrue(session.IsActive);
            Assert.IsNotNull(session.Items);
            Assert.IsTrue(session.Items.Count > 0);
        }

        [TestMethod]
        [ExpectedException(typeof(System.ArgumentException))]
        public void StartSession_WithInvalidCategory_ShouldThrow()
        {
            _gameService.StartSession("unknownCategory");
        }

        [TestMethod]
        public void GetSession_ShouldReturnExistingSession()
        {
            var session = _gameService.StartSession("movies");
            var result = _gameService.GetSession(session.Id);

            Assert.IsNotNull(result);
            Assert.AreEqual(session.Id, result.Id);
        }

        [TestMethod]
        public void NextItem_ShouldReturnNextGameItem()
        {
            var session = _gameService.StartSession("movies");
            var first = session.CurrentItem;
            var next = _gameService.NextItem(session.Id);

            Assert.IsNotNull(next);
            Assert.AreNotEqual(first?.Text, next?.Text);
        }

        [TestMethod]
        public void SubmitGuess_CorrectGuess_ShouldReturnTrue()
        {
            var session = _gameService.StartSession("movies");
            var current = session.CurrentItem;
            var result = _gameService.SubmitGuess(session.Id, "Player1", current.Text, out var correct);

            Assert.IsTrue(result);
            Assert.IsTrue(correct);
        }

        [TestMethod]
        public void SubmitGuess_IncorrectGuess_ShouldReturnTrueButNotCorrect()
        {
            var session = _gameService.StartSession("movies");
            var result = _gameService.SubmitGuess(session.Id, "Player1", "WrongGuess", out var correct);

            Assert.IsTrue(result);
            Assert.IsFalse(correct);
        }

        [TestMethod]
        public void Skip_ShouldAdvanceToNextItem()
        {
            var session = _gameService.StartSession("movies");
            var first = session.CurrentItem;
            var result = _gameService.Skip(session.Id, out var next);

            Assert.IsTrue(result);
            Assert.AreNotEqual(first?.Text, next?.Text);
        }

        [TestMethod]
        public void AddItem_ShouldAddToRepository()
        {
            var newItem = new GameItem { Category = "movies", Text = "Interstellar" };
            _gameService.AddItem(newItem);

            var items = _gameService.GetItemsByCategory("movies").ToList();
            Assert.IsTrue(items.Any(i => i.Text == "Interstellar"));
        }
    }
}
