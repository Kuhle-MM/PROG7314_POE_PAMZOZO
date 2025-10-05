
using PROG7314_POE.Controllers;
using PROG7314_POE.Services;

namespace PROG7314_POE
{
    public class Program
    {
        public static void Main(string[] args)
        {
            var builder = WebApplication.CreateBuilder(args);

            // Add services to the container.
            builder.Services.AddHttpClient<GeminiService>();
            builder.Services.AddScoped<GeminiService>();
            builder.Services.AddSingleton<NavigationService>();

            builder.Services.AddHttpClient<TranslationService>();
            builder.Services.AddScoped<TranslationService>();

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

            app.UseHttpsRedirection();
            app.UseAuthorization();

            app.MapControllers();

            app.Run();
        }
    }
}
