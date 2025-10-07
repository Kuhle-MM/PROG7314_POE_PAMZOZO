using System.Collections.Generic;
using System.Net.Http;
using System.Text.Json;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Configuration;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using Moq;
using PROG7314_POE.Controllers;
using PROG7314_POE.Models;
using PROG7314_POE.Services;

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
            var mockHttp = new Mock<HttpClient>();
            var configMock = new Mock<IConfiguration>();
            configMock.Setup(c => c["GoogleTranslateApiKey"]).Returns("fake-api-key");

            _mockService = new Mock<TranslationService>(mockHttp.Object, configMock.Object)
            {
                CallBase = true
            };

            _controller = new TranslationController(_mockService.Object);
        }

        [TestMethod]
        public async Task Convert_ReturnsOk_WithValidRequest()
        {
            // Arrange
            var req = new TranslationRequest { Text = "Hello", From = "en", To = "af" };

            _mockService
                .Setup(s => s.TranslateAsync("Hello", "en", "af"))
                .ReturnsAsync((true, "Hallo", (string?)null));

            // Act
            var result = await _controller.Convert(req) as OkObjectResult;

            // Assert
            Assert.IsNotNull(result);

            // Serialize and inspect response
            var json = JsonSerializer.Serialize(result.Value);
            var data = JsonSerializer.Deserialize<Dictionary<string, object>>(json);

            Assert.AreEqual("Hello", data["original"].ToString());
            Assert.AreEqual("en", data["from"].ToString());
            Assert.AreEqual("af", data["to"].ToString());
            Assert.AreEqual("Hallo", data["translated"].ToString());
        }

        [TestMethod]
        public async Task Convert_ReturnsBadRequest_WhenTextIsEmpty()
        {
            // Arrange
            var req = new TranslationRequest { Text = "", From = "en", To = "af" };

            // Act
            var result = await _controller.Convert(req) as BadRequestObjectResult;

            // Assert
            Assert.IsNotNull(result);

            var json = JsonSerializer.Serialize(result.Value);
            var data = JsonSerializer.Deserialize<Dictionary<string, object>>(json);

            Assert.AreEqual("Text is required.", data["error"].ToString());
        }

        [TestMethod]
        public async Task Convert_ReturnsBadRequest_WhenTargetLanguageNotAllowed()
        {
            // Arrange
            var req = new TranslationRequest { Text = "Hello", From = "en", To = "xx" };

            _mockService.Setup(s => s.IsLanguageAllowed("xx")).Returns(false);

            // Act
            var result = await _controller.Convert(req) as BadRequestObjectResult;

            // Assert
            Assert.IsNotNull(result);

            var json = JsonSerializer.Serialize(result.Value);
            var data = JsonSerializer.Deserialize<Dictionary<string, object>>(json);

            Assert.AreEqual("Target language not supported. Allowed: en, af, xh, zu", data["error"].ToString());
        }
    }
}
