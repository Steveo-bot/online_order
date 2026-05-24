# 🍔 OnlineOrder

A full-stack food ordering web application built with **Spring Boot** and **React**. Users can browse restaurants and menus, add items to their cart, and check out — all secured with session-based authentication.

![App Banner](https://placehold.co/1200x400/ff6b35/ffffff?text=OnlineOrder+—+Food+Ordering+App)

---

## Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Database Schema](#database-schema)
- [API Reference](#api-reference)
- [Getting Started](#getting-started)
- [Running with Docker](#running-with-docker)
- [Running Locally](#running-locally)
- [Configuration](#configuration)
- [Caching](#caching)
- [Security](#security)
- [Project Structure](#project-structure)

---

## Overview

OnlineOrder lets customers:

1. **Browse** a list of restaurants and their menus
2. **Register / Log in** with email and password
3. **Add items** to a personal cart
4. **Checkout** to clear the cart and place the order

The React frontend is compiled into static files and served directly by Spring Boot, so the whole application ships as a single JAR.

![Browse Restaurants](https://placehold.co/900x500/f9f9f9/333333?text=📷+Restaurant+List+Screenshot)

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.5 |
| Security | Spring Security (session-based, JDBC) |
| Database | PostgreSQL 15 |
| ORM / Data Access | Spring Data JDBC |
| Caching | Caffeine (in-memory, 60 s TTL) |
| Build Tool | Gradle 8 |
| Containerization | Docker + Docker Compose |
| Frontend | React (pre-built, served as static assets) |

---

## Architecture

The backend follows a classic **layered architecture**:

```
HTTP Request
     │
     ▼
┌─────────────┐
│  Controller │   CartController, MenuController, CustomerController
└──────┬──────┘
       │
       ▼
┌─────────────┐
│   Service   │   CartService, CustomerService, RestaurantService, MenuItemService
└──────┬──────┘
       │  (Caffeine cache sits here)
       ▼
┌─────────────┐
│ Repository  │   Spring Data JDBC repositories
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  PostgreSQL │
└─────────────┘
```

The React frontend is bundled into `src/main/resources/public/` and served as static content by the embedded Tomcat server.

![Architecture Diagram](https://placehold.co/900x420/e8f4fd/1a73e8?text=📷+Architecture+Diagram)

---

## Database Schema

Six tables form the core data model:

```
customers ──────────────── carts ─────────────── order_items
  id (PK)                    id (PK)               id (PK)
  email (UNIQUE)             customer_id (FK)      cart_id (FK)
  password                   total_price           menu_item_id (FK)
  enabled                                          price
  first_name                                       quantity
  last_name

authorities                restaurants            menu_items
  id (PK)                    id (PK)               id (PK)
  email (FK → customers)     name                  restaurant_id (FK)
  authority                  address               name
                             image_url             price
                             phone                 description
                                                   image_url
```

Each customer gets exactly **one cart** (created at sign-up). Order items live inside that cart until checkout clears them.

The database is **auto-initialized** on startup via `database-init.sql`, which also seeds three sample restaurants (Burger King, SGD Tofu House, Fashion Wok) with menu items.

---

## API Reference

### Authentication

| Method | Endpoint | Auth Required | Description |
|--------|----------|:---:|-------------|
| `POST` | `/signup` | ✗ | Register a new account |
| `POST` | `/login` | ✗ | Log in (form login, returns HTTP 200) |
| `POST` | `/logout` | ✗ | Log out (returns HTTP 200) |

**Sign-up request body:**
```json
{
  "email": "jane@example.com",
  "password": "secret123",
  "first_name": "Jane",
  "last_name": "Doe"
}
```

---

### Restaurants & Menus

| Method | Endpoint | Auth Required | Description |
|--------|----------|:---:|-------------|
| `GET` | `/restaurants/menu` | ✗ | Get all restaurants with their menus |
| `GET` | `/restaurant/{restaurantId}/menu` | ✗ | Get menu for a specific restaurant |

**Sample response for `/restaurants/menu`:**
```json
[
  {
    "id": 1,
    "name": "Burger King",
    "address": "773 N Mathilda Ave, Sunnyvale, CA 94085",
    "phone": "(408) 736-0101",
    "image_url": "https://...",
    "menu_items": [
      {
        "id": 1,
        "name": "Whopper Meal",
        "price": 10.59,
        "description": "...",
        "image_url": "https://..."
      }
    ]
  }
]
```

---

### Cart

All cart endpoints require an authenticated session.

| Method | Endpoint | Auth Required | Description |
|--------|----------|:---:|-------------|
| `GET` | `/cart` | ✓ | Get current user's cart |
| `POST` | `/cart` | ✓ | Add a menu item to the cart |
| `POST` | `/cart/checkout` | ✓ | Checkout — clears the cart |

**Add to cart request body:**
```json
{
  "menu_id": 3
}
```

**Get cart response:**
```json
{
  "id": 7,
  "total_price": 18.58,
  "order_items": [
    {
      "id": 12,
      "menu_item_id": 2,
      "name": "Whopper Meal",
      "price": 10.59,
      "quantity": 1,
      "image_url": "https://..."
    }
  ]
}
```

![Cart UI](https://placehold.co/900x500/fff8f0/ff6b35?text=📷+Cart+Screenshot)

---

## Getting Started

### Prerequisites

- **Java 21** (or newer)
- **Docker & Docker Compose** (recommended for the database)
- **Gradle** (or use the included `./gradlew` wrapper)

---

## Running with Docker

The easiest way to get PostgreSQL running:

```bash
# Start the database
docker compose up -d

# Build and run the application
./gradlew bootRun
```

The app will be available at **http://localhost:8080**.

> **Port conflict?** If 8080 is taken, add `server.port=8081` to `application.yml`.  
> If PostgreSQL port 5432 is taken, change the left-side port in `docker-compose.yml` to `5433:5432`.

---

## Running Locally

If you already have a PostgreSQL instance running locally, you can skip Docker and override the connection settings:

```bash
./gradlew bootRun \
  --args='--spring.datasource.url=jdbc:postgresql://localhost:5432/onlineorder \
          --spring.datasource.username=postgres \
          --spring.datasource.password=secret'
```

Or set environment variables before running:

```bash
export DATABASE_URL=localhost
export DATABASE_PORT=5432
export DATABASE_USERNAME=postgres
export DATABASE_PASSWORD=secret

./gradlew bootRun
```

---

## Configuration

All configuration lives in `src/main/resources/application.yml`. Key settings and their environment variable overrides:

| Setting | Env Variable | Default |
|---------|-------------|---------|
| DB host | `DATABASE_URL` | `localhost` |
| DB port | `DATABASE_PORT` | `5432` |
| DB username | `DATABASE_USERNAME` | `postgres` |
| DB password | `DATABASE_PASSWORD` | `secret` |
| DB init mode | `INIT_DB` | `always` |

> ⚠️ **`INIT_DB=always`** drops and recreates all tables on every startup. Change to `never` in production to preserve your data.

---

## Caching

Cart lookups are cached in-memory using **Caffeine** to reduce database round-trips.

| Annotation | Method | Effect |
|------------|--------|--------|
| `@Cacheable("cart")` | `getCart(customerId)` | Returns cached result; hits DB only on first call |
| `@CacheEvict(key = "#customerId")` | `addMenuItemToCart(...)` | Evicts the cache entry so next `getCart` fetches fresh data |
| `@CacheEvict(key = "#customerId")` | `clearCart(...)` | Evicts the cache entry on checkout |

Cache entries expire automatically after **60 seconds** (configured in `application.yml`).

---

## Security

Authentication uses **Spring Security** with a JDBC-backed `UserDetailsManager` pointing at the `customers` and `authorities` tables.

| Route pattern | Accessible by |
|---------------|---------------|
| Static files, `/`, `*.json`, `*.png` | Everyone |
| `POST /signup`, `POST /login`, `POST /logout` | Everyone |
| `GET /restaurants/**`, `GET /restaurant/**` | Everyone |
| `/cart`, `/cart/checkout` | Authenticated users only |

- Passwords are hashed with **BCrypt** (via Spring's delegating encoder).
- CSRF protection is **disabled** — appropriate for a same-origin SPA.
- Login returns **HTTP 200** on success; unauthenticated access to protected routes returns **HTTP 401**.

---

## Project Structure

```
OnlineOrder/
├── src/
│   ├── main/
│   │   ├── java/com/laioffer/onlineorder/
│   │   │   ├── OnlineOrderApplication.java   # Entry point
│   │   │   ├── AppConfig.java                # Security config, beans
│   │   │   ├── DevRunner.java                # Dev-time seed runner
│   │   │   ├── controller/
│   │   │   │   ├── CartController.java       # GET/POST /cart
│   │   │   │   ├── CustomerController.java   # POST /signup
│   │   │   │   └── MenuController.java       # GET /restaurants/menu
│   │   │   ├── service/
│   │   │   │   ├── CartService.java          # Cart logic + caching
│   │   │   │   ├── CustomerService.java      # Registration logic
│   │   │   │   ├── RestaurantService.java
│   │   │   │   └── MenuItemService.java
│   │   │   ├── repository/                   # Spring Data JDBC repos
│   │   │   ├── entity/                       # DB row records
│   │   │   └── model/                        # DTOs and request bodies
│   │   └── resources/
│   │       ├── application.yml               # App configuration
│   │       ├── database-init.sql             # Schema + seed data
│   │       └── public/                       # React build output
│   └── test/
│       └── java/com/laioffer/onlineorder/
│           ├── CartServiceTests.java
│           └── OnlineOrderApplicationTests.java
├── docker-compose.yml                        # PostgreSQL service
├── Dockerfile                                # App container image
└── build.gradle                              # Dependencies & build config
```

---

## Running Tests

```bash
./gradlew test
```

Test results are saved to `build/test-results/test/`.

---

*Built with Spring Boot 3.5 · Java 21 · PostgreSQL 15*
