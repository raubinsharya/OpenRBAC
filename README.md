# OpenRBAC 🚀

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen.svg)](https://github.com/raubinsharya/OpenRBAC)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.1-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)

OpenRBAC is a high-performance, developer-friendly **Role-Based Access Control (RBAC)** engine designed for modern microservices and multi-tenant applications. It provides a robust framework for managing users, roles, groups, and permissions with built-in support for hierarchical structures, transition-safe audits, and temporary access.

---

## ✨ Key Features

- 🌍 **Multi-Tenancy (Realms):** Isolate data and configuration across different organizations or environments using Realms.
- 🌳 **Hierarchical Groups:** Support for nested groups with membership inheritance and materialized path querying for extreme performance.
- 🔐 **Flexible Permission Model:** Define permissions using a `Resource:Action` pattern. Support for direct user permissions, role-based, and group-based assignments.
- ⏳ **Temporary Access:** Built-in support for expiry dates on all assignments (Roles, Groups, Permissions).
- 🛡️ **Annotation-Driven Security:** Secure your Spring Boot endpoints easily with `@RequireAnyRole`, `@RequireAllPermissions`, and more.
- 🆔 **Keycloak Integration:** Designed to work seamlessly with Keycloak for identity management.
- 🔎 **Advanced Filtering:** Powerful API filtering and pagination powered by JPA Specifications and Criteria API.

---

## 🛠️ Tech Stack

- **Framework:** [Spring Boot 3.4.x](https://spring.io/projects/spring-boot)
- **Language:** Java 17+
- **Security:** Spring Security & Keycloak (OpenID Connect)
- **Persistence:** Jakarta Persistence (JPA), Hibernate
- **Build Tool:** Maven
- **Utilities:** Lombok, MapStruct (for DTO mapping)

---

## 🚀 Getting Started

### Prerequisites

- JDK 17 or higher
- Maven 3.8+
- A running database (PostgreSQL recommended) or H2 for testing

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/raubinsharya/OpenRBAC.git
   cd OpenRBAC
   ```

2. Configure your database and Keycloak settings in `src/main/resources/application.yml`.

3. Build and run the application:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

The API will be available at `http://localhost:8080`.

---

## 🗺️ Project Structure

```text
src/main/java/com/open/rbac/openrbac/
├── annotations      # Security & utility annotations
├── aspects          # AOP aspects for security checks
├── controllers      # RESTful API Endpoints
├── dtos             # Data Transfer Objects
├── models           # JPA Entities (User, Role, Permission, etc.)
├── repositories     # Data access layer
├── services         # Business logic layer
└── specifications   # JPA Specifications for advanced querying
```

---

## 📖 API Documentation (Summary)

| Endpoint | Method | Description |
| :--- | :--- | :--- |
| `/api/v1/realms` | `GET/POST` | Manage Realms (Multi-tenancy) |
| `/api/v1/realms/{id}/users` | `GET` | List users in a realm |
| `/api/v1/realms/.../roles` | `GET/POST` | Manage roles and permissions |
| `/api/v1/realms/.../groups` | `GET/POST` | Manage hierarchical groups |
| `/api/v1/realms/.../users/{id}/roles` | `POST` | Assign roles to users (with expiry) |
| `/api/v1/realms/.../users/{id}/permissions` | `GET` | List direct user permissions |

---

## 🤝 Contributing

We welcome contributions! Please feel free to submit Pull Requests or open Issues for any bugs or feature requests.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

<p align="center">Made with ❤️ for the Developer Community</p>
