# OpenRBAC 🚀

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen.svg)](https://github.com/raubinsharya/OpenRBAC)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.1-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)

OpenRBAC is a high-performance, developer-friendly **Role-Based Access Control (RBAC)** engine designed for modern microservices and multi-tenant applications. It goes beyond simple RBAC by offering a unified, high-speed authorization model that handles complex hierarchies and inheritance with ease.

> **💻 Frontend UI Available:** You can manage OpenRBAC visually using the official Angular frontend: **[open-rbac-ui-angular](https://github.com/raubinsharya/open-rbac-ui-angular)**.

---

### 🏷️ Keywords & Topics

`RBAC` • `Access Control` • `IAM` • `Spring Boot` • `Keycloak` • `Multi-tenancy` • `Authorization` • `Java Security` • `User Management` • `Permission Management` • `Hierarchical Groups`

---

## 🏁 Project Status & Roadmap

### ✅ Available Now

- **Multi-Tenancy:** Robust Realm-based isolation for all resources.
- **Hierarchical Groups:** Full support for nested groups with recursive membership inheritance.
- **Admin UI:** A sleek, modern [Angular dashboard](https://github.com/raubinsharya/open-rbac-ui-angular) for managing realms, users, groups, and permissions visually.
- **Unified "Effective" Views:**
  - **Single-Query Architecture:** All user roles and permissions (Direct, Role-based, Group-based) are unified into efficient read-only views (`UserEffectiveRole`, `UserEffectivePermission`) utilizing Database Views/Subselects.
  - **Performance:** Solves the "N+1" problem by fetching all effective security contexts in a single query.
  - **Strict Consistency:** Filtering ensures that disabled or blocked roles, groups, and permissions instantly revoke access across the entire hierarchy.
- **Granular RBAC:**
  - **Four-Level Permission Inheritance:**
    1. `DIRECT`: Assigned explicitly to a user.
    2. `ROLE`: Inherited via roles assigned to the user.
    3. `GROUP`: Inherited via permissions assigned to groups the user is in.
    4. `GROUP_ROLE`: Inherited via roles assigned to those groups.
- **Temporary Access:** Expiry date support for all assignments (Roles, Groups, Permissions) which is automatically enforced in the unified views.
- **Enterprise-Grade Validation:** Strict Entity-level (Jakarta) validation protects database integrity, coupled with idempotent DTO endpoints for effortless UI integrations.
- **Annotation Security:** AOP-driven access control (`@RequireAnyRole`, `@RequireAllPermissions`).
- **Advanced Filtering:** Powerful API for searching by `assignmentType` (e.g., `DIRECT`, `GROUP`), status, and flexible date ranges.

### 🚀 Coming Soon (Planned)

- **Audit Logging:** Detailed event streaming and compliance reporting for every security change.
- **Logical Deletion (Soft Deletes):** Keep historical relationship mapping intact while safely removing entities.
- **ABAC Support:** Introduction of Attribute-Based Access Control for more dynamic policies.
- **API Keys:** Management of programmatic access keys and client credentials.
- **Client SDKs:** Official packages in popular languages (Node.js, Python, Go, Java) to seamlessly integrate OpenRBAC into any application stack.
- **Redis Cache:** Centralized caching for near-zero latency authorization checks.
- **Webhooks:** Real-time notifications for security events (e.g., membership expiry).

---

## ✨ Key Features

- 🌍 **Multi-Tenancy (Realms):** Isolate data and configuration across different organizations or environments.
- 🌳 **Hierarchical Groups:** Materialized Path implementation for extremely fast tree traversal and inheritance queries.
- ⚡ **High-Performance "Effective" Models:**
  - Don't waste time calculating permissions in Java loops.
  - OpenRBAC uses optimized database sub-selects to union all permission sources (Direct, Role, Group, Group-Role) into a single, queryable virtual table.
  - Instantly know _exactly_ why a user has a permission (Source Group, Assignment Type, e.t.c).
- ⏳ **Temporary Access:** Built-in support for expiry dates on all security relationships.
- 🛡️ **Annotation-Driven Security:** Secure your Spring Boot endpoints easily with custom annotations.
- 🔎 **Deep Search API:** Filter roles and permissions not just by name, but by how they were assigned (`assignmentType`), when they expire, who assigned them, and more.

---

## 🛠️ Tech Stack

- **Framework:** [Spring Boot 3.4.x](https://spring.io/projects/spring-boot)
- **Language:** Java 17+
- **Security:** Spring Security & Keycloak (OpenID Connect)
- **Persistence:** Jakarta Persistence (JPA), Hibernate
- **Database:** PostgreSQL (Recommended) / H2 (Testing)
- **Build Tool:** Maven
- **Utilities:** Lombok, MapStruct
- **Frontend:** [Angular 17+](https://github.com/raubinsharya/open-rbac-ui-angular)

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

To install the UI, follow the instructions in the [open-rbac-ui-angular](https://github.com/raubinsharya/open-rbac-ui-angular) repository.

---

## 🗺️ Project Structure

```text
src/main/java/com/open/rbac/openrbac/
├── annotations      # Security annotations (@RequireAnyRole)
├── controllers      # RESTful API Endpoints
├── dtos             # Data Transfer Objects
├── models           # JPA Entities & Immutable Views (@Subselect)
│   ├── UserEffectiveRole.java       # Unified view of all user roles
│   └── UserEffectivePermission.java # Unified view of all user permissions
├── repositories     # Spring Data Repositories
├── services         # Business Logic Service Layer
└── specifications   # JPA Specifications for dynamic filtering
```

---

## 📖 API Documentation

**Interactive Swagger UI is available out-of-the-box!**
Once the application is running, simply navigate to:
👉 `http://localhost:8080/swagger-ui.html`
to explore, test, and authenticate with all API endpoints directly from your browser.

### Important Endpoints Summary

| Endpoint                                           | Method     | Description                                                       |
| :------------------------------------------------- | :--------- | :---------------------------------------------------------------- |
| **Realms**                                         |
| `/api/v1/realms`                                   | `GET/POST` | Manage Realms (Multi-tenancy)                                     |
| **Users**                                          |
| `/api/v1/realms/{id}/users`                        | `GET`      | List users in a realm                                             |
| **Roles & Permissions**                            |
| `/api/v1/realms/{id}/users/{uid}/roles`            | `GET`      | Get **Effective** Roles (Direct + Group inherited)                |
| `/api/v1/realms/{id}/users/{uid}/permissions`      | `GET`      | Get **Effective** Permissions (Direct + Role + Group + GroupRole) |
| `/api/v1/realms/{id}/users/{uid}/check-permission` | `POST`     | Boolean check if user has specific access                         |
| **Groups**                                         |
| `/api/v1/realms/{id}/groups`                       | `GET/POST` | Manage hierarchical groups                                        |

You can filter effective roles and permissions using query parameters like:

- `?assignmentType=GROUP` (See only inherited items)
- `?assignmentType=DIRECT` (See only direct assignments)
- `?expiryDateAfter=2024-01-01`
- `?isActive=true`

---

## 🤝 Contributing

We welcome contributions! Please feel free to submit Pull Requests or open Issues for any bugs or feature requests.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

<p align="center">Made with ❤️ for the Developer Community</p>
