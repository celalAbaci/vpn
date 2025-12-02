package com.celalabaci.service.agent;

import com.celalabaci.exception.MessageType;
import com.celalabaci.exception.SshConnectionException;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

@Slf4j
@Service
public class SshAgentServiceImpl implements ISshAgentService {

    private Session session;

    @Override
    public void connect(String serverIp, String username, String password) {
        try {
            JSch jsch = new JSch();
            session = jsch.getSession(username, serverIp, 22);
            session.setPassword(password);

            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no"); // Host key doğrulamasını atla (Prod'da dikkatli olunmalı)
            session.setConfig(config);
            session.setTimeout(10000); // 10 saniye timeout

            session.connect();
            log.info("SSH Bağlantısı Başarılı: {}@{}", username, serverIp);
        } catch (Exception e) {
            log.error("SSH Bağlantı Hatası: {}", e.getMessage());
            throw new SshConnectionException(MessageType.SSH_CONNECTION_FAILED, "Sunucuya bağlanılamadı: " + e.getMessage());
        }
    }

    @Override
    public String runCommand(String command) {
        if (session == null || !session.isConnected()) {
            throw new SshConnectionException(MessageType.SSH_CONNECTION_FAILED, "Aktif bir SSH oturumu yok.");
        }

        StringBuilder outputBuffer = new StringBuilder();
        try {
            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            channel.setInputStream(null);
            channel.setErrStream(System.err);

            InputStream in = channel.getInputStream();
            channel.connect();

            // Çıktıyı oku
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    outputBuffer.append(line).append("\n");
                }
            }
            channel.disconnect();
            return outputBuffer.toString();

        } catch (Exception e) {
            log.error("Komut Çalıştırma Hatası: {}", command, e);
            throw new SshConnectionException(MessageType.SSH_CONNECTION_FAILED, "Komut hatası: " + e.getMessage());
        }
    }

    @Override
    public void disconnect() {
        if (session != null && session.isConnected()) {
            session.disconnect();
            log.info("SSH Bağlantısı Kapatıldı.");
        }
    }

    @Override
    public String runSingleCommand(String serverIp, String username, String password, String command) {
        try {
            connect(serverIp, username, password);
            return runCommand(command);
        } finally {
            disconnect();
        }
    }
}