# Lost & Found Portal

A responsive full-stack web application that helps users report lost or found items, search listings, submit ownership claims, and track item recovery.

## Features

- Report lost and found items with descriptions, categories, locations, dates, and photos.
- Search and filter listings by keyword, category, and status.
- Submit ownership claims with proof of ownership.
- Approve or reject claims and update item recovery status.
- User dashboard for managing listings and claims.
- Admin dashboard for managing listings and users.
- Automatic email notifications for important activities.

## Technology Stack

| Component | Technology |
|---|---|
| Frontend | React.js, Vite, CSS |
| Backend | Java, Spring Boot |
| Database | MySQL |
| API | REST API |
| Email | Spring Boot Mail |
| Build Tool | Maven |
| Testing | JUnit 5, Mockito |

## Project Structure

```text
internship_project/
├── backend/
│   ├── src/
│   ├── pom.xml
│   └── mvnw.cmd
├── frontend/
│   ├── src/
│   └── package.json
└── README.md
```

## Prerequisites

- Java 17 or higher
- Node.js and npm
- MySQL Server 8.0+
- Git

## Installation and Execution

### 1. Create the Database

```sql
CREATE DATABASE lost_and_found_db;
```

Configure the database connection in the backend configuration file.

### 2. Run the Backend

Open Git Bash or a terminal:

```bash
cd backend
./mvnw spring-boot:run
```

On Windows, use:

```bash
mvnw.cmd spring-boot:run
```

The backend runs at `http://localhost:8080`.

### 3. Run the Frontend

Open a new terminal:

```bash
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173` in your browser.

## Testing

Run the backend tests using:

```bash
cd backend
./mvnw test
```

The project includes automated tests for item management, claim processing, email notifications, and application functionality.

## Future Enhancements

- AI-based image matching for lost and found items.
- Interactive campus maps.
- In-app messaging between users.
- Mobile application support.

## License

Developed as a student project for educational and portfolio purposes.
