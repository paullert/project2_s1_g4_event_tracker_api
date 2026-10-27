# Event Tracker API

REST API for events, locations, invitations, and attendance, with database-backed
user, manager, and administrator permissions.

**Team:** Paul Conner, Sarah Wafa, Ann Jojo, and Billy Provins.

**Tech stack:** Java 17, Spring Boot 4.1, Spring Security OAuth2 Resource Server,
Spring Data JPA, PostgreSQL 16, Gradle, Docker, JUnit, and Testcontainers.

- [Live API](https://project2-s1-g4-event-tracker-api.onrender.com/) Note: this project is hosted on Render.com, which may take a few seconds to load. It also might be no longer available if reading this in the future.
- [Live API documentation](https://project2-s1-g4-event-tracker-api.onrender.com/api/v1) This can also be accessed wherever api is hosted at {BASE_URL}/api/vi
- [Android app](https://github.com/paullert/project2_s1_g4_event_tracker_android)
- [openapi.yaml](docs/openapi.yaml)

## Local setup

Install Git and Docker Desktop. Java 17 is needed only to run Gradle on the host.
These commands use PowerShell; macOS/Linux users can use `./gradlew` instead.

1. Clone the repository:

   ```powershell
   git clone https://github.com/paullert/project2_s1_g4_event_tracker_api.git
   cd project2_s1_g4_event_tracker_api
   ```

2. Start a local PostgreSQL database:

   ```powershell
   docker network create event-tracker
   docker run -d --name event-tracker-db --network event-tracker -p 5432:5432 -e POSTGRES_DB=event_tracker -e POSTGRES_USER=event_tracker -e POSTGRES_PASSWORD=event_tracker_dev -v event-tracker-data:/var/lib/postgresql/data postgres:16
   docker exec event-tracker-db pg_isready -U event_tracker -d event_tracker
   ```

   Wait until the last command reports that connections are accepted. Initialize
   this **new local database only**; the schema script recreates its tables:

   ```powershell
   docker cp docs/sql event-tracker-db:/tmp/sql
   docker exec event-tracker-db psql -U event_tracker -d event_tracker -v ON_ERROR_STOP=1 -f /tmp/sql/database.sql -f /tmp/sql/add_revoked_invite_status.sql -f /tmp/sql/backfill_manager_ownership.sql
   ```

3. Create an ignored `.env` file in the repository root:

   ```properties
   DB_URL=jdbc:postgresql://event-tracker-db:5432/event_tracker
   DB_USER=event_tracker
   DB_PASSWORD=event_tracker_dev
   GOOGLE_CLIENT_ID=YOUR_ANDROID_CLIENT_ID.apps.googleusercontent.com
   PORT=8080
   ```

   Use the same Android OAuth client ID as the app's `gradle.properties`.
   The app performs PKCE sign-in; the API validates Google ID tokens from the
   `Authorization: Bearer` header. No Google client secret or password login is used.

4. Build and start the API:

   ```powershell
   docker build -t event-tracker-api .
   docker run -d --name event-tracker-api --network event-tracker --env-file .env -e PORT=8080 -p 8080:8080 event-tracker-api
   docker logs -f event-tracker-api
   ```

5. Open [local API docs](http://localhost:8080/api/v1) or request
   [events](http://localhost:8080/api/v1/events). Android emulators use
   `http://10.0.2.2:8080/`. New Google accounts receive `USER`; assign the first
   real administrator through the database. Protected routes return 401 without
   a valid token and 403 when a role/ownership check fails.

To run the API on the host instead, change the `.env` database hostname to
`localhost` and run `.\gradlew.bat bootRun`. Run `.\gradlew.bat test` with Docker
running for Testcontainers. The Docker image build skips tests.

Stop with `docker stop event-tracker-api event-tracker-db`; restart existing
containers with `docker start event-tracker-db event-tracker-api`.

## Further documentation

- [Authentication configuration](docs/authentication.md)
- [OpenAPI contract](docs/openapi.yaml)
- [Database schema](docs/sql/database.sql)
