package com.shofyou.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

public class FileUploadHelper {

    private ValueCallback<Uri[]> fileCallback;
    private ActivityResultLauncher<Intent> launcher;

    public FileUploadHelper(Activity activity) {
        launcher = ((androidx.activity.ComponentActivity) activity)
                .registerForActivityResult(
                        new ActivityResultContracts.StartActivityForResult(),
                        result -> {
                            if (fileCallback == null) return;
                            Uri[] results = null;

                            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                                // دعم الاختيار المتعدد
                                if (result.getData().getClipData() != null) {
                                    int count = result.getData().getClipData().getItemCount();
                                    results = new Uri[count];
                                    for (int i = 0; i < count; i++) {
                                        results[i] = result.getData().getClipData().getItemAt(i).getUri();
                                    }
                                } else if (result.getData().getData() != null) {
                                    results = new Uri[]{result.getData().getData()};
                                }
                            }
                            fileCallback.onReceiveValue(results);
                            fileCallback = null;
                        });
    }

    public boolean handleFileChooser(ValueCallback<Uri[]> callback, WebChromeClient.FileChooserParams params) {
        fileCallback = callback;
        boolean isVideo = false;

        String[] types = params.getAcceptTypes();
        if (types != null) {
            for (String t : types) {
                if (t != null && t.toLowerCase().contains("video")) {
                    isVideo = true;
                    break;
                }
            }
        }

        Intent intent;
        if (isVideo) {
            // فتح معرض الفيديو مباشرة
            intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
            intent.setType("video/*");
        } else {
            // فتح معرض الصور مباشرة (بدون مدير ملفات)
            intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
        }

        // تفعيل خاصية الاختيار المتعدد
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

        launcher.launch(intent);
        return true;
    }
}
