using Google.Apis.Calendar.v3;

namespace PROG7314_POE.Events
{
    public class GoogleCalendarEvent
    {
        public static Google.Apis.Calendar.v3.Data.Events GetUpcomingEvents(CalendarService service)
        {
            var request = service.Events.List("primary");
            request.TimeMin = DateTime.Now;
            request.TimeMax = DateTime.Now.AddDays(3);
            request.ShowDeleted = false;
            request.SingleEvents = true;
            request.OrderBy = EventsResource.ListRequest.OrderByEnum.StartTime;

            Google.Apis.Calendar.v3.Data.Events events = request.Execute();

            foreach (var eventItem in events.Items)
            {
                Console.WriteLine($"{eventItem.Summary} at {eventItem.Start.DateTime}");
            }

            return events;
        }
    }
}
