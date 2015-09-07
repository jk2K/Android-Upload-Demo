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
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * A canned request for retrieving the response body at a given URL as a String.
 */
public class VolleyMultiPartRequest extends Request<String> {

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
    protected Map<String, String> getParams() throws AuthFailureError {
        return mParams;
    }

    @Override
    public String getBodyContentType() {
        return "multipart/form-data;boundary=" + mBoundary;
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        // 输出流
        DataOutputStream dos = new DataOutputStream(mOutputStream);
        try {
            writeFirstBoundary();
            dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                    + "ic_action_file_attachment_light.png" + "\"" + lineEnd);
            dos.writeBytes(lineEnd);

            dos.writeBytes("hhhahhaha");

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

