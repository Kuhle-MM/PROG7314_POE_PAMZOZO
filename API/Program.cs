
using PROG7314_POE.Controllers;
using PROG7314_POE.Services;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.IdentityModel.Tokens;
using System.Text;
namespace PROG7314_POE
{
    public class Program
    {
        public static void Main(string[] args)
        {
            var builder = WebApplication.CreateBuilder(args);

            builder.WebHost.ConfigureKestrel(options =>
            {
                // Listen on all IP addresses (0.0.0.0) on port 7298
                options.ListenAnyIP(7298);
            });

            // Add services to the container.
            builder.Services.AddHttpClient<GeminiService>();
            builder.Services.AddScoped<GeminiService>();
            builder.Services.AddHttpClient<IGeminiService, GeminiService>();

            builder.Services.AddSingleton<NavigationService>();

            builder.Services.AddHttpClient<TranslationService>();
            builder.Services.AddScoped<ITranslationService, TranslationService>();

            builder.Services.AddControllers();
            // Learn more about configuring Swagger/OpenAPI at https://aka.ms/aspnetcore/swashbuckle
            builder.Services.AddEndpointsApiExplorer();
            builder.Services.AddSwaggerGen();
            //builder.Services.AddSingleton<GeminiService>();
            //builder.Services.AddSingleton<NavigationService>();
            //builder.Services.AddSingleton<CameraService>();
            builder.Services.AddSingleton<GoogleCalendarService>();


            builder.Services.AddSingleton<PROG7314_POE.Repository.InMemoryRepository>();
            builder.Services.AddSingleton<PROG7314_POE.Services.IGameService, PROG7314_POE.Services.GameService>();

            builder.Services.AddAuthentication(JwtBearerDefaults.AuthenticationScheme)
                .AddJwtBearer(options =>
                {
                    options.TokenValidationParameters = new TokenValidationParameters
                    {
                        ValidateIssuer = true,
                        ValidateAudience = true,
                        ValidateLifetime = true,
                        ValidateIssuerSigningKey = true,
                        ValidIssuer = "MyApi",             // change to your issuer
                        ValidAudience = "MyDevices",       // change to your audience
                        IssuerSigningKey = new SymmetricSecurityKey(
                            Encoding.UTF8.GetBytes("super_secret_key_123!")) // store safely
                    };
                });

            var app = builder.Build();
            // Enable swagger in all environments
            app.UseSwagger();
            app.UseSwaggerUI(c =>
            {
                c.SwaggerEndpoint("/swagger/v1/swagger.json", "PAM API v1");
                c.RoutePrefix = "swagger"; // Means swagger will be at /swagger
            });
            // Configure the HTTP request pipeline.
            //if (app.Environment.IsDevelopment())
            //{
            //    app.UseSwagger();
            //    app.UseSwaggerUI();
            //}

            //app.UseHttpsRedirection();
            app.UseAuthentication();
            app.UseAuthorization();

            app.MapControllers();

            app.Run();
        }
    }
}
