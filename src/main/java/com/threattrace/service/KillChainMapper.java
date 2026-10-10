package com.threattrace.service;

import com.threattrace.domain.*;
import com.threattrace.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class KillChainMapper {

    private final EventLogRepository eventLogRepository;
    private final KillChainStageRepository stageRepository;
    private final EventStageMappingRepository mappingRepository;

    public KillChainMapper(EventLogRepository eventLogRepository,
                           KillChainStageRepository stageRepository,
                           EventStageMappingRepository mappingRepository) {
        this.eventLogRepository = eventLogRepository;
        this.stageRepository = stageRepository;
        this.mappingRepository = mappingRepository;
    }

    @Transactional
    public List<EventStageMapping> mapCaseEventsToKillChain(Long caseId) {
        List<EventLog> events = eventLogRepository.findByInvestigationCaseIdOrderByEventTimeAsc(caseId);
        List<KillChainStage> stages = stageRepository.findAllByOrderByStageOrderAsc();

        Map<String, KillChainStage> stageMap = new HashMap<>();
        for (KillChainStage s : stages) {
            stageMap.put(s.getStageName(), s);
        }

        List<EventStageMapping> newMappings = new ArrayList<>();

        for (EventLog event : events) {
            // Check if mapping already exists
            List<EventStageMapping> existing = mappingRepository.findByEventLogId(event.getId());
            if (!existing.isEmpty()) continue;

            String proc = event.getProcessName() != null ? event.getProcessName().toLowerCase() : "";
            String payload = event.getRawPayload() != null ? event.getRawPayload().toLowerCase() : "";
            String type = event.getEventType().toUpperCase();
            String target = event.getTarget() != null ? event.getTarget().toLowerCase() : "";

            // Rule 1: Delivery Stage
            if (proc.contains("winword.exe") || proc.contains("excel.exe") || proc.contains("outlook.exe") || target.contains(".docm") || target.contains(".pdf")) {
                if (stageMap.containsKey("Delivery")) {
                    newMappings.add(new EventStageMapping(event, stageMap.get("Delivery"), "RUL-DEL-01", "Execution launched from email attachment / office document (Delivery stage)."));
                }
            }

            // Rule 2: Exploitation Stage
            if (proc.contains("cmd.exe") || proc.contains("powershell.exe") || proc.contains("bash") || payload.contains("-encodedcommand")) {
                if (stageMap.containsKey("Exploitation")) {
                    newMappings.add(new EventStageMapping(event, stageMap.get("Exploitation"), "RUL-EXP-01", "Command shell / script engine invocation (Exploitation stage)."));
                }
            }

            // Rule 3: Installation / Persistence Stage
            if (target.contains("run") || target.contains("startup") || proc.contains("schtasks.exe") || proc.contains("reg.exe")) {
                if (stageMap.containsKey("Installation")) {
                    newMappings.add(new EventStageMapping(event, stageMap.get("Installation"), "RUL-INS-01", "Registry run key or scheduled task persistence modification (Installation stage)."));
                }
            }

            // Rule 4: Command & Control (C2) Stage
            if ("NETWORK".equals(type) && (target.contains(":4444") || target.contains(":8080") || target.contains(":8443") || "HIGH".equalsIgnoreCase(event.getSeverity()) || "CRITICAL".equalsIgnoreCase(event.getSeverity()))) {
                if (stageMap.containsKey("Command & Control (C2)")) {
                    newMappings.add(new EventStageMapping(event, stageMap.get("Command & Control (C2)"), "RUL-C2-01", "Outbound network socket connection on non-standard port (C2 stage)."));
                }
            }

            // Rule 5: Actions on Objectives Stage
            if (proc.contains("rundll32.exe") || target.contains("lsass") || target.contains("shadow") || proc.contains("vssadmin")) {
                if (stageMap.containsKey("Actions on Objectives")) {
                    newMappings.add(new EventStageMapping(event, stageMap.get("Actions on Objectives"), "RUL-ACT-01", "Credential dumping / LSASS memory access observed (Actions on Objectives stage)."));
                }
            }
        }

        return mappingRepository.saveAll(newMappings);
    }
}
