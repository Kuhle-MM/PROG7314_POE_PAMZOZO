using Microsoft.AspNetCore.Mvc;
using Microsoft.IdentityModel.Tokens;
using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;

namespace PROG7314_POE.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class TokenController : ControllerBase
    {
        [HttpGet]
        public IActionResult GetToken()
        {
            var claims = new[]
            {
            new Claim(JwtRegisteredClaimNames.Sub, "RaspberryPi"),
            new Claim(JwtRegisteredClaimNames.Jti, Guid.NewGuid().ToString())
        };

            var key = new SymmetricSecurityKey(Encoding.UTF8.GetBytes("this_is_a_super_secret_key_with_32bytes!"));
            var creds = new SigningCredentials(key, SecurityAlgorithms.HmacSha256);

            var token = new JwtSecurityToken(
               issuer: "MyApi",
               audience: "MyDevices",
               claims: new[] { new Claim(JwtRegisteredClaimNames.Sub, "RaspberryPi") },
               expires: DateTime.Now.AddYears(1), 
               signingCredentials: creds
            );   

            return Ok(new { token = new JwtSecurityTokenHandler().WriteToken(token) });
        }
    }
}
