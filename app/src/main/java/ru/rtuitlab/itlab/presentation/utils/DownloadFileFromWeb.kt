package ru.rtuitlab.itlab.presentation.utils

import android.R
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.*
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import ru.rtuitlab.itlab.data.remote.api.micro_file_service.models.FileInfo
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.OutputStream
import java.net.URL


object DownloadFileFromWeb {
	private var msg: String? = ""
	private var lastMsg = ""

	@SuppressLint("Range")
	fun downloadFile(context:Context, url: String,fileInfo: FileInfo, titleDirectory:String) {

		context.registerReceiver(attachmentDownloadCompleteReceive, IntentFilter(
				DownloadManager.ACTION_DOWNLOAD_COMPLETE)
		);

		val directory = File(Environment.DIRECTORY_DOWNLOADS)
		if (!directory.exists()) {
			directory.mkdirs()
		}

		val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

		val downloadUri = Uri.parse(url)

		val request = DownloadManager.Request(downloadUri).apply {
			setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
				.setAllowedOverRoaming(false)
				.setTitle(fileInfo.filename)
				.setDescription(fileInfo.fileDescription)
				.setTitle(fileInfo.filename)
				.setDescription("ITLab")
				.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, titleDirectory + "/" + fileInfo.filename)
				.setNotificationVisibility(DownloadManager.Request. VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

		}

		val downloadId = downloadManager.enqueue(request)

		val c: Cursor = downloadManager.query(DownloadManager.Query().setFilterById(downloadId))


		val query = DownloadManager.Query().setFilterById(downloadId)
		runBlocking {
			var downloading = true
			while (downloading) {
				val cursor: Cursor = downloadManager.query(query)
				cursor.moveToFirst()
				if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
					downloading = false
				}
				val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
				msg = statusMessage(fileInfo, directory,titleDirectory, status)
				if (msg != lastMsg) {
					launch(Dispatchers.Main){
						Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
						if (status == DownloadManager.STATUS_SUCCESSFUL) {
							val exp = fileInfo.filename.substring(fileInfo.filename.lastIndexOf(".") + 1)
							Log.d("DOWN",url)
							val uri =Uri.parse(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)!!.path + "/"+ titleDirectory + "/" + fileInfo.filename).path!!
							Log.d("DOWN",uri)

						}
					}

					lastMsg = msg ?: ""
				}

				cursor.close()
			}
		}
	}

	private var attachmentDownloadCompleteReceive: BroadcastReceiver = object : BroadcastReceiver() {
		@SuppressLint("Range")
		override fun onReceive(context: Context, intent: Intent) {
			val action = intent.action
			if (DownloadManager.ACTION_DOWNLOAD_COMPLETE == action) {
				val downloadId = intent.getLongExtra(
					DownloadManager.EXTRA_DOWNLOAD_ID, 0
				)
				Log.d("DOWN",intent.data.toString())
				Log.d("DOWN",intent.extras.toString())

				val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

				openDownloadedAttachment(context, downloadId)
			}
		}
	}

	/**
	 * Used to open the downloaded attachment.
	 *
	 * @param context    Content.
	 * @param downloadId Id of the downloaded file to open.
	 */
	@SuppressLint("Range")
	private fun openDownloadedAttachment(context: Context, downloadId: Long) {
		val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
		val query = DownloadManager.Query()
		query.setFilterById(downloadId)
		val cursor = downloadManager.query(query)
		if (cursor.moveToFirst()) {
			val downloadStatus = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
			val downloadLocalUri =
				cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
			val downloadMimeType =
				cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_MEDIA_TYPE))
			if (downloadStatus == DownloadManager.STATUS_SUCCESSFUL && downloadLocalUri != null) {
				openDownloadedAttachment(context, Uri.parse(downloadLocalUri), downloadMimeType)
			}
		}
		cursor.close()
	}

	/**
	 * Used to open the downloaded attachment.
	 *
	 *
	 * 1. Fire intent to open download file using external application.
	 *
	 * 2. Note:
	 * 2.a. We can't share fileUri directly to other application (because we will get FileUriExposedException from Android7.0).
	 * 2.b. Hence we can only share content uri with other application.
	 * 2.c. We must have declared FileProvider in manifest.
	 * 2.c. Refer - https://developer.android.com/reference/android/support/v4/content/FileProvider.html
	 *
	 * @param context            Context.
	 * @param attachmentUri      Uri of the downloaded attachment to be opened.
	 * @param attachmentMimeType MimeType of the downloaded attachment.
	 */
	private fun openDownloadedAttachment(
		context: Context,
		uri: Uri,
		attachmentMimeType: String
	) {
		var attachmentUri: Uri? = uri
		if (attachmentUri != null) {
			// Get Content Uri.
			if (ContentResolver.SCHEME_FILE == attachmentUri.scheme) {
				// FileUri - Convert it to contentUri.
				val file = File(attachmentUri.path!!)
				Log.d("OPEN","${attachmentUri.path}")
				attachmentUri =
					FileProvider.getUriForFile(context, "ru.rtuitlab.itlab.domain.provider", file)
				Log.d("OPEN","${attachmentUri}")

			}
			val openAttachmentIntent = Intent(Intent.ACTION_VIEW)
			openAttachmentIntent.setDataAndType(attachmentUri, attachmentMimeType)
			openAttachmentIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
			try {
				context.startActivity(openAttachmentIntent)
			} catch (e: ActivityNotFoundException) {
				Toast.makeText(
					context,
					"context.getString(R.string.unable_to_open_file)",
					Toast.LENGTH_LONG
				).show()
			}
		}
	}
	private fun statusMessage(file: FileInfo, directory: File,titleDirectory: String, status: Int): String? {
		var msg = ""
		msg = when (status) {
			DownloadManager.STATUS_FAILED -> "Download has been failed, please try again"
			DownloadManager.STATUS_PAUSED -> "Paused"
			DownloadManager.STATUS_PENDING -> "Downloading..."
			DownloadManager.STATUS_RUNNING -> "Downloading..."
			DownloadManager.STATUS_SUCCESSFUL -> "Image downloaded successfully in $directory" + File.separator + titleDirectory + File.separator + file.filename
			else -> "There's nothing to download"
		}
		return msg
	}

	// extension function to get / download bitmap from url
	fun URL.toBitmap(): Bitmap?{
		return BitmapFactory.decodeStream(openStream())
	}
	// extension function to save an image to internal storage
	fun Bitmap.saveToInternalStorage(context : Context,fileInfo: FileInfo?):Uri? {
		// get the context wrapper instance
		val wrapper = ContextWrapper(context)

		// initializing a new file
		// bellow line return a directory in internal storage
		var file = wrapper.getDir("images", Context.MODE_PRIVATE)

		// create a file to save the image
		file = File(file, fileInfo?.id.toString())

		// get the file output stream
		val stream: OutputStream = FileOutputStream(file)

		// compress bitmap
		compress(Bitmap.CompressFormat.JPEG, 10, stream)

		// flush the stream
		stream.flush()

		// close stream
		stream.close()

		// return the saved image uri
		return Uri.parse(file.absolutePath)

	}

}