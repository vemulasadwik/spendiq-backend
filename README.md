# SpendIQ Pro — Spring Boot Backend

## Tech Stack
- **Java 21** + **Spring Boot 3.2**
- **Spring Security** + **JWT** (stateless auth)
- **Spring Data JPA** + **Hibernate**
- **MySQL 8**
- **Lombok**
- **Maven**

---

## Project Structure

```
src/main/java/com/spendiq/
├── SpendIQApplication.java
├── config/
│   └── SecurityConfig.java          # JWT + CORS setup
├── controller/
│   ├── AuthController.java          # /api/auth/**
│   ├── ExpenseController.java       # /api/expenses/**
│   ├── GroupSplitController.java    # /api/splits/**
│   ├── NotificationController.java  # /api/notifications/**
│   └── AnalyticsController.java     # /api/analytics/**
├── dto/
│   ├── request/                     # Incoming payloads
│   └── response/                    # Outgoing payloads
├── entity/
│   ├── User.java
│   ├── Expense.java
│   ├── GroupSplit.java
│   ├── SplitOwe.java
│   └── Notification.java
├── exception/                       # Custom exceptions + global handler
├── repository/                      # JPA repositories
├── security/
│   ├── JwtUtil.java
│   └── JwtAuthFilter.java
└── service/
    ├── AuthService.java
    ├── ExpenseService.java
    ├── GroupSplitService.java
    ├── NotificationService.java
    └── AnalyticsService.java
```

---

## Setup

### 1. MySQL — Create Database
```sql
CREATE DATABASE spendiq_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```
Or run the full schema:
```bash
mysql -u root -p < schema.sql
```

### 2. Configure application.properties
Edit `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/spendiq_db
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD
app.jwt.secret=YourLongSecretKeyHere
```

### 3. Run
```bash
mvn spring-boot:run
```
Server starts at `http://localhost:8080`

---

## API Reference

### Authentication
| Method | Endpoint            | Auth | Description           |
|--------|---------------------|------|-----------------------|
| POST   | /api/auth/register  | ❌   | Register new user     |
| POST   | /api/auth/login     | ❌   | Login, returns JWT    |
| GET    | /api/auth/me        | ✅   | Get current user      |
| GET    | /api/auth/users     | ✅   | List all users        |

### Expenses
| Method | Endpoint                     | Auth | Description                    |
|--------|------------------------------|------|--------------------------------|
| GET    | /api/expenses                | ✅   | Get all (with filters)         |
| GET    | /api/expenses?type=expense   | ✅   | Filter by type                 |
| GET    | /api/expenses?category=Food  | ✅   | Filter by category             |
| GET    | /api/expenses?from=&to=      | ✅   | Filter by date range           |
| GET    | /api/expenses?search=Netflix | ✅   | Search by title                |
| GET    | /api/expenses/recurring      | ✅   | Get recurring entries          |
| GET    | /api/expenses/{id}           | ✅   | Get one                        |
| POST   | /api/expenses                | ✅   | Create entry                   |
| PUT    | /api/expenses/{id}           | ✅   | Update entry                   |
| DELETE | /api/expenses/{id}           | ✅   | Delete entry                   |

### Group Splits
| Method | Endpoint                       | Auth | Description                    |
|--------|--------------------------------|------|--------------------------------|
| GET    | /api/splits                    | ✅   | Get all my splits              |
| GET    | /api/splits/{id}               | ✅   | Get split detail               |
| POST   | /api/splits                    | ✅   | Create split + notify members  |
| DELETE | /api/splits/{id}               | ✅   | Delete split                   |
| PATCH  | /api/splits/{id}/pay/{userId}  | ✅   | Mark user as paid              |
| PATCH  | /api/splits/{id}/unpay/{userId}| ✅   | Undo payment                   |
| POST   | /api/splits/{id}/qr            | ✅   | Upload QR code (payer only)    |
| GET    | /api/splits/{id}/qr            | ❌   | Serve QR image (public)        |

### Notifications
| Method | Endpoint                           | Auth | Description            |
|--------|------------------------------------|------|------------------------|
| GET    | /api/notifications                 | ✅   | All notifications      |
| GET    | /api/notifications/unread          | ✅   | Unread only            |
| GET    | /api/notifications/count           | ✅   | Unread count (badge)   |
| PATCH  | /api/notifications/{id}/dismiss    | ✅   | Dismiss one            |
| PATCH  | /api/notifications/dismiss-all     | ✅   | Dismiss all            |

### Analytics
| Method | Endpoint              | Auth | Description                      |
|--------|-----------------------|------|----------------------------------|
| GET    | /api/analytics        | ✅   | Full analytics (current year)    |
| GET    | /api/analytics?year=  | ✅   | Analytics for specific year      |

---

## Postman
Import `SpendIQ_Postman_Collection.json` into Postman.
The **Login** request auto-saves the JWT token to `{{token}}` variable for all subsequent requests.

---

## Request / Response Examples

### POST /api/auth/login
```json
// Request
{ "email": "arjun@email.com", "password": "arjun123" }

// Response
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "user": { "id": 1, "name": "Arjun Sharma", "email": "arjun@email.com", "avatar": "AS" }
  }
}
```

### POST /api/splits (Create Group Split)
```json
// Request
{
  "title": "Pizza Night",
  "totalAmount": 2400.00,
  "date": "2026-03-09",
  "note": "Dominos",
  "paidByUserId": 1,
  "memberUserIds": [1, 2, 3]
}

// Response — split created, notifications sent to users 2 & 3
{
  "success": true,
  "data": {
    "id": 1,
    "title": "Pizza Night",
    "totalAmount": 2400.00,
    "perHead": 800.00,
    "paidBy": { "id": 1, "name": "Arjun Sharma" },
    "owes": [
      { "userId": 2, "userName": "Priya Patel", "amount": 800.00, "paid": false },
      { "userId": 3, "userName": "Rahul Mehta", "amount": 800.00, "paid": false }
    ]
  }
}
```
