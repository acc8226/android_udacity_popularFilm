package com.android.example.popularmovie.netutil;

/**
 * Created by hp on 2017/2/25.
 */

public interface DownloadCallback<T> {

    /**
     * Indicates that the callback handler needs to update its appearance or information based on
     * the result of the task. Expected to be called from the main thread.
     */
    void onSuccess(T result);

    void onError(String errorString);

    /**
     * Indicates that the download operation has finished. This method is called even if the
     * download hasn't completed successfully.
     */
    void finishDownloading();
}

