import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import javax.annotation.Nullable

object RealPathUtil {

	fun writeFileContent(context: Context,uri: Uri): String? {
		val selectedFileInputStream: InputStream = context.contentResolver.openInputStream(uri)!!
		val certCacheDir: File = context.getExternalFilesDir(null)!!
		var isCertCacheDirExists = certCacheDir.exists()
		if (!isCertCacheDirExists) {
			isCertCacheDirExists = certCacheDir.mkdirs()
		}
		if (isCertCacheDirExists) {
			val filePath = certCacheDir.absolutePath + "/" + getFileDisplayName(context,uri)
			val selectedFileOutPutStream: OutputStream = FileOutputStream(filePath)
			val buffer = ByteArray(1024)
			var length: Int
			while (selectedFileInputStream.read(buffer).also { length = it } > 0) {
				selectedFileOutPutStream.write(buffer, 0, length)
			}
			selectedFileOutPutStream.flush()
			selectedFileOutPutStream.close()
			return filePath
		}
		selectedFileInputStream.close()
		return null
	}

	@SuppressLint("Range")
	@Nullable
	private fun getFileDisplayName(context: Context,uri: Uri): String? {
		var displayName: String? = null
		context.contentResolver
			.query(uri, null, null, null, null, null).use { cursor ->
				if (cursor != null && cursor.moveToFirst()) {
					displayName = cursor.getString(
						cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
					)
					Log.d("Display Name {}","$displayName")
				}
			}
		return displayName
	}



}