# 🚀 API Testing Guide - Bharatnaai App

## 📱 Complete Registration & Login API Integration

### ✅ **What's Implemented:**

1. **Full Registration Flow** with API integration
2. **Full Login Flow** with API integration  
3. **Real-time Form Validation**
4. **Error Handling & Loading States**
5. **Network Configuration** with proper headers

---

## 🔧 **API Configuration:**

### **Base URL:**
```
https://shakita-unlacquered-nakita.ngrok-free.dev/
```

### **Endpoints:**
- **Registration:** `POST /auth/register`
- **Login:** `POST /auth/login`

---

## 🧪 **Testing Steps:**

### **1. Registration API Test:**

#### **Step 1:** Navigate to Registration
- Launch app → Profile Tab → "Login" → "Sign Up"

#### **Step 2:** Fill Registration Form
```
Full Name: John Doe
Email: john@example.com
Phone: 1234567890
Password: password123
Confirm Password: password123
```

#### **Step 3:** Submit & Monitor
- Click "Sign Up" button
- Watch for loading state
- Check Android logs for API call

#### **Expected API Request:**
```json
POST /auth/register
Content-Type: application/json

{
    "username": "John Doe",
    "email": "john@example.com", 
    "phone": "1234567890",
    "password": "password123"
}
```

#### **Success Response Expected:**
```json
{
    "success": true,
    "message": "Registration successful",
    "data": { ... }
}
```

### **2. Login API Test:**

#### **Step 1:** Navigate to Login
- From registration success → Login screen
- Or: Profile Tab → "Login"

#### **Step 2:** Fill Login Form
```
Email/Phone: john@example.com
Password: password123
```

#### **Step 3:** Submit & Monitor
- Click "Log In" button
- Watch for loading state
- Check Android logs for API call

#### **Expected API Request:**
```json
POST /auth/login
Content-Type: application/json

{
    "email": "john@example.com",
    "password": "password123"
}
```

#### **Success Response Expected:**
```json
{
    "success": true,
    "message": "Login successful",
    "data": { 
        "token": "...",
        "user": { ... }
    }
}
```

---

## 🔍 **Monitoring & Debugging:**

### **Android Studio Logcat:**
Filter by: `bharatnaai` or `OkHttp`

### **Expected Log Output:**
```
D/OkHttp: --> POST https://shakita-unlacquered-nakita.ngrok-free.dev/auth/register
D/OkHttp: Content-Type: application/json
D/OkHttp: {"username":"John Doe","email":"john@example.com"...}
D/OkHttp: --> END POST
D/OkHttp: <-- 200 OK https://shakita-unlacquered-nakita.ngrok-free.dev/auth/register
D/OkHttp: {"success":true,"message":"Registration successful"...}
```

---

## ⚠️ **Error Scenarios to Test:**

### **1. Network Errors:**
- Turn off WiFi/Data → Try registration
- Expected: "Network error" message

### **2. Server Errors:**
- Invalid credentials → Try login
- Expected: Server error message displayed

### **3. Validation Errors:**
- Empty fields → Form validation
- Invalid email → Email format error
- Password mismatch → Confirmation error

---

## 🛠️ **Troubleshooting:**

### **If API calls fail:**

1. **Check ngrok URL:**
   - Ensure ngrok is running
   - Verify URL in Constants.kt matches

2. **Check Network Permissions:**
   - INTERNET permission in AndroidManifest.xml
   - usesCleartextTraffic="true" for HTTP

3. **Check Backend:**
   - Ensure your backend is running
   - Test endpoints with Postman first

4. **Check Logs:**
   - Look for OkHttp logs
   - Check for exception stack traces

---

## 🎯 **Success Indicators:**

### **Registration Success:**
- ✅ Loading state shown
- ✅ API request logged
- ✅ Success toast displayed
- ✅ Navigation to Login screen

### **Login Success:**
- ✅ Loading state shown  
- ✅ API request logged
- ✅ Success toast displayed
- ✅ Navigation to MainActivity

---

## 📋 **Next Steps After Testing:**

1. **Token Storage** - Save JWT tokens securely
2. **Auto-login** - Check saved tokens on app start
3. **Profile Management** - User profile CRUD operations
4. **Error Refinement** - Better error messages
5. **Loading UI** - Progress indicators

---

**🚀 Your app now has complete API integration! Test the registration flow and verify the API calls are working correctly.**
