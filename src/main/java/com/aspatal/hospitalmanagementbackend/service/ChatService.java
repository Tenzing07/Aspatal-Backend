//
//package com.aspatal.hospitalmanagementbackend.service;
//
//import com.aspatal.hospitalmanagementbackend.entity.ChatMessage;
//import com.aspatal.hospitalmanagementbackend.entity.User;
//import com.aspatal.hospitalmanagementbackend.repository.ChatMessageRepository;
//import com.aspatal.hospitalmanagementbackend.repository.UserRepository;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.Optional;
//
//@Service
//public class ChatService {
//    private final ChatMessageRepository chatMessageRepository;
//    private final UserRepository userRepository;
//
//    public ChatService(ChatMessageRepository chatMessageRepository, UserRepository userRepository) {
//        this.chatMessageRepository = chatMessageRepository;
//        this.userRepository = userRepository;
//    }
//
//    public void sendMessage(Long senderId, Long receiverId, String content) {
//        Optional<User> senderOpt = userRepository.findById(senderId);
//        Optional<User> receiverOpt = userRepository.findById(receiverId);
//
//        if (senderOpt.isEmpty() || receiverOpt.isEmpty()) {
//            throw new RuntimeException("Sender or receiver not found");
//        }
//
//        ChatMessage message = new ChatMessage();
//        message.setSender(senderOpt.get());
//        message.setReceiver(receiverOpt.get());
//        message.setContent(content);
//        chatMessageRepository.save(message);
//    }
//
//    public List<ChatMessage> getChatHistory(Long userId1, Long userId2) {
//        return chatMessageRepository.findBySenderIdAndReceiverIdOrReceiverIdAndSenderId(userId1, userId2, userId2, userId1);
//    }
//}

package com.aspatal.hospitalmanagementbackend.service;

import com.aspatal.hospitalmanagementbackend.entity.ChatMessage;
import com.aspatal.hospitalmanagementbackend.entity.User;
import com.aspatal.hospitalmanagementbackend.repository.ChatMessageRepository;
import com.aspatal.hospitalmanagementbackend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ChatService {
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    public ChatService(ChatMessageRepository chatMessageRepository, UserRepository userRepository) {
        this.chatMessageRepository = chatMessageRepository;
        this.userRepository = userRepository;
    }

    public void sendMessage(Long senderId, Long receiverId, String content) {
        Optional<User> senderOpt = userRepository.findById(senderId);
        Optional<User> receiverOpt = userRepository.findById(receiverId);

        if (senderOpt.isEmpty() || receiverOpt.isEmpty()) {
            throw new RuntimeException("Sender or receiver not found");
        }

        ChatMessage message = new ChatMessage();
        message.setSender(senderOpt.get());
        message.setReceiver(receiverOpt.get());
        message.setContent(content);
        chatMessageRepository.save(message);
    }

    public List<ChatMessage> getChatHistory(Long userId1, Long userId2) {
        return chatMessageRepository.findBySenderIdAndReceiverIdOrSenderIdAndReceiverIdOrderBySentAtAsc(
                userId1, userId2, userId2, userId1
        );
    }

    public List<ChatMessage> getMessagesInvolvingUser(Long userId) {
        return chatMessageRepository.findBySenderIdOrReceiverId(userId, userId);
    }
}