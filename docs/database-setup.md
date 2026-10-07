# Database Setup

Our API uses a PostgreSQL database hosted by Neon.

## Neon connection information

- Branch: `production`
- Database: `neondb`
- Role/username: `neondb_owner`

## Local environment setup

1. In Neon, click **Connect**.
2. Select the `production` branch, `neondb` database, and `neondb_owner` role.
3. Copy the connection string.
4. Create a `.env` file in the project root, next to `build.gradle.kts`.

```env
DATABASE_URL=
```

5. Paste connection string into DATABASE_URL variable. 
