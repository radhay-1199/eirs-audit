package org.gl.eir.service;

import com.jcraft.jsch.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gl.eir.configuration.SftpConfigurations;
import org.gl.eir.dto.ListDto;
import org.gl.eir.dto.TacListDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Vector;

@Service
public class SftpService {
    private final Logger log = LogManager.getLogger(getClass());
    @Autowired
    SftpConfigurations sftpConfigurations;

    public Optional<List<ListDto>> downloadAndSaveFile(String datePatternRegex) {
        String sftpServer = sftpConfigurations.getSftpServer();
        int sftpPort = sftpConfigurations.getSftpPort();
        String sftpUsername = sftpConfigurations.getSftpUsername();
        String sftpPassword = sftpConfigurations.getSftpPassword();
        String remoteFolderPath = sftpConfigurations.getRemoteFolderPath();

        List<ListDto> tacList = new ArrayList<>();

        JSch jsch = new JSch();
        Session session = null;
        ChannelSftp sftpChannel = null;

        try {
            session = jsch.getSession(sftpUsername, sftpServer, sftpPort);
            session.setPassword(sftpPassword);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            sftpChannel = (ChannelSftp) session.openChannel("sftp");
            sftpChannel.connect();

            Vector<ChannelSftp.LsEntry> remoteFiles = sftpChannel.ls(remoteFolderPath);

            for (ChannelSftp.LsEntry remoteFile : remoteFiles) {
                String remoteFileName = remoteFile.getFilename();
                System.out.println(remoteFileName);

                if (remoteFileName.matches(datePatternRegex)) {
                    try (InputStream inputStream = sftpChannel.get(remoteFolderPath + "/" + remoteFileName);
                         InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                         BufferedReader reader = new BufferedReader(inputStreamReader)) {
                        String line;
                        boolean isFirstLine = true;
                        while ((line = reader.readLine()) != null) {
                            if (isFirstLine) {
                                isFirstLine = false;
                                continue;
                            }
                            String[] parts = line.split(",", -1);
                            if (parts.length >= 3) {
                                String imeiValue = parts[0].isEmpty() ? null : parts[0];
                                String imsiValue = parts[1].isEmpty() ? null : parts[1];
                                String msisdnValue = parts[2].isEmpty() ? null : parts[2];

                                ListDto dto = ListDto.builder()
                                        .imei(imeiValue)
                                        .imsi(imsiValue)
                                        .msisdn(msisdnValue)
                                        .build();
                                tacList.add(dto);
                            }
                        }
                    }
                    File localFolder = new File(sftpConfigurations.getLocalFolderPath());
                    if (!localFolder.exists()) {
                        localFolder.mkdirs();
                    }
                    File localFile = new File(localFolder.getAbsolutePath() + File.separator + remoteFileName);
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(localFile))) {
                        for (ListDto dto : tacList) {
                            writer.write(dto.toString());
                            writer.newLine();
                        }
                    }

                    log.info("File transferred and saved successfully!");
                    System.out.println("File transferred and saved successfully!");
                    return Optional.of(tacList);
                }
            }

        } catch (JSchException | SftpException | IOException e) {
            e.printStackTrace(); // Handle exceptions appropriately
        } finally {
            if (sftpChannel != null) {
                sftpChannel.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }
        return Optional.empty();
    }


    public Optional<List<TacListDto>> downloadAndSaveTacFile(String datePatternRegex) {
        String sftpServer = sftpConfigurations.getSftpServer();
        int sftpPort = sftpConfigurations.getSftpPort();
        String sftpUsername = sftpConfigurations.getSftpUsername();
        String sftpPassword = sftpConfigurations.getSftpPassword();
        String remoteFolderPath = sftpConfigurations.getRemoteFolderPath();

        List<TacListDto> tacList = new ArrayList<>();

        JSch jsch = new JSch();
        Session session = null;
        ChannelSftp sftpChannel = null;

        try {
            session = jsch.getSession(sftpUsername, sftpServer, sftpPort);
            session.setPassword(sftpPassword);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            sftpChannel = (ChannelSftp) session.openChannel("sftp");
            sftpChannel.connect();

            Vector<ChannelSftp.LsEntry> remoteFiles = sftpChannel.ls(remoteFolderPath);

            for (ChannelSftp.LsEntry remoteFile : remoteFiles) {
                String remoteFileName = remoteFile.getFilename();

                if (remoteFileName.matches(datePatternRegex)) {
                    try (InputStream inputStream = sftpChannel.get(remoteFolderPath + "/" + remoteFileName);
                         InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                         BufferedReader reader = new BufferedReader(inputStreamReader)) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            TacListDto dto = TacListDto.builder()
                                    .tac(line.trim())
                                    .build();
                            tacList.add(dto);
                        }
                    }
                    File localFolder = new File(sftpConfigurations.getLocalFolderPath());
                    if (!localFolder.exists()) {
                        localFolder.mkdirs();
                    }
                    File localFile = new File(localFolder.getAbsolutePath() + File.separator + remoteFileName);
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(localFile))) {
                        for (TacListDto dto : tacList) {
                            writer.write(dto.getTac());
                            writer.newLine();
                        }
                    }

                    log.info("File transferred and saved successfully!");
                    System.out.println("File transferred and saved successfully!");
                    return Optional.of(tacList);
                }
            }

        } catch (JSchException | SftpException | IOException e) {
            e.printStackTrace(); // Handle exceptions appropriately
        } finally {
            if (sftpChannel != null) {
                sftpChannel.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }
        return Optional.empty();
    }

}