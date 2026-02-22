# Cake Shop System

A desktop point-of-sale and shop management application for a cake shop, built with **JavaFX** and **MySQL**. It supports customer-facing ordering (products, cart, custom/DIY cakes, bookings), member registration, staff and admin dashboards, revenue and sales reporting, and email-based password recovery.

---

## Overview

- **Purpose:** Full-featured cake shop management: login, product catalog, shopping cart, custom cake orders, table/DIY bookings, payments, receipts, member management, and admin analytics.
- **Entry point:** Login screen; after authentication the user sees either the **Admin** view (dashboard, users, products, revenue, staff performance) or the **Cashier** view (product catalog, cart, orders).
- **Architecture:** JavaFX FXML views with controller classes, MySQL persistence via DAOs, in-memory caches for reference data, and a central `SessionManager` for user state and UI references (stage, snackbar container).

---

## Tech Stack

| Layer | Technology |
|-------|------------|
| **Language** | Java 24 |
| **UI** | JavaFX 24 (FXML, CSS) |
| **Database** | MySQL (JDBC via `mysql-connector-j` 9.3.0) |
| **Build** | Maven |
| **Icons** | Ikonli (FontAwesome 6), FontAwesomeFX |
| **Auth** | BCrypt (jbcrypt 0.4) |
| **Email** | Jakarta Mail 2.0 (e.g. Gmail SMTP) |
| **Config** | dotenv-java (`.env` for DB and Gmail) |
| **Misc** | ControlsFX, Gson, JavaFX Swing (for printing) |

---

## Project Structure

```
Cake_Shop_System/
├── pom.xml                          # Maven build, Java 24, JavaFX plugin
├── .env                             # MYSQL_*, GMAIL_*, APP_PASSWORD (not committed)
├── Launcher.java                    # main() → Application.launch(CakeShopSystemApplication.class)
├── CakeShopSystemApplication.java   # JavaFX start(): login scene, CSS, SnackBar container
├── MainApp.java                     # Alternate entry (direct to Dashboard; dev/debug)
│
├── src/main/java/com/cakeshopsystem/
│   ├── controllers/                 # FXML controllers
│   │   ├── MainController.java      # Main layout: nav, user menu, theme toggle, content area
│   │   ├── LoginFormController.java
│   │   ├── RegisterMemberController.java, ForgotPasswordController.java, ConfirmPasswordController.java
│   │   ├── ProductViewController.java, ProductCardController.java
│   │   ├── CartController.java, PaymentController.java, ReceiptController.java
│   │   ├── CustomOrderController.java, CustomOrderFormController.java, CustomOrderDetailController.java
│   │   ├── DiyOrderController.java, DiyOrderFormController.java
│   │   ├── BookingController.java, BookingCardController.java
│   │   ├── OptionsAndPricingController.java
│   │   ├── EditUserConfigController.java, EditPaymentConfigController.java
│   │   ├── AddUserController.java
│   │   └── admin/
│   │       ├── DashboardController.java, RevenueController.java, StaffPerformanceController.java
│   │       ├── UserViewController.java, UserCardController.java
│   │       ├── MemberViewController.java
│   │       ├── AdminProductController.java, AddProductController.java, EditProductController.java
│   │       ├── SalesProductController.java
│   │       └── AddUser.fxml related
│   │
│   ├── models/                      # Domain entities
│   │   ├── User.java, Role.java, Member.java
│   │   ├── Product.java, Category.java, Cake.java, Drink.java, Inventory.java
│   │   ├── CartItem.java, Order.java, OrderItem.java, Payment.java
│   │   ├── Booking.java, DiyCakeBooking.java, CustomCakeBooking.java
│   │   ├── CakeRecipe.java, CakeRecipeInstruction.java, CustomCake.java, PrebakedCake.java
│   │   ├── Size.java, Flavour.java, Topping.java, Ingredient.java
│   │   ├── ReceiptData.java, ProductSales.java, ActivityItem.java
│   │   ├── OTP.java, InventoryMovement.java
│   │   └── ...
│   │
│   └── utils/
│       ├── databaseconnection/DB.java        # MySQL connection (dotenv: MYSQL_*)
│       ├── dotenv/dotenv.java               # Loads .env (MySQL + Gmail)
│       ├── session/SessionManager.java      # Stage, user, isAdmin, snackbar, dark mode, discount rate
│       ├── ChangeScene.java                 # Scene switching, preserves SnackBar, maximized state
│       ├── MailHelper.java, GmailSMTP       # Email (e.g. OTP / forgot password)
│       ├── OTPCodeHasher.java, Validator.java, ImageHelper.java, EnumHelper.java
│       ├── dao/                             # Data access (25+ DAOs)
│       │   ├── UserDAO.java, RoleDAO.java, MemberDAO.java
│       │   ├── ProductDAO.java, CategoryDAO.java, CakeDAO.java, DrinkDAO.java
│       │   ├── OrderDAO.java, OrderItemDAO.java, PaymentDAO.java
│       │   ├── BookingDAO.java, DiyCakeBookingDAO.java, CustomCakeBookingDAO.java
│       │   ├── InventoryDAO.java, InventoryMovementDAO.java
│       │   ├── DashboardDAO.java, StaffPerformanceDAO.java
│       │   ├── OneTimePasswordDAO.java, CustomCakeQuotaDAO.java
│       │   ├── CakeRecipeDAO.java, CakeRecipeInstructionDAO.java
│       │   ├── SizeDAO.java, FlavourDAO.java, ToppingDAO.java, IngredientDAO.java
│       │   └── ...
│       ├── cache/                           # In-memory caches for dropdowns/lookups
│       │   ├── UserCache.java, RoleCache.java, MemberCache.java
│       │   ├── ProductCache.java, CategoryCache.java, CakeCache.java, DrinkCache.java
│       │   ├── OrderCache.java, OrderItemCache.java, PaymentCache.java
│       │   ├── BookingCache.java, DiyCakeBookingCache.java, CustomCakeBookingCache.java
│       │   ├── InventoryCache.java, InventoryMovementCache.java
│       │   ├── CakeRecipeCache.java, CakeRecipeInstructionCache.java
│       │   ├── SizeCache.java, FlavourCache.java, ToppingCache.java, IngredientCache.java
│       │   └── ...
│       ├── services/                        # Business logic
│       │   ├── CartService.java, OrderService.java
│       ├── components/                      # Reusable UI
│       │   ├── SnackBar.java, BreadcrumbBar.java, BreadcrumbManager
│       ├── chart/ChartColorFixer.java
│       ├── events/AppEvents.java
│       └── constants/OrderOption.java, SnackBarType
│
└── src/main/resources/
    ├── views/                        # FXML screens
    │   ├── LoginForm.fxml, ForgotPassword.fxml, ConfirmPassword.fxml
    │   ├── Main.fxml                  # Shell: sidebar + content (Dashboard or ProductView)
    │   ├── RegisterMember.fxml
    │   ├── ProductView.fxml, ProductCard.fxml, CakeCard_Order.fxml, CustomCakeCard.fxml
    │   ├── Cart.fxml, Payment.fxml, Receipt.fxml, ReceiptPrint.fxml
    │   ├── CustomOrder.fxml, CustomOrderForm.fxml, CustomOrderDetail.fxml
    │   ├── DiyOrder.fxml, DiyOrderForm.fxml
    │   ├── Booking.fxml, BookingCard.fxml
    │   ├── OptionsAndPricing.fxml
    │   ├── EditUserConfig.fxml, EditPaymentConfig.fxml
    │   └── admin/
    │       ├── Dashboard.fxml, Revenue.fxml, StaffPerformance.fxml
    │       ├── UserView.fxml, UserCard.fxml, MemberView.fxml
    │       ├── AdminProduct.fxml, AddProduct.fxml, EditProduct.fxml
    │       ├── SalesProduct.fxml, AddUser.fxml
    │       └── ...
    ├── assets/css/
    │   ├── style.css                 # Imports: base, variable, snack-bar, components, table, date-picker
    │   ├── variable.css               # .root / .root.dark theme variables
    │   ├── base.css, components.css, table.css, date-picker.css, snack-bar.css
    ├── images/                        # Product images, receipts output, etc.
    └── receipts/                      # Generated receipt files (if used)
```

---

## Features

### Authentication & Users

- **Login** with role-based redirect: Admin → Dashboard; Cashier → Product catalog.
- **Forgot password** via email (Gmail SMTP) and OTP; **Confirm password** for reset.
- **User config** (profile) and **payment config** edit screens.
- **Session:** `SessionManager` holds current `User`, `isAdmin`, stage, snackbar container, dark mode, and member discount rate (e.g. 50% for “expiring soon” products).

### Customer / Cashier Flow

- **Product catalog** (ProductView, product cards) for cakes and drinks.
- **Cart** with CartService; **Payment** and **Receipt** (including ReceiptPrint).
- **Custom cake orders:** CustomOrder, CustomOrderForm, CustomOrderDetail (sizes, flavours, toppings, quotas).
- **DIY orders:** DiyOrder, DiyOrderForm (bookings for DIY cake sessions).
- **Table booking:** Booking, BookingCard.
- **Options and pricing:** OptionsAndPricing view for configurable product options.

### Members

- **Member registration** (RegisterMember).
- **Member management** in admin (MemberView, MemberViewController).

### Admin

- **Dashboard:** Overview (DashboardController, DashboardDAO).
- **Users:** UserView, UserCard, AddUser; role-based access.
- **Products:** AdminProduct, AddProduct, EditProduct; categories, inventory, shelf life.
- **Sales & revenue:** SalesProduct, Revenue views and DAOs.
- **Staff performance:** StaffPerformance view and DAO.

### UI / UX

- **Theming:** Light/dark mode via `variable.css` (`.root` and `.root.dark`); theme toggle in MainController.
- **SnackBar** notifications (top-center) and **BreadcrumbBar** for navigation context.
- **Scene switching** via `ChangeScene.switchScene()`; keeps SnackBar layer and window maximized where applicable.

---

## Configuration

- **Database:** Create a `.env` in the project root (do not commit secrets):
  - `MYSQL_URL` — JDBC URL (e.g. `jdbc:mysql://localhost:3306/your_db`)
  - `MYSQL_USERNAME`
  - `MYSQL_PASSWORD`
- **Email (e.g. forgot password / OTP):**
  - `GMAIL_ACCOUNT`
  - `APP_PASSWORD` (app-specific password for Gmail)

`DB.java` and `dotenv.java` read these at runtime.

---

## Running the Application

- **Recommended:** `mvn clean javafx:run` (uses `CakeShopSystemApplication` as main class in `pom.xml`).
- **Alternative:** Run `Launcher` (or `CakeShopSystemApplication`) from the IDE; ensure VM options for JavaFX if needed.
- **Debug/quick admin:** `MainApp` starts directly on the admin Dashboard (no login).

---

## Summary

The **Cake Shop System** is a JavaFX + MySQL desktop app that covers login, roles, product catalog, cart, payments, receipts, custom/DIY cake orders, bookings, members, and admin dashboards (users, products, sales, revenue, staff performance). Styling is centralized in `variable.css` (and related CSS), with light/dark themes and a consistent set of DAOs, caches, and services used across the app.
