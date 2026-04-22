package com.hook.fakewifi

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import hook.tool.FakeWifiConfig
import hook.tool.loadConfig
import hook.tool.saveConfig

class ConfigProvider : ContentProvider() {
    override fun onCreate(): Boolean = true

    override fun call(method: String, arg: String?, extras: Bundle?): Bundle? {
        val appContext = context?.applicationContext ?: return Bundle()
        return when (method) {
            METHOD_GET_CONFIG -> loadConfig(appContext).toBundle()
            METHOD_PUT_CONFIG -> {
                saveConfig(FakeWifiConfig.fromBundle(extras), appContext)
                loadConfig(appContext).toBundle()
            }

            else -> super.call(method, arg, extras)
        }
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
    ): Cursor? = null

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?,
    ): Int = 0

    companion object {
        const val METHOD_GET_CONFIG = "get_config"
        const val METHOD_PUT_CONFIG = "put_config"
    }
}
