package com.abc.facultyload.service;

import com.abc.facultyload.entity.PasswordResetToken;
import com.abc.facultyload.entity.User;
import com.abc.facultyload.exception.AppException;
import com.abc.facultyload.repository.PasswordResetTokenRepository;
import com.abc.facultyload.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    @Value("${spring.mail.from:no-reply@abcengg.com}")
    private String fromEmail;

    @Transactional
    public void sendPasswordResetEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException("No account found with that email address", HttpStatus.NOT_FOUND));

        // Invalidate any previous tokens for this user
        tokenRepository.deleteAllByUserId(user.getId());

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .used(false)
                .build();
        tokenRepository.save(resetToken);

        String resetLink = "http://localhost:3000/reset-password?token=" + token;
        sendEmail(user.getEmail(), user.getName(), resetLink);
    }

    private void sendEmail(String to, String name, String resetLink) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Password Reset Request – Faculty Load Analyzer");
            helper.setText("""
                    <html>
                    <body style="font-family:Arial,sans-serif;background:#f4f6fa;padding:32px">
                      <div style="max-width:500px;margin:auto;background:#fff;border-radius:12px;padding:32px;box-shadow:0 2px 16px #0001">
                        <h2 style="color:#0f3460;margin-bottom:8px">Password Reset</h2>
                        <p>Dear <b>%s</b>,</p>
                        <p>We received a request to reset your Faculty Load Analyzer account password. Click the button below to set a new password. This link expires in <b>10 minutes</b>.</p>
                        <div style="text-align:center;margin:32px 0">
                          <a href="%s" style="background:#0f3460;color:#fff;padding:14px 32px;border-radius:8px;text-decoration:none;font-size:16px;font-weight:bold">Reset Password</a>
                        </div>
                        <p style="font-size:13px;color:#888">If you did not request this, you can ignore this email — your password will not change.</p>
                        <hr style="border:none;border-top:1px solid #eee;margin:24px 0">
                        <p style="font-size:12px;color:#aaa">Faculty Load Analyzer &mdash; ABC Engineering College</p>
                      </div>
                    </body>
                    </html>
                    """.formatted(name != null ? name : "User", resetLink), true);
            mailSender.send(message);
            log.info("Password reset email sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send password reset email: {}", e.getMessage(), e);
            throw new AppException("Could not send reset email: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new AppException("Invalid or expired reset link", HttpStatus.BAD_REQUEST));

        if (resetToken.isUsed()) {
            throw new AppException("This reset link has already been used", HttpStatus.BAD_REQUEST);
        }
        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AppException("Reset link has expired. Please request a new one.", HttpStatus.BAD_REQUEST);
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new AppException("Password must be at least 6 characters", HttpStatus.BAD_REQUEST);
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
        log.info("Password reset successful for user {}", user.getEmail());
    }
}
