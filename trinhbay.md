# 🔍 Professional Code Review Report: Ticket & Invoice System

This document contains a comprehensive review and analysis of the newly integrated **Shared Ticket & Invoice Details System** for users and admins.

---

## 📋 1. Executive Summary

| Target System | Scope of Changes | Status |
| :--- | :--- | :--- |
| **User "My Tickets" Interface** | Replaced simple QR popup with full Ticket Stub + itemized invoice details. | **PASS** |
| **Admin Check-in System** | Replaced basic check-in dialog with the comprehensive Invoice Details Dialog. | **PASS** |
| **Payment Success Verification** | Implemented QR Lock & Delay Release banner based on config parameters. | **PASS** |
| **Stability & Safety Audit** | Fixed critical thread safety and null callback crash vectors. | **PASS** |

---

## 🛠️ 2. Architectural Design Review

### 2.1 Shared UI Component (`dialog_ticket_invoice.xml`)
The system leverages a single high-fidelity, scrollable XML layout (`dialog_ticket_invoice.xml`) that acts as both a user ticket display and an admin invoice display. 

*   **Aesthetics & Visual Hierarchy**:
    *   Designed in a physical ticket format using `bg_dashed_line.xml` as a stub separator.
    *   Dynamic background rounded container (`bg_white_rounded_16`).
    *   Two-column attribute grids for financial invoice alignment.
*   **Reusability**:
    *   Sections are separated into clean semantic containers (`layout_qr_container`, `layout_customer_info`).
    *   Hiding role-specific elements in Java runtime is clean and reduces duplication.

### 2.2 Security & QR Fraud Prevention
*   **Delayed Release Logic**:
    *   QR code is generated dynamically on client-side only if current time is within `ConfigController.getInstance().getQrReleaseThresholdMs()` of the event start time.
    *   If QR is locked, the ticket displays a friendly notification badge stating when the ticket details will release and unlocks the "Cancel Ticket" option.
    *   Once the QR is active, the "Cancel Ticket" action is **forcefully hidden** to prevent ticket cloning frauds where users obtain entry credentials and immediately request a refund.

---

## 🪲 3. Stability & Code Quality Inspections

During this audit, several critical stability/crash issues were investigated and corrected.

### 3.1 [FIXED] Null Pointer Callback Vulnerability
*   **File**: `AdminManageBookingsActivity.java`
*   **Issue**: Methods `getEventById` and `getUserById` were invoked with `null` in place of the `onError` consumer parameter. In Firestore database implementations, any network disruption or rule permission denial triggers `addOnFailureListener`, which calls `onError.accept(e.getMessage())`. If `onError` is null, this throws a `NullPointerException` and immediately crashes the application.
*   **Fix**: Modified the code to supply an active logging consumer `e -> android.util.Log.e(...)` instead of `null` to absorb and report faults gracefully.

```diff
-        eventController.getEventById(booking.getEventId(), event -> { ... }, null);
+        eventController.getEventById(booking.getEventId(), event -> { ... }, e -> android.util.Log.e("AdminManageBookings", "Lỗi tải sự kiện: " + e));
```

### 3.2 Threading & UI Thread Safety
*   **Verification**: All Firestore callback processing that updates views is strictly bound inside `runOnUiThread(...)` blocks or active main-thread handlers.
*   **Activity Lifecycles**: Added checking conditions (`if (isFinishing() || isDestroyed()) return;`) in activities before inflating or launching AlertDialogs. This prevents `WindowManager$BadTokenException` crashes (e.g. when an admin turns the screen or presses back while database calls are resolving).

### 3.3 Safe String Slicing
*   **File**: `AdminTopUserAdapter.java`
*   **Fix**: Handled edge cases where temporary mock user IDs had lengths less than 5 characters to avoid `StringIndexOutOfBoundsException` crashes during rendering.
```java
String userId = entry.getKey();
holder.tvName.setText(name != null ? name : "User #" + (userId.length() > 5 ? userId.substring(0, 5) : userId));
```

---

## 💡 4. Future Refactoring Recommendations

1.  **Extract Hardcoded Strings to `strings.xml`**:
    *   Texts such as `"Tên: "` or `"Liên hệ: "` in `AdminManageBookingsActivity.java` are currently hardcoded in Vietnamese. 
    *   *Action*: Shift them to resource bundles (`R.string.label_customer_name`) to support the internationalization protocols requested by the project system.
2.  **Network State Pre-Checks**:
    *   Before executing `btnCheckIn.setOnClickListener`, verify if the local device has active internet access. Show an offline Toast if Firebase sync cannot be guaranteed.

---

## 🧪 5. Verification Guide (Test Cases)

### Test Case 1: User Invoice Viewer
1.  Log in as a user and navigate to **My Tickets**.
2.  Select an active booking.
3.  **Expected Outcome**: The popup should render a clear Invoice Breakdown (Unit price x Quantity, Discount, Total Payment, Method, and Date) at the bottom. If the event is scheduled for > 24 hours (or the remote config threshold), the QR code must be hidden and a yellow delay release notice banner should be displayed.

### Test Case 2: Admin Scan & Check-in
1.  Log in as an administrator.
2.  Open **Bookings Management** and launch the QR Check-in scanner.
3.  Scan the user's active QR code.
4.  **Expected Outcome**:
    *   Instead of a bare notification, the admin is shown the full invoice summary including customer contact details (Name, Email, Phone).
    *   If the ticket is active and unused, the "Confirm Check-in" button is enabled.
    *   Clicking check-in completes the transaction, updates Firestore state to `Completed`, and renders a green "✅ VÉ ĐÃ SỬ DỤNG" badge upon subsequent scans.
