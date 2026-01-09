# Employee Management System

A comprehensive web application built with Java Spring Boot and Thymeleaf for managing employees, attendance, customer relationships, and sales.

## Features

### 1. Employee Management
- Add, edit, delete, and view employee information
- Track employee details: name, email, phone, department, position, hire date, salary, and status
- Search functionality to find employees quickly
- Employee status tracking (Active/Inactive)

### 2. Attendance Management
- Record employee check-in and check-out times
- Track attendance by date and employee
- Support for different attendance statuses (Present, Absent, Late, On Leave)
- Calculate working hours automatically
- Filter attendance records by employee and date

### 3. Customer Relationship Management (CRM)
- Manage customer information including company details and contact information
- Track customer status (Active, Inactive, Potential)
- Store customer address and industry information
- Search and filter customers
- Add notes for customer interactions

### 4. Sales Management
- Record sales transactions
- Link sales to employees and customers
- Track product/service sales with quantity and pricing
- Automatic total amount calculation
- Sales status tracking (Pending, Completed, Cancelled)
- Filter sales by employee, customer, and date range
- View sales reports

### 5. Dashboard
- Overview of key metrics
- Statistics for employees, attendance, customers, and sales
- Recent sales summary
- Quick access to all modules

## Technology Stack

- **Backend**: Java 17, Spring Boot 3.2.0
- **Frontend**: Thymeleaf, Bootstrap 5, HTML5, CSS3
- **Database**: H2 (Development), MySQL (Production ready)
- **Build Tool**: Maven
- **ORM**: Spring Data JPA / Hibernate

## Prerequisites

- Java JDK 17 or higher
- Maven 3.6+ (optional, can use Maven wrapper)
- MySQL (optional, for production)

## Setup Instructions

### 1. Clone or Download the Project

```bash
cd employee-management-system
```

### 2. Configure Database (Optional)

The application uses H2 in-memory database by default. To use MySQL:

1. Edit `src/main/resources/application.properties`
2. Uncomment MySQL configuration lines
3. Update database connection details:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/employee_db?createDatabaseIfNotExist=true
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```
4. Comment out H2 configuration lines

### 3. Build the Project

```bash
mvn clean install
```

### 4. Run the Application

```bash
mvn spring-boot:run
```

Or using Java:

```bash
java -jar target/employee-management-1.0.0.jar
```

### 5. Access the Application

Open your browser and navigate to:

```
http://localhost:8080
```

### 6. H2 Console (Development)

Access the H2 database console at:

```
http://localhost:8080/h2-console
```

- JDBC URL: `jdbc:h2:mem:employeedb`
- Username: `sa`
- Password: (leave empty)

## Project Structure

```
employee-management-system/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/employee/
│   │   │       ├── EmployeeManagementApplication.java
│   │   │       ├── controller/
│   │   │       │   ├── HomeController.java
│   │   │       │   ├── EmployeeController.java
│   │   │       │   ├── AttendanceController.java
│   │   │       │   ├── CustomerController.java
│   │   │       │   └── SaleController.java
│   │   │       ├── model/
│   │   │       │   ├── Employee.java
│   │   │       │   ├── Attendance.java
│   │   │       │   ├── Customer.java
│   │   │       │   └── Sale.java
│   │   │       ├── repository/
│   │   │       │   ├── EmployeeRepository.java
│   │   │       │   ├── AttendanceRepository.java
│   │   │       │   ├── CustomerRepository.java
│   │   │       │   └── SaleRepository.java
│   │   │       └── service/
│   │   │           ├── EmployeeService.java
│   │   │           ├── AttendanceService.java
│   │   │           ├── CustomerService.java
│   │   │           └── SaleService.java
│   │   └── resources/
│   │       ├── static/
│   │       │   └── css/
│   │       │       └── style.css
│   │       ├── templates/
│   │       │   ├── index.html
│   │       │   ├── employees/
│   │       │   ├── attendances/
│   │       │   ├── customers/
│   │       │   └── sales/
│   │       └── application.properties
│   └── test/
├── pom.xml
└── README.md
```

## Usage Guide

### Employee Management

1. **Add Employee**: Navigate to Employees → Add New Employee
2. **Edit Employee**: Click Edit button on employee list
3. **View Employee**: Click View button to see detailed information
4. **Delete Employee**: Click Delete button (with confirmation)
5. **Search**: Use the search box to find employees by name

### Attendance Management

1. **Add Attendance**: Navigate to Attendance → Add Attendance
2. **Filter**: Filter by employee or date using the filter form
3. **Check In/Out**: Use check-in and check-out functionality (to be implemented in future)
4. **View Records**: View all attendance records with check-in/out times

### Customer Management

1. **Add Customer**: Navigate to Customers → Add New Customer
2. **Edit Customer**: Click Edit button to update customer information
3. **View Customer**: Click View to see complete customer details
4. **Search**: Search customers by company name

### Sales Management

1. **Add Sale**: Navigate to Sales → Add New Sale
2. **Filter Sales**: Filter by employee, customer, or date range
3. **View Details**: Click View to see complete sale information
4. **Calculate Total**: Total amount is automatically calculated from quantity × unit price

## API Endpoints

### Employees
- `GET /employees` - List all employees
- `GET /employees/new` - Show employee form
- `POST /employees/save` - Save employee
- `GET /employees/edit/{id}` - Edit employee
- `GET /employees/view/{id}` - View employee
- `GET /employees/delete/{id}` - Delete employee

### Attendance
- `GET /attendances` - List all attendance records
- `GET /attendances/new` - Show attendance form
- `POST /attendances/save` - Save attendance
- `POST /attendances/checkin/{employeeId}` - Check in
- `POST /attendances/checkout/{employeeId}` - Check out

### Customers
- `GET /customers` - List all customers
- `GET /customers/new` - Show customer form
- `POST /customers/save` - Save customer
- `GET /customers/edit/{id}` - Edit customer
- `GET /customers/view/{id}` - View customer
- `GET /customers/delete/{id}` - Delete customer

### Sales
- `GET /sales` - List all sales
- `GET /sales/new` - Show sale form
- `POST /sales/save` - Save sale
- `GET /sales/edit/{id}` - Edit sale
- `GET /sales/view/{id}` - View sale
- `GET /sales/delete/{id}` - Delete sale

## Configuration

### Application Properties

Key configuration options in `application.properties`:

- `server.port`: Server port (default: 8080)
- `spring.datasource.*`: Database configuration
- `spring.jpa.hibernate.ddl-auto`: Database schema generation (update/create/none)
- `spring.h2.console.enabled`: Enable H2 console (default: true)

## Troubleshooting

### Port Already in Use

If port 8080 is already in use, change it in `application.properties`:

```properties
server.port=8081
```

### Database Connection Issues

- Ensure MySQL is running (if using MySQL)
- Check database credentials in `application.properties`
- Verify database exists or set `createDatabaseIfNotExist=true`

### Build Issues

- Ensure Java 17+ is installed: `java -version`
- Clean and rebuild: `mvn clean install`
- Check Maven version: `mvn -version`

## Future Enhancements

- User authentication and authorization
- Role-based access control
- Reporting and analytics
- Export functionality (PDF, Excel)
- Email notifications
- Advanced search and filters
- Dashboard charts and graphs
- Mobile responsive improvements
- REST API endpoints
- Integration with third-party services

## License

This project is open source and available for educational purposes.

## Support

For issues and questions, please check the code comments or refer to Spring Boot documentation.

## Author

Created as a comprehensive employee management solution using Spring Boot and Thymeleaf.

