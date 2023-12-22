package org.gl.eir.service;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gl.eir.builder.ModulesAuditTrailBuilder;
import org.gl.eir.dto.ListDto;
import org.gl.eir.dto.TacListDto;
import org.gl.eir.enums.EirListAction;
import org.gl.eir.enums.EirListType;
import org.gl.eir.enums.ListName;
import org.gl.eir.enums.ListType;
import org.gl.eir.model.app.CfgFeatureAlert;
import org.gl.eir.model.audit.ModulesAuditTrail;
import org.gl.eir.repository.app.CfgFeatureAlertRepository;
import org.gl.eir.repository.app.GenericRepository;
import org.gl.eir.repository.audit.ModulesAuditTrailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class EirAuditService implements Runnable{

    private final Logger log = LogManager.getLogger(getClass());

    private final String action;

    public EirAuditService(String action) {
        this.action = action;
    }

    @Value("${report.file.path}")
    private String csvFilePath;

    @Autowired
    SftpService sftpService;
    @Autowired
    GenericRepository genericRepository;
    @Autowired
    ModulesAuditTrailRepository modulesAuditTrailRepository;
    @Autowired
    CfgFeatureAlertRepository cfgFeatureAlertRepository;

//    ListName.BLOCKEDLIST black, ListName.EXCEPTIONLIST white, ListName.TRACKEDLIST grey

    private final String featureName = "EIR AUDIT MODULE";

    @Override
    public void run() {
        int moduleAudiTrailId = 0;
        int listSuccessCount = 0;
        int tacListSuccessCount = 0;
        LocalDateTime startTime = LocalDateTime.now();
        ModulesAuditTrail startAudit = ModulesAuditTrailBuilder.forInsert(201, "Initial", "NA", featureName, "INSERT", "Started " + featureName +":"+ action+" process", startTime);
        startAudit = modulesAuditTrailRepository.save(startAudit);
        moduleAudiTrailId = startAudit.getId();
        try {
            String serverName = getHostname();
            List<ListName> listNames = Arrays.asList(ListName.BLOCKEDLIST, ListName.EXCEPTIONLIST, ListName.TRACKEDLIST);
            List<ListName> tacListNames = Arrays.asList(ListName.ALLOWEDTACLIST, ListName.BLOCKEDTACLIST);
            List<Map<String, String>> urls = genericRepository.getListOfUrls();
            for(ListName listName: listNames) {
                if(action.equalsIgnoreCase(EirListAction.START.name())){
                    for (Map<String, String> urlMap : urls) {
                        String configKey = urlMap.get("config_key");
                        String configValue = urlMap.get("config_value");
                        String modifiedUrl = configValue
                                .replace("<LIST>", EirListType.fromListName(listName).name())
                                .replace("<ACTION>", EirListAction.START.name());

                        log.info("Modified URL for " + configKey + ": " + modifiedUrl);
                        sendGetRequest(modifiedUrl);
                    }
                } else if(action.equalsIgnoreCase(EirListAction.DOWNLOAD.name())) {
                    String fileName = generateFileName(listName, ListType.FULL);
                    Optional<List<ListDto>> eirsList = sftpService.downloadAndSaveFile(fileName);
                    if (eirsList.isPresent()){
                        for (Map<String, String> urlMap : urls) {
                            String configKey = urlMap.get("config_key");
                            String configValue = urlMap.get("config_value");
                            String modifiedUrl = configValue
                                    .replace("<LIST>", EirListType.fromListName(listName).name())
                                    .replace("<ACTION>", EirListAction.DOWNLOAD.name());

                            log.info("Modified URL for " + configKey + ": " + modifiedUrl);
                            Optional<List<ListDto>> eirList = Optional.ofNullable(getListDataFromUrl(modifiedUrl));
                            if (!eirList.isPresent()) {
                                log.info("File not found for list: "+listName);
                                continue;
                            }
                            listSuccessCount = generateListReport(listName, serverName, eirsList.get(), eirList.get(), configKey);

                        }
                    }
                } else {
                    throw new IllegalArgumentException("Invalid action argument provided");
                }

            }

            for(ListName listName: tacListNames) {
                if (action.equalsIgnoreCase(EirListAction.START.name())) {
                    for (Map<String, String> urlMap : urls) {
                        String configKey = urlMap.get("config_key");
                        String configValue = urlMap.get("config_value");
                        String modifiedUrl = configValue
                                .replace("<LIST>", EirListType.fromListName(listName).name())
                                .replace("<ACTION>", "START");

                        log.info("Modified URL for " + configKey + ": " + modifiedUrl);
                        sendGetRequest(modifiedUrl);
                    }
                } else if(action.equalsIgnoreCase(EirListAction.DOWNLOAD.name())) {
                String fileName = generateFileName(listName, ListType.FULL);
                Optional<List<TacListDto>> eirsList = sftpService.downloadAndSaveTacFile(fileName);
                if (eirsList.isPresent()){
                    for (Map<String, String> urlMap : urls) {
                        String configKey = urlMap.get("config_key");
                        String configValue = urlMap.get("config_value");
                        String modifiedUrl = configValue
                                .replace("<LIST>", EirListType.fromListName(listName).name())
                                .replace("<ACTION>", EirListAction.DOWNLOAD.name());

                        log.info("Modified URL for " + configKey + ": " + modifiedUrl);
                        Optional<List<TacListDto>> eirList = Optional.ofNullable(getTacListDataFromUrl(modifiedUrl));
                        if (!eirList.isPresent()) {
                            log.info("File not found for list: " + listName);
                            continue;
                        }
                        tacListSuccessCount = generateTacListReport(listName, serverName, eirsList.get(), eirList.get(), configKey);
                    }
                }
            } else {
                    throw new IllegalArgumentException("Invalid action argument provided");
                }
            }
            ModulesAuditTrail tacAudit = ModulesAuditTrailBuilder.forUpdate(moduleAudiTrailId, 200,  "Success", "NA", featureName, "UPDATE", "Process Completed for "+ featureName+":"+action, listSuccessCount , tacListSuccessCount, startTime);
            modulesAuditTrailRepository.save(tacAudit);
        } catch(Exception ex) {
            log.error("Exception in audit process: {}", ex);
            Optional<CfgFeatureAlert> alert = cfgFeatureAlertRepository.findByAlertId("alert1604");
            log.error("Raising 1604");
            System.out.println("Raising 1604");
            if (alert.isPresent()) {
                raiseAnAlert(alert.get().getAlertId(), ex.getMessage(), featureName, 0);
            }
            ex.printStackTrace();
            if(moduleAudiTrailId == 0) {
                ModulesAuditTrail audit = ModulesAuditTrailBuilder.forInsert(501, "NA", ex.getMessage(), featureName, "INSERT", "Exception during " + featureName + ":"+action+" process", startTime);
                modulesAuditTrailRepository.save(audit);
            } else {
                ModulesAuditTrail tacAudit = ModulesAuditTrailBuilder.forUpdate(moduleAudiTrailId, 501, "NA", ex.getMessage(), featureName, "UPDATE", "Exception during " + featureName + ":"+action+" process", 0 , 0, startTime);
                modulesAuditTrailRepository.save(tacAudit);
            }
        }

    }

    public static String getHostname() {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            return localHost.getHostName();
        } catch (Exception ex) {
            return "NA";
        }
    }

    public static String generateFileName(ListName listName, ListType type) {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String formattedDate = yesterday.format(formatter);
        return listName + "_" + type + "_" + formattedDate + "\\d{2}\\.csv";
    }

    private List<ListDto> getListDataFromUrl(String url) throws Exception {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        con.setRequestMethod("GET");

        int responseCode = con.getResponseCode();
        if (responseCode == 200) {
            try (InputStream in = con.getInputStream();
                 InputStreamReader reader = new InputStreamReader(in);
                 CSVReader csvReader = new CSVReader(reader)) {
                List<String[]> csvData = csvReader.readAll();
                List<ListDto> list = new ArrayList<>();

                // Skip the header row (IMEI, IMSI, MSISDN)
                csvData.remove(0);

                for (String[] row : csvData) {
                    String imei = row[0].equals("null") ? null : row[0];
                    String imsi = row[1].equals("null") ? null : row[1];
                    String msisdn = row[2].equals("null") ? null : row[2];

                    ListDto dto = ListDto.builder()
                            .imei(imei)
                            .imsi(imsi)
                            .msisdn(msisdn)
                            .build();
                    list.add(dto);
                }

                log.info("File parsed successfully for url: {}", url);
                return list;
            } catch (IOException | CsvException e) {
                log.error("Exception while downloading file for url: {}, {}", url, e);
                Optional<CfgFeatureAlert> alert = cfgFeatureAlertRepository.findByAlertId("alert1602");
                log.error("Raising 1602");
                System.out.println("Raising 1602");
                if (alert.isPresent()) {
                    raiseAnAlert(alert.get().getAlertId(), url, featureName, 0);
                }
                e.printStackTrace();
                return Collections.emptyList();
            }
        } else if (responseCode == 406) {
            log.error("File not created yet for url: {}", url);
            Optional<CfgFeatureAlert> alert = cfgFeatureAlertRepository.findByAlertId("alert1603");
            log.error("Raising 1603");
            System.out.println("Raising 1603");
            if (alert.isPresent()) {
                raiseAnAlert(alert.get().getAlertId(), url, featureName, 0);
            }
            return Collections.emptyList();
        } else {
            log.error("Error: HTTP Status " + responseCode);
            Optional<CfgFeatureAlert> alert = cfgFeatureAlertRepository.findByAlertId("alert1602");
            log.error("Raising 1602");
            System.out.println("Raising 1602");
            if (alert.isPresent()) {
                raiseAnAlert(alert.get().getAlertId(), url, featureName, 0);
            }
            return Collections.emptyList();
        }
    }

    private List<TacListDto> getTacListDataFromUrl(String url) throws Exception {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        con.setRequestMethod("GET");

        int responseCode = con.getResponseCode();
        if (responseCode == 200) {
            try (InputStream in = con.getInputStream();
                 InputStreamReader reader = new InputStreamReader(in);
                 CSVReader csvReader = new CSVReader(reader)) {
                List<String[]> csvData = csvReader.readAll();
                List<TacListDto> list = new ArrayList<>();

                // Skip the header row (IMEI, IMSI, MSISDN)
                csvData.remove(0);

                for (String[] row : csvData) {
                    TacListDto dto = TacListDto.builder()
                                    .tac(row[0])
                                            .build();
                    list.add(dto);
                }

                log.info("File parsed successfully for url: {}", url);
                return list;
            } catch (IOException | CsvException e) {
                log.error("Exception while downloading file for url: {}, {}", url, e);
                Optional<CfgFeatureAlert> alert = cfgFeatureAlertRepository.findByAlertId("alert1602");
                log.error("Raising 1602");
                System.out.println("Raising 1602");
                if (alert.isPresent()) {
                    raiseAnAlert(alert.get().getAlertId(), url, featureName, 0);
                }
                e.printStackTrace();
                return Collections.emptyList();
            }
        } else if (responseCode == 406) {
            log.error("File not created yet for url {}", url);
            Optional<CfgFeatureAlert> alert = cfgFeatureAlertRepository.findByAlertId("alert1603");
            log.error("Raising 1603");
            System.out.println("Raising 1603");
            if (alert.isPresent()) {
                raiseAnAlert(alert.get().getAlertId(), url, featureName, 0);
            }
            return Collections.emptyList();
        } else {
            log.error("Error: HTTP Status " + responseCode);
            Optional<CfgFeatureAlert> alert = cfgFeatureAlertRepository.findByAlertId("alert1602");
            log.error("Raising 1602");
            System.out.println("Raising 1602");
            if (alert.isPresent()) {
                raiseAnAlert(alert.get().getAlertId(), url, featureName, 0);
            }
            return Collections.emptyList();
        }
    }

    public int generateListReport(ListName listType, String serverName, List<ListDto> eirsList, List<ListDto> eirList, String configKey) throws IOException {
        int count = 0;
        // Create a timestamp for the report file name
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
        String timestamp = sdf.format(new Date());

        // for eirs
        String eirsFileName = "report" + "_" + configKey + "_" + serverName + "_" + listType + "_" + timestamp + ".csv";
        Path filePath = Paths.get(csvFilePath).resolve(eirsFileName);

        try (FileWriter writer = new FileWriter(filePath.toString());
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                     .withHeader("serverName", "imei", "imsi", "msisdn", "result"))) {

            for (ListDto eirsEntry : eirsList) {
                boolean foundInEirList = eirList.contains(eirsEntry);
                if (foundInEirList) {
                    // Entry present in both lists, skip
                    continue;
                }

                int comparison = eirList.contains(eirsEntry) ? 0 : 1;
                csvPrinter.printRecord(serverName, eirsEntry.getImei(), eirsEntry.getImsi(), eirsEntry.getMsisdn(), comparison);
                count++;
            }

            for (ListDto eirEntry : eirList) {
                boolean foundInEirsList = eirsList.contains(eirEntry);
                if (foundInEirsList) {
                    // Entry present in both lists, skip
                    continue;
                }

                int comparison = eirsList.contains(eirEntry) ? 0 : 2;
                csvPrinter.printRecord(serverName, eirEntry.getImei(), eirEntry.getImsi(), eirEntry.getMsisdn(), comparison);
                count++;
            }

            csvPrinter.flush();
        }

        return count;
    }

    public int generateTacListReport(ListName listType, String serverName, List<TacListDto> eirsList, List<TacListDto> eirList, String configKey) throws IOException {
        int count = 0;
        // Create a timestamp for the report file name
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
        String timestamp = sdf.format(new Date());

        // Define the file name for the report
        String fileName = "report"+ "_" + configKey + "_" + serverName + "_" + listType + "_" + timestamp + ".csv";
        Path filePath = Paths.get(csvFilePath).resolve(fileName);

        try (FileWriter writer = new FileWriter(filePath.toString());
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                     .withHeader("serverName", "tac", "result"))) {
            for (TacListDto eirsEntry : eirsList) {
                boolean foundInEirList = eirList.contains(eirsEntry);
                if (!foundInEirList) {
                    csvPrinter.printRecord(serverName, eirsEntry.getTac(), 1);
                    count++;
                }
            }

            for (TacListDto eirEntry : eirList) {
                boolean foundInEirsList = eirsList.contains(eirEntry);
                if (!foundInEirsList) {
                    csvPrinter.printRecord(serverName, eirEntry.getTac(), 2);
                    count++;
                }
            }

            csvPrinter.flush();
        }

        return count;
    }

    public void raiseAnAlert(String alertCode, String alertMessage, String alertProcess, int userId) {
        try {   // <e>  alertMessage    //      <process_name> alertProcess
            String path = System.getenv("APP_HOME") + "alert/start.sh";
            ProcessBuilder pb = new ProcessBuilder(path, alertCode, alertMessage, alertProcess, String.valueOf(userId));
            Process p = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = null;
            String response = null;
            while ((line = reader.readLine()) != null) {
                response += line;
            }
            log.info("Alert is generated :response " + response);
        } catch (Exception ex) {
            ex.printStackTrace();
            log.error("Not able to execute Alert mgnt jar "+ ex.getLocalizedMessage() + " ::: " + ex.getMessage());
        }
    }

    public String sendGetRequest(String urlString) {
        StringBuilder response = new StringBuilder();

        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            in.close();
            log.info("Sent request to url: {}, code: {}, response: {}", urlString, connection.getResponseCode(), response.toString());
        } catch (Exception e) {
            log.error("Error during START action for the url: {}, error: {}", urlString, e);
            e.printStackTrace();
        }

        return response.toString();
    }
}
