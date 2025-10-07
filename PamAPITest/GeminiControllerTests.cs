using Microsoft.VisualStudio.TestTools.UnitTesting;
using Moq;
using PROG7314_POE.Controllers;
using PROG7314_POE.Services;
using Microsoft.AspNetCore.Mvc;
using System.Threading.Tasks;
using PROG7314_POE.Models;

namespace PROG7314_POE.Tests
{
    [TestClass]
    public class GeminiControllerTests
    {
        private Mock<IGeminiService> _mockService;
        private GeminiController _controller;

        [TestInitialize]
        public void Setup()
        {
            _mockService = new Mock<IGeminiService>();
            _controller = new GeminiController(_mockService.Object);
        }

        [TestMethod]
        public async Task AskGemini_ReturnsOk_WithValidQuestion()
        {
            // Arrange
            var question = "Hello";
            var expectedAnswer = "Hi there!";
            _mockService.Setup(s => s.AskGeminiAsync(question))
                        .ReturnsAsync(expectedAnswer);

            var query = new GeminiController.GeminiQuery { Question = question };

            // Act
            var result = await _controller.AskGemini(query);

            // Assert
            var okResult = result as OkObjectResult;
            Assert.IsNotNull(okResult);

            // Cast to the response type
            var responseData = okResult.Value as GeminiAnswerResponse;
            Assert.IsNotNull(responseData);
            Assert.AreEqual(expectedAnswer, responseData.answer);
        }


        [TestMethod]
        public async Task AskGemini_ReturnsBadRequest_WhenQuestionIsEmpty()
        {
            // Arrange
            var query = new GeminiController.GeminiQuery { Question = "" };

            // Act
            var result = await _controller.AskGemini(query);

            // Assert
            Assert.IsInstanceOfType(result, typeof(BadRequestObjectResult));
        }
    }
}
