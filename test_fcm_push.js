/**
 * HƯỚNG DẪN KIỂM TRA HỆ THỐNG THÔNG BÁO ĐẨY (FCM PUSH NOTIFICATION)
 * 
 * Để chạy tệp tin thử nghiệm này, bạn hãy thực hiện các bước sau:
 * 
 * 1. Truy cập vào Firebase Console của bạn.
 * 2. Vào mục "Project settings" -> tab "Service accounts".
 * 3. Bấm nút "Generate new private key" để tải tệp JSON khóa dịch vụ.
 * 4. Đổi tên tệp vừa tải về thành "serviceAccountKey.json" và đặt vào cùng thư mục gốc của dự án này.
 * 5. Mở terminal tại thư mục gốc dự án và chạy:
 *    npm install firebase-admin
 * 6. Lấy Token thiết bị của bạn từ logcat Android Studio (Tìm từ khóa "FCMService" hoặc "Refreshed token").
 * 7. Thay thế chuỗi token vào biến `DEVICE_TOKEN` dưới đây.
 * 8. Chạy kiểm tra bằng lệnh:
 *    node test_fcm_push.js
 */

const admin = require('firebase-admin');
const path = require('path');

// 1. Khai báo Token thiết bị cần test (thay thế token của bạn vào đây)
const DEVICE_TOKEN = 'THAY_THE_TOKEN_THIET_BI_CUA_BAN_VAO_DAY';

// 2. Kiểm tra tệp tin chứng thực dịch vụ
const serviceAccountPath = path.join(__dirname, 'serviceAccountKey.json');
let serviceAccount;

try {
    serviceAccount = require(serviceAccountPath);
} catch (error) {
    console.error('❌ LỖI: Không tìm thấy tệp "serviceAccountKey.json" tại thư mục gốc!');
    console.log('👉 Vui lòng tải khóa dịch vụ private key từ Firebase Console và đặt vào thư mục gốc dự án.');
    process.exit(1);
}

// 3. Khởi tạo ứng dụng Firebase Admin
admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
});

// 4. Xây dựng nội dung thông báo đẩy cần gửi
const message = {
    token: DEVICE_TOKEN,
    notification: {
        title: 'Đặt vé thành công! 🎉',
        body: 'Sự kiện âm nhạc "Đêm Nhạc Trẻ 2026" của bạn đã được xác nhận. Hãy sẵn sàng nhé!'
    },
    data: {
        type: 'TICKET', // Loại thông báo: TICKET (Vé), GIFT (Quà tặng), SYSTEM (Hệ thống)
        title: 'Đặt vé thành công! 🎉',
        body: 'Sự kiện âm nhạc "Đêm Nhạc Trẻ 2026" của bạn đã được xác nhận. Hãy sẵn sàng nhé!'
    },
    android: {
        priority: 'high',
        notification: {
            sound: 'default',
            clickAction: 'vn.humg.hai.event_ticket_booking_app.view.NotificationActivity'
        }
    }
};

// 5. Tiến hành gửi tin nhắn đẩy
console.log('⏳ Đang kết nối Firebase và gửi thông báo đẩy thử nghiệm...');
admin.messaging().send(message)
    .then((response) => {
        console.log('✅ THÀNH CÔNG: Đã gửi thông báo đẩy thành công!');
        console.log('Message ID:', response);
        console.log('👉 Hãy kiểm tra điện thoại/giả lập của bạn xem có nhận được thông báo không nhé.');
        process.exit(0);
    })
    .catch((error) => {
        console.error('❌ THẤT BẠI: Gửi thông báo đẩy lỗi!');
        console.error(error);
        process.exit(1);
    });
