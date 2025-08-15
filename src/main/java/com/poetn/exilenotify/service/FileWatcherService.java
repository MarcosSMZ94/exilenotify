package com.poetn.exilenotify.service;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import org.springframework.stereotype.Service;

@Service
public class FileWatcherService {
    public void fileWatcher(Path filePath, Runnable action) throws IOException, InterruptedException {
       Path folder = filePath.getParent();
       String fileName = filePath.getFileName().toString();

        WatchService watchService = FileSystems.getDefault().newWatchService();
        folder.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

        while (true) { 
            WatchKey key = watchService.take();

            for(WatchEvent<?> event : key.pollEvents()) {
                Path alterFile = (Path) event.context();

                if (alterFile.toString().equals(fileName)) {
                    action.run();
                }
            }

            boolean valid = key.reset();
            if (!valid) break;
        }
    }
}

