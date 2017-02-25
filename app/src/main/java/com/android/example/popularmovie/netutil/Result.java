package com.android.example.popularmovie.netutil;


/**
 * Wrapper class that serves as a union of a result value and an exception. When the download
 * task has completed, either the result value or exception can be a non-null value.
 * This allows you to pass exceptions to the UI thread that were thrown during doInBackground().
 */
public class Result {

    public String mResultValue;
    public Exception mException;
    public Result(String resultValue) {
        mResultValue = resultValue;
    }
    public Result(Exception exception) {
        mException = exception;
    }
}
