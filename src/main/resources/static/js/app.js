// Threat Trace Client Application Logic
let currentCaseId = 1;
let currentTimelineEvents = [];

document.addEventListener("DOMContentLoaded", () => {
    loadCaseData(currentCaseId);
});

function switchTab(tabId) {
    document.querySelectorAll(".tab-btn").forEach(btn => btn.classList.remove("active"));
    document.querySelectorAll(".tab-content").forEach(content => content.classList.remove("active"));

    event.currentTarget.classList.add("active");
    document.getElementById(tabId).classList.add("active");

    if (tabId === "lineageTab") loadProcessTree();
    if (tabId === "killChainTab") loadKillChainAndDNA();
    if (tabId === "riskTab") loadRiskBreakdown();
    if (tabId === "mitigationTab") loadMitigations();
    if (tabId === "notesTab") loadAnalystNotes();
}

async function loadCaseData(caseId) {
    currentCaseId = caseId;
    try {
        const caseRes = await fetch(`/api/cases/${caseId}`);
        const caseData = await caseRes.json();

        document.getElementById("kpiRiskScore").innerText = caseData.riskScore ? caseData.riskScore.toFixed(1) : "0.0";
        document.getElementById("kpiSeverityLevel").innerText = `SEVERITY: ${caseData.severityLevel || 'MEDIUM'}`;

        loadTimeline();
        loadKillChainAndDNA();
    } catch (e) {
        console.error("Failed to load case data:", e);
    }
}

async function loadTimeline() {
    try {
        const res = await fetch(`/api/events/timeline/${currentCaseId}`);
        currentTimelineEvents = await res.json();

        document.getElementById("kpiTotalEvents").innerText = currentTimelineEvents.length;
        renderTimelineTable(currentTimelineEvents);
    } catch (e) {
        console.error("Failed to load timeline:", e);
    }
}

function renderTimelineTable(events) {
    const tbody = document.getElementById("timelineTableBody");
    tbody.innerHTML = "";

    if (events.length === 0) {
        tbody.innerHTML = `<tr><td colspan="8" class="text-center">No event logs recorded for this case.</td></tr>`;
        return;
    }

    events.forEach(e => {
        const tr = document.createElement("tr");
        const sevClass = `badge badge-${(e.severity || 'low').toLowerCase()}`;

        tr.innerHTML = `
            <td><code>${e.eventCode || 'EVT-' + e.id}</code></td>
            <td>${e.eventTime ? e.eventTime.replace('T', ' ') : ''}</td>
            <td><strong>${e.eventType}</strong></td>
            <td><span class="${sevClass}">${e.severity}</span></td>
            <td>${e.device ? e.device.hostname : 'N/A'}</td>
            <td>${e.processName || e.userName || 'N/A'}</td>
            <td>${e.target || e.source || '-'}</td>
            <td>${e.description || '-'}</td>
        `;
        tbody.appendChild(tr);
    });
}

function applyTimelineFilters() {
    const typeFilter = document.getElementById("filterEventType").value.toUpperCase();
    const sevFilter = document.getElementById("filterSeverity").value.toUpperCase();
    const hostFilter = document.getElementById("filterHostname").value.toLowerCase();

    const filtered = currentTimelineEvents.filter(e => {
        const matchType = !typeFilter || e.eventType === typeFilter;
        const matchSev = !sevFilter || e.severity === sevFilter;
        const matchHost = !hostFilter || (e.device && e.device.hostname.toLowerCase().includes(hostFilter));
        return matchType && matchSev && matchHost;
    });

    renderTimelineTable(filtered);
}

async function loadProcessTree() {
    const container = document.getElementById("processTreeContainer");
    container.innerHTML = "<p>Reconstructing process lineage tree...</p>";

    try {
        const res = await fetch(`/api/events/process-tree/${currentCaseId}`);
        const forest = await res.json();

        if (forest.length === 0) {
            container.innerHTML = "<p>No parent-child process tree relationships found.</p>";
            return;
        }

        container.innerHTML = "";
        forest.forEach(root => {
            container.appendChild(renderTreeNode(root));
        });
    } catch (e) {
        container.innerHTML = `<p class="error">Tree Build Error: ${e.message}</p>`;
    }
}

function renderTreeNode(node) {
    const div = document.createElement("div");
    div.className = "tree-node";
    const sevClass = `badge badge-${(node.severity || 'low').toLowerCase()}`;

    let html = `
        <div>
            <span class="tree-node-title">⚙ ${node.processName}</span>
            <span class="${sevClass}">${node.severity}</span>
            <span style="color: var(--text-secondary); font-size: 11px;"> [${node.eventCode}] (${node.eventTime ? node.eventTime.replace('T', ' ') : ''})</span>
            ${node.target ? `<div style="font-size: 11px; color: #a0aec0;">Target: ${node.target}</div>` : ''}
        </div>
    `;

    if (node.children && node.children.length > 0) {
        const childrenDiv = document.createElement("div");
        node.children.forEach(child => {
            childrenDiv.appendChild(renderTreeNode(child));
        });
        div.innerHTML = html;
        div.appendChild(childrenDiv);
    } else {
        div.innerHTML = html;
    }

    return div;
}

async function loadKillChainAndDNA() {
    try {
        await fetch(`/api/analysis/kill-chain/${currentCaseId}`, { method: 'POST' });
        const dnaRes = await fetch(`/api/analysis/dna/${currentCaseId}`);
        const dna = await dnaRes.json();

        document.getElementById("kpiLeadActor").innerText = dna.leadThreatActorMatch || "ANALYZED";
        document.getElementById("kpiActorSimilarity").innerText = `${dna.similarityScorePercentage || 0}% Lead Match`;

        const stages = ["Reconnaissance", "Weaponization", "Delivery", "Exploitation", "Installation", "Command & Control (C2)", "Actions on Objectives"];
        const pipelineContainer = document.getElementById("killChainPipeline");
        pipelineContainer.innerHTML = "";

        let activeCount = 0;
        stages.forEach(stageName => {
            const isObserved = dna.observedKillChainStages && dna.observedKillChainStages.includes(stageName);
            if (isObserved) activeCount++;
            const stageDiv = document.createElement("div");
            stageDiv.className = `pipeline-stage ${isObserved ? 'active-stage' : ''}`;
            stageDiv.innerHTML = `
                <div style="font-size: 11px; color: var(--text-secondary);">${isObserved ? '✔ DETECTED' : 'INACTIVE'}</div>
                <div style="font-weight: 700; margin-top: 4px;">${stageName}</div>
            `;
            pipelineContainer.appendChild(stageDiv);
        });

        document.getElementById("kpiStagesCount").innerText = `${activeCount} / 7`;

        const seqContainer = document.getElementById("dnaSequence");
        seqContainer.innerHTML = "";
        if (dna.eventTypeSequence) {
            dna.eventTypeSequence.forEach(t => {
                const chip = document.createElement("span");
                chip.className = "badge badge-medium";
                chip.style.marginRight = "6px";
                chip.innerText = t;
                seqContainer.appendChild(chip);
            });
        }
    } catch (e) {
        console.error("Kill chain error:", e);
    }
}

async function loadRiskBreakdown() {
    try {
        const res = await fetch(`/api/analysis/risk/${currentCaseId}`, { method: 'POST' });
        const risk = await res.json();

        document.getElementById("riskTotalScoreDisplay").innerText = risk.totalScore.toFixed(1);
        const list = document.getElementById("riskFactorBreakdownList");
        list.innerHTML = "";

        risk.explanations.forEach(exp => {
            const item = document.createElement("div");
            item.style.padding = "10px";
            item.style.borderBottom = "1px solid var(--border-color)";
            item.innerText = exp;
            list.appendChild(item);
        });
    } catch (e) {
        console.error("Risk error:", e);
    }
}

async function loadMitigations() {
    const strategy = document.getElementById("mitigationStrategySelect").value;
    const grid = document.getElementById("mitigationsGrid");
    grid.innerHTML = "<p>Evaluating mitigation catalog...</p>";

    try {
        const res = await fetch(`/api/analysis/mitigations/${currentCaseId}?strategy=${strategy}`);
        const mitigations = await res.json();

        grid.innerHTML = "";
        mitigations.forEach(cm => {
            const card = document.createElement("div");
            card.className = "kpi-card";
            card.style.marginBottom = "12px";
            card.innerHTML = `
                <div style="display: flex; justify-space-between; align-items: center;">
                    <strong style="color: var(--accent-cyan);">${cm.mitigationStrategy.actionName}</strong>
                    <span class="badge badge-high">Rec. Score: ${cm.recommendationScore.toFixed(1)}</span>
                </div>
                <p style="font-size: 12px; margin: 8px 0;">${cm.mitigationStrategy.description}</p>
                <div style="font-size: 11px; color: var(--text-secondary);">
                    Category: ${cm.mitigationStrategy.category} | Effort: ${cm.mitigationStrategy.effortLevel} | Impact: ${cm.mitigationStrategy.businessImpact}
                </div>
                <div style="margin-top: 8px; font-size: 11px; font-style: italic;">Rationale: ${cm.analystRationale}</div>
            `;
            grid.appendChild(card);
        });
    } catch (e) {
        grid.innerHTML = `<p class="error">Failed to load mitigations: ${e.message}</p>`;
    }
}

async function loadAnalystNotes() {
    try {
        const res = await fetch(`/api/analysis/notes/${currentCaseId}`);
        const notes = await res.json();

        const list = document.getElementById("analystNotesList");
        list.innerHTML = "";

        notes.forEach(n => {
            const item = document.createElement("div");
            item.style.padding = "10px";
            item.style.borderBottom = "1px solid var(--border-color)";
            item.innerHTML = `
                <div><strong>${n.analyst.fullName}</strong> ${n.isFalsePositiveFlag ? '<span class="badge badge-medium">FALSE POSITIVE</span>' : ''}</div>
                <p style="font-size: 13px;">${n.noteText}</p>
                <div style="font-size: 10px; color: var(--text-secondary);">${n.createdAt ? n.createdAt.replace('T', ' ') : ''}</div>
            `;
            list.appendChild(item);
        });
    } catch (e) {
        console.error("Notes error:", e);
    }
}

async function addAnalystNote() {
    const text = document.getElementById("noteTextInput").value;
    const fp = document.getElementById("falsePositiveCheckbox").checked;

    if (!text.trim()) return;

    await fetch(`/api/analysis/notes/${currentCaseId}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ noteText: text, isFalsePositive: fp })
    });

    document.getElementById("noteTextInput").value = "";
    loadAnalystNotes();
}

async function exportReport(format) {
    const statusMsg = document.getElementById("exportStatusMessage");
    statusMsg.innerText = `Generating ${format} report...`;

    try {
        const res = await fetch(`/api/reports/export/${currentCaseId}?format=${format}`, { method: 'POST' });
        const report = await res.json();
        statusMsg.innerHTML = `✔ Report generated: <a href="/api/reports/download?filePath=${encodeURIComponent(report.filePath)}" style="color: var(--accent-cyan);" target="_blank">Download ${report.reportFormat} Report</a>`;
    } catch (e) {
        statusMsg.innerText = `Export Error: ${e.message}`;
    }
}

function openUploadModal() { document.getElementById("uploadModal").style.display = "flex"; }
function closeUploadModal() { document.getElementById("uploadModal").style.display = "none"; }

async function uploadLogFile() {
    const input = document.getElementById("logFileInput");
    if (!input.files || input.files.length === 0) return;

    const file = input.files[0];
    const formData = new FormData();
    formData.append("file", file);

    const isJson = file.name.endsWith(".json");
    const endpoint = isJson ? `/api/events/import/json/${currentCaseId}` : `/api/events/import/csv/${currentCaseId}`;

    const status = document.getElementById("uploadStatus");
    status.innerText = "Parsing log file...";

    try {
        const res = await fetch(endpoint, { method: 'POST', body: formData });
        const result = await res.json();

        status.innerText = `Import Complete! Processed: ${result.totalProcessed}, Valid: ${result.validCount}, Errors: ${result.errorCount}`;
        loadTimeline();
        setTimeout(closeUploadModal, 2000);
    } catch (e) {
        status.innerText = `Upload Error: ${e.message}`;
    }
}
