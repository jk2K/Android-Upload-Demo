package com.jk2k.helloworld;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.alexbbb.uploadservice.AbstractUploadServiceReceiver;
import com.alexbbb.uploadservice.ContentType;
import com.alexbbb.uploadservice.UploadRequest;
import com.alexbbb.uploadservice.UploadService;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONObject;

import java.security.cert.LDAPCertStoreParameters;
import java.util.ArrayList;
import java.util.UUID;

import me.iwf.photopicker.PhotoPickerActivity;
import me.iwf.photopicker.utils.PhotoPickerIntent;

public class MainActivity extends AppCompatActivity {
    public final static int REQUEST_CODE = 1;
    private static final String TAG = "UploadServiceDemo";
    private String photoPath = null;
    private EditText paramName;
    private String paramNameString = "";
    private String serverUrl = "http://posttestserver.com/post.php?dir=example";
    private final AbstractUploadServiceReceiver uploadReceiver = new AbstractUploadServiceReceiver() {

        @Override
        public void onProgress(String uploadId, int progress) {
            Log.i(TAG, "The progress of the upload with ID " + uploadId + " is: " + progress);
        }

        @Override
        public void onError(String uploadId, Exception exception) {
            String message = "Error in upload with ID: " + uploadId + ". " + exception.getLocalizedMessage();
            Log.e(TAG, message, exception);
        }

        @Override
        public void onCompleted(String uploadId, int serverResponseCode, String serverResponseMessage) {
            uploadSuccess();
            Log.d(TAG, serverResponseMessage);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        uploadReceiver.register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        uploadReceiver.unregister(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        paramName = (EditText) findViewById(R.id.parameterName);

        findViewById(R.id.selectPhotoButton).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                PhotoPickerIntent intent = new PhotoPickerIntent(MainActivity.this);
                intent.setPhotoCount(1);
                intent.setShowCamera(true);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

        findViewById(R.id.uploadServiceButton).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // android-upload-service
                if (!userInputIsValid()) {
                    return;
                }
                ;
                final UploadRequest request = new UploadRequest(MainActivity.this,
                        UUID.randomUUID().toString(),
                        serverUrl);
                request.addFileToUpload(photoPath,
                        paramNameString,
                        "test",
                        ContentType.APPLICATION_OCTET_STREAM);


                // set a custom user agent string for the upload request
                // if you comment the following line, the system default user-agent will be used
                request.setCustomUserAgent("UploadServiceDemo/1.0");

                try {
                    //Start upload service and display the notification
                    UploadService.startUpload(request);

                } catch (Exception exc) {
                    //You will end up here only if you pass an incomplete UploadRequest
                    Log.e("AndroidUploadService", exc.getLocalizedMessage(), exc);
                }
            }
        });

        findViewById(R.id.volleyButton).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // volley
                if (!userInputIsValid()) {
                    return;
                }
                VolleyMultiPartRequest request = new VolleyMultiPartRequest(
                        serverUrl,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.d(TAG, response);
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d(TAG, error.getLocalizedMessage());
                            }
                        }
                );
                request.addFile(paramNameString, photoPath);
                HelloApplication.getInstance().getRequestQueue().add(request);
            }
        });

        findViewById(R.id.okHttpButton).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // OKHttp
                if (!userInputIsValid()) {
                    return;
                }
                ;
            }
        });
    }

    /**
     * 上传成功
     */
    private void uploadSuccess() {
        Snackbar.make(findViewById(android.R.id.content), "上传成功", Snackbar.LENGTH_SHORT).show();
    }

    private boolean userInputIsValid() {
        // 隐藏软键盘, 防止遮住了 SnackBar 的提示
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        if (photoPath == null) {
            Snackbar.make(findViewById(android.R.id.content), "请选择图片", Snackbar.LENGTH_SHORT).show();
            return false;
        }
        paramNameString = paramName.getText().toString();
        if (paramNameString.length() == 0) {
            Snackbar.make(findViewById(android.R.id.content), "请填写参数信息", Snackbar.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        ArrayList<String> photos = null;
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
            if (data != null) {
                photos = data.getStringArrayListExtra(PhotoPickerActivity.KEY_SELECTED_PHOTOS);
                photoPath = photos.get(0);
                Log.i("test", photoPath);
            }
        }
    }
}
