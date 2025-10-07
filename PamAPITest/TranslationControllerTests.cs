using Microsoft.VisualStudio.TestTools.UnitTesting;
using Moq;
using PROG7314_POE.Controllers;
using PROG7314_POE.Models;
using PROG7314_POE.Services;
using Microsoft.AspNetCore.Mvc;
using System.Threading.Tasks;

namespace PROG7314_POE.Tests
{
    [TestClass]
    public class TranslationControllerTests
    {
        private Mock<TranslationService> _mockService;
        private TranslationController _controller;

        [TestInitialize]
        public void Setup()
        {
            _mockService = new Mock<TranslationService>(null, null); // nulls are fine since we’re mocking all calls
            _controller = new TranslationController(_mockService.Object);
        }

        [TestMethod]
        public async Task Convert_ReturnsOk_WithValidRequest()
        {
            // Arrange
            var request = new TranslationRequest
            {
                Text = "Hello",
                From = "en",
                To = "af"
            };

            _mockService.Setup(s => s.IsLanguageAllowed("af")).Returns(true);
            _mockService
                .Setup(s => s.TranslateAsync("Hello", "en", "af"))
                .ReturnsAsync((true, "Hallo", null));

            // Act
            var result = await _controller.Convert(request);

            // Assert
            Assert.IsInstanceOfType(result, typeof(OkObjectResult));
            var okResult = (OkObjectResult)result;

            dynamic data = okResult.Value;
            Assert.AreEqual("Hello", data.original);
            Assert.AreEqual("en", data.detected);
            Assert.AreEqual("Hallo", data.translated);
        }

        [TestMethod]
        public async Task Convert_ReturnsBadRequest_WhenTextIsEmpty()
        {
            // Arrange
            var request = new TranslationRequest { Text = "" };

            // Act
            var result = await _controller.Convert(request);

            // Assert
            Assert.IsInstanceOfType(result, typeof(BadRequestObjectResult));
            var badResult = (BadRequestObjectResult)result;

            dynamic data = badResult.Value;
            Assert.AreEqual("Text is required.", data.error);
        }

        [TestMethod]
        public async Task Convert_ReturnsBadRequest_WhenTargetLanguageNotAllowed()
        {
            // Arrange
            var request = new TranslationRequest { Text = "Hello", To = "jp" };
            _mockService.Setup(s => s.IsLanguageAllowed("jp")).Returns(false);

            // Act
            var result = await _controller.Convert(request);

            // Assert
            Assert.IsInstanceOfType(result, typeof(BadRequestObjectResult));
            var badResult = (BadRequestObjectResult)result;

            dynamic data = badResult.Value;
            Assert.AreEqual("Target language not supported. Allowed: en, af, xh, zu", data.error);
        }
    }
}
