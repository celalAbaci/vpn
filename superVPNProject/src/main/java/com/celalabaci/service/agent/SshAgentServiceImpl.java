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
            config.put("StrictHostKeyChecking", "no"); // Skip host key checking (Careful in Prod)
            config.put("PreferredAuthentications", "publickey,keyboard-interactive,password");
            session.setConfig(config);
            session.setTimeout(10000); // 10 seconds timeout

            session.connect();
            log.info("SSH Connection Successful: {}@{}", username, serverIp);
        } catch (Exception e) {
            log.error("SSH Connection Error: {}", e.getMessage());
            throw new SshConnectionException(MessageType.SSH_CONNECTION_FAILED, "Could not connect to server: " + e.getMessage());
        }
    }

    @Override
    public String runCommand(String command) {
        if (session == null || !session.isConnected()) {
            throw new SshConnectionException(MessageType.SSH_CONNECTION_FAILED, "No active SSH session.");
        }

        StringBuilder outputBuffer = new StringBuilder();
        try {
            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            channel.setInputStream(null);
            channel.setErrStream(System.err);

            InputStream in = channel.getInputStream();
            channel.connect();

            // Read output
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    outputBuffer.append(line).append("\n");
                }
            }
            channel.disconnect();
            return outputBuffer.toString();

        } catch (Exception e) {
            log.error("Command Execution Error: {}", command, e);
            throw new SshConnectionException(MessageType.SSH_CONNECTION_FAILED, "Command error: " + e.getMessage());
        }
    }

    @Override
    public void disconnect() {
        if (session != null && session.isConnected()) {
            session.disconnect();
            log.info("SSH Connection Closed.");
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
