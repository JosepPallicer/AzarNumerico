package openHelper

import android.content.ContentValues
import model.User
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {

        private const val DATABASE_NAME = "azarnumerico_database.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_USERS = "users"
        private const val TABLE_REBUYS = "rebuys"

        private const val CREATE_TABLE_USERS = """
            CREATE TABLE $TABLE_USERS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                password TEXT NOT NULL,
                coins INTEGER DEFAULT 100
            );
            """

        private const val CREATE_TABLE_REBUYS = """
            CREATE TABLE $TABLE_REBUYS (
                rebuy_id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                rebuy_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY(user_id) REFERENCES $TABLE_USERS(id)
            );
            """

    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(CREATE_TABLE_USERS)
        db?.execSQL(CREATE_TABLE_REBUYS)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_REBUYS")
        onCreate(db)
    }

    fun getAllUsers(): Single<List<User>> {
        return Single.create { emitter ->
            try {
                val userList = mutableListOf<User>()
                val db = this.readableDatabase
                val cursor = db.query("users", null, null, null, null, null, null)

                while (cursor.moveToNext()) {
                    val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                    val password = cursor.getString(cursor.getColumnIndexOrThrow("password"))
                    val coins = cursor.getInt(cursor.getColumnIndexOrThrow("coins"))
                    userList.add(User(name, password, coins))
                }
                cursor.close()
                emitter.onSuccess(userList)
            } catch (e: Exception) {
                emitter.onError(e)
            }
        }
    }

    fun getUserId(username: String): Int? {

        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf("id"),
            "name = ?",
            arrayOf(username),
            null, null, null
        )
        var userId: Int? = null
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
        }
        cursor.close()
        return userId
    }

    fun loginUser(username: String, password: String): Maybe<User> {

        return Maybe.create { emitter ->
            try {
                val db = this.readableDatabase
                val cursor = db.query(
                    "users",
                    arrayOf("id", "name", "coins", "password"),
                    "name = ? AND password = ?",
                    arrayOf(username, password),
                    null,
                    null,
                    null
                )
                if (cursor.moveToFirst()) {
                    val user = User(
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getString(cursor.getColumnIndexOrThrow("password")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("coins"))
                    )
                    emitter.onSuccess(user)
                } else {
                    emitter.onComplete()
                }
                cursor.close()
            } catch (e: Exception) {
                emitter.onError(e)
            }

        }

    }

    fun insertReBuy(userId: Int){

        val db = this.writableDatabase
        val values = ContentValues(). apply {
            put("user_id", userId)
        }
        db.insert(TABLE_REBUYS, null, values)
    }

    fun updateUserCoins(username: String, coins: Int) {

        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put("coins", coins)
        db.update(TABLE_USERS, contentValues, "name = ?", arrayOf(username))

    }

    fun getUserScore(): Single<List<User>> {
        return Single.create { emitter ->
            try {
                val userList = mutableListOf<User>()
                val db = this.readableDatabase
                val cursor = db.query(TABLE_USERS, null, null, null, null, null, "coins DESC")

                while (cursor.moveToNext()) {
                    val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                    val password = cursor.getString(cursor.getColumnIndexOrThrow("password"))
                    val coins = cursor.getInt(cursor.getColumnIndexOrThrow("coins"))
                    userList.add(User(name, password, coins))
                }
                cursor.close()
                emitter.onSuccess(userList)
            } catch (e: Exception) {
                emitter.onError(e)
            }
        }
    }

}
