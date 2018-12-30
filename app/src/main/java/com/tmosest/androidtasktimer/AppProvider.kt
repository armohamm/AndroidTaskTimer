package com.tmosest.androidtasktimer

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri
import android.util.Log

/**
 * Basic Database class for the application.
 *
 * Provider for TaskTimer app. This is the only class that knows about [AppDatabase].
 */
private const val TAG = "AppProvider"

private const val TASKS = 100
private const val TASKS_ID = 101

private const val TIMINGS = 200
private const val TIMINGS_ID = 201

private const val TASK_DURATION = 400
private const val TASK_DURATION_ID = 401

const val CONTENT_AUTHORITY = "com.tmosest.androidtasktimer.provider"
val CONTENT_AUTHORITY_URI = Uri.parse("content://$CONTENT_AUTHORITY")

class AppProvider : ContentProvider() {

    private val uriMatcher by lazy { buildUriMatcher() }

    private fun buildUriMatcher() : UriMatcher {
        Log.d(TAG, "buildUriMatcher: starts")
        val matcher = UriMatcher(UriMatcher.NO_MATCH)
        // e.g. content://com.tmosest.androidtasktimer.provider/Tasks
        matcher.addURI(CONTENT_AUTHORITY, TasksContract.TABLE_NAME, TASKS)
        // e.g. content://com.tmosest.androidtasktimer.provider/Tasks/#/8
        matcher.addURI(CONTENT_AUTHORITY, "${TasksContract.TABLE_NAME}/#", TASKS_ID)
        // e.g. content://com.tmosest.androidtasktimer.provider/Timings
        matcher.addURI(CONTENT_AUTHORITY, TimingsContract.TABLE_NAME, TASKS)
        // e.g. content://com.tmosest.androidtasktimer.provider/Timings/#/8
        matcher.addURI(CONTENT_AUTHORITY, "${TimingsContract.TABLE_NAME}/#", TIMINGS_ID)
        return matcher
    }

    override fun onCreate(): Boolean {
        Log.d(TAG, "onCreate: starts")
        val appDatabase = AppDatabase.getInstance(context)
        return true
    }

    override fun getType(uri : Uri): String? {
        val match = uriMatcher.match(uri)
        return when (match) {
            TASKS -> TasksContract.CONTENT_TYPE
            TASKS_ID -> TasksContract.CONTENT_ITEM_TYPE
            TIMINGS -> TimingsContract.CONTENT_TYPE
            TIMINGS_ID -> TimingsContract.CONTENT_ITEM_TYPE
            else -> throw IllegalArgumentException("unknown Uri: $uri")
        }
    }

    override fun query(uri : Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        Log.d(TAG, "query: called with $uri")

        val match = uriMatcher.match(uri)
        Log.d(TAG, "query match is $match")

        val queryBuilder = SQLiteQueryBuilder()

        when (match) {
            TASKS -> queryBuilder.tables = TasksContract.TABLE_NAME
            TASKS_ID -> {
                queryBuilder.tables = TasksContract.TABLE_NAME
                val taskId = TasksContract.getId(uri)
                queryBuilder.appendWhereEscapeString("${TasksContract.Columns.ID} = $taskId")
            }
            TIMINGS -> queryBuilder.tables = TimingsContract.TABLE_NAME
            TIMINGS_ID -> {
                queryBuilder.tables = TimingsContract.TABLE_NAME
                val timingId = TimingsContract.getId(uri)
                queryBuilder.appendWhereEscapeString("${TimingsContract.Columns.ID} = $timingId")
            }
            /*
            TASK_DURATION -> queryBuilder.tables = DurationContract.TABLE_NAME
            TASK_DURATION_ID -> {
                queryBuilder.tables = DurationContract.TABLE_NAME
                val durationId = DurationContract.getId(uri)
                queryBuilder.appendWhereEscapeString("${DurationContract.Columns.ID} = $durationId")
            }
            */
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }

        val db = AppDatabase.getInstance(context).readableDatabase
        val cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder)
        Log.d(TAG, "query: rows in cursor = ${cursor.count}") // TODO remove this line

        return cursor
    }

    override fun insert(uri: Uri, values: ContentValues): Uri? {
        Log.d(TAG, "insert: called with uri $uri")
        val match = uriMatcher.match(uri)
        Log.d(TAG, "insert: match is $match")

        val recordId: Long
        val returnUri: Uri

        when(match) {
            TASKS -> {
                val db = AppDatabase.getInstance(context).writableDatabase
                recordId = db.insert(TasksContract.TABLE_NAME, null, values)
                if (recordId != -1L) {
                    returnUri = TasksContract.buildUriFromId(recordId)
                } else {
                    throw SQLException("Failed to insert, Uri was $uri")
                }
            }
            TIMINGS -> {
                val db = AppDatabase.getInstance(context).writableDatabase
                recordId = db.insert(TimingsContract.TABLE_NAME, null, values)
                if (recordId != -1L) {
                    returnUri = TimingsContract.buildUriFromId(recordId)
                } else {
                    throw SQLException("Failed to insert, Uri was $uri")
                }
            }
            else -> throw java.lang.IllegalArgumentException("Unknown uri: $uri")
        }

        Log.d(TAG, "exiting insert, returning $returnUri")
        return returnUri
    }

    override fun update(uri : Uri, values: ContentValues, selection: String?, selectionArgs: Array<String>?): Int {
        Log.d(TAG, "update: called with uri $uri")
        val match = uriMatcher.match(uri)
        Log.d(TAG, "update: match is $match")

        val count: Int
        var selectionCriteria: String

        when (match) {
            TASKS -> {
                val db = AppDatabase.getInstance(context).writableDatabase
                count = db.update(TasksContract.TABLE_NAME, values, selection, selectionArgs)
            }
            TASKS_ID -> {
                val db = AppDatabase.getInstance(context).writableDatabase
                val id = TasksContract.getId(uri)
                selectionCriteria = "${TasksContract.Columns.ID} = $id"

                if (selection != null && selection.isNotEmpty()) {
                    selectionCriteria += "AND ($selection)"
                }

                count = db.update(TasksContract.TABLE_NAME, values, selectionCriteria, selectionArgs)
            }
            TIMINGS -> {
                val db = AppDatabase.getInstance(context).writableDatabase
                count = db.update(TimingsContract.TABLE_NAME, values, selection, selectionArgs)
            }
            TIMINGS_ID -> {
                val db = AppDatabase.getInstance(context).writableDatabase
                val id = TimingsContract.getId(uri)
                selectionCriteria = "${TimingsContract.Columns.ID} = $id"

                if (selection != null && selection.isNotEmpty()) {
                    selectionCriteria += "AND ($selection)"
                }

                count = db.update(TimingsContract.TABLE_NAME, values, selectionCriteria, selectionArgs)
            }
            else -> throw java.lang.IllegalArgumentException("Unknown uri: $uri")
        }
        Log.d(TAG, "Exiting update, returning $count")
        return count
    }

    override fun delete(uri : Uri, selection: String?, selectionArgs: Array<String>?): Int {
        Log.d(TAG, "update: called with uri $uri")
        val match = uriMatcher.match(uri)
        Log.d(TAG, "update: match is $match")

        val count: Int
        var selectionCriteria: String

        when (match) {
            TASKS -> {
                val db = AppDatabase.getInstance(context).writableDatabase
                count = db.delete(TasksContract.TABLE_NAME, selection, selectionArgs)
            }
            TASKS_ID -> {
                val db = AppDatabase.getInstance(context).writableDatabase
                val id = TasksContract.getId(uri)
                selectionCriteria = "${TasksContract.Columns.ID} = $id"

                if (selection != null && selection.isNotEmpty()) {
                    selectionCriteria += "AND ($selection)"
                }

                count = db.delete(TasksContract.TABLE_NAME, selectionCriteria, selectionArgs)
            }
            TIMINGS -> {
                val db = AppDatabase.getInstance(context).writableDatabase
                count = db.delete(TimingsContract.TABLE_NAME, selection, selectionArgs)
            }
            TIMINGS_ID -> {
                val db = AppDatabase.getInstance(context).writableDatabase
                val id = TimingsContract.getId(uri)
                selectionCriteria = "${TimingsContract.Columns.ID} = $id"

                if (selection != null && selection.isNotEmpty()) {
                    selectionCriteria += "AND ($selection)"
                }

                count = db.delete(TimingsContract.TABLE_NAME, selectionCriteria, selectionArgs)
            }
            else -> throw java.lang.IllegalArgumentException("Unknown uri: $uri")
        }
        Log.d(TAG, "Exiting update, returning $count")
        return count
    }
}
