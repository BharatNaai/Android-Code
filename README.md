# BharatNaai Android App

Welcome to the BharatNaai Android app! This document provides a comprehensive overview of the project, its architecture, and key features to help new developers get started.

## 1. Project Overview

BharatNaai is a mobile application designed to connect users with nearby salons and barbers. Users can search for salons, view their services and ratings, and book appointments. The app is built natively for Android using Kotlin and follows modern Android development best practices.

## 2. Features

- **User Authentication**: Secure registration, login, and session management.
- **Location-Based Search**: Find nearby salons using the device's location.
- **Salon Search & Filtering**: Search for salons and filter results by distance, rating, price, and services.
- **Salon Details**: View detailed information about a salon, including its services, barbers, and available slots.
- **Booking**: (In-progress) Book appointments with barbers.

## 3. Project Structure

The project follows the MVVM (Model-View-ViewModel) architecture pattern and is organized into the following key packages:

- **`data`**: Handles all data operations.
  - **`model`**: Contains Kotlin data classes that represent the app's data structures (e.g., `User`, `Salon`).
  - **`network`**: Manages network communication using Retrofit (`ApiService`, `ApiClient`).
  - **`repository`**: Acts as the single source of truth for data. Repositories like `AuthRepository` abstract the data source (network or local) from the ViewModels.
  - **`session`**: Includes `SessionManager` for handling user session data, such as access and refresh tokens.
- **`di`**: Manages dependency injection (not fully implemented yet, but planned).
- **`ui`**: Contains all UI-related components (Activities and Fragments).
  - **`auth`**: Holds all authentication-related screens (`LoginActivity`, `RegisterActivity`).
  - **`home`**: The main screen of the app after login.
  - **`search`**: The salon search and results screen.
  - **`profile`**: The user profile screen.
  - **`saloon_details`**: The screen for viewing salon details.
- **`utils`**: Contains helper classes and utility functions, such as `CommonMethod` for checking internet connectivity and `LocationHelper` for handling location services.

## 4. Authentication Flow

The authentication system is a critical part of the app. It ensures that user data is secure and that sessions are managed correctly. Here’s a step-by-step breakdown:

### a. Registration

1.  **UI (`RegisterActivity`)**: The user enters their details (name, email, password).
2.  **ViewModel (`RegisterViewModel`)**: The ViewModel validates the input and calls the `AuthRepository` to perform the registration.
3.  **Repository (`AuthRepository`)**: 
    - It first checks for an internet connection using `CommonMethod.isInternetAvailable()`.
    - If online, it calls the `registerUser` method in `ApiService`.
    - It returns an `ApiResult` (Success or Error) to the ViewModel.
4.  **API (`ApiService`)**: A `POST` request is sent to the `/auth/register` endpoint.

### b. Login

1.  **UI (`LoginActivity`)**: The user enters their email and password.
2.  **ViewModel (`LoginViewModel`)**: The ViewModel calls `AuthRepository.loginUser()`.
3.  **Repository (`AuthRepository`)**: 
    - It checks for an internet connection.
    - It calls the `loginUser` method in `ApiService`.
    - On a successful response, it uses `SessionManager` to securely save the received `accessToken` and `refreshToken`.
4.  **Session Management (`SessionManager`)**: This class uses `SharedPreferences` to store the tokens. The `accessToken` is used to authorize subsequent API calls, while the `refreshToken` is used to obtain a new access token when the current one expires.

### c. Session Persistence & Token Handling

- **Checking Login Status**: Before launching the main part of the app, `SplashActivity` can check `SessionManager.isLoggedIn()` to see if a valid session exists. If so, the user is directed to the `MainActivity`; otherwise, they are sent to `LoginActivity`.
- **Token Refresh**: The `AuthRepository` contains a `refreshToken()` method that is called when an API request fails due to an expired access token. It sends the `refreshToken` to the `/auth/refresh` endpoint to get a new access token.
- **Logout**: The `logout()` method in `AuthRepository` calls `SessionManager.clearSession()` to delete all stored tokens, effectively logging the user out.

## 5. How to Contribute

1.  **Understand the Architecture**: Familiarize yourself with the MVVM pattern and the project structure outlined above.
2.  **Check for Internet**: Before making any network call, always use the `isInternetAvailable()` method from `CommonMethod` within the repository layer.
3.  **Use `ApiResult`**: Wrap all network responses in the `ApiResult` sealed class to handle success, loading, and error states consistently.
4.  **Keep UI Dumb**: Fragments and Activities should only be responsible for observing data from the ViewModel and displaying it. All business logic should reside in the ViewModels and Repositories.

This guide should provide a solid starting point. Feel free to explore the code and ask if you have any more questions!
