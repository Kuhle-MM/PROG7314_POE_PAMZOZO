using Google.Apis.Calendar.v3;
using Google.Apis.Calendar.v3.Data;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Diagnostics;
using PROG7314_POE.Models;
using PROG7314_POE.Services;

namespace PROG7314_POE.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class GoogleCalendarController : ControllerBase
    {
        [HttpPost("create-event")]
        public IActionResult CreateEvent([FromHeader(Name = "Authorization")] string bearerToken, [FromBody] GoogleCalendarEventDTO dto)
        {
            if (string.IsNullOrEmpty(bearerToken) || !bearerToken.StartsWith("Bearer "))
                return Unauthorized("Missing or invalid Authorization header");

            string accessToken = bearerToken.Substring("Bearer ".Length).Trim();

            var service = GoogleCalendarService.GetCalendarServiceFromToken(accessToken);

            var newEvent = new Event()
            {
                Summary = dto.Summary,
                Description = dto.Description,
                Start = new EventDateTime() { DateTime = dto.StartTime, TimeZone = "Africa/Johannesburg" },
                End = new EventDateTime() { DateTime = dto.EndTime, TimeZone = "Africa/Johannesburg" },
                Reminders = new Event.RemindersData()
                {
                    UseDefault = false,
                    Overrides = new EventReminder[]
                    {
                    new EventReminder() { Method = "popup", Minutes = 24 * 60 }, //reminder popup displays 1 day before the scheduled event
                    new EventReminder() { Method = "popup", Minutes = 60 } //reminder popup displays 1 hour before the scheduled event
                    }
                }
            };

            service.Events.Insert(newEvent, "primary").Execute();

            return Ok(newEvent);
        }

        [HttpGet("upcoming-events")]
        public IActionResult GetUpcomingEvents([FromHeader(Name = "Authorization")] string bearerToken)
        {
            if (string.IsNullOrEmpty(bearerToken) || !bearerToken.StartsWith("Bearer "))
                return Unauthorized("Missing or invalid Authorization header");

            string accessToken = bearerToken.Substring("Bearer ".Length).Trim();

            var service = GoogleCalendarService.GetCalendarServiceFromToken(accessToken);

            var request = service.Events.List("primary");
            request.TimeMin = DateTime.Now;
            request.TimeMax = DateTime.Now.AddDays(3);
            request.ShowDeleted = false;
            request.SingleEvents = true;
            request.OrderBy = EventsResource.ListRequest.OrderByEnum.StartTime;

            var events = request.Execute();

            var upcoming = events.Items.Select(e => new
            {
                e.Summary,
                Start = e.Start.DateTime,
                End = e.End.DateTime,
                e.Description
            }).ToList();

            return Ok(upcoming);
        }

    }
}
