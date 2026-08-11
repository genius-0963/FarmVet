# FarmVet Connect

**A comprehensive agricultural management platform connecting farmers with veterinarians for better crop and livestock management.**

![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)
![Java](https://img.shields.io/badge/Java-17-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.3-green.svg)
![React](https://img.shields.io/badge/React-18.3.1-blue.svg)

---

## 📋 Table of Contents

- [Project Overview](#project-overview)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [System Architecture](#system-architecture)
- [Project Structure](#project-structure)
- [Backend Architecture](#backend-architecture)
- [Frontend Architecture](#frontend-architecture)
- [Database Schema](#database-schema)
- [API Endpoints](#api-endpoints)
- [Installation & Setup](#installation--setup)
- [Environment Variables](#environment-variables)
- [Running the Application](#running-the-application)
- [Deployment](#deployment)
- [Contributing](#contributing)
- [License](#license)

---

## 🌱 Project Overview

FarmVet Connect is a full-stack web application designed to streamline agricultural management by providing farmers with tools to manage their crops, track pesticide usage, and connect with veterinarians for livestock health management. The platform features role-based access control, ensuring that farmers, veterinarians, and administrators have appropriate access to relevant features.

**Key Objectives:**
- Simplify crop management and tracking
- Monitor pesticide usage and disease control
- Facilitate farmer-veterinarian connections
- Provide real-time data management
- Ensure secure authentication and authorization

---

## ✨ Features

### User Management
- **User Registration**: Multi-role registration (Farmer, Veterinarian, Admin)
- **Secure Authentication**: BCrypt password encryption
- **Role-Based Access Control**: Different features based on user roles
- **Session Management**: Persistent login via localStorage

### Crop Management
- **Add/Edit/Delete Crops**: Full CRUD operations
- **Farmer-Specific Views**: Farmers see only their crops
- **Detailed Crop Information**: Name, acres, location, soil type, growing season
- **Manager Assignment**: Track crop managers and contact information

### Pesticide Management
- **Disease Tracking**: Monitor crop diseases
- **Pesticide Application**: Track pesticide usage and quantities
- **Status Management**: Update pesticide application status
- **Farmer-Specific Records**: View pesticides by farmer

### Veterinarian Management
- **Veterinarian Directory**: Browse available veterinarians
- **Detailed Profiles**: Experience, specialization, contact info
- **CRUD Operations**: Full management of veterinarian records
- **Search & Filter**: Find veterinarians by specialty

---

## 🛠 Technology Stack

### Backend
- **Framework**: Spring Boot 3.4.3
- **Language**: Java 17
- **Build Tool**: Maven
- **Database**: H2 (in-memory) / MySQL (production)
- **ORM**: Spring Data JPA / Hibernate
- **Security**: Spring Security with BCrypt
- **API Documentation**: RESTful APIs

### Frontend
- **Framework**: React 18.3.1
- **Routing**: React Router DOM 6.17.0
- **HTTP Client**: Axios 1.5.0
- **State Management**: React Context API
- **Styling**: CSS3
- **Build Tool**: Create React App

### Development Tools
- **Spring Boot DevTools**: Hot reload for backend
- **Git**: Version control
- **HikariCP**: Database connection pooling

---

## 🏗 System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     FarmVet Connect                         │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌─────────────────┐         ┌─────────────────┐           │
│  │   React Frontend │         │  Spring Backend  │           │
│  │                 │         │                 │           │
│  │  - User Auth    │◄────────┤  - REST APIs     │           │
│  │  - Crop Mgmt    │  HTTP   │  - Security     │           │
│  │  - Pest Mgmt    │         │  - Business     │           │
│  │  - Vet Directory│         │    Logic        │           │
│  └─────────────────┘         └────────┬────────┘           │
│                                       │                     │
│                              ┌────────▼────────┐            │
│                              │  H2 / MySQL DB   │            │
│                              │                 │            │
│                              │  - Users        │            │
│                              │  - Crops        │            │
│                              │  - Pesticides   │            │
│                              │  - Vets         │            │
│                              └─────────────────┘            │
│                                                               │
└─────────────────────────────────────────────────────────────┘
```

### Architecture Pattern
The application follows a **Model-View-Controller (MVC)** architecture pattern:

- **Model**: JPA Entities representing database tables
- **View**: React components rendering user interfaces
- **Controller**: Spring REST controllers handling HTTP requests
- **Service Layer**: Business logic separation
- **Repository Layer**: Data access abstraction

---

## 📁 Project Structure

```
farmvet-backend/
│
├── frontend/                          # React Frontend Application
│   ├── public/
│   ├── src/
│   │   ├── components/
│   │   │   └── ProtectedRoute.js     # Route protection component
│   │   ├── context/
│   │   │   └── AuthContext.js        # Authentication context
│   │   ├── pages/
│   │   │   ├── Home.js              # Landing page
│   │   │   ├── Login.js             # Login page
│   │   │   ├── Register.js          # Registration page
│   │   │   ├── Crops.js             # Crop management
│   │   │   ├── Pesticides.js        # Pesticide management
│   │   │   └── Veterinarians.js     # Veterinarian directory
│   │   ├── App.js                   # Main app component
│   │   ├── index.js                 # Entry point
│   │   └── App.css                  # Global styles
│   ├── .env                         # Environment variables
│   └── package.json                 # Frontend dependencies
│
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── example/
│       │           └── demo/
│       │               ├── DemoApplication.java      # Main entry point
│       │               ├── controller/              # REST Controllers
│       │               │   ├── AuthController.java
│       │               │   ├── CropsController.java
│       │               │   ├── HomeController.java
│       │               │   ├── PesticideController.java
│       │               │   └── VeterinarianController.java
│       │               ├── model/                   # JPA Entities
│       │               │   ├── PrimaryUser.java
│       │               │   ├── Crops.java
│       │               │   ├── Pesticides.java
│       │               │   └── Veterinarian.java
│       │               ├── repository/               # Data Repositories
│       │               │   ├── PrimaryUserRepository.java
│       │               │   ├── CropsRepository.java
│       │               │   ├── PesticideRepository.java
│       │               │   └── VeterinarianRepository.java
│       │               ├── service/                  # Business Logic
│       │               │   ├── PrimaryUserService.java
│       │               │   ├── CropsService.java
│       │               │   ├── PesticideService.java
│       │               │   └── VeterinarianService.java
│       │               └── security/                # Security Config
│       │                   └── SecurityConfig.java
│       └── resources/
│           └── application.properties                # Application config
│
├── pom.xml                            # Maven dependencies
├── mvnw                               # Maven wrapper
├── .gitignore                         # Git ignore rules
└── README.md                          # This file
```

---

## 🔧 Backend Architecture

### Layer Structure

#### 1. **Controller Layer**
Handles HTTP requests and responses, acting as the entry point for API calls.

**Controllers:**
- `AuthController`: User registration and login endpoints
- `CropsController`: Crop CRUD operations
- `PesticideController`: Pesticide management endpoints
- `VeterinarianController`: Veterinarian directory operations
- `HomeController`: Health check and home endpoints

#### 2. **Service Layer**
Contains business logic and acts as an intermediary between controllers and repositories.

**Services:**
- `PrimaryUserService`: User registration and authentication logic
- `CropsService`: Crop management business logic
- `PesticideService`: Pesticide tracking logic
- `VeterinarianService`: Veterinarian management logic

#### 3. **Repository Layer**
Data access layer using Spring Data JPA for database operations.

**Repositories:**
- `PrimaryUserRepository`: User data access with custom queries
- `CropsRepository`: Crop data access with farmer-specific queries
- `PesticideRepository`: Pesticide data access
- `VeterinarianRepository`: Veterinarian data access

#### 4. **Model Layer**
JPA entities representing database tables with proper annotations.

**Entities:**
- `PrimaryUser`: User authentication and profile data
- `Crops`: Crop information and management data
- `Pesticides`: Pesticide usage and disease tracking
- `Veterinarian`: Veterinarian profiles and contact information

#### 5. **Security Layer**
Spring Security configuration for authentication and authorization.

**Components:**
- `SecurityConfig`: CORS configuration, password encoding, security filter chain
- BCrypt password encryption
- Role-based access control (RBAC)

### Key Design Patterns

- **Dependency Injection**: Using Spring's `@Autowired` annotation
- **Repository Pattern**: Abstract data access with Spring Data JPA
- **Service Layer Pattern**: Business logic separation
- **DTO Pattern**: Data transfer objects for API communication
- **Builder Pattern**: Optional entity construction

---

## 🎨 Frontend Architecture

### Component Structure

#### 1. **Main Components**
- `App.js`: Root component with routing configuration
- `App.css`: Global styling and theme

#### 2. **Page Components**
- `Home.js`: Landing page with navigation
- `Login.js`: User authentication form
- `Register.js`: User registration form with role selection
- `Crops.js`: Crop management interface
- `Pesticides.js`: Pesticide tracking interface
- `Veterinarians.js`: Veterinarian directory

#### 3. **Shared Components**
- `ProtectedRoute.js`: Route protection wrapper for authenticated routes

#### 4. **Context Providers**
- `AuthContext.js`: Global authentication state management

### State Management

- **React Context API**: Used for authentication state
- **Local Component State**: Form data, loading states, UI toggles
- **localStorage**: Persistent user session storage

### Routing

- **React Router DOM**: Client-side routing
- **Protected Routes**: Authentication-gated pages
- **Dynamic Navigation**: Role-based menu rendering

### API Communication

- **Axios**: HTTP client for REST API calls
- **Base URL Configuration**: Environment variable for API endpoint
- **Error Handling**: Try-catch blocks with user feedback

---

## 🗄 Database Schema

### Entity Relationship Diagram

```
┌─────────────────┐
│  primaryusers   │
├─────────────────┤
│ id (PK)         │
│ username        │
│ email           │
│ phone           │
│ password        │
│ role            │
└─────────────────┘
         │
         │ 1:N
         │
┌────────▼────────┐
│     crops       │
├─────────────────┤
│ id (PK)         │
│ farmerId (FK)   │
│ cropName        │
│ acres           │
│ location        │
│ soilType        │
│ startMonth      │
│ endMonth        │
│ manager         │
│ contact         │
└─────────────────┘
         │
         │ 1:N
         │
┌────────▼────────┐
│  pesticides     │
├─────────────────┤
│ id (PK)         │
│ farmerId (FK)   │
│ cropName        │
│ acres           │
│ diseaseName     │
│ pesticideName   │
│ pesticideStatus │
│ pesticideQty    │
└─────────────────┘

┌─────────────────┐
│  veterinarian   │
├─────────────────┤
│ id (PK)         │
│ username        │
│ position        │
│ experience      │
│ about           │
│ speciality      │
│ email           │
│ phone           │
│ password        │
└─────────────────┘
```

### Table Descriptions

#### primaryusers
Stores user authentication and profile information.
- **id**: Auto-generated primary key
- **username**: Unique username for login
- **email**: User email address
- **phone**: Contact phone number
- **password**: BCrypt encrypted password
- **role**: User role (FARMER, VETERINARIAN, ADMIN)

#### crops
Stores crop management information linked to farmers.
- **id**: Auto-generated primary key
- **farmerId**: Foreign key to primaryusers
- **cropName**: Name of the crop
- **acres**: Land area in acres
- **location**: Geographic location
- **soilType**: Soil classification
- **startMonth**: Planting month
- **endMonth**: Harvest month
- **manager**: Assigned manager name
- **contact**: Manager contact information

#### pesticides
Tracks pesticide usage and disease management.
- **id**: Auto-generated primary key
- **farmerId**: Foreign key to primaryusers
- **cropName**: Associated crop
- **acres**: Affected area
- **diseaseName**: Identified disease
- **pesticideName**: Pesticide used
- **pesticideStatus**: Application status
- **pesticideQuantity**: Amount used

#### veterinarian
Stores veterinarian profiles and contact information.
- **id**: Auto-generated primary key
- **username**: Veterinarian name
- **position**: Job title
- **experience**: Years of experience
- **about**: Professional bio
- **speciality**: Area of specialization
- **email**: Contact email
- **phone**: Contact phone
- **password**: Account password

---

## 🔌 API Endpoints

### Authentication Endpoints

#### Register User
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "string",
  "email": "string",
  "phone": "string",
  "password": "string",
  "role": "FARMER|VETERINARIAN|ADMIN"
}
```

#### Login User
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "string",
  "password": "string"
}
```

### Crop Endpoints

#### Get All Crops
```http
GET /api/crops
```

#### Get Crops by Farmer
```http
GET /api/crops/farmer/{farmerId}
```

#### Get Crop by ID
```http
GET /api/crops/{id}
```

#### Add Crop
```http
POST /api/crops
Content-Type: application/json

{
  "farmerId": "long",
  "cropName": "string",
  "acres": "string",
  "location": "string",
  "soilType": "string",
  "startMonth": "string",
  "endMonth": "string",
  "manager": "string",
  "contact": "string"
}
```

#### Update Crop
```http
PUT /api/crops/{id}
Content-Type: application/json

{
  "farmerId": "long",
  "cropName": "string",
  "acres": "string",
  "location": "string",
  "soilType": "string",
  "startMonth": "string",
  "endMonth": "string",
  "manager": "string",
  "contact": "string"
}
```

#### Delete Crop
```http
DELETE /api/crops/{id}
```

### Pesticide Endpoints

#### Get All Pesticides
```http
GET /api/pesticides
```

#### Get Pesticides by Farmer
```http
GET /api/pesticides/farmer/{farmerId}
```

#### Add Pesticide
```http
POST /api/pesticides
Content-Type: application/json

{
  "farmerId": "long",
  "cropName": "string",
  "acres": "string",
  "diseaseName": "string",
  "pesticideName": "string",
  "pesticideStatus": "string",
  "pesticideQuantity": "string"
}
```

#### Update Pesticide Status
```http
PATCH /api/pesticides/{id}/status
Content-Type: application/json

"status string"
```

#### Delete Pesticide
```http
DELETE /api/pesticides/{id}
```

### Veterinarian Endpoints

#### Get All Veterinarians
```http
GET /api/veterinarians
```

#### Get Veterinarian by ID
```http
GET /api/veterinarians/{id}
```

#### Add Veterinarian
```http
POST /api/veterinarians
Content-Type: application/json

{
  "username": "string",
  "position": "string",
  "experience": "string",
  "about": "string",
  "speciality": "string",
  "email": "string",
  "phone": "string",
  "password": "string"
}
```

#### Update Veterinarian
```http
PUT /api/veterinarians/{id}
Content-Type: application/json

{
  "username": "string",
  "position": "string",
  "experience": "string",
  "about": "string",
  "speciality": "string",
  "email": "string",
  "phone": "string",
  "password": "string"
}
```

#### Delete Veterinarian
```http
DELETE /api/veterinarians/{id}
```

---

## 🚀 Installation & Setup

### Prerequisites

- **Java 17** or higher
- **Maven 3.6+** (or use included mvnw wrapper)
- **Node.js 16+** and npm
- **MySQL** (optional, for production)

### Backend Setup

1. **Clone the repository**
```bash
git clone https://github.com/github-0963/farmvet-backend.git
cd farmvet-backend
```

2. **Configure Database**
The application uses H2 in-memory database by default. For MySQL, update `application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/farmvet
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
```

3. **Build the project**
```bash
./mvnw clean install
```

4. **Run the backend**
```bash
./mvnw spring-boot:run
```

The backend will start on `http://localhost:8082`

### Frontend Setup

1. **Navigate to frontend directory**
```bash
cd frontend
```

2. **Install dependencies**
```bash
npm install
```

3. **Configure environment variables**
Create a `.env` file in the frontend directory:

```env
REACT_APP_API_BASE_URL=http://localhost:8082/api
```

4. **Start the development server**
```bash
npm start
```

The frontend will start on `http://localhost:3000`

---

## 🔐 Environment Variables

### Backend (application.properties)

```properties
# Database Configuration
DATABASE_URL=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
DATABASE_USER=sa
DATABASE_PASSWORD=
DATABASE_DRIVER=org.h2.Driver
DATABASE_PLATFORM=org.hibernate.dialect.H2Dialect

# Server Configuration
server.port=8082

# Logging
logging.level.org.springframework=DEBUG
logging.level.com.zaxxer.hikari=DEBUG
logging.level.org.hibernate=DEBUG

# Connection Pool
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.max-lifetime=1800000
spring.datasource.hikari.connection-timeout=20000
```

### Frontend (.env)

```env
REACT_APP_API_BASE_URL=http://localhost:8082/api
```

---

## ▶️ Running the Application

### Development Mode

**Terminal 1 - Backend:**
```bash
./mvnw spring-boot:run
```

**Terminal 2 - Frontend:**
```bash
cd frontend
npm start
```

### Production Mode

**Build Backend:**
```bash
./mvnw clean package
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

**Build Frontend:**
```bash
cd frontend
npm run build
```

The production build will be in the `frontend/build` directory.

---

## 🌐 Deployment

### Backend Deployment

The backend can be deployed to:
- **Heroku**: Using the Heroku Maven plugin
- **AWS**: Deploy JAR to EC2 or Elastic Beanstalk
- **DigitalOcean**: Deploy to App Platform or Droplet
- **Azure**: Deploy to Azure App Service

**Environment Variables for Production:**
- Set `DATABASE_URL` to production database
- Configure CORS for production domain
- Set appropriate logging levels

### Frontend Deployment

The frontend can be deployed to:
- **Vercel**: Connect GitHub repository for auto-deployment
- **Netlify**: Drag and drop the build folder
- **AWS S3**: Host static site with CloudFront
- **GitHub Pages**: Deploy from gh-pages branch

**Current Deployment:**
- Frontend: http://farmvetconnect.vercel.app
- Backend: Configure backend URL in production environment

---

## 👥 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Guidelines

- Follow existing code style and conventions
- Write meaningful commit messages
- Test your changes thoroughly
- Update documentation as needed
- Ensure all tests pass before submitting

---

## 📝 License

This project is licensed under the MIT License.

---

## 👤 Author

**GitHub**: [@github-0963](https://github.com/github-0963)

---

## 🙏 Acknowledgments

- Spring Boot team for the amazing framework
- React community for excellent documentation
- Agricultural community for inspiration

---

## 📞 Support

For support, please open an issue in the GitHub repository or contact the maintainer.

---

**Built with ❤️ for the agricultural community**
