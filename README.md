# NIT Trichy CDI — Order-to-Audit System

A full-stack MVP for a small food manufacturer covering the complete order-to-audit workflow.

## Tech Stack
- **Frontend**: React 18 + Vite + Tailwind CSS + React Router + Axios
- **Backend**: Java 17 + Spring Boot 3.2 + Spring Security (JWT) + JPA/Hibernate
- **Database**: MySQL 8

---

## Prerequisites
| Tool | Version |
|------|---------|
| Java | 17+ |
| Maven | 3.8+ |
| Node.js | 18+ |
| MySQL | 8.0+ |

---

## How to Run

### 1. MySQL Setup
Start MySQL and run:
```sql
CREATE DATABASE IF NOT EXISTS nit_audit_db;
```
Or just run: `mysql -u root -p < db_setup.sql`

If your MySQL password is **not** `root`, edit:
```
backend/src/main/resources/application.properties
```
Change `spring.datasource.password=root` to your password.

---

### 2. Start the Backend
```bash
cd backend
mvn spring-boot:run
```
- Runs on **http://localhost:8080**
- On first start, all tables are auto-created and benchmark seed data is inserted automatically.

---

### 3. Start the Frontend
Open a **new terminal**:
```bash
cd frontend
npm install      # only needed once
npm run dev
```
- Runs on **http://localhost:3000**

---

## Login Credentials (Benchmark Data)

| Role       | Username     | Password  |
|------------|-------------|-----------|
| Admin      | admin        | admin123  |
| Salesman   | salesman1    | sales123  |
| Salesman   | salesman2    | sales123  |
| Production | production1  | prod123   |

---

## Features by Role

### Admin
- Dashboard with live KPIs (orders, revenue, production stats)
- Product management (CRUD, GST rates)
- Customer management
- Raw material management
- Bill of Materials (BOM) editor per product
- Production order management + variance reports
- Sales order approval / rejection
- GST invoice generation & print view
- Inventory ledger (purchases, adjustments)
- CSV report downloads (sales, inventory, production variance)
- User management (create, activate/deactivate)

### Salesman
- Create and view own sales orders
- Customer management

### Production
- Create and manage production orders
- Record actual consumption vs planned (BOM-driven)
- Stock auto-deducted on completion
- Inventory ledger + purchase entries

---

## Benchmark Seed Data

### Products
| SKU | Name | Price | GST |
|-----|------|-------|-----|
| TK-500 | Tomato Ketchup 500g | ₹85 | 12% |
| MP-250 | Mango Pickle 250g | ₹120 | 5% |
| MFJ-200 | Mixed Fruit Jam 200g | ₹95 | 12% |

### Customers
- FreshMart Retail Pvt Ltd (Chennai)
- GreenLeaf Distributors (Bengaluru)
- NutriChoice Foods (Pune)

### Raw Materials (12 items)
Fresh Tomatoes, Sugar, Vinegar, Salt, Raw Mangoes, Mustard Oil, Spice Mix, Mixed Fruits, Pectin, Glass Bottles 500ml, Glass Jars 250ml, Glass Jars 200ml

### BOM (per unit)
- **Tomato Ketchup 500g**: 850g tomatoes + 120g sugar + 30ml vinegar + 10g salt + 1 bottle
- **Mango Pickle 250g**: 220g raw mango + 25ml mustard oil + 15g spice mix + 8g salt + 1 jar
- **Mixed Fruit Jam 200g**: 180g mixed fruits + 90g sugar + 2g pectin + 1 jar

---

## API Endpoints Summary

| Method | Endpoint | Access |
|--------|----------|--------|
| POST | /api/auth/login | Public |
| GET/POST/PUT/DELETE | /api/products | Admin |
| GET/POST/PUT/DELETE | /api/customers | Admin, Salesman |
| GET/POST/PUT | /api/raw-materials | Admin, Production |
| GET/POST | /api/bom/product/{id} | Admin |
| GET/POST/PUT | /api/production | Admin, Production |
| GET/POST | /api/sales-orders | Admin, Salesman |
| PUT | /api/sales-orders/{id}/approve | Admin |
| PUT | /api/sales-orders/{id}/reject | Admin |
| POST | /api/sales-orders/{id}/invoice | Admin |
| GET | /api/sales-orders/invoices | Admin |
| GET/POST | /api/inventory/ledger | Admin, Production |
| POST | /api/inventory/purchase | Admin, Production |
| POST | /api/inventory/adjustment | Admin |
| GET | /api/reports/dashboard | Admin |
| GET | /api/reports/sales-orders/csv | Admin |
| GET | /api/reports/inventory/csv | Admin |
| GET | /api/reports/production/variance/csv | Admin |
| GET/POST | /api/users | Admin |

---

## Project Structure
```
NIT_Order_Audit_System/
├── backend/
│   ├── pom.xml
│   └── src/main/java/com/nitaudit/
│       ├── config/         # SecurityConfig, DataSeeder
│       ├── controller/     # 10 REST controllers
│       ├── dto/            # Request/Response DTOs
│       ├── model/          # 10 JPA entities
│       ├── repository/     # 10 Spring Data repos
│       ├── security/       # JWT utils + filter
│       └── service/        # ProductionService, SalesOrderService
├── frontend/
│   └── src/
│       ├── pages/          # 11 page components
│       ├── App.jsx         # Router
│       ├── Layout.jsx      # Nav + role-based menu
│       ├── AuthContext.jsx # JWT auth state
│       └── api.js          # Axios instance
└── db_setup.sql
```
