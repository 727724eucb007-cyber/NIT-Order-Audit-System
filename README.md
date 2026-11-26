# NIT Trichy CDI — Order-to-Audit System

A full-stack MVP for a small food manufacturer that manages the complete order-to-audit workflow, including inventory, production, sales orders, GST invoicing, and reporting.

## Tech Stack

- **Frontend:** React 18, Vite, Tailwind CSS, React Router, Axios
- **Backend:** Java 17, Spring Boot 3.2.5, Spring Security (JWT), JPA/Hibernate
- **Database:** MySQL 8

---

## Prerequisites

| Tool | Version |
|---|---|
| Java | 17+ |
| Maven | 3.8+ |
| Node.js | 18+ |
| MySQL | 8.0+ |

---

## How to Run

### 1. MySQL Setup

Start MySQL and create the database:

```sql
CREATE DATABASE IF NOT EXISTS nit_audit_db;
```

Alternatively, execute the provided `db_setup.sql` file.

### 2. Configure Environment Variables

Open PowerShell in the project root and set the database password and JWT secret:

```powershell
$env:DB_PASSWORD="your_mysql_password"
$env:JWT_SECRET = [Convert]::ToBase64String((1..48 | ForEach-Object { Get-Random -Maximum 256 }))
```

Replace `your_mysql_password` with your actual MySQL password.

Generate a fresh JWT secret using the command above. Never publish actual passwords or JWT secrets.

### 3. Start the Backend

Open a terminal in the project root:

```powershell
cd backend
mvn spring-boot:run
```

Backend URL: http://localhost:8080

### 4. Start the Frontend

Open a second terminal in the project root:

```powershell
cd frontend
npm install
npm run dev
```

Frontend URL: http://localhost:3000

Keep both terminals running while using the application. Set the environment variables again whenever you start a new backend terminal.

---

## Login Credentials (Benchmark Data)

| Role | Username | Password |
|---|---|---|
| Admin | admin | admin123 |
| Salesman | salesman1 | sales123 |
| Salesman | salesman2 | sales123 |
| Production | production1 | prod123 |

---

## Features by Role

### Admin

- Dashboard with operational KPIs
- Product management (CRUD and GST rates)
- Customer management
- Raw material management
- Bill of Materials (BOM) management
- Production order management and variance reports
- Sales order approval and rejection
- GST invoice generation and print view
- Inventory ledger with purchase and adjustment entries
- CSV report downloads for sales, inventory, and production variance
- User management, including activation and deactivation

### Salesman

- Create and view sales orders
- Customer management

### Production

- Create and manage production orders
- Record actual material consumption against planned BOM quantities
- Automatic raw material stock deduction on production completion
- Inventory ledger and purchase entries

---

## Benchmark Data — Murukku 200g

### Product

| SKU | Product | Selling Price | GST |
|---|---|---:|---:|
| MUR-200 | Murukku 200g | ₹50 | 5% |

### Bill of Materials (Per Packet)

| Raw Material | Quantity |
|---|---:|
| Flour | 0.2 kg |
| Cooking Oil | 0.05 L |
| Salt/Spice | 0.006 kg |
| Pouch | 1 piece |
| Carton | 0.05 piece |

### Production Benchmark

- Planned production: 200 packets
- Actual production: 188 packets
- Good packets: 188
- Damaged pouches: 6
- Production yield variance: -6%
- Oil consumption variance: +5%

### Sales Benchmark

- Retailer A (Tamil Nadu): 150 packets at ₹50 each
- Taxable amount: ₹7,500
- GST at 5%: ₹375
- Invoice total: ₹7,875
- Payment recording: Not implemented in the current benchmark workflow

### Stock Shortage Validation

- Finished goods available after Retailer A dispatch: 88 packets
- Distributor B order: 120 packets
- Stock shortage detected: 32 packets
- The order remains pending when sufficient finished goods are unavailable.

### Customers

- Retailer A — Tamil Nadu
- Distributor B — Karnataka

### Raw Materials and Opening Stock

| Raw Material | Opening Stock |
|---|---:|
| Flour | 100 kg |
| Cooking Oil | 30 L |
| Salt/Spice | 5 kg |
| Pouch | 500 pieces |
| Carton | 30 pieces |

### Purchase Benchmark

- Flour purchased: 60 kg
- Purchase rate: ₹45 per kg
- Total purchase value: ₹2,700

### Inventory Benchmark

| Raw Material | Expected Closing Stock |
|---|---:|
| Flour | 120 kg |
| Cooking Oil | 19.5 L |
| Salt/Spice | 3.8 kg |
| Pouch | 306 pieces |
| Carton | 21 pieces |

**Inventory note:** The expected flour closing balance is 120 kg. The current inventory ledger requires reconciliation of the 100 kg opening stock entry before this balance can be considered verified.

### Finished Goods

- Opening stock: 50 packets
- Production completed: 188 packets
- Retailer A dispatch: 150 packets
- Closing stock: 88 packets

---

## API Endpoints Summary

| Method | Endpoint | Access |
|---|---|---|
| POST | `/api/auth/login` | Public |
| GET/POST/PUT/DELETE | `/api/products` | Admin |
| GET/POST/PUT/DELETE | `/api/customers` | Admin, Salesman |
| GET/POST/PUT | `/api/raw-materials` | Admin, Production |
| GET/POST | `/api/bom/product/{id}` | Admin |
| GET/POST/PUT | `/api/production` | Admin, Production |
| GET/POST | `/api/sales-orders` | Admin, Salesman |
| PUT | `/api/sales-orders/{id}/approve` | Admin |
| PUT | `/api/sales-orders/{id}/reject` | Admin |
| POST | `/api/sales-orders/{id}/invoice` | Admin |
| GET | `/api/sales-orders/invoices` | Admin |
| GET/POST | `/api/inventory/ledger` | Admin, Production |
| POST | `/api/inventory/purchase` | Admin, Production |
| POST | `/api/inventory/adjustment` | Admin |
| GET | `/api/reports/dashboard` | Admin |
| GET | `/api/reports/sales-orders/csv` | Admin |
| GET | `/api/reports/inventory/csv` | Admin |
| GET | `/api/reports/production/variance/csv` | Admin |
| GET/POST | `/api/users` | Admin |

---

## Project Structure

```text
NIT_Order_Audit_System/
├── backend/
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/nitaudit/
│       │   │   ├── config/
│       │   │   ├── controller/
│       │   │   ├── dto/
│       │   │   ├── model/
│       │   │   ├── repository/
│       │   │   ├── security/
│       │   │   └── service/
│       │   └── resources/
│       └── test/
├── frontend/
│   └── src/
│       ├── pages/
│       ├── App.jsx
│       ├── Layout.jsx
│       ├── AuthContext.jsx
│       └── api.js
├── db_setup.sql
├── .gitignore
└── README.md
```

---

## Security Notes

- Database passwords and JWT secrets are supplied through environment variables.
- Do not commit `.env` files, credentials, or generated tokens.
- The listed login credentials are benchmark/demo accounts. Change them before any production deployment.

---

## Project Status

This project is an MVP developed for the NIT Trichy CDI internship problem statement. Some benchmark items, including payment recording and flour opening-stock ledger reconciliation, remain incomplete and are disclosed above.
