# 💧 Water Supply App

A complete Android application for managing water supply services with separate workflows for **Customers, Administrators, and Delivery Men**.

> 🚧 **Project Status:** Under Development  
> 📱 **Platform:** Android  
> 🔥 **Backend:** Firebase

---

## 📱 About the Project

The **Water Supply App** is designed to simplify the management of water delivery services.

Customers can place water orders, manage monthly packages, view order history and bills, while administrators can manage customers, orders, payments, monthly bills, notifications, and delivery assignments.

A separate Delivery Man workflow allows assigned delivery personnel to view their orders and mark them as delivered.

---

## ✨ Current Features

### 👤 Customer

- 🔐 Customer Registration & Login
- 🔑 Forgot Password
- 🏠 Customer Dashboard
- 💧 Place New Water Order
- 🧊 Normal & Cold Water Options
- 📦 Quantity Selection
- 💵 Cash & Online Payment Options
- 🕐 Delivery Time Selection
- 📋 Order History
- ❌ Cancel Pending Orders
- 📦 Monthly Water Package
- 📅 Monthly Delivery Calendar
- 🟢 Enable Delivery for Selected Days
- 🔴 Disable Delivery for Selected Days
- 🧾 Current Monthly Bill
- 📜 Monthly Bill History
- 🔔 Customer Notifications
- 👤 Customer Profile
- 🚪 Logout
- 🗑️ Delete Account

---

### 👨‍💼 Administrator

- 🔐 Admin Login
- 🔑 Forgot Password
- 🏠 Admin Dashboard
- 📦 New Orders Management
- 👥 Customer Management
- 📋 All Orders
- 🧾 Monthly Bills
- 💳 Payment Management
- 🔔 Admin Notifications
- 👤 Admin Profile
- 🚚 Delivery Man Assignment
- 🔄 Order Status Management

### Order Status Flow

```text
Pending
   ↓
Accepted
   ↓
Preparing
   ↓
Out for Delivery
   ↓
Delivered
```

Admin can also reject pending orders.

---

### 🚚 Delivery Man

- 🔐 Delivery Man Login
- 🏠 Delivery Man Dashboard
- 📦 View Assigned Orders
- 👤 Customer Details
- 📞 Customer Phone Number
- 📍 Customer Address
- 💧 Water Type & Quantity
- 💰 Order Amount
- 💳 Payment Status
- 🕐 Delivery Information
- ✅ Mark Order as Delivered
- 🔔 Customer Delivery Notification

---

### 🔔 Notifications

The application supports notification records for important events such as:

- 🆕 New Order
- ✅ Order Accepted
- ❌ Order Rejected
- 🔄 Order Preparing
- 🚚 Out for Delivery
- 📦 Order Delivered
- 💳 Payment Updates

---

### 📦 Monthly Package

Customers can subscribe to a monthly water delivery package.

Features include:

- 💧 Water type selection
- 🔢 Daily quantity
- 💰 Daily price calculation
- 🕐 Delivery time selection
- 📅 Delivery calendar
- 🟢 Delivery ON days
- 🔴 Delivery OFF days
- 🤖 Daily order generation
- 🧾 Monthly bill generation

### Monthly Package Flow

```text
Activate Package
       ↓
Select Delivery Days
       ↓
Daily Order Generation
       ↓
Order Delivery
       ↓
Monthly Bill
       ↓
Payment
```

---

## 💰 Water Pricing

| Water Type | Price |
|------------|------:|
| Normal Water | ₹25 |
| Cold Water | ₹30 |

---

## 🛠️ Technologies Used

- **Android Studio**
- **Java**
- **XML**
- **Firebase Authentication**
- **Firebase Firestore**
- **Gradle**
- **RecyclerView**
- **SQLite / Local Android Components**
- **Material Android UI Components**

---

## 🔥 Firebase Integration

The application uses Firebase for backend services.

### Firebase Authentication

Used for:

- Customer Login
- Admin Login
- Delivery Man Login
- Customer Registration
- Password Reset

### Cloud Firestore

Main collections include:

```text
users
orders
monthlyPackages
monthlyPackageSchedules
monthlyBills
notifications
```

---

## 📂 Project Structure

```text
WaterSupply/
│
├── app/
│   └── src/
│       └── main/
│           ├── java/
│           │   └── com/example/watersupply/
│           │
│           │       ├── MainActivity.java
│           │       ├── RegisterActivity.java
│           │
│           │       ├── CustomerDashboardActivity.java
│           │       ├── CustomerOrdersActivity.java
│           │       ├── CustomerProfileActivity.java
│           │       ├── CustomerNotificationsActivity.java
│           │       ├── CustomerCurrentBillActivity.java
│           │       └── CustomerBillHistoryActivity.java
│           │
│           │       ├── PlaceOrderActivity.java
│           │       ├── MonthlyPackageActivity.java
│           │       ├── MyMonthlyPackageActivity.java
│           │       ├── DeliveryCalendarActivity.java
│           │
│           │       ├── AdminLoginActivity.java
│           │       ├── AdminDashboardActivity.java
│           │       ├── AdminOrdersActivity.java
│           │       ├── AdminCustomersActivity.java
│           │       ├── AdminPaymentsActivity.java
│           │       ├── AdminMonthlyBillsActivity.java
│           │       ├── AdminNotificationsActivity.java
│           │       └── AdminProfileActivity.java
│           │
│           │       ├── NewOrdersActivity.java
│           │       ├── AssignDeliveryManActivity.java
│           │
│           │       ├── DeliveryManLoginActivity.java
│           │       ├── DeliveryManDashboardActivity.java
│           │       └── DeliveryManOrdersActivity.java
│           │
│           │       ├── NotificationHelper.java
│           │       ├── MonthlyOrderGenerator.java
│           │       └── MonthlyBillGenerator.java
│           │
│           └── res/
│               ├── layout/
│               ├── drawable/
│               ├── mipmap/
│               └── values/
│
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradlew
├── gradlew.bat
└── README.md
```

---

## 🔄 Application Workflow

```text
                    WATER SUPPLY APP
                           │
            ┌──────────────┼──────────────┐
            ↓              ↓              ↓
        CUSTOMER         ADMIN       DELIVERY MAN
            │              │              │
            ↓              ↓              ↓
       Place Order     Manage Orders   Assigned Orders
            │              │              │
            ↓              ↓              ↓
     Monthly Package   Assign Driver   Deliver Order
            │              │              │
            ↓              ↓              ↓
        View Bills     Track Status    Mark Delivered
            │              │              │
            └──────────────┼──────────────┘
                           ↓
                    Firebase Backend
```

---

## 📊 Order Management

Each order contains important information such as:

```text
Customer
Phone
Address
Water Type
Quantity
Price
Total Amount
Payment Method
Payment Status
Order Status
Order Type
Delivery Information
Timestamp
```

---

## 🔐 User Roles

The application separates functionality based on user roles.

```text
Customer
   │
   ├── Place Orders
   ├── Monthly Package
   ├── Bills
   └── Notifications

Admin
   │
   ├── Manage Customers
   ├── Manage Orders
   ├── Manage Payments
   ├── Manage Bills
   ├── Manage Notifications
   └── Assign Delivery Man

Delivery Man
   │
   ├── View Assigned Orders
   └── Mark Orders Delivered
```

---

## 🚀 Future Improvements

The following features may be added in future versions:

- 💳 Real Online Payment Gateway
- 🗺️ GPS / Map-based Location
- 📍 Live Delivery Tracking
- 🔔 Firebase Cloud Messaging Push Notifications
- 👨‍💼 Advanced Admin Reports
- 📊 Sales & Revenue Analytics
- 🚚 Delivery Man Availability Management
- 🌐 Web-based Admin Panel
- ☁️ Server-side Automated Monthly Order Generation
- 🔒 Improved Secure Credential Storage

---

## 👨‍💻 Developer

**Raj Sirsode**

---

## ⭐ Support

If you find this project useful or interesting, consider giving the repository a ⭐ on GitHub.

---
