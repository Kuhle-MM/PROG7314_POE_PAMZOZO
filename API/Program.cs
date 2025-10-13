using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.IdentityModel.Tokens;
using PROG7314_POE.Services;
using System.Text;

namespace PROG7314_POE
{
    public class Program
    {
        public static void Main(string[] args)
        {
            var builder = WebApplication.CreateBuilder(args);

            // Load configuration from appsettings.json
            var jwtSettings = builder.Configuration.GetSection("Jwt");
            var jwtKey = jwtSettings["Key"];
            var jwtIssuer = jwtSettings["Issuer"];
            var jwtAudience = jwtSettings["Audience"];

            builder.WebHost.ConfigureKestrel(options =>
            {
                options.ListenAnyIP(7298); // Listen on all LAN interfaces
            });

            // Services
            builder.Services.AddHttpClient<GeminiService>();
            builder.Services.AddScoped<GeminiService>();
            builder.Services.AddSingleton<NavigationService>();
            builder.Services.AddHttpClient<TranslationService>();
            builder.Services.AddScoped<ITranslationService, TranslationService>();
            builder.Services.AddSingleton<GoogleCalendarService>();
            builder.Services.AddSingleton<PROG7314_POE.Repository.InMemoryRepository>();
            builder.Services.AddSingleton<PROG7314_POE.Services.IGameService, PROG7314_POE.Services.GameService>();
            builder.Services.AddSingleton<CameraService>();

            builder.Services.AddCors(options =>
            {
                options.AddPolicy("AllowAll", policy =>
                {
                    policy.AllowAnyOrigin()
                          .AllowAnyMethod()
                          .AllowAnyHeader();
                });
            });

            // JWT Authentication
            builder.Services.AddAuthentication(JwtBearerDefaults.AuthenticationScheme)
                .AddJwtBearer(options =>
                {
                    options.RequireHttpsMetadata = false; // LAN mode
                    options.SaveToken = true;
                    options.TokenValidationParameters = new TokenValidationParameters
                    {
                        ValidateIssuer = true,
                        ValidateAudience = true,
                        ValidateLifetime = true,
                        ValidateIssuerSigningKey = true,
                        ValidIssuer = jwtIssuer,
                        ValidAudience = jwtAudience,
                        IssuerSigningKey = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(jwtKey))
                    };
                });

            builder.Services.AddControllers();
            builder.Services.AddEndpointsApiExplorer();
            builder.Services.AddSwaggerGen();

            var app = builder.Build();

            // Swagger
            app.UseSwagger();
            app.UseSwaggerUI(c =>
            {
                c.SwaggerEndpoint("/swagger/v1/swagger.json", "PAM API v1");
                c.RoutePrefix = "swagger";
            });

            // Use CORS before auth
            app.UseCors("AllowAll");

            app.UseAuthentication();
            app.UseAuthorization();

            app.MapControllers();

            app.Run();
        }
    }
}
