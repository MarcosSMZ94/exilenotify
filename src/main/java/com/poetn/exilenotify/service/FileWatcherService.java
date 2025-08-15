package com.poetn.exilenotify.service;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.stereotype.Service;

@Service
public class FileWatcherService {
    private final AtomicBoolean running = new AtomicBoolean(false);
    private WatchService watchService;

    public void startWatching(Path filePath, Runnable action) throws IOException, InterruptedException {
        Path folder = filePath.getParent();
        String fileName = filePath.getFileName().toString();

        watchService = FileSystems.getDefault().newWatchService();
        folder.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
        
        running.set(true);

        try {
            while (running.get()) { 
                WatchKey key = watchService.take();

                for (WatchEvent<?> event : key.pollEvents()) {
                    if (event.kind() == StandardWatchEventKinds.OVERFLOW) {
                        continue;
                    }

                    Path modifiedFile = (Path) event.context();

                    if (modifiedFile.toString().equals(fileName)) {
                        action.run();
                    }
                }

                boolean valid = key.reset();
                if (!valid) {
                    break;
                }
            }
        } finally {
            closeWatchService();
        }
    }

    public void stopWatching() {
        running.set(false);
        closeWatchService();
    }

    private void closeWatchService() {
        if (watchService != null) {
            try {
                watchService.close();
            } catch (IOException e) {
            }
        }
    }

    public boolean isRunning() {
        return running.get();
    }
}

