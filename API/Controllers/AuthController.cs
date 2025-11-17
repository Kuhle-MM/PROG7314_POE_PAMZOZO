using System;
using System.Collections.Generic;
using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Configuration;
using Microsoft.IdentityModel.Tokens;

namespace PROG7314_POE.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class AuthController : ControllerBase
    {
        private readonly IConfiguration _config;
        public AuthController(IConfiguration config)
        {
            _config = config;
        }

        // Device token endpoint (client credentials style)
        // POST /api/auth/device-token { "client_id":"my-pi-001", "client_secret":"..." }
        [HttpPost("device-token")]
        public IActionResult DeviceToken([FromBody] DeviceTokenRequest req)
        {
            var deviceDict = _config.GetSection("DeviceCredentials").Get<Dictionary<string, string>>() ?? new();
            if (!deviceDict.TryGetValue(req.ClientId, out var expectedSecret))
                return Unauthorized("Unknown client_id");

            if (req.ClientSecret != expectedSecret)
                return Unauthorized("Invalid client_secret");

            var token = CreateToken(req.ClientId, "device");
            return Ok(new { access_token = token, token_type = "Bearer" });
        }

        // Simple user login (replace with Identity)
        // POST /api/auth/login { "username":"user1","password":"pass" }
        [HttpPost("login")]
        public IActionResult Login([FromBody] LoginRequest req)
        {
            // TODO: Replace this with proper user validation or Identity
            if (req.Username == "testuser" && req.Password == "password")
            {
                var token = CreateToken(req.Username, "user");
                return Ok(new { access_token = token, token_type = "Bearer" });
            }
            return Unauthorized("Invalid credentials");
        }

        private string CreateToken(string name, string role)
        {
            var jwtConfig = _config.GetSection("Jwt");
            var key = jwtConfig.GetValue<string>("Key");
            var issuer = jwtConfig.GetValue<string>("Issuer");
            var audience = jwtConfig.GetValue<string>("Audience");
            var lifetime = jwtConfig.GetValue<int>("TokenLifetimeMinutes");

            var claims = new List<Claim>
            {
                new Claim(JwtRegisteredClaimNames.Sub, name),
                new Claim("name", name),
                new Claim("role", role)
            };

            var securityKey = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(key));
            var cred = new SigningCredentials(securityKey, SecurityAlgorithms.HmacSha256);
            var now = DateTime.UtcNow;
            var token = new JwtSecurityToken(
                issuer,
                audience,
                claims,
                notBefore: now,
                expires: now.AddMinutes(lifetime),
                signingCredentials: cred
            );

            return new JwtSecurityTokenHandler().WriteToken(token);
        }

        public class DeviceTokenRequest
        {
            public string ClientId { get; set; }
            public string ClientSecret { get; set; }
        }

        public class LoginRequest
        {
            public string Username { get; set; }
            public string Password { get; set; }
        }
    }
}
