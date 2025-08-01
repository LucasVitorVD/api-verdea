package com.verdea.api_verdea.services.resetPassword;

import com.verdea.api_verdea.entities.PasswordResetToken;
import com.verdea.api_verdea.entities.User;
import com.verdea.api_verdea.exceptions.UserNotFoundException;
import com.verdea.api_verdea.repositories.PasswordResetTokenRepository;
import com.verdea.api_verdea.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepo;
    private final UserRepository userRepo;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    @Async
    public void sendResetPasswordEmail(String email) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado"));

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(30));
        tokenRepo.save(resetToken);

        String link = "http://localhost:3000/register/reset-password?token=" + token;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Redefinição de senha");
        message.setText("Clique no link para redefinir sua senha: " + link);
        mailSender.send(message);
    }

    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepo.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token inválido"));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expirado");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepo.save(user);
        tokenRepo.delete(resetToken);
    }
}