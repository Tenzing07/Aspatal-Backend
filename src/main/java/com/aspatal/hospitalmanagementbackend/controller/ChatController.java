//package com.aspatal.hospitalmanagementbackend.controller;
//
//import com.aspatal.hospitalmanagementbackend.entity.ChatMessage;
//import com.aspatal.hospitalmanagementbackend.entity.Role;
//import com.aspatal.hospitalmanagementbackend.entity.User;
//import com.aspatal.hospitalmanagementbackend.repository.UserRepository;
//import com.aspatal.hospitalmanagementbackend.service.ChatService;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/chat")
//public class ChatController {
//    private final ChatService chatService;
//    private final UserRepository userRepository;
//
//    public ChatController(ChatService chatService, UserRepository userRepository) {
//        this.chatService = chatService;
//        this.userRepository = userRepository;
//    }
//
//    @PostMapping("/send")
//    public ResponseEntity<?> sendMessage(@RequestBody Map<String, String> payload) {
//        String email = SecurityContextHolder.getContext().getAuthentication().getName();
//        Long senderId = userRepository.findByEmail(email)
//                .map(User::getId)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        // Get the admin user (assuming only one admin exists)
//        Long adminId = userRepository.findByRole(Role.valueOf("ADMIN"))
//                .stream()
//                .findFirst()
//                .map(User::getId)
//                .orElseThrow(() -> new RuntimeException("No admin found"));
//
//        String content = payload.get("content");
//
//        // Patient sends to admin
//        chatService.sendMessage(senderId, adminId, content);
//        return ResponseEntity.ok().body(Map.of("message", "Message sent", "receiverId", adminId));
//    }
//
//    @GetMapping("/history")
//    public List<ChatMessage> getChatHistory() {
//        String email = SecurityContextHolder.getContext().getAuthentication().getName();
//        Long currentUserId = userRepository.findByEmail(email)
//                .map(User::getId)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        // Get admin ID for chat history
//        Long adminId = userRepository.findByRole(Role.valueOf("ADMIN"))
//                .stream()
//                .findFirst()
//                .map(User::getId)
//                .orElseThrow(() -> new RuntimeException("No admin found"));
//
//        return chatService.getChatHistory(currentUserId, adminId);
//    }
//}

package com.aspatal.hospitalmanagementbackend.controller;

import com.aspatal.hospitalmanagementbackend.entity.ChatMessage;
import com.aspatal.hospitalmanagementbackend.entity.Role;
import com.aspatal.hospitalmanagementbackend.entity.Patient;
import com.aspatal.hospitalmanagementbackend.repository.PatientRepository;
import com.aspatal.hospitalmanagementbackend.entity.User;
import com.aspatal.hospitalmanagementbackend.repository.UserRepository;
import com.aspatal.hospitalmanagementbackend.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    private final ChatService chatService;
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;

    public ChatController(ChatService chatService, UserRepository userRepository, PatientRepository patientRepository) {
        this.chatService = chatService;
        this.userRepository = userRepository;
        this.patientRepository=patientRepository;
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(@RequestBody Map<String, String> payload) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Long senderId = userRepository.findByEmail(email)
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Long adminId = userRepository.findByRole(Role.valueOf("ADMIN"))
                .stream()
                .findFirst()
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("No admin found"));

        String content = payload.get("content");
        Long receiverId;

        // If sender is admin, receiver is from payload; else, receiver is admin
        if (senderId.equals(adminId)) {
            receiverId = Long.valueOf(payload.get("receiverId"));
        } else {
            receiverId = adminId;
        }

        chatService.sendMessage(senderId, receiverId, content);
        return ResponseEntity.ok().body(Map.of("message", "Message sent", "receiverId", receiverId));
    }

    @GetMapping("/history")
    public List<ChatMessage> getChatHistory() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Long currentUserId = userRepository.findByEmail(email)
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Long adminId = userRepository.findByRole(Role.valueOf("ADMIN"))
                .stream()
                .findFirst()
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("No admin found"));

        return chatService.getChatHistory(currentUserId, adminId);
    }

    @GetMapping("/adminHistory")
    public List<ChatMessage> getChatHistory(@RequestParam("patientId") Long patientId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Long currentUserId = userRepository.findByEmail(email)
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return chatService.getChatHistory(currentUserId, patientId);
    }

    @GetMapping("/inbox")
    public List<Map<String, Object>> getInbox() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Long adminId = userRepository.findByEmail(email)
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        System.out.println("Fetching inbox for adminId: "+adminId);

        List<ChatMessage> messages = chatService.getMessagesInvolvingUser(adminId);
        System.out.println("Messages involving admin: "+ messages.size());

        List<Map<String, Object>> inbox = messages.stream()
                .map(msg -> msg.getSender().getId().equals(adminId) ? msg.getReceiver() : msg.getSender())
                .distinct()
                .map(user -> {
                    ChatMessage lastMessage = messages.stream()
                            .filter(msg ->
                                    (msg.getSender().getId().equals(adminId) && msg.getReceiver().getId().equals(user.getId())) ||
                                            (msg.getSender().getId().equals(user.getId()) && msg.getReceiver().getId().equals(adminId)))
                            .sorted((m1, m2) -> m2.getSentAt().compareTo(m1.getSentAt()))
                            .findFirst()
                            .orElse(null);

                    if (lastMessage == null) {
                        System.out.println("No messages found for user "+ user.getId());
                        return null;
                    }

                    String patientName = patientRepository.findByUserId(user.getId())
                            .map(Patient::getName)
                            .orElse("Unknown Patient");

                    Map<String, Object> inboxEntry = new HashMap<>();
                    inboxEntry.put("patientId", user.getId());
                    inboxEntry.put("patientName", patientName);
                    inboxEntry.put("lastMessage", lastMessage.getContent());
                    inboxEntry.put("lastMessageTime", lastMessage.getSentAt());
                    System.out.println("Added inbox entry for user "+ user.getId()+" : "+ inboxEntry);
                    return inboxEntry;
                })
                .filter(inboxEntry -> inboxEntry != null)
                .collect(Collectors.toList());

        System.out.println("Final inbox size: "+ inbox.size());
        System.out.println("Final inbox content: {}"+ inbox);
        return inbox;
    }
}