# 🚗 Car Rental System (Full Stack)

A modern, full-stack car rental application built with **Spring Boot 3** (Backend) and **React + Vite** (Frontend).

![License](https://img.shields.io/badge/License-MIT-blue.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.3-green)
![React](https://img.shields.io/badge/React-18-blue)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791)

## ✨ Features

- **User Roles:** Admin & User (Customer).
- **Authentication:** Secure JWT-based stateless authentication.
- **Car Management:** Admin can add, edit, delete cars.
- **Rental System:** Users can browse available cars and book them.
- **Concurrency Control:** Prevents double-booking using **Pessimistic Locking**.
- **Activity Logging:** Tracks all admin and user actions.
- **Modern UI:** Dark-themed, responsive design with glassmorphism effects.

## 🛠️ Tech Stack

### Backend (`/backend-rent-car`)
- **Framework:** Spring Boot 3.2.3
- **Language:** Java 21
- **Database:** PostgreSQL
- **Security:** Spring Security + JWT
- **ORM:** Spring Data JPA (Hibernate)
- **Tools:** Maven, Lombok

### Frontend (`/frontend-rent-car`)
- **Framework:** React 18
- **Build Tool:** Vite
- **Styling:** Vanilla CSS (Custom Design System)
- **Routing:** React Router DOM
- **HTTP Client:** Axios

## 🚀 Getting Started

### Prerequisites
- JDK 21+
- Node.js 18+
- PostgreSQL
- Maven (optional, wrapper included)

### 1. Database Setup
Create a PostgreSQL database named `rent_car_db`:
```sql
CREATE DATABASE rent_car_db;
```
*Note: Update `src/main/resources/application.properties` if your DB credentials are not `postgres`/`postgres`.*

### 2. Backend Setup
```bash
cd backend-rent-car
# Run the application (it will auto-install dependencies)
./mvnw spring-boot:run
```
Server runs on: `http://localhost:8080`

### 3. Frontend Setup
```bash
cd frontend-rent-car
# Install dependencies
npm install
# Run development server
npm run dev
```
Client runs on: `http://localhost:5173`

## 👤 Default Accounts
The application seeds an admin account on startup:

| Role | Username | Password |
|------|----------|----------|
| **Admin** | `admin` | `admin123` |
| **User** | *(Register via UI)* | *(Set via UI)* |

## 📂 Project Structure
```
root/
├── backend-rent-car/    # Spring Boot Application
├── frontend-rent-car/   # React Application
└── README.md           # This file
```

---

*Created by [Yuzzar](https://github.com/Yuzzar)*
