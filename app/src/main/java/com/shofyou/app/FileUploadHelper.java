package com.shofyou.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;

public class FileUploadHelper {
    private ValueCallback<Uri[]> fileCallback;
    private final ActivityResultLauncher<PickVisualMediaRequest> imagePicker;
    private final ActivityResultLauncher<Intent> videoPicker;

    public FileUploadHelper(ComponentActivity activity) {
        imagePicker = activity.registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(), uris -> {
            if (fileCallback == null) return;
            if (uris != null && !uris.isEmpty()) {
                fileCallback.onReceiveValue(uris.toArray(new Uri[0]));
            } else {
                fileCallback.onReceiveValue(null);
            }
            fileCallback = null;
        });

        videoPicker = activity.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (fileCallback == null) return;
            Uri[] results = null;
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                if (result.getData().getClipData() != null) {
                    int count = result.getData().getClipData().getItemCount();
                    results = new Uri[count];
                    for (int i = 0; i < count; i++) results[i] = result.getData().getClipData().getItemAt(i).getUri();
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
        if (params.getAcceptTypes() != null) {
            for (String t : params.getAcceptTypes()) {
                if (t != null && t.toLowerCase().contains("video")) { isVideo = true; break; }
            }
        }
        if (isVideo) {
            videoPicker.launch(params.createIntent());
        } else {
            imagePicker.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE).build());
        }
        return true;
    }
}
