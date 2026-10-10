package com.threattrace.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import com.threattrace.domain.*;
import com.threattrace.dto.DTOs.ImportResultDTO;
import com.threattrace.exception.CustomExceptions.ResourceNotFoundException;
import com.threattrace.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class EventImportService {

    private final EventLogRepository eventLogRepository;
    private final InvestigationCaseRepository caseRepository;
    private final EndpointDeviceRepository deviceRepository;
    private final AssetRepository assetRepository;
    private final EventValidationService validationService;
    private final ObjectMapper objectMapper;

    public EventImportService(EventLogRepository eventLogRepository,
                              InvestigationCaseRepository caseRepository,
                              EndpointDeviceRepository deviceRepository,
                              AssetRepository assetRepository,
                              EventValidationService validationService,
                              ObjectMapper objectMapper) {
        this.eventLogRepository = eventLogRepository;
        this.caseRepository = caseRepository;
        this.deviceRepository = deviceRepository;
        this.assetRepository = assetRepository;
        this.validationService = validationService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ImportResultDTO importCsvLogs(Long caseId, MultipartFile file) {
        InvestigationCase investigationCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new ResourceNotFoundException("Investigation case not found: " + caseId));

        List<String> errors = new ArrayList<>();
        Set<String> processedCodes = new HashSet<>();
        List<EventLog> validEventsToSave = new ArrayList<>();
        Map<String, EventLog> importedCodeToEventMap = new HashMap<>();

        int totalProcessed = 0;
        int validCount = 0;

        try (Reader reader = new InputStreamReader(file.getInputStream());
             CSVReader csvReader = new CSVReader(reader)) {

            String[] header = csvReader.readNext();
            if (header == null) {
                errors.add("Uploaded CSV file is empty.");
                return new ImportResultDTO(caseId, 0, 0, 1, errors);
            }

            Map<String, Integer> headerIndex = new HashMap<>();
            for (int i = 0; i < header.length; i++) {
                headerIndex.put(header[i].trim().toLowerCase(), i);
            }

            String[] line;
            while ((line = csvReader.readNext()) != null) {
                totalProcessed++;
                Map<String, String> row = extractRowMap(headerIndex, line);

                List<String> rowErrors = validationService.validateRawRecord(row, processedCodes);
                if (!rowErrors.isEmpty()) {
                    errors.add("Row " + totalProcessed + " Error: " + String.join("; ", rowErrors));
                    continue;
                }

                try {
                    EventLog eventLog = buildEventLogFromMap(row, investigationCase, importedCodeToEventMap);
                    processedCodes.add(eventLog.getEventCode());
                    validEventsToSave.add(eventLog);
                    if (eventLog.getEventCode() != null) {
                        importedCodeToEventMap.put(eventLog.getEventCode(), eventLog);
                    }
                    validCount++;
                } catch (Exception e) {
                    errors.add("Row " + totalProcessed + " Conversion Error: " + e.getMessage());
                }
            }

            eventLogRepository.saveAll(validEventsToSave);

        } catch (Exception e) {
            errors.add("CSV Read Failure: " + e.getMessage());
        }

        return new ImportResultDTO(caseId, totalProcessed, validCount, errors.size(), errors);
    }

    @Transactional
    public ImportResultDTO importJsonLogs(Long caseId, MultipartFile file) {
        InvestigationCase investigationCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new ResourceNotFoundException("Investigation case not found: " + caseId));

        List<String> errors = new ArrayList<>();
        Set<String> processedCodes = new HashSet<>();
        List<EventLog> validEventsToSave = new ArrayList<>();
        Map<String, EventLog> importedCodeToEventMap = new HashMap<>();

        int totalProcessed = 0;
        int validCount = 0;

        try {
            List<Map<String, String>> records = objectMapper.readValue(
                    file.getInputStream(), new TypeReference<List<Map<String, String>>>() {});

            for (Map<String, String> row : records) {
                totalProcessed++;
                List<String> rowErrors = validationService.validateRawRecord(row, processedCodes);
                if (!rowErrors.isEmpty()) {
                    errors.add("Item " + totalProcessed + " Error: " + String.join("; ", rowErrors));
                    continue;
                }

                try {
                    EventLog eventLog = buildEventLogFromMap(row, investigationCase, importedCodeToEventMap);
                    processedCodes.add(eventLog.getEventCode());
                    validEventsToSave.add(eventLog);
                    if (eventLog.getEventCode() != null) {
                        importedCodeToEventMap.put(eventLog.getEventCode(), eventLog);
                    }
                    validCount++;
                } catch (Exception e) {
                    errors.add("Item " + totalProcessed + " Conversion Error: " + e.getMessage());
                }
            }

            eventLogRepository.saveAll(validEventsToSave);

        } catch (Exception e) {
            errors.add("JSON Read Failure: " + e.getMessage());
        }

        return new ImportResultDTO(caseId, totalProcessed, validCount, errors.size(), errors);
    }

    private Map<String, String> extractRowMap(Map<String, Integer> headerIndex, String[] line) {
        Map<String, String> row = new HashMap<>();
        for (Map.Entry<String, Integer> entry : headerIndex.entrySet()) {
            int idx = entry.getValue();
            if (idx < line.length) {
                row.put(entry.getKey(), line[idx]);
            }
        }
        return row;
    }

    private EventLog buildEventLogFromMap(Map<String, String> row, InvestigationCase investigationCase, Map<String, EventLog> batchMap) {
        EventLog eventLog = new EventLog();
        eventLog.setInvestigationCase(investigationCase);

        String eventCode = row.get("event_code");
        if (eventCode == null || eventCode.trim().isEmpty()) {
            eventCode = "EVT-" + UUID.randomUUID().toString().substring(0, 8);
        }
        eventLog.setEventCode(eventCode);

        eventLog.setEventTime(validationService.parseTimestamp(row.get("event_time")));
        eventLog.setEventType(row.get("event_type").toUpperCase());
        eventLog.setSeverity(row.getOrDefault("severity", "LOW").toUpperCase());
        eventLog.setUserName(row.get("user_name"));
        eventLog.setProcessName(row.get("process_name"));
        eventLog.setTarget(row.get("target"));
        eventLog.setSource(row.get("source"));
        eventLog.setDescription(row.get("description"));
        eventLog.setRawPayload(row.get("raw_payload"));

        // Match or Auto-create Endpoint Device & Asset
        String hostname = row.getOrDefault("hostname", "UNKNOWN-HOST");
        EndpointDevice device = deviceRepository.findByHostname(hostname).orElseGet(() -> {
            Asset defaultAsset = assetRepository.findById(3L).orElseGet(() -> {
                Asset asset = new Asset("Auto-Discovered Asset", "WORKSTATION", 5, "General IT", "10.0.0.0/8");
                return assetRepository.save(asset);
            });
            EndpointDevice newDevice = new EndpointDevice(defaultAsset, hostname, "10.0.0.100", "Windows 11 / Linux");
            return deviceRepository.save(newDevice);
        });
        eventLog.setDevice(device);

        // Resolve Parent Event ID if provided
        String parentCodeOrId = row.get("parent_event_id");
        if (parentCodeOrId != null && !parentCodeOrId.trim().isEmpty()) {
            // Check batch map first
            if (batchMap.containsKey(parentCodeOrId)) {
                eventLog.setParentEvent(batchMap.get(parentCodeOrId));
            } else {
                // Try finding by eventCode or numeric ID in database
                Optional<EventLog> parentByCode = eventLogRepository.findByEventCode(parentCodeOrId);
                if (parentByCode.isPresent()) {
                    eventLog.setParentEvent(parentByCode.get());
                } else {
                    try {
                        Long parentId = Long.parseLong(parentCodeOrId);
                        eventLogRepository.findById(parentId).ifPresent(eventLog::setParentEvent);
                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        return eventLog;
    }
}
