# STUDENT ACHIEVEMENT PORTAL
> **One place where every certificate, hackathon, medal, paper, internship and prize a student earns is uploaded, verified and never lost again.**

---

## 🛠️ Technology Stack
- **Framework:** Spring Boot 4.x / 3.x (Web, Security, Data JPA, Validation, DevTools)
- **Build System:** Apache Maven
- **Templating:** Thymeleaf (Server-rendered HTML, search-engine indexable, CSRF protected)
- **Styling:** Hand-written Vanilla CSS (~25KB, CSS Grid, Flexbox, Custom Properties) — Zero framework payload
- **Database:** MySQL 8.0 / Embedded H2 (MySQL Mode) with automated seed data
- **Vault:** Local File Vault with SHA-256 Checksum deduplication and integrity sealing
- **Security:** Spring Security with BCrypt hashing, Role-Based Access Control (RBAC), and transactional audit logging

---

## 📁 Project Structure

```
student-achievement-portal/
├── backend/                      Spring Boot app (Java, Maven, MySQL/H2)
│   ├── pom.xml
│   ├── data/                     embedded H2 database files
│   ├── file-vault/               uploaded certificate proofs
│   └── src/main/
│       ├── java/app/             config, controller, model, repository, service
│       └── resources/            application.properties only
└── frontend/                     everything the user sees
    ├── templates/                Thymeleaf pages (admin, auth, faculty, student, ...)
    └── static/                   css/style.css, js/app.js
```

`backend/pom.xml` copies `../frontend` onto the classpath at build time, so the pages and
assets are served exactly as before — no code changes needed.

---

## 🚀 Running the Project

```bash
cd backend
mvn spring-boot:run
```

The portal runs on **`http://localhost:8085`** (port configurable in [application.properties](backend/src/main/resources/application.properties)).

> After editing a file in `frontend/`, re-run `mvn spring-boot:run` (or `mvn process-resources` while it is running) so the change is copied into the build.

## Deploying on Render

The project includes a root-level `Dockerfile` for deployment as a Render web service.

1. Push the complete project folder (including `backend/` and `frontend/`) to a GitHub repository.
2. In Render, select **New + → Web Service**, connect the repository, and choose **Docker** as the runtime.
3. If the repository contains the project inside a parent folder, set **Root Directory** to that folder. If the project itself is the repository root, leave this blank.
4. Create the service. Render builds the Dockerfile and provides the `PORT` value automatically.
5. For a demo deployment, add the environment variable `SPRING_H2_CONSOLE_ENABLED=false`. Once deployment finishes, open the Render URL.

**Persistence and demo security:** The default H2 database and uploaded files are stored on the service's local filesystem and can be lost on Render restarts or redeploys. For persistence, attach a Render disk mounted at `/var/data` (on a plan that supports disks), then set `SPRING_DATASOURCE_URL` to `jdbc:h2:file:/var/data/portaldb;MODE=MySQL;DATABASE_TO_UPPER=FALSE;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE` and `APP_UPLOAD_DIR` to `/var/data/file-vault`. Keep this as a single service instance. The seeded demo accounts have known passwords; do not use this deployment with real student data unless you first secure the accounts and storage.

## Deploying the Static Preview on Netlify

The `netlify/` directory contains a standalone landing-page preview, and the root `netlify.toml` configures Netlify to publish it without a build command. In Netlify, import this GitHub repository and deploy the `main` branch. This static preview does not include the Thymeleaf pages, sign-in, live data, or upload functionality; those require the Spring Boot application.

---

## 🔑 Demo Accounts (Pre-Seeded & Ready)

| Role | Name | Email | Password | Scope / Department |
| :--- | :--- | :--- | :--- | :--- |
| **Student** | Demo Student | `student@example.com` | `student123` | CSE (AIML), Roll: DEMO001 |
| **Faculty Verifier** | Dr. K. Srinivas Rao | `cse.faculty@anurag.edu.in` | `faculty123` | Computer Science & Engineering (Scope: ALL) |
| **Sports Director** | Prof. P. Ramana | `sports@anurag.edu.in` | `sports123` | Physical Education (Scope: SPORTS) |
| **Placement Cell** | Ms. Shalini Gupta | `placements@anurag.edu.in` | `place123` | Placement Cell (Scope: INTERNSHIP) |
| **Admin** | System Administrator | `admin@anurag.edu.in` | `admin123` | System-wide Audit & Configuration |

*Note: The login page includes one-click autofill buttons for evaluators.*

---

## 🎯 Completed Feature Checklist (All 13 Features)

1. **Feature 01: Accounts and Roles** — Student, Faculty, Admin, Placement & Sports verifiers. BCrypt password hashing.
2. **Feature 02: Profile and Portfolio** — Public shareable URL (`/portfolio/DEMO001`), SEO JSON-LD schema, privacy gates (Public, College Only, Private), print-ready resume style.
3. **Feature 03: Certificate Upload** — Camera capture scanner (`capture="environment"`), pre-flight file validation, SHA-256 checksum deduplication, isolated file vault.
4. **Feature 04: Hackathons** — Team size, individual role, outcome (Winner/Finalist), project demo URL.
5. **Feature 05: Sports Achievements** — Sport name, format (singles/team), medal position (Gold/Silver/Bronze), routed to Physical Education.
6. **Feature 06: Paper Publications** — Journal/Conference/Patent, DOI number validation, ISSN/ISBN, venue, author position.
7. **Feature 07: Internships** — Company, duration, stipend, work mode, offer letter + completion certificate support.
8. **Feature 08: Competitions** — Coding contests, technical fests, rank secured, prize details.
9. **Feature 09: Faculty Verification** — Pre-filtered queue, side-by-side proof preview workspace, approve/reject/return for correction with reason, append-only verification log.
10. **Feature 10: Centralised Achievement Database** — Full-text search by roll number, student name, branch, category, level; one-click CSV export.
11. **Feature 11: Analytics & Reports** — Zero-framework server-rendered SVG bar charts, NAAC Criterion 5 compliance tables, NBA criteria, permanent snapshot archiver.
12. **Feature 12: Notifications** — In-database notification queue, unread count badge in header, mark as read drawer.
13. **Feature 13: Admin Console** — Configurable category schemas, configurable scoring weight matrix (Section 13), verifier routing, immutable audit log table.
