package com.example.reto8

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.DatabaseUtils
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.*


class DBHelper(context: Context?) : SQLiteOpenHelper(context, DATABASE_NAME, null, 1) {
    private val hp: HashMap<*, *>? = null
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "create table contacts " +
                    "(id integer primary key, name text,phone text,email text, url text, products text, classification integer)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS contacts")
        onCreate(db)
    }

    fun insertContact(
        name: String?,
        phone: String?,
        email: String?,
        url: String?,
        products: String?,
        classification: Int?
    ): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put("name", name)
        contentValues.put("phone", phone)
        contentValues.put("email", email)
        contentValues.put("url", url)
        contentValues.put("products", products)
        contentValues.put("classification",classification)
        db.insert("contacts", null, contentValues)
        return true
    }

    fun getData(id: Int): Cursor {
        val db = this.readableDatabase
        return db.rawQuery("select * from contacts where id=$id", null)
    }

    fun numberOfRows(): Int {
        val db = this.readableDatabase
        return DatabaseUtils.queryNumEntries(db, CONTACTS_TABLE_NAME).toInt()
    }

    fun updateContact(
        id: Int?,
        name: String?,
        phone: String?,
        email: String?,
        url: String?,
        products: String?,
        classification: Int?
    ): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put("name", name)
        contentValues.put("phone", phone)
        contentValues.put("email", email)
        contentValues.put("url", url)
        contentValues.put("products", products)
        contentValues.put("classification", classification)
        db.update(
            "contacts", contentValues, "id = ? ", arrayOf(
                (id!!).toString()
            )
        )
        return true
    }

    fun deleteContact(id: Int?): Int {
        val db = this.writableDatabase
        return db.delete(
            "contacts",
            "id = ? ", arrayOf((id!!).toString())
        )
    }

    //hp = new HashMap();
    val allContacts: ArrayList<Contact>
        get() {
            val arrayList = ArrayList<Contact>()

            //hp = new HashMap();
            val db = this.readableDatabase
            val res = db.rawQuery("select * from contacts", null)
            res.moveToFirst()
            while (!res.isAfterLast) {
                val contact = Contact(res.getString(res.getColumnIndex(CONTACTS_COLUMN_NAME)),res.getInt(res.getColumnIndex(CONTACTS_COLUMN_CLASSIFICATION)))
                arrayList.add(contact)
                res.moveToNext()
            }
            return arrayList
        }

    companion object {
        const val DATABASE_NAME = "MyDBName.db"
        const val CONTACTS_TABLE_NAME = "contacts"
        const val CONTACTS_COLUMN_ID = "id"
        const val CONTACTS_COLUMN_NAME = "name"
        const val CONTACTS_COLUMN_EMAIL = "email"
        const val CONTACTS_COLUMN_URL = "url"
        const val CONTACTS_COLUMN_PRODUCT = "products"
        const val CONTACTS_COLUMN_PHONE = "phone"
        const val CONTACTS_COLUMN_CLASSIFICATION = "classification"
    }
}