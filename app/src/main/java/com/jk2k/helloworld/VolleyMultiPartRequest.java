package com.jk2k.helloworld;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * A canned request for retrieving the response body at a given URL as a String.
 */
public class VolleyMultiPartRequest extends Request<String> {

    private static final String TAG = "UploadServiceDemo";
    private final Listener<String> mListener;
    // POST param
    private Map<String, String> mParams = null;
    // upload file
    private Map<String, String> mFileUploads = null;
    public static final int TIMEOUT_MS = 30000;
    // delimiter
    private final String mBoundary = "Volley-" + System.currentTimeMillis();
    private final String lineEnd = "\r\n";
    private final String twoHyphens = "--";
    // output stream
    ByteArrayOutputStream mOutputStream = new ByteArrayOutputStream();

    /**
     * Creates a new request with the given method.
     *
     * @param url           URL to fetch the string at
     * @param listener      Listener to receive the String response
     * @param errorListener Error listener, or null to ignore errors
     */
    public VolleyMultiPartRequest(String url, Listener<String> listener, ErrorListener errorListener) {

        super(Method.POST, url, errorListener);
        // set retry policy
        setRetryPolicy(new DefaultRetryPolicy(TIMEOUT_MS, 1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        mListener = listener;
        mParams = new HashMap<String, String>();
        mFileUploads = new HashMap<String, String>();
    }

    /**
     * 参数开头的分隔符
     *
     * @throws IOException
     */
    private void writeFirstBoundary() throws IOException {
        mOutputStream.write((twoHyphens + mBoundary + lineEnd).getBytes());
    }

    @Override
    public String getBodyContentType() {
        return "multipart/form-data;boundary=" + mBoundary;
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        try {
            // 输出流
            DataOutputStream dos = new DataOutputStream(mOutputStream);

            // 上传参数
            for (String key : mParams.keySet()) {
                writeFirstBoundary();

                final String paramType = "Content-Type: text/plain; charset=UTF-8" + lineEnd;
                dos.writeBytes(paramType);
                dos.writeBytes("Content-Disposition: form-data; name=\"" + key + "\"" + lineEnd);
                dos.writeBytes("Content-Transfer-Encoding: 8bit\r\n\r\n");
                String value = mParams.get(key);
                dos.writeBytes(URLEncoder.encode(value, "UTF-8"));

                dos.writeBytes(lineEnd);
            }

            // 上传文件
            for (String key : mFileUploads.keySet()) {
                writeFirstBoundary();

                File file = new File(mFileUploads.get(key));
                final String type = "Content-Type: application/octet-stream" + lineEnd;
                dos.writeBytes(type);
                String fileName = file.getName();
                dos.writeBytes("Content-Disposition: form-data; name=\"" + key + "\"; filename=\"" + URLEncoder.encode(fileName, "UTF-8") + "\"" + lineEnd);
                dos.writeBytes("Content-Transfer-Encoding: binary\r\n\r\n");

                FileInputStream fin = new FileInputStream(file);
                final byte[] tmp = new byte[4096];
                int len = 0;
                while ((len = fin.read(tmp)) != -1) {
                    mOutputStream.write(tmp, 0, len);
                }
                fin.close();

                dos.writeBytes(lineEnd);
            }

            // send multipart form data necessary after file data...
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + mBoundary + twoHyphens + lineEnd);

            return mOutputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Add a parameter to be sent in the multipart request
     *
     * @param name  The name of the parameter
     * @param value the value of the parameter
     */
    public void addParam(String name, String value) {
        mParams.put(name, value);
    }

    /**
     * Add a file to be uploaded in the multipart request
     *
     * @param name     The name of the file key
     * @param filePath The path to the file. This file MUST exist.
     */
    public void addFile(String name, String filePath) {
        mFileUploads.put(name, filePath);
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        String parsed;
        try {
            parsed = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
        } catch (UnsupportedEncodingException e) {
            parsed = new String(response.data);
        }
        return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(String response) {
        mListener.onResponse(response);
    }
}

