// Threat Trace client: case data is always read from the application API.
let currentCaseId = null;
let currentTimelineEvents = [];
let timelineRequestPending = false;
let timelineRequestDone = Promise.resolve();
let resolveTimelineRequest = null;
const TIMELINE_REFRESH_MS = 15000;

const KILL_CHAIN_STAGES = [
    "Reconnaissance",
    "Weaponization",
    "Delivery",
    "Exploitation",
    "Installation",
    "Command & Control (C2)",
    "Actions on Objectives"
];

document.addEventListener("DOMContentLoaded", initializeApp);

async function initializeApp() {
    renderTableMessage("Loading event records…", "loading");
    window.setInterval(() => {
        const activeTab = document.querySelector(".tab-content.active")?.id;
        if (currentCaseId && activeTab === "timelineTab") loadTimeline({ quiet: true });
    }, TIMELINE_REFRESH_MS);
    await loadCases();
}

async function requestJSON(url, options = {}) {
    const response = await fetch(url, options);
    if (!response.ok) {
        let detail = "";
        try {
            const body = await response.json();
            detail = body.message || body.error || "";
        } catch (_) {
            detail = response.statusText;
        }
        throw new Error(detail || `Request failed (${response.status})`);
    }
    if (response.status === 204) return null;
    return response.json();
}

function setAppStatus(message, isError = false) {
    const status = document.getElementById("appStatus");
    status.textContent = message || "";
    status.hidden = !message;
    status.classList.toggle("error", Boolean(isError));
}

function setStatusMessage(elementId, message, kind = "") {
    const element = document.getElementById(elementId);
    if (!element) return;
    element.textContent = message || "";
    element.className = `status-message${kind ? ` ${kind}` : ""}`;
}

function emptyPanel(message, className = "state-panel") {
    const panel = document.createElement("div");
    panel.className = className;
    panel.textContent = message;
    return panel;
}

function textCell(value, className = "") {
    const cell = document.createElement("td");
    cell.textContent = value === null || value === undefined || value === "" ? "—" : String(value);
    if (className) cell.className = className;
    return cell;
}

function formatDateTime(value) {
    if (!value) return "—";
    return String(value).replace("T", " ");
}

function setSeverity(element, severity) {
    const normalized = String(severity || "").toLowerCase();
    element.className = `severity-text${normalized ? ` severity-${normalized}` : ""}`;
    element.textContent = severity ? String(severity).toUpperCase() : "Not assessed";
}

function resetCaseView() {
    currentTimelineEvents = [];
    resetAnalysisPanels();
    document.getElementById("caseNumberLabel").textContent = "—";
    document.getElementById("caseTitleLabel").textContent = "Select an investigation";
    document.getElementById("caseDescriptionLabel").textContent = "Case details will appear here.";
    document.getElementById("caseStatusLabel").textContent = "Awaiting case";
    document.querySelector(".case-state").classList.remove("is-open");
    document.getElementById("kpiTotalEvents").textContent = "—";
    document.getElementById("kpiRiskScore").textContent = "—";
    setSeverity(document.getElementById("kpiSeverityLevel"), "");
    document.getElementById("kpiLeadActor").textContent = "Pending analysis";
    document.getElementById("kpiActorSimilarity").textContent = "No attribution assessment";
    document.getElementById("kpiStagesCount").textContent = "— / 7";
    document.getElementById("timelineCount").textContent = "— events";
    document.getElementById("timelineRange").textContent = "Awaiting data";
    document.getElementById("timelineLastUpdated").textContent = "Not yet refreshed";
    setTimelineLiveState("Waiting for a case");
    renderTableMessage("Select a case to view its event records.");
}

function setTimelineLiveState(label, failed = false) {
    const state = document.getElementById("timelineLiveState");
    document.getElementById("timelineLiveLabel").textContent = label;
    state.classList.toggle("update-error", failed);
}

function resetAnalysisPanels() {
    document.getElementById("processTreeContainer").replaceChildren(emptyPanel("Select a case to load process lineage."));
    document.getElementById("killChainPipeline").replaceChildren(emptyPanel("No stages mapped yet."));
    document.getElementById("killChainSummary").textContent = "Awaiting event analysis";
    document.getElementById("dnaSequence").replaceChildren(emptyPanel("No sequence available."));
    document.getElementById("dnaLeadActorBadge").textContent = "Awaiting analysis";
    document.getElementById("dnaActorSimilarity").textContent = "No similarity calculated";
    document.getElementById("riskTotalScoreDisplay").textContent = "—";
    document.getElementById("riskFactorBreakdownList").replaceChildren(emptyPanel("Risk factors will appear when analysis is available."));
    setSeverity(document.getElementById("riskSeverityDisplay"), "");
    document.getElementById("mitigationsGrid").replaceChildren(emptyPanel("Recommendations will appear after analysis."));
    document.getElementById("analystNotesList").replaceChildren(emptyPanel("No case notes loaded."));
}

async function loadCases(preferredId = null) {
    const selector = document.getElementById("caseSelect");
    selector.innerHTML = "";
    const loading = document.createElement("option");
    loading.value = "";
    loading.textContent = "Loading cases…";
    selector.appendChild(loading);
    selector.disabled = true;

    try {
        const cases = await requestJSON("/api/cases");
        selector.innerHTML = "";
        if (!Array.isArray(cases) || cases.length === 0) {
            const option = document.createElement("option");
            option.value = "";
            option.textContent = "No cases available";
            selector.appendChild(option);
            currentCaseId = null;
            resetCaseView();
            setAppStatus("No investigations are available yet. Create a case to begin.");
            return;
        }

        cases.forEach(investigation => {
            const option = document.createElement("option");
            option.value = investigation.id;
            option.textContent = `${investigation.caseNumber || `Case ${investigation.id}`} · ${investigation.title || "Untitled investigation"}`;
            selector.appendChild(option);
        });

        const target = preferredId || currentCaseId;
        const selected = cases.find(item => String(item.id) === String(target)) || cases[0];
        selector.value = selected.id;
        selector.disabled = false;
        setAppStatus("");
        await loadCaseData(selected.id);
    } catch (error) {
        selector.innerHTML = "";
        const option = document.createElement("option");
        option.value = "";
        option.textContent = "Cases unavailable";
        selector.appendChild(option);
        currentCaseId = null;
        resetCaseView();
        setAppStatus(`Unable to load cases: ${error.message}`, true);
    } finally {
        selector.disabled = selector.options.length < 2 || !currentCaseId;
    }
}

function switchTab(tabId, button) {
    document.querySelectorAll(".tab-btn").forEach(tab => {
        const active = tab === button;
        tab.classList.toggle("active", active);
        tab.setAttribute("aria-selected", String(active));
    });
    document.querySelectorAll(".tab-content").forEach(panel => {
        const active = panel.id === tabId;
        panel.classList.toggle("active", active);
        panel.hidden = !active;
    });

    if (!currentCaseId) return;
    if (tabId === "lineageTab") loadProcessTree();
    if (tabId === "killChainTab") loadKillChainAndDNA();
    if (tabId === "riskTab") loadRiskBreakdown();
    if (tabId === "mitigationTab") loadMitigations();
    if (tabId === "notesTab") loadAnalystNotes();
}

async function loadCaseData(caseId) {
    if (!caseId) {
        currentCaseId = null;
        resetCaseView();
        return;
    }
    currentCaseId = caseId;
    currentTimelineEvents = [];
    resetAnalysisPanels();
    setTimelineLiveState("Loading case timeline…");
    renderTableMessage("Loading event records…", "loading");
    document.getElementById("kpiTotalEvents").textContent = "…";
    document.getElementById("kpiRiskScore").textContent = "—";
    setSeverity(document.getElementById("kpiSeverityLevel"), "");
    document.getElementById("kpiLeadActor").textContent = "Pending analysis";
    document.getElementById("kpiActorSimilarity").textContent = "No attribution assessment";
    document.getElementById("kpiStagesCount").textContent = "— / 7";

    try {
        const caseData = await requestJSON(`/api/cases/${encodeURIComponent(caseId)}`);
        if (String(currentCaseId) !== String(caseId)) return;
        document.getElementById("caseNumberLabel").textContent = caseData.caseNumber || "—";
        document.getElementById("caseTitleLabel").textContent = caseData.title || "Untitled investigation";
        document.getElementById("caseDescriptionLabel").textContent = caseData.description || "No case description recorded.";
        document.getElementById("caseStatusLabel").textContent = caseData.status || "Status unavailable";
        document.querySelector(".case-state").classList.toggle("is-open", Boolean(caseData.status && caseData.status.toUpperCase() !== "CLOSED"));
        document.getElementById("kpiRiskScore").textContent = caseData.riskScore === null || caseData.riskScore === undefined ? "—" : Number(caseData.riskScore).toFixed(1);
        setSeverity(document.getElementById("kpiSeverityLevel"), caseData.severityLevel || "");
        await loadTimeline();
        await loadKillChainAndDNA();
        const activeTab = document.querySelector(".tab-content.active")?.id;
        if (activeTab === "lineageTab") await loadProcessTree();
        if (activeTab === "riskTab") await loadRiskBreakdown();
        if (activeTab === "mitigationTab") await loadMitigations();
        if (activeTab === "notesTab") await loadAnalystNotes();
    } catch (error) {
        renderTableMessage(`Unable to load this case: ${error.message}`, "error");
        setAppStatus(`Unable to load investigation: ${error.message}`, true);
    }
}

async function loadTimeline({ quiet = false } = {}) {
    if (!currentCaseId) return;
    if (timelineRequestPending) {
        await timelineRequestDone;
        if (currentCaseId) return loadTimeline({ quiet });
        return;
    }
    timelineRequestPending = true;
    timelineRequestDone = new Promise(resolve => { resolveTimelineRequest = resolve; });
    const requestCaseId = currentCaseId;
    if (!quiet) renderTableMessage("Loading event records…", "loading");
    try {
        const events = await requestJSON(`/api/events/timeline/${encodeURIComponent(requestCaseId)}`);
        if (String(requestCaseId) !== String(currentCaseId)) return;
        if (!Array.isArray(events)) throw new Error("The timeline response was not a list of events.");
        currentTimelineEvents = events;
        document.getElementById("kpiTotalEvents").textContent = String(events.length);
        const first = events[0]?.eventTime;
        const last = events[events.length - 1]?.eventTime;
        document.getElementById("timelineRange").textContent = events.length ? `${formatDateTime(first)} — ${formatDateTime(last)}` : "No event range yet";
        document.getElementById("timelineLastUpdated").textContent = `Updated ${new Intl.DateTimeFormat(undefined, { hour: "2-digit", minute: "2-digit", second: "2-digit" }).format(new Date())}`;
        setTimelineLiveState("Auto-refresh every 15 sec");
        applyTimelineFilters();
    } catch (error) {
        if (String(requestCaseId) !== String(currentCaseId)) return;
        currentTimelineEvents = [];
        document.getElementById("kpiTotalEvents").textContent = "—";
        document.getElementById("timelineCount").textContent = "Events unavailable";
        document.getElementById("timelineRange").textContent = "API request failed";
        document.getElementById("timelineLastUpdated").textContent = "Refresh failed";
        setTimelineLiveState("Retrying automatically", true);
        renderTableMessage(`Unable to load event records: ${error.message}`, "error");
    } finally {
        timelineRequestPending = false;
        resolveTimelineRequest?.();
        resolveTimelineRequest = null;
    }
}

async function refreshTimeline() {
    if (!currentCaseId) return;
    setTimelineLiveState("Refreshing now…");
    await loadTimeline();
}

function renderTableMessage(message, kind = "") {
    const tbody = document.getElementById("timelineTableBody");
    if (!tbody) return;
    tbody.replaceChildren();
    const row = document.createElement("tr");
    const cell = document.createElement("td");
    cell.colSpan = 9;
    cell.className = `table-message${kind === "error" ? " error" : ""}`;
    cell.textContent = message;
    row.appendChild(cell);
    tbody.appendChild(row);
}

function renderTimelineTable(events) {
    const tbody = document.getElementById("timelineTableBody");
    tbody.replaceChildren();
    document.getElementById("timelineCount").textContent = `${events.length} ${events.length === 1 ? "event" : "events"}`;
    if (events.length === 0) {
        const anyCaseEvents = currentTimelineEvents.length > 0;
        renderTableMessage(anyCaseEvents ? "No events match these filters. Adjust or clear the filters." : "No event logs have been imported for this case.");
        return;
    }

    events.forEach(item => {
        const row = document.createElement("tr");
        const eventCell = textCell(item.eventCode || (item.id ? `EVT-${item.id}` : "—"), "mono");
        const code = document.createElement("code");
        code.textContent = eventCell.textContent;
        eventCell.replaceChildren(code);
        row.appendChild(eventCell);
        const timeCell = document.createElement("td");
        timeCell.className = "timestamp-cell";
        const timeSeverity = String(item.severity || "").toLowerCase();
        const marker = document.createElement("span");
        marker.className = `timeline-marker${["critical", "high", "medium", "low"].includes(timeSeverity) ? ` marker-${timeSeverity}` : ""}`;
        marker.setAttribute("aria-hidden", "true");
        const timestamp = document.createElement("span");
        timestamp.className = "mono timestamp";
        timestamp.textContent = formatDateTime(item.eventTime);
        timeCell.append(marker, timestamp);
        row.appendChild(timeCell);
        row.appendChild(textCell(item.eventType || "—"));
        const severityCell = document.createElement("td");
        const badge = document.createElement("span");
        const severity = String(item.severity || "UNKNOWN").toLowerCase();
        badge.className = `badge badge-${["critical", "high", "medium", "low"].includes(severity) ? severity : ""}`;
        badge.textContent = item.severity || "Unknown";
        severityCell.appendChild(badge);
        row.appendChild(severityCell);
        row.appendChild(textCell(item.device?.hostname || "—"));
        row.appendChild(textCell(item.processName || "—", "mono"));
        row.appendChild(textCell(item.userName || "—"));
        row.appendChild(textCell([item.target, item.source].filter(Boolean).join(" ← ") || "—", "mono"));
        row.appendChild(textCell(item.description || "—"));
        tbody.appendChild(row);
    });
}

function applyTimelineFilters() {
    if (!document.getElementById("filterEventType")) return;
    const typeFilter = document.getElementById("filterEventType").value.toUpperCase();
    const severityFilter = document.getElementById("filterSeverity").value.toUpperCase();
    const hostnameFilter = document.getElementById("filterHostname").value.trim().toLowerCase();
    const filtered = currentTimelineEvents.filter(item => {
        const host = String(item.device?.hostname || "").toLowerCase();
        return (!typeFilter || String(item.eventType || "").toUpperCase() === typeFilter)
            && (!severityFilter || String(item.severity || "").toUpperCase() === severityFilter)
            && (!hostnameFilter || host.includes(hostnameFilter));
    });
    renderTimelineTable(filtered);
}

function clearTimelineFilters() {
    document.getElementById("filterEventType").value = "";
    document.getElementById("filterSeverity").value = "";
    document.getElementById("filterHostname").value = "";
    applyTimelineFilters();
}

async function loadProcessTree() {
    const container = document.getElementById("processTreeContainer");
    container.replaceChildren(emptyPanel("Reconstructing process lineage…"));
    try {
        const forest = await requestJSON(`/api/events/process-tree/${encodeURIComponent(currentCaseId)}`);
        container.replaceChildren();
        if (!Array.isArray(forest) || forest.length === 0) {
            container.appendChild(emptyPanel("No parent–child process relationships were returned for this case."));
            return;
        }
        forest.forEach(root => container.appendChild(renderTreeNode(root)));
    } catch (error) {
        container.replaceChildren(emptyPanel(`Could not load process lineage: ${error.message}`, "state-panel error-text"));
    }
}

function renderTreeNode(node) {
    const wrapper = document.createElement("div");
    wrapper.className = "tree-node";
    const card = document.createElement("div");
    card.className = "tree-node-card";
    const main = document.createElement("div");
    main.className = "tree-node-main";
    const process = document.createElement("span");
    process.className = "tree-node-title";
    process.textContent = node.processName || "Process name unavailable";
    main.appendChild(process);
    if (node.severity) {
        const badge = document.createElement("span");
        const level = String(node.severity).toLowerCase();
        badge.className = `badge badge-${["critical", "high", "medium", "low"].includes(level) ? level : ""}`;
        badge.textContent = node.severity;
        main.appendChild(badge);
    }
    card.appendChild(main);
    const details = document.createElement("div");
    details.className = "tree-node-details";
    [node.eventCode, formatDateTime(node.eventTime), node.target ? `Target: ${node.target}` : ""].filter(Boolean).forEach(value => {
        const part = document.createElement("span");
        part.textContent = value;
        details.appendChild(part);
    });
    card.appendChild(details);
    wrapper.appendChild(card);

    if (Array.isArray(node.children) && node.children.length) {
        const branch = document.createElement("div");
        branch.className = "tree-branch";
        node.children.forEach(child => branch.appendChild(renderTreeNode(child)));
        wrapper.appendChild(branch);
    }
    return wrapper;
}

async function loadKillChainAndDNA() {
    if (!currentCaseId) return;
    const pipeline = document.getElementById("killChainPipeline");
    pipeline.replaceChildren(emptyPanel("Mapping observed events to the attack chain…"));
    try {
        if (currentTimelineEvents.length === 0) {
            renderKillChain(KILL_CHAIN_STAGES, []);
            document.getElementById("killChainSummary").textContent = "Import events to map stages";
            updateActorMatch(null);
            document.getElementById("dnaSequence").replaceChildren(emptyPanel("Import events to generate a behavioral sequence."));
            document.getElementById("dnaLeadActorBadge").textContent = "No profile match yet";
            document.getElementById("dnaActorSimilarity").textContent = "No events available for comparison";
            return;
        }
        await requestJSON(`/api/analysis/kill-chain/${encodeURIComponent(currentCaseId)}`, { method: "POST" });
        const dna = await requestJSON(`/api/analysis/dna/${encodeURIComponent(currentCaseId)}`);
        const observed = Array.isArray(dna.observedKillChainStages) ? dna.observedKillChainStages : [];
        renderKillChain(KILL_CHAIN_STAGES, observed);
        updateActorMatch(dna);
        renderDNASequence(dna.eventTypeSequence || []);
    } catch (error) {
        pipeline.replaceChildren(emptyPanel(`Analysis unavailable: ${error.message}`, "state-panel error-text"));
        document.getElementById("killChainSummary").textContent = "Analysis request failed";
        document.getElementById("kpiStagesCount").textContent = "— / 7";
        updateActorMatch(null);
        document.getElementById("dnaSequence").replaceChildren(emptyPanel("Behavioral sequence is unavailable."));
    }
}

function renderKillChain(stages, observedStages) {
    const pipeline = document.getElementById("killChainPipeline");
    pipeline.replaceChildren();
    const normalized = observedStages.map(stage => String(stage).trim().toLowerCase());
    const activeCount = stages.filter(stage => normalized.includes(stage.toLowerCase())).length;
    stages.forEach((stage, index) => {
        const active = normalized.includes(stage.toLowerCase());
        const card = document.createElement("div");
        card.className = `pipeline-stage${active ? " active-stage" : ""}`;
        const state = document.createElement("span");
        state.className = "stage-state";
        state.textContent = active ? "Observed" : "Not observed";
        const name = document.createElement("span");
        name.className = "stage-name";
        name.textContent = `${String(index + 1).padStart(2, "0")} · ${stage}`;
        card.append(state, name);
        pipeline.appendChild(card);
    });
    const empty = stages.length === 0;
    if (empty) pipeline.appendChild(emptyPanel("No stages are available for analysis."));
    document.getElementById("kpiStagesCount").textContent = `${activeCount} / 7`;
    document.getElementById("killChainSummary").textContent = stages.length ? `${activeCount} of 7 stages observed` : "No events mapped";
}

function updateActorMatch(dna) {
    if (!dna) {
        document.getElementById("kpiLeadActor").textContent = "Pending analysis";
        document.getElementById("kpiActorSimilarity").textContent = "No similarity calculated";
        return;
    }
    const lead = dna.leadThreatActorMatch && dna.leadThreatActorMatch !== "NONE" ? dna.leadThreatActorMatch : "No profile match";
    const score = Number(dna.similarityScorePercentage);
    document.getElementById("kpiLeadActor").textContent = lead;
    document.getElementById("kpiActorSimilarity").textContent = Number.isFinite(score) ? `${score.toFixed(1)}% behavioral similarity · lead only` : "No similarity score returned";
    document.getElementById("dnaLeadActorBadge").textContent = lead;
    document.getElementById("dnaActorSimilarity").textContent = Number.isFinite(score) ? `${score.toFixed(1)}% behavioral similarity` : "No similarity calculated";
}

function renderDNASequence(sequence) {
    const container = document.getElementById("dnaSequence");
    container.replaceChildren();
    if (!Array.isArray(sequence) || sequence.length === 0) {
        container.appendChild(emptyPanel("No event sequence was returned."));
        return;
    }
    sequence.forEach((type, index) => {
        if (index > 0) {
            const arrow = document.createElement("span");
            arrow.className = "muted";
            arrow.textContent = "→";
            container.appendChild(arrow);
        }
        const chip = document.createElement("span");
        chip.className = "badge";
        chip.textContent = type;
        container.appendChild(chip);
    });
}

async function loadRiskBreakdown() {
    if (!currentCaseId) return;
    const list = document.getElementById("riskFactorBreakdownList");
    list.replaceChildren(emptyPanel("Calculating risk from current case evidence…"));
    try {
        const risk = await requestJSON(`/api/analysis/risk/${encodeURIComponent(currentCaseId)}`, { method: "POST" });
        const score = Number(risk.totalScore);
        const displayScore = Number.isFinite(score) ? score.toFixed(1) : "—";
        document.getElementById("riskTotalScoreDisplay").textContent = displayScore;
        document.getElementById("kpiRiskScore").textContent = displayScore;
        const severity = score >= 75 ? "CRITICAL" : score >= 50 ? "HIGH" : score >= 25 ? "MEDIUM" : "LOW";
        setSeverity(document.getElementById("riskSeverityDisplay"), Number.isFinite(score) ? severity : "");
        setSeverity(document.getElementById("kpiSeverityLevel"), Number.isFinite(score) ? severity : "");
        list.replaceChildren();
        const explanations = Array.isArray(risk.explanations) ? risk.explanations : [];
        if (!explanations.length) {
            list.appendChild(emptyPanel("No risk factors were returned for this case."));
            return;
        }
        explanations.forEach((explanation, index) => {
            const item = document.createElement("div");
            item.className = "risk-factor";
            const text = document.createElement("p");
            text.textContent = explanation;
            const factor = document.createElement("strong");
            const impactValues = [risk.assetCriticalityScore, risk.maxSeverityWeight, risk.killChainProgressionScore];
            const rawImpact = index < impactValues.length ? impactValues[index] : (index === 3 ? risk.suspiciousChainMultiplier : null);
            const impact = Number(rawImpact);
            factor.textContent = Number.isFinite(impact)
                ? (index === 3 ? `×${impact.toFixed(2)}` : `+${impact.toFixed(1)} pts`)
                : `FACTOR ${String(index + 1).padStart(2, "0")}`;
            item.append(text, factor);
            list.appendChild(item);
        });
    } catch (error) {
        list.replaceChildren(emptyPanel(`Risk analysis unavailable: ${error.message}`, "state-panel error-text"));
    }
}

async function recalculateRisk() {
    await loadRiskBreakdown();
}

async function loadMitigations() {
    if (!currentCaseId) return;
    const grid = document.getElementById("mitigationsGrid");
    grid.replaceChildren(emptyPanel("Loading recommendations…"));
    try {
        const strategy = document.getElementById("mitigationStrategySelect").value;
        const recommendations = await requestJSON(`/api/analysis/mitigations/${encodeURIComponent(currentCaseId)}?strategy=${encodeURIComponent(strategy)}`);
        grid.replaceChildren();
        if (!Array.isArray(recommendations) || recommendations.length === 0) {
            grid.appendChild(emptyPanel("No mitigation recommendations are available for this case."));
            return;
        }
        recommendations.forEach((recommendation, index) => {
            const mitigation = recommendation.mitigationStrategy || {};
            const card = document.createElement("article");
            card.className = "mitigation-card";
            const meta = document.createElement("div");
            meta.className = "mitigation-meta";
            const rank = document.createElement("span");
            rank.className = "badge";
            rank.textContent = `Priority ${index + 1}`;
            const score = document.createElement("span");
            score.className = "mitigation-score";
            const numericScore = Number(recommendation.recommendationScore);
            score.textContent = Number.isFinite(numericScore) ? `Recommendation score ${numericScore.toFixed(1)}` : "Score unavailable";
            meta.append(rank, score);
            const title = document.createElement("h3");
            title.textContent = mitigation.actionName || "Mitigation action";
            const description = document.createElement("p");
            description.textContent = mitigation.description || "No action details provided.";
            const attributes = document.createElement("div");
            attributes.className = "mitigation-meta";
            attributes.textContent = [mitigation.category, mitigation.effortLevel ? `Effort: ${mitigation.effortLevel}` : "", mitigation.businessImpact ? `Business impact: ${mitigation.businessImpact}` : ""].filter(Boolean).join(" · ") || "No classification details";
            const rationale = document.createElement("p");
            rationale.textContent = recommendation.analystRationale ? `Rationale: ${recommendation.analystRationale}` : "No rationale supplied.";
            card.append(meta, title, description, attributes, rationale);
            grid.appendChild(card);
        });
    } catch (error) {
        grid.replaceChildren(emptyPanel(`Recommendations unavailable: ${error.message}`, "state-panel error-text"));
    }
}

async function loadAnalystNotes() {
    if (!currentCaseId) return;
    const list = document.getElementById("analystNotesList");
    list.replaceChildren(emptyPanel("Loading case notes…"));
    try {
        const notes = await requestJSON(`/api/analysis/notes/${encodeURIComponent(currentCaseId)}`);
        list.replaceChildren();
        if (!Array.isArray(notes) || notes.length === 0) {
            list.appendChild(emptyPanel("No analyst notes have been recorded for this case."));
            return;
        }
        notes.forEach(note => {
            const item = document.createElement("article");
            item.className = "note-item";
            const heading = document.createElement("strong");
            heading.textContent = note.analyst?.fullName || "Analyst";
            item.appendChild(heading);
            if (note.isFalsePositiveFlag) {
                const flag = document.createElement("span");
                flag.className = "badge badge-medium";
                flag.textContent = "False positive";
                flag.style.marginLeft = "8px";
                item.appendChild(flag);
            }
            const content = document.createElement("p");
            content.textContent = note.noteText || "";
            const meta = document.createElement("div");
            meta.className = "note-meta";
            meta.textContent = formatDateTime(note.createdAt);
            item.append(content, meta);
            list.appendChild(item);
        });
    } catch (error) {
        list.replaceChildren(emptyPanel(`Case notes unavailable: ${error.message}`, "state-panel error-text"));
    }
}

async function addAnalystNote() {
    if (!currentCaseId) return setStatusMessage("noteInputStatus", "Select a case before adding a note.", "error");
    const input = document.getElementById("noteTextInput");
    const noteText = input.value.trim();
    if (!noteText) return setStatusMessage("noteInputStatus", "Enter a note before saving.", "error");
    setStatusMessage("noteInputStatus", "Saving note…");
    try {
        await requestJSON(`/api/analysis/notes/${encodeURIComponent(currentCaseId)}`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ noteText, isFalsePositive: document.getElementById("falsePositiveCheckbox").checked })
        });
        input.value = "";
        document.getElementById("falsePositiveCheckbox").checked = false;
        setStatusMessage("noteInputStatus", "Note added to the case record.", "success");
        await loadAnalystNotes();
    } catch (error) {
        setStatusMessage("noteInputStatus", `Could not save note: ${error.message}`, "error");
    }
}

async function exportReport(format) {
    if (!currentCaseId) return setStatusMessage("exportStatusMessage", "Select a case before exporting.", "error");
    setStatusMessage("exportStatusMessage", `Preparing ${format} export…`);
    try {
        const report = await requestJSON(`/api/reports/export/${encodeURIComponent(currentCaseId)}?format=${encodeURIComponent(format)}`, { method: "POST" });
        const link = document.createElement("a");
        link.className = "download-link";
        link.href = `/api/reports/download?filePath=${encodeURIComponent(report.filePath)}`;
        link.target = "_blank";
        link.rel = "noopener";
        link.textContent = `Download ${report.reportFormat || format} report`;
        const status = document.getElementById("exportStatusMessage");
        status.replaceChildren(document.createTextNode("Report is ready. "), link);
        status.className = "status-message success";
    } catch (error) {
        setStatusMessage("exportStatusMessage", `Export failed: ${error.message}`, "error");
    }
}

function openUploadModal() {
    if (!currentCaseId) return setAppStatus("Create or select an investigation before importing evidence.", true);
    document.getElementById("uploadStatus").textContent = "";
    document.getElementById("uploadModal").hidden = false;
}

function closeUploadModal() {
    document.getElementById("uploadModal").hidden = true;
}

async function uploadLogFile() {
    if (!currentCaseId) return setStatusMessage("uploadStatus", "Select an investigation first.", "error");
    const input = document.getElementById("logFileInput");
    if (!input.files || input.files.length === 0) return setStatusMessage("uploadStatus", "Choose a CSV or JSON file first.", "error");
    const file = input.files[0];
    const extension = file.name.split(".").pop().toLowerCase();
    if (!["csv", "json"].includes(extension)) return setStatusMessage("uploadStatus", "Only CSV and JSON log files are supported.", "error");
    const formData = new FormData();
    formData.append("file", file);
    const endpoint = extension === "json" ? "json" : "csv";
    setStatusMessage("uploadStatus", "Parsing log file…");
    try {
        const result = await requestJSON(`/api/events/import/${endpoint}/${encodeURIComponent(currentCaseId)}`, { method: "POST", body: formData });
        setStatusMessage("uploadStatus", `Import complete · ${result.totalProcessed} processed · ${result.validCount} valid · ${result.errorCount} errors`, "success");
        await loadCaseData(currentCaseId);
        input.value = "";
        window.setTimeout(closeUploadModal, 1800);
    } catch (error) {
        setStatusMessage("uploadStatus", `Import failed: ${error.message}`, "error");
    }
}

function openNewCaseModal() {
    document.getElementById("newCaseStatus").textContent = "";
    document.getElementById("newCaseModal").hidden = false;
}

function closeNewCaseModal() {
    document.getElementById("newCaseModal").hidden = true;
}

async function createNewCase() {
    const caseNumber = document.getElementById("newCaseNumber").value.trim();
    const title = document.getElementById("newCaseTitle").value.trim();
    const description = document.getElementById("newCaseDesc").value.trim();
    if (!caseNumber || !title) return setStatusMessage("newCaseStatus", "Enter a case number and title.", "error");
    setStatusMessage("newCaseStatus", "Creating investigation…");
    try {
        const created = await requestJSON("/api/cases", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ caseNumber, title, description })
        });
        document.getElementById("newCaseNumber").value = "";
        document.getElementById("newCaseTitle").value = "";
        document.getElementById("newCaseDesc").value = "";
        closeNewCaseModal();
        await loadCases(created.id);
    } catch (error) {
        setStatusMessage("newCaseStatus", `Could not create case: ${error.message}`, "error");
    }
}
