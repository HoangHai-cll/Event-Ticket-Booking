package vn.humg.hai.event_ticket_booking_app.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;
import vn.humg.hai.event_ticket_booking_app.model.NotificationModel;

public class LocalNotificationDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "local_notifications.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_NAME = "notifications";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_BODY = "body";
    private static final String COLUMN_TIMESTAMP = "timestamp";
    private static final String COLUMN_TYPE = "type";
    private static final String COLUMN_IS_READ = "is_read";

    private static LocalNotificationDbHelper instance;

    public static synchronized LocalNotificationDbHelper getInstance(Context context) {
        if (instance == null) {
            instance = new LocalNotificationDbHelper(context.getApplicationContext());
        }
        return instance;
    }

    private LocalNotificationDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TITLE + " TEXT, " +
                COLUMN_BODY + " TEXT, " +
                COLUMN_TIMESTAMP + " INTEGER, " +
                COLUMN_TYPE + " TEXT, " +
                COLUMN_IS_READ + " INTEGER DEFAULT 0)";
        db.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // Insert Notification
    public long insertNotification(String title, String body, String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_BODY, body);
        values.put(COLUMN_TIMESTAMP, System.currentTimeMillis());
        values.put(COLUMN_TYPE, type != null ? type : "SYSTEM");
        values.put(COLUMN_IS_READ, 0); // Unread by default

        long result = db.insert(TABLE_NAME, null, values);
        db.close();
        return result;
    }

    // Get All Notifications sorted by timestamp DESC
    public List<NotificationModel> getAllNotifications() {
        List<NotificationModel> notificationList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_NAME + " ORDER BY " + COLUMN_TIMESTAMP + " DESC";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                NotificationModel model = new NotificationModel();
                model.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                model.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)));
                model.setBody(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BODY)));
                model.setTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP)));
                model.setType(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE)));
                model.setIsRead(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_READ)));
                notificationList.add(model);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return notificationList;
    }

    // Get Unread Count
    public int getUnreadCount() {
        String countQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_IS_READ + " = 0";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();
        return count;
    }

    // Mark all as read
    public void markAllAsRead() {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_IS_READ, 1);
        db.update(TABLE_NAME, values, COLUMN_IS_READ + " = 0", null);
        db.close();
    }

    // Mark single as read
    public void markAsRead(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_IS_READ, 1);
        db.update(TABLE_NAME, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    // Delete single notification
    public void deleteNotification(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    // Clear all
    public void clearAllNotifications() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
        db.close();
    }
}
