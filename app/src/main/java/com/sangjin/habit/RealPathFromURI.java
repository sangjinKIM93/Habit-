package com.sangjin.habit;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

import androidx.core.content.ContextCompat;

import java.io.File;

public class RealPathFromURI {

    private static String TAG = "RealPathFromURI";

    // URI 로부터 Path를 얻어오는 메소드
    public static String getRealPathFromURI(final Context context, final Uri uri) {

        // DocumentProvider
        // DocumentsContract는 documents provider 와 플랫폼 사이의 계약을 제한한다.
        if (DocumentsContract.isDocumentUri(context, uri)) {

            // ExternalStorageProvider.. 가져온 uri가 외부저장소에서 온 것이라면
            if (isExternalStorageDocument(uri)) {

                Log.d(TAG, "ExternalStoragaeProvider");
                // 이 파일의 아이디를 불러오고,
                final String docId = DocumentsContract.getDocumentId(uri);
                // uri를 : 기준으로 잘라준다음,
                final String[] split = docId.split(":");
                // 타입을 알아본다.
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    Log.d(TAG, "primary");
                    // 파일타입이 primary라면 파일의 절대경로와 그 파일위치를 붙여준다.
                    return context.getExternalFilesDir(null).getAbsolutePath() + "/"
                            + split[1];
                } else {
                    // primary가 아니라면 SD카드 경로와 파일위치를 붙여준다.
                    Log.d(TAG, "not primary");
                    String SDcardpath = getRemovableSDCardPath(context).split("/Android")[0];
                    return SDcardpath +"/"+ split[1];
                }
            }

            // DownloadsProvider.. 가져온 uri가 다운로드에 있는 파일이라면
            else if (isDownloadsDocument(uri)) {
                Log.d(TAG, "DownloadsProvider");
                // 파일의 아이디를 가져오고
                final String id = DocumentsContract.getDocumentId(uri);
                // 다운로드에서 가져온 uri를 표시할 수 있는 문구를 넣어서 합쳐준다.
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }

            // MediaProvider.. 가져온 uri가 미디어 파일이라면
            else if (isMediaDocument(uri)) {
                Log.d(TAG, "MediaProvider");
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] { split[1] };

                return getDataColumn(context, contentUri, selection,
                        selectionArgs);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();
            return getDataColumn(context, uri, null, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }


    public static String getRemovableSDCardPath(Context context) {
        File[] storages = ContextCompat.getExternalFilesDirs(context, null);
        if (storages.length > 1 && storages[0] != null && storages[1] != null)
            return storages[1].toString();
        else
            return "";
    }


    public static String getDataColumn(Context context, Uri uri,
                                       String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = { column };

        try {
            cursor = context.getContentResolver().query(uri, projection,
                    selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri
                .getAuthority());
    }


    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri
                .getAuthority());
    }


    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri
                .getAuthority());
    }


    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri
                .getAuthority());
    }


}
