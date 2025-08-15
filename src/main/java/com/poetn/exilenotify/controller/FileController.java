package com.poetn.exilenotify.controller;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.poetn.exilenotify.service.FileParserService;
import com.poetn.exilenotify.service.FileWatcherService;
import com.poetn.exilenotify.websocket.TradeWebSocketHandler;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Controller
public class FileController {
    private final FileParserService fileParserService;
    private final FileWatcherService fileWatcherService;
    private final TradeWebSocketHandler webSocketHandler;
    
    @Value("${pathurl}")
    private String url;

    private volatile String lastTradeMsg = "Waiting for messages...";
    private CompletableFuture<Void> watcherTask;

    public FileController(FileParserService fileParserService, FileWatcherService fileWatcherService, TradeWebSocketHandler webSocketHandler) {
        this.fileParserService = fileParserService;
        this.fileWatcherService = fileWatcherService;
        this.webSocketHandler = webSocketHandler;
    }

    @PostConstruct
    public void initWatcher() {
        Path filePath = Path.of(url);
        System.out.println("Starting file watcher for: " + url);
        
        Runnable action = () -> {
            try {
                File file = new File(url);
                String tradeMsg = fileParserService.getLastTradeMessage(file);
                
                if (tradeMsg != null && !tradeMsg.equals(lastTradeMsg)) {
                    lastTradeMsg = tradeMsg;
                    webSocketHandler.broadcastMessage(tradeMsg);
                } else {
                }
            } catch (Exception e) {
                lastTradeMsg = "Error reading file: " + e.getMessage();
                webSocketHandler.broadcastMessage(lastTradeMsg);
            }
        };

        watcherTask = CompletableFuture.runAsync(() -> {
            try {
                System.out.println("File watcher task started");
                fileWatcherService.startWatching(filePath, action);
            } catch (Exception e) {
                System.err.println("Error starting watcher: " + e.getMessage());
                lastTradeMsg = "Error starting watcher: " + e.getMessage();
            }
        });
    }

    @PreDestroy
    public void cleanup() {
        fileWatcherService.stopWatching();
        if (watcherTask != null) {
            watcherTask.cancel(true);
        }
    }

    @GetMapping("/")
    public String showPage(Model model) {
        model.addAttribute("tradeMsg", lastTradeMsg);
        model.addAttribute("currentPath", url);
        model.addAttribute("watcherStatus", fileWatcherService.isRunning() ? "Running" : "Stopped");
        return "index";
    }

    @PostMapping("/")
    public String refreshPage(Model model) {
        try {
            File file = new File(url);
            String tradeMsg = fileParserService.getLastTradeMessage(file);
            if (tradeMsg != null) {
                lastTradeMsg = tradeMsg;
            }
        } catch (Exception e) {
            lastTradeMsg = "Error reading file: " + e.getMessage();
        }
        
        model.addAttribute("tradeMsg", lastTradeMsg);
        model.addAttribute("currentPath", url);
        model.addAttribute("watcherStatus", fileWatcherService.isRunning() ? "Running" : "Stopped");
        return "index";
    }

    @PostMapping("/update-path")
    public String updatePath(@RequestParam String filePath, Model model) {
        try {
            Properties props = new Properties();
            props.setProperty("spring.application.name", "ExileNotify");
            props.setProperty("pathurl", filePath);

            try (FileWriter writer = new FileWriter("src/main/resources/application.properties")) {
                props.store(writer, "Updated by user");
            }
            
            this.url = filePath;
            initWatcher();

            model.addAttribute("message", "Path updated successfully! Application restarted with new path.");
            model.addAttribute("messageType", "success");
        } catch (Exception e) {
            model.addAttribute("message", "Error updating path: " + e.getMessage());
            model.addAttribute("messageType", "error");
        }

        model.addAttribute("tradeMsg", lastTradeMsg);
        model.addAttribute("currentPath", url);
        model.addAttribute("watcherStatus", fileWatcherService.isRunning() ? "Running" : "Stopped");
        return "index";
    }
}