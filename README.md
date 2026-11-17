# CSA09 Assignment 5 – Data Persistence Engineering

## Student Details
- **Name:** Ishwarya
- **Register Number:** 192472108
- **Course:** CSA09 – Programming in Java
- **Assignment:** Assignment 5
- **Java Version:** JDK 17
- **Database:** MySQL 8.0.46
- **JDBC Driver:** MySQL Connector/J 26.10.0

## Project Description
This assignment demonstrates Java file handling, buffered I/O, the DAO design pattern, object serialization, database transactions, input validation, JDBC benchmarking, and database migration.

## Assignment Tasks

| Task | Description |
|---|---|
| A1 | Design a File Format |
| A2 | Buffered I/O |
| A3 | DAO Design Pattern |
| A4 | Serialization |
| A5 | Transactions and SQL Injection |
| B1 | Pass Store Implementation |
| B2 | Adversarial Inputs |
| B3 | JDBC Benchmark |
| B4 | Database Migration |

## Technologies Used
- Java
- Java I/O Streams
- JDBC
- MySQL
- CSV File Handling
- Object Serialization

## How to Compile and Run

For programs that do not require JDBC:

```cmd
javac A1FileFormat.java
java A1FileFormat
```

For JDBC programs, place the MySQL Connector/J JAR in the project folder and use:

```cmd
javac -cp ".;mysql-connector-j-26.10.0.jar" FileName.java
java -cp ".;mysql-connector-j-26.10.0.jar" ClassName
```

Replace `FileName` and `ClassName` with the appropriate source file and Java class names.

## Database Configuration
Create the local MySQL database and configure the connection details before running JDBC programs.

**Security:** Do not publish database passwords, API keys, or other credentials in this repository. Configure credentials locally.

## Execution Status
- A1 – Executed successfully.
- A2 – Executed successfully.
- A3 – Executed successfully.
- A4 – Executed successfully.
- A5 – Executed successfully.
- B1 – Implementation prepared; final verification pending.
- B2 – Executed successfully.
- B3 – Benchmark execution and measurements pending completion.
- B4 – Executed successfully.

## Repository
GitHub: https://github.com/Ishwaryachagam/CSA09-192472108-Assignment5
