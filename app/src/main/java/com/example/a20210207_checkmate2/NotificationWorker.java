package com.example.a20210207_checkmate2;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;
import com.google.common.util.concurrent.ListenableFuture; // This will now work

public class NotificationWorker extends ListenableWorker {

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    // FIXED: Removed the long firebase-relocation path
    public ListenableFuture<Result> startWork() {
        return CallbackToFutureAdapter.getFuture(completer -> {
            Log.d("CheckmateNotification", "Worker: Start fetching glucose data...");

            NotificationHelper notificationHelper = new NotificationHelper(getApplicationContext());

            notificationHelper.createNotification(() -> {
                Log.d("CheckmateNotification", "Worker: Data received, notification shown.");
                completer.set(Result.success());
            });

            return "NotificationWorkerTask";
        });
    }
}