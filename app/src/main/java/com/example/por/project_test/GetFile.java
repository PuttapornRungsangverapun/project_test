package com.example.por.project_test;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by User on 31/3/2560.
 */

public class GetFile {
    private Context ctx;

    GetFile(Context ctx) {
        this.ctx = ctx;
    }

    public byte[] getData(Uri uri) {//อ่านไฟล์โดยให้pathไปreturnเป็นbyte binaryกลับมา
        byte[] result = null;
        try {
            InputStream inputStream = ctx.getContentResolver().openInputStream(uri);

            if (inputStream != null && inputStream.available() > 10e6) {
                Toast.makeText(ctx, "File size must be 10mb", Toast.LENGTH_SHORT).show();
                inputStream.close();
                return null;
            }
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[16384];//อ่านทั้ฝหมด16kb ในเgoogleบอกเร็วสุด
            if (inputStream != null) {
                while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
            }
            buffer.flush();//เขียนข้อไปให้หมด

            result = buffer.toByteArray();
            if (inputStream != null) {
                inputStream.close();//ถ้าไม่ปิดแสดงว่ามีคนใช้อยู่จะลบไม่ได้
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = ctx.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

}
