# 🏦 ATM Management System

![Java](https://img.shields.io/badge/Java-Programming-red)
![Swing](https://img.shields.io/badge/Java-Swing-blue)
![Database](https://img.shields.io/badge/MySQL-Database-orange)
![JDBC](https://img.shields.io/badge/JDBC-Connector-green)
![Status](https://img.shields.io/badge/Project-Completed-success)

A **desktop-based ATM Management System** built using **Java, Swing GUI,
JDBC, and MySQL**. The project simulates core ATM operations such as
**user authentication, balance inquiry, withdrawal, deposit, and
transaction history**.

------------------------------------------------------------------------

## 📌 Features

✔ Secure **User Login System**\
✔ **Balance Inquiry**\
✔ **Deposit Money**\
✔ **Withdraw Cash**\
✔ **Transaction History**\
✔ **Logout System**\
✔ Interactive **GUI using Java Swing**

------------------------------------------------------------------------

## 🛠️ Technologies Used

  Technology   Purpose
  ------------ ---------------------------
  Java         Core programming language
  Swing        Graphical User Interface
  JDBC         Database connectivity
  MySQL        Database storage

------------------------------------------------------------------------

## 🗄️ Database Design

### Users Table

  Column     Type      Description
  ---------- --------- -------------------------
  id         INT       Primary Key
  username   VARCHAR   User login name
  password   VARCHAR   User password
  balance    DOUBLE    Current account balance

### Transactions Table

  Column    Type        Description
  --------- ----------- ----------------------
  id        INT         Primary Key
  user_id   INT         Linked user
  type      VARCHAR     Deposit / Withdrawal
  amount    DOUBLE      Transaction amount
  date      TIMESTAMP   Transaction time

------------------------------------------------------------------------

## ⚙️ Installation & Setup

### 1️⃣ Clone the Repository

``` bash
git clone https://github.com/adityayadav-codes/atm-management-system.git
```

### 2️⃣ Create Database

``` sql
CREATE DATABASE atm_system;
```

### 3️⃣ Import Tables

Run the SQL file provided in the project folder.

### 4️⃣ Add MySQL Connector

Download MySQL JDBC Driver and add it to your project classpath.

https://dev.mysql.com/downloads/connector/j/

### 5️⃣ Run the Project

Compile and run the **Main.java** file.

------------------------------------------------------------------------

## 📂 Project Structure

    ATM-Management-System
    │
    ├── src
    │   ├── Login.java
    │   ├── Dashboard.java
    │   ├── Deposit.java
    │   ├── Withdraw.java
    │   ├── Balance.java
    │   └── DBConnection.java
    │
    ├── database
    │   └── atm_system.sql
    │
    ├── screenshots
    │   ├── login.png
    │   ├── dashboard.png
    │   └── transaction.png
    │
    └── README.md

------------------------------------------------------------------------

## 🎯 Learning Outcomes

-   Java **Swing GUI development**
-   **JDBC database integration**
-   **MySQL database design**
-   **Object-Oriented Programming (OOP)**
-   Building **desktop banking applications**

------------------------------------------------------------------------

## 🚀 Future Improvements

-   Add **ATM card number generation**
-   Implement **PIN authentication**
-   Add **Admin panel**
-   Improve UI using **JavaFX**
-   Generate **transaction receipts**

------------------------------------------------------------------------

## 👨‍💻 Author

**Aditya Yadav**

GitHub: https://github.com/adityayadav-codes
