package com.codechacha.saf

import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_open_example.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*


class OpenExampleActivity : AppCompatActivity() {
    private val TAG = "OpenExampleActivity"
    private val READ_REQUEST_CODE: Int = 42

    // Read file
    var DataArray = Array(2000, {arrayOfNulls<Short> (8)})


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_open_example)
        openImage()
    }

    private fun openImage() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
          //  type = "image/*"
            type = "*/*"                // kawa すべてのファイルを見えるようにする
        }

        startActivityForResult(intent, READ_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            resultData?.data?.also { uri ->
                Log.i(TAG, "Uri: $uri")
            //    dumpImageMetaData(uri)
            //    showImage(uri)                          // ここでアプリ停止、*.csvファイルをイメージファイルとして開こうとするから

            //    val inputStream: InputStream = context.getContentResolver().openInputStream(uri)      // Kotlinではcontext削除

                val inputStream: InputStream? = getContentResolver().openInputStream(uri)
                val inputStreamReader = InputStreamReader(inputStream)
                val reader = BufferedReader(inputStreamReader)


// lineに1行分の文字列が入る。行数分だけ実行する。
                var rowCount = 0
                for(line in reader.lines()) {
             //   var stmp1 = reader.readLine()

                    // カンマの数でNORMAL(2個）のデータか、DEBUG（7個）のデータかを判別する
                val b = line.chunked(1).filter { it == ","}
                var nn = b.size

                    val array = line.split(",").map{it.trim()}
                    for(i in 0..nn) {
                        DataArray[rowCount][i] = array[i].toShort()
                    }
                    var count = line
                    rowCount = rowCount + 1
               }





            }
        }
    }


    fun dumpImageMetaData(uri: Uri) {
        val cursor: Cursor? = contentResolver.query( uri, null, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val displayName: String =
                    it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                Log.i(TAG, "Display Name: $displayName")

                val sizeIndex: Int = it.getColumnIndex(OpenableColumns.SIZE)
                val size: String = if (!it.isNull(sizeIndex)) {
                    it.getString(sizeIndex)
                } else {
                    "Unknown"
                }
                Log.i(TAG, "Size: $size")
            }
        }
    }

    @Throws(IOException::class)
    private fun getBitmapFromUri(uri: Uri): Bitmap {
        val parcelFileDescriptor: ParcelFileDescriptor? = contentResolver.openFileDescriptor(uri, "r")
        val fileDescriptor: FileDescriptor = parcelFileDescriptor!!.fileDescriptor
        val image: Bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        parcelFileDescriptor.close()
        return image
    }

    private fun showImage(uri: Uri) {
        GlobalScope.launch {
            val bitmap = getBitmapFromUri(uri)
            withContext(Dispatchers.Main) {
                mainImageView.setImageBitmap(bitmap)
            }
        }
    }

}
