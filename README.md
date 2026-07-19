# 💳 payKaroo | Microservices Payment Backend

A production-style **Payment Backend** built using **Spring Boot Microservices**. The application demonstrates secure payment processing, authentication, event-driven communication, fault tolerance, caching, and refund management.

This project was built to learn and implement real-world backend concepts commonly used in fintech applications.

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Language / Framework | Java 17, Spring Boot 3.5 |
| Service Discovery | Netflix Eureka |
| API Gateway | Spring Cloud Gateway (WebFlux) |
| Security | Spring Security, JWT (JJWT), BCrypt |
| Database | PostgreSQL (one DB per service) |
| Caching | Redis (idempotency keys) |
| Messaging | Apache Kafka + Zookeeper |
| Payments | Razorpay Java SDK (test mode) |
| Email | JavaMail + Mailtrap (sandbox SMTP) |
| Resilience | Resilience4j (circuit breaker, retry) |
| Inter-service calls | OpenFeign |
| Cross-cutting logging | Spring AOP |
| Containerization | Docker, Docker Compose |
| Build | Maven |

## 🏗️ Architecture Overview

The system is split into decoupled microservices that communicate via REST (Synchronous) and Apache Kafka (Asynchronous).

**Auth Service**: Manages JWT/Refresh Token generation, user authentication, and profile data.

**Payment Service**: Core ledger handling Razorpay integration, transaction states, and refund logic.

**Notification Service**: Consumes payment/auth events to trigger transactional emails.

## 🏗️ Microservices Architecture

The application is divided into independent services.

```
                        ┌─────────────────-┐
                        │  Eureka Server   │
                        │   (Discovery)    │
                        └────────┬─────────┘
                                 │
                    ┌────────────┴──────────-──┐
                    │      API Gateway         │
                    │  (Routing + JWT filter)  │
                    └────────────┬─────────────┘
                                 │
        ┌────────────────────────┼───────────────────────---─┐
        │                        │                           │
┌───────▼──────-──┐    ┌──────────▼──────-───┐    ┌──────────▼──────────┐
│  auth-service   │    │  payment-service    │    │ notification-service │
│  (JWT, users)   │◄───┤  (Razorpay, Redis,  ├───►│  (Kafka consumer,     │
│                 │Feign│   Kafka producer)   │Kafka│   email via Mailtrap)│
└───────┬─────────┘    └──────────┬──────────┘    └──────────┬───────────┘
        │                         │                          │
┌───────▼─────-───┐    ┌──────────▼─────────-─┐    ┌──────────▼───────────┐
│ auth_service_db │    │ payment_service_db   │    │ notification_service │
│   (PostgreSQL)  │    │   (PostgreSQL)       │    │   _db (PostgreSQL)   │
└─────────────────┘    └──────────┬───────────┘    └──────────────────────┘
                                   │
                        ┌──────────┴────────--──┐
                        │   Redis (idempotency) │
                        │   Kafka (events)      │
                        └───────────────────────┘
```

## 🔐 Key Features & Design Choices

- **Database-per-service** — enforces true service independence; no service reads another's tables directly.
- **Dual-layer JWT validation** — the gateway performs coarse-grained authentication on every request; each service independently validates the token and populates its own Spring Security context for fine-grained `@PreAuthorize` checks.
- **Idempotency via Redis** — duplicate `create-order` requests (same `Idempotency-Key`) return the cached original response rather than creating a duplicate Razorpay order.
- **Event-driven notifications** — `payment-service` never calls `notification-service` directly; it publishes events to Kafka, and `notification-service` reacts independently. This decouples the two services and means notification failures never block a payment.
- **Self-contained events** — Kafka event payloads carry the user's email directly (fetched via Feign from `auth-service` at publish time), since `notification-service` has no database access to `auth-service`'s data.
- **Circuit breaker + retry** — Razorpay API calls are wrapped with Resilience4j; transient failures are retried automatically, and sustained failures trip the circuit to fail fast with a fallback response rather than cascading.
- **AOP audit logging** — every `PaymentService`/`RefundService` method call is automatically logged (method name, user, success/failure) via a single `@Around` aspect, with no logging code inside the business logic itself.

- ## ⚠️Known Limitations

Documented here deliberately, as an honest account of scope boundaries in this iteration:
 
- **No rate limiting** — the API currently has no request throttling. A production version would add Spring Cloud Gateway's Redis-backed `RequestRateLimiter` at the gateway layer.
- **Dual-write risk between DB and Kafka** — `payment-service` writes to PostgreSQL and publishes to Kafka as two separate, non-atomic operations. A production-grade fix would use the Transactional Outbox Pattern (write the event to an outbox table in the same DB transaction, with a separate poller/CDC process publishing to Kafka).
- **Webhook is implemented but not live-tested** — the Razorpay webhook endpoint (HMAC verification + event handling) is fully implemented, but real end-to-end testing requires a public tunnel (e.g., ngrok) to receive Razorpay's server-to-server callbacks, which wasn't completed in this iteration.
- **Internal service-to-service endpoints are not separately secured** — `GET /api/auth/user/{userId}` (used by Feign) is reachable under the same `permitAll()` rule as public auth endpoints. A production system would isolate internal-only endpoints from the public gateway entirely.

- ## 🚀Running Locally

### Prerequisites
- JDK 17
- Maven
- Docker Desktop
- PostgreSQL running locally (or via Docker)
- A Razorpay test-mode account (Key ID + Key Secret)
- A Mailtrap account (SMTP credentials)

### Setup 
1. Create three PostgreSQL databases: `auth_service_db`, `payment_service_db`, `notification_service_db`
2. Start supporting infrastructure:
```bash
   docker-compose up -d
```
   (Starts Redis, Kafka, and Zookeeper)

3. In each service, create `src/main/resources/application-local.yml` with your local secrets (DB password, JWT secret, Razorpay keys, Mailtrap credentials) — see `application.yml` in each service for the expected keys.
4. Start services in this order: `eureka-server` → `api-gateway` → `auth-service` → `payment-service` → `notification-service`
5. Confirm all services are registered at the Eureka dashboard: `http://localhost:8761`

### Testing the payment flow
 
A standalone HTML test page (`razorpay-test.html`, not part of the repo) drives the full Razorpay checkout flow using their hosted Checkout.js — creates an order, opens the test checkout, and calls `/verify` with the real signature returned. Use Razorpay's test UPI ID `success@razorpay` to simulate a successful payment in test mode.

## ℹ️API Overview

All requests go through the gateway at `http://localhost:8080`.
 
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/auth/register` | Public | Register a new user |
| POST | `/api/auth/login` | Public | Login, returns access + refresh token |
| POST | `/api/auth/refresh` | Public | Exchange refresh token for new access token |
| POST | `/api/auth/logout` | Required | Revoke refresh token |
| POST | `/api/payments/create-order` | Required | Create a Razorpay order (idempotent) |
| POST | `/api/payments/verify` | Required | Verify payment signature |
| GET | `/api/payments/history` | Required | Paginated payment history |
| GET | `/api/payments/{id}` | Required | Get single payment |
| POST | `/api/payments/webhook` | Signature-based | Razorpay server-to-server webhook |
| POST | `/api/refunds/initiate` | Required | Initiate full/partial refund |
| GET | `/api/refunds/{id}` | Required | Get refund status |
| GET | `/api/notifications` | Required | User's notification history |
| GET | `/api/admin/audit-logs` | Admin only | View audit trail |

## Author

**Pallavi**
[GitHub](https://github.com/Pallavi1502) · [LinkedIn](https://www.linkedin.com/in/pallavi-354155271/)
