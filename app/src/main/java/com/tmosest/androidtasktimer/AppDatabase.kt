package com.tmosest.androidtasktimer

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.lang.IllegalArgumentException

/**
 * Basic Database class for the application.
 *
 * The only class that should use this is the [AppProvider].
 *
 * This class is an example of a singleton.
 */
private const val TAG = "AppDatabae"

private const val DATABASE_NAME = "TaskTimer.db"
private const val DATABASE_VERSION = 3

internal class AppDatabase private constructor(context : Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    init {
        Log.d(TAG, "initializing")
    }

    override fun onCreate(database: SQLiteDatabase) {
        Log.d(TAG, "onCreate called")
        val createTableSQl = """CREATE TABLE ${TasksContract.TABLE_NAME} (
            ${TasksContract.Columns.ID} INTEGER PRIMARY KEY NOT NULL,
            ${TasksContract.Columns.TASK_NAME} TEXT NOT NULL,
            ${TasksContract.Columns.DESCRIPTION} TEXT,
            ${TasksContract.Columns.TASK_SORT_ORDER} INTEGER
        )""".replaceIndent(" ")
        Log.d(TAG, "creating with $createTableSQl")
        database.execSQL(createTableSQl)
    }

    override fun onUpgrade(database: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.d(TAG, "onUpgrade: oldVersion: $oldVersion newVersion: $newVersion")
        when (newVersion) {
            1 -> {
                // upgrade logic
            }
            else -> throw IllegalArgumentException("onUpgrade() with unknown newVersion: $newVersion")
        }
    }

    companion object : SingletonHolder<AppDatabase, Context>(::AppDatabase)
}
