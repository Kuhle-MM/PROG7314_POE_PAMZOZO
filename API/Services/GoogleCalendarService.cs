using Google.Apis.Auth.OAuth2;
using Google.Apis.Calendar.v3;
using Google.Apis.Services;

namespace PROG7314_POE.Services
{
    public class GoogleCalendarService
    {
        public static CalendarService GetCalendarServiceFromToken(string accessToken)
        {
            var credential = GoogleCredential.FromAccessToken(accessToken);

            var service = new CalendarService(new BaseClientService.Initializer()
            {
                HttpClientInitializer = credential,
                ApplicationName = "P.A.M - Personal Assistant with Mobility"
            });

            return service;
        }
    }
}
