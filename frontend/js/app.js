(function () {
  'use strict';

  const state = {
    cases: [],
    caseData: null,
    investigation: null,
    indicators: [],
    knownIndicator: null,
    investigationAvailable: false,
    entities: [],
    relationships: [],
    evidence: [],
    confidence: null,
    report: null,
    activeTab: 'overview',
    selectedRelationshipId: null,
    selectedEntityId: null,
    selectedEvidenceId: null,
    stressResult: null,
    caseLoadError: null,
    loading: false
  };

  const $ = (selector, root = document) => root.querySelector(selector);
  const $$ = (selector, root = document) => [...root.querySelectorAll(selector)];

  function escapeHtml(value) {
    return String(value ?? '')
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#039;');
  }

  function safeDate(value) {
    if (!value) return '—';
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? String(value) : date.toLocaleString();
  }

  function label(value) {
    return String(value ?? '').replace(/_/g, ' ');
  }

  function showHomeAlert(message, kind = 'error') {
    const el = $('#home-alert');
    el.hidden = !message;
    el.className = `inline-alert ${kind}`;
    el.textContent = message || '';
  }

  function showCaseAlert(message, kind = 'error') {
    const el = $('#case-alert');
    el.hidden = !message;
    el.className = `inline-alert ${kind}`;
    el.textContent = message || '';
  }

  function setConnection(online, text) {
    const el = $('#connection-status');
    el.className = `connection ${online ? 'online' : 'offline'}`;
    const textEl = el.querySelector('.connection-text');
    if (textEl) textEl.textContent = text;
  }

  function handleApiError(error) {
    return error && error.status ? 'API error' : 'API unavailable';
  }

  function setView(name) {
    const home = $('#home-view');
    const caseView = $('#case-view');
    const caseNav = $('#case-side-nav');
    const casesNav = $('#rail-cases');
    home.hidden = name !== 'home';
    caseView.hidden = name !== 'case';
    if (caseNav) caseNav.hidden = name !== 'case';
    if (casesNav) casesNav.classList.toggle('active', name === 'home');
    $('#page-title').textContent = name === 'home' ? 'Case register' : 'Case investigation';
  }

  function updateUrl(caseId) {
    const url = new URL(window.location.href);
    if (caseId) url.searchParams.set('caseId', caseId);
    else url.searchParams.delete('caseId');
    window.history.replaceState({}, '', url);
  }

  function casePanelLoading() {
    const markup = '<div class="empty-state"><strong>Loading case…</strong></div>';
    ['overview-content', 'entities-content', 'relationships-content', 'evidence-content', 'assessment-content', 'review-content', 'report-content']
      .forEach(id => { const el = $('#' + id); if (el) el.innerHTML = markup; });
  }

  function casePanelError(message) {
    const markup = `<div class="empty-state error-state"><strong>Unable to load investigation data.</strong><span>${escapeHtml(message || 'The backend did not return the requested investigation.')}</span></div>`;
    ['overview-content', 'entities-content', 'relationships-content', 'evidence-content', 'assessment-content', 'review-content', 'report-content']
      .forEach(id => { const el = $('#' + id); if (el) el.innerHTML = markup; });
  }

  function setInvestigationData(investigation) {
    const result = investigation && typeof investigation === 'object' ? investigation : {};
    state.investigation = result;
    state.caseData = result.case || state.caseData || null;
    state.indicators = Array.isArray(result.indicators) ? result.indicators : [];
    state.knownIndicator = result.knownIndicator || state.indicators[0] || null;
    state.entities = Array.isArray(result.entities) ? result.entities : [];
    state.relationships = Array.isArray(result.relationships) ? result.relationships : [];
    state.evidence = Array.isArray(result.evidence) ? result.evidence : [];
    state.confidence = result.confidence || null;
    state.investigationAvailable = Boolean(state.caseData && String(state.caseData.status || '').toUpperCase() !== 'OPEN');
    state.caseLoadError = null;
  }

  function setNotInvestigatedState() {
    const preservedIndicators = Array.isArray(state.indicators) ? state.indicators : [];
    const preservedIndicator = state.knownIndicator || preservedIndicators[0] || null;
    state.investigation = null;
    state.indicators = preservedIndicators;
    state.knownIndicator = preservedIndicator;
    state.entities = [];
    state.relationships = [];
    state.evidence = [];
    state.confidence = null;
    state.investigationAvailable = false;
    state.caseLoadError = null;
  }

  function isMissingInvestigation(error) {
    return Boolean(error && error.status === 404);
  }

  const CASE_CREATE_STATUS = 'OPEN';
  const REVIEW_STATUSES = ['PENDING', 'ACCEPTED', 'FLAGGED', 'REJECTED'];
  // These values are verified against the current backend/contract: Case.status requires a value,
  // the documented initial status is OPEN, and ReviewService accepts these four review statuses.


  async function loadCases() {
    showHomeAlert('');
    $('#cases-body').innerHTML = '<tr><td colspan="6" class="state-cell">Loading cases…</td></tr>';
    try {
      const result = await DavisApi.listCases();
      if (!Array.isArray(result)) throw new Error('The cases response is not a list.');
      state.cases = result;
      setConnection(true, 'API connected');
      renderCases();
    } catch (error) {
      state.cases = [];
      setConnection(Boolean(error && error.status), handleApiError(error));
      $('#cases-body').innerHTML = '<tr><td colspan="6" class="state-cell error-state">Unable to load cases. Please try again.</td></tr>';
      showHomeAlert(`Unable to load cases: ${error.message}`);
    }
  }

  function renderCases() {
    if (!Array.isArray(state.cases) || state.cases.length === 0) {
      $('#cases-body').innerHTML = '<tr><td colspan="6" class="state-cell"><strong>No cases available.</strong><span>Create a case to start an investigation.</span></td></tr>';
      return;
    }

    $('#cases-body').innerHTML = state.cases.map(caseData => `<tr>
      <td><div class="table-primary">${escapeHtml(caseData.caseName || '—')}</div><div class="table-secondary">${escapeHtml(label(caseData.category || ''))}</div></td>
      <td>#${escapeHtml(caseData.caseId)}</td>
      <td><span class="status-pill">${escapeHtml(caseData.status || '—')}</span></td>
      <td>${escapeHtml(safeDate(caseData.createdAt))}</td>
      <td>${escapeHtml(safeDate(caseData.lastScanAt))}</td>
      <td><button class="table-action" type="button" data-open-case="${escapeHtml(caseData.caseId)}">Open</button></td>
    </tr>`).join('');
  }

  async function openCase(caseId, preferredTab = 'overview') {
    state.caseData = null;
    state.investigation = null;
    state.indicators = [];
    state.knownIndicator = null;
    state.investigationAvailable = false;
    state.entities = [];
    state.relationships = [];
    state.evidence = [];
    state.confidence = null;
    state.report = null;
    state.selectedRelationshipId = null;
    state.selectedEntityId = null;
    state.selectedEvidenceId = null;
    state.stressResult = null;
    state.caseLoadError = null;
    state.activeTab = preferredTab;
    setView('case');
    updateUrl(caseId);
    showCaseAlert('');
    $('#case-title').textContent = 'Loading…';
    $('#case-meta').textContent = '';
    $('#case-overview-strip').innerHTML = '';
    casePanelLoading();
    setActiveTab(preferredTab);

    try {
      await loadCaseData(caseId);
      renderCaseChrome();
      renderAllCasePanels();
      setConnection(true, 'API connected');
      if (!state.investigationAvailable) {
        showCaseAlert('This case has not been investigated yet.', 'success');
      }
    } catch (error) {
      state.caseLoadError = error.message;
      setConnection(Boolean(error && error.status), handleApiError(error));
      if (state.caseData) {
        renderCaseChrome();
        renderAllCasePanels();
      } else {
        showCaseAlert(`Unable to load case: ${error.message}`);
        casePanelError(error.message);
      }
    }
  }

  async function loadCaseData(caseId) {
    const caseResult = await DavisApi.getCase(caseId);
    if (!caseResult || typeof caseResult !== 'object' || caseResult.caseId == null) {
      throw new Error('The case response is missing the required caseId.');
    }
    state.caseData = caseResult;

    try {
      const investigationResult = await DavisApi.getInvestigation(caseId);
      if (!investigationResult || typeof investigationResult !== 'object' || !investigationResult.case || investigationResult.case.caseId == null) {
        throw new Error('The investigation response is missing the required case object.');
      }
      setInvestigationData(investigationResult);
    } catch (error) {
      if (isMissingInvestigation(error)) {
        setNotInvestigatedState();
        return;
      }
      throw error;
    }
  }

  function renderCaseChrome() {
    const c = state.caseData || {};
    $('#case-title').textContent = c.caseName || '—';
    $('#case-meta').innerHTML = `<span>#${escapeHtml(c.caseId)}</span><span>${escapeHtml(c.status || '—')}</span><span>${escapeHtml(label(c.category || ''))}</span>`;
    $('#case-overview-strip').innerHTML = renderSummary(state.investigation?.summary);
    const runButton = $('#run-investigation');
    if (runButton) runButton.disabled = !state.knownIndicator;
  }

  function renderSummary(summary) {
    if (!summary || typeof summary !== 'object') return '';
    const s = summary;
    const values = [
      ['Indicators', s.indicatorCount],
      ['Entities', s.entityCount],
      ['Relationships', s.relationshipCount],
      ['Evidence', s.evidenceCount]
    ];
    return `<div class="summary-strip">
      ${values.map(([name, value]) => `<div class="summary-item"><span>${name}</span><strong>${escapeHtml(value ?? '—')}</strong></div>`).join('')}
    </div>`;
  }

  function setActiveTab(tab) {
    state.activeTab = tab;
    $$('.tab', $('#case-tabs')).forEach(button => button.classList.toggle('active', button.dataset.tab === tab));
    $$('.side-tab', $('#case-side-nav')).forEach(button => button.classList.toggle('active', button.dataset.tab === tab));
    $$('.tab-panel', $('#case-view')).forEach(panel => panel.classList.toggle('active', panel.dataset.panel === tab));
  }

  function renderAllCasePanels() {
    renderOverview();
    renderEntities();
    renderRelationships();
    renderEvidence();
    renderAssessment();
    renderReview();
    renderReportPanel();
  }

  function renderOverview() {
    const c = state.caseData || {};
    const i = state.investigation || {};
    const indicator = state.knownIndicator;
    const indicators = Array.isArray(state.indicators) ? state.indicators : [];
    const timeline = Array.isArray(i.timeline) ? i.timeline : [];

    const additionalIndicators = indicators.slice(1);
    const registeredIndicators = additionalIndicators.length
      ? `<div class="registered-indicators"><div class="section-kicker">OTHER REGISTERED INDICATORS</div>${additionalIndicators.map(item => `<div class="registered-indicator"><span>#${escapeHtml(item.indicatorId)} · ${escapeHtml(item.indicatorType)}</span><strong>${escapeHtml(item.indicatorValue)}</strong>${item.description ? `<small>${escapeHtml(item.description)}</small>` : ''}</div>`).join('')}</div>`
      : '';

    const notInvestigatedBlock = !state.investigationAvailable
      ? `<div class="section-block"><div class="empty-state compact"><strong>Not investigated yet.</strong><span>Add a known indicator, then run the investigation to load entities, relationships, evidence, and assessment.</span></div></div>`
      : '';

    const timelineBlock = timeline.length
      ? `<div class="section-block overview-timeline">
          <div class="section-heading"><div><div class="section-kicker">TIMELINE</div><h2>Evidence observations</h2></div></div>
          <div class="timeline-list">${timeline.map(event => `<button class="timeline-row" type="button" data-evidence-id="${escapeHtml(event.evidenceId)}">
            <span class="timeline-time">${escapeHtml(safeDate(event.observedAt))}</span>
            <span><strong>${escapeHtml(event.evidenceType || '—')}</strong><small>${escapeHtml(event.source || '—')} · ${escapeHtml(event.direction || '—')}${event.relationshipId != null ? ` · Relationship #${escapeHtml(event.relationshipId)}` : ''}</small><small>${escapeHtml(event.description || '—')}</small></span>
          </button>`).join('')}</div>
        </div>`
      : '<div class="section-block"><div class="empty-state compact"><strong>No timeline events.</strong><span>The backend returned no evidence observations.</span></div></div>';

    const confidence = i.confidence || state.confidence || null;
    const resultBanner = state.investigationAvailable
      ? `<div class="result-banner">
          <div class="result-banner-main">
            <div class="section-kicker">INVESTIGATION RESULT</div>
            <h2>Controlled intelligence analysis processed</h2>
            <p>DAVIS expanded the known indicator into backend entities, relationships, evidence and an attribution-confidence assessment.</p>
          </div>
          <div class="result-banner-metrics">
            <div><span>Assessment</span><strong>${escapeHtml(confidence?.riskLevel || '—')}</strong></div>
            <div><span>Confidence</span><strong>${escapeHtml(confidence?.score ?? '—')}</strong></div>
          </div>
        </div>`
      : '';

    $('#overview-content').innerHTML = `${resultBanner}<div class="overview-grid">
      <div class="primary-panel">
        <div class="section-heading"><div><div class="section-kicker">CASE INFORMATION</div><h2>Case details</h2></div></div>
        <div class="kv-grid">
          <div><span>Created</span><strong>${escapeHtml(safeDate(c.createdAt))}</strong></div>
          <div><span>Last scan</span><strong>${escapeHtml(safeDate(c.lastScanAt))}</strong></div>
          <div><span>Type</span><strong>${escapeHtml(label(c.caseType || '—'))}</strong></div>
          <div class="full-span"><span>Description</span><strong class="normal-weight">${escapeHtml(c.description || '—')}</strong></div>
        </div>
      </div>
      <div class="secondary-panel">
        <div class="section-kicker">KNOWN INDICATOR</div>
        ${indicator ? `<div class="indicator-focus"><span class="mini-type">Indicator #${escapeHtml(indicator.indicatorId)} · ${escapeHtml(indicator.indicatorType)}</span><strong>${escapeHtml(indicator.indicatorValue)}</strong><p>${escapeHtml(indicator.description || '')}</p></div>` : '<div class="empty-state compact"><strong>No known indicator.</strong><span>Add the starting clue before running the investigation.</span></div>'}
        ${registeredIndicators}
      </div>
    </div>
    ${notInvestigatedBlock}
    ${state.investigationAvailable ? timelineBlock : ''}
    ${i.disclaimer ? `<div class="disclaimer">${escapeHtml(i.disclaimer)}</div>` : ''}`;

    $$('.timeline-row').forEach(row => row.addEventListener('click', () => {
      state.selectedEvidenceId = String(row.dataset.evidenceId);
      setActiveTab('evidence');
      renderEvidence();
    }));
  }

  function renderEntities() {
    if (state.caseLoadError) {
      $('#entities-content').innerHTML = `<div class="empty-state error-state"><strong>Unable to load entities.</strong><span>${escapeHtml(state.caseLoadError)}</span></div>`;
      return;
    }
    if (!state.investigationAvailable) {
      $('#entities-content').innerHTML = '<div class="empty-state"><strong>Investigation not run yet.</strong><span>Run the investigation after adding a known indicator.</span></div>';
      return;
    }
    if (!state.entities.length) {
      $('#entities-content').innerHTML = '<div class="empty-state"><strong>No entities available.</strong><span>The backend returned no entities for this case.</span></div>';
      return;
    }
    $('#entities-content').innerHTML = `<div class="section-heading"><div><div class="section-kicker">ENTITY REGISTER</div><h2>Discovered entities</h2></div></div>
      <div class="table-wrap"><table class="data-table"><thead><tr><th>Entity type</th><th>Value</th><th>Discovery confidence</th><th>Description</th></tr></thead><tbody>
      ${state.entities.map(entity => `<tr><td><span class="tag">${escapeHtml(entity.entityType)}</span></td><td><div class="table-primary monospace">${escapeHtml(entity.entityValue)}</div></td><td>${escapeHtml(entity.discoveryConfidence ?? '—')}</td><td>${escapeHtml(entity.description || '—')}</td></tr>`).join('')}
      </tbody></table></div>`;
  }

  function timelineRelationshipMap() {
    const map = new Map();
    const timeline = Array.isArray(state.investigation?.timeline) ? state.investigation.timeline : [];
    timeline.forEach(item => {
      if (item.relationshipId != null && item.evidenceId != null) {
        const key = String(item.relationshipId);
        if (!map.has(key)) map.set(key, []);
        map.get(key).push(String(item.evidenceId));
      }
    });
    return map;
  }

  function selectRelationship(id) {
    state.selectedRelationshipId = String(id);
    renderRelationshipDetail();
    $$('.relationship-row').forEach(row => row.classList.toggle('selected', row.dataset.relationshipId === state.selectedRelationshipId));
    const graph = $('#relationship-graph');
    if (graph) {
      $$('.edge-group', graph).forEach(edge => edge.classList.toggle('selected', edge.dataset.edgeId === state.selectedRelationshipId));
    }
  }

  function selectGraphNode(id) {
    state.selectedEntityId = String(id);
    const entity = state.entities.find(item => String(item.entityId) === state.selectedEntityId);
    const detail = $('#graph-selection');
    if (!detail) return;
    if (!entity) {
      detail.innerHTML = '<div class="empty-state compact"><strong>Entity not found</strong><span>The selected graph node is not present in the current entity response.</span></div>';
      return;
    }
    detail.innerHTML = `<div class="detail-card">
      <div class="selection-title">SELECTED ENTITY · #${escapeHtml(entity.entityId)}</div>
      <div class="selection-value monospace">${escapeHtml(entity.entityValue)}</div>
      <div class="selection-meta">${escapeHtml(entity.entityType)} · Discovery confidence ${escapeHtml(entity.discoveryConfidence ?? '—')}</div>
      <div class="detail-description">${escapeHtml(entity.description || 'No description returned by the backend.')}</div>
    </div>`;
  }

  function renderRelationships() {
    if (state.caseLoadError) {
      $('#relationships-content').innerHTML = `<div class="empty-state error-state"><strong>Unable to load relationships.</strong><span>${escapeHtml(state.caseLoadError)}</span></div>`;
      return;
    }
    if (!state.investigationAvailable) {
      $('#relationships-content').innerHTML = '<div class="empty-state"><strong>Investigation not run yet.</strong><span>Relationships and the graph appear after the backend investigation succeeds.</span></div>';
      return;
    }
    if (!state.relationships.length && !state.investigation?.graph?.nodes?.length) {
      $('#relationships-content').innerHTML = '<div class="empty-state"><strong>No relationship data.</strong><span>The backend returned no relationships or graph nodes for this case.</span></div>';
      return;
    }

    $('#relationships-content').innerHTML = `<div class="relationship-layout">
      <div class="graph-panel" id="relationship-graph"></div>
      <div class="relationship-panel">
        <div class="section-heading slim"><div><div class="section-kicker">RELATIONSHIP REGISTER</div><h2>Relationships</h2></div></div>
        <div class="relationship-list" id="relationship-list"></div>
        <div id="relationship-detail"></div>
      </div>
    </div>`;

    const list = $('#relationship-list');
    if (state.relationships.length) {
      list.innerHTML = state.relationships.map(item => `<button class="relationship-row ${state.selectedRelationshipId === String(item.relationshipId) ? 'selected' : ''}" data-relationship-id="${escapeHtml(item.relationshipId)}" type="button">
        <span class="relationship-type">${escapeHtml(item.relationshipType)}</span>
        <span class="relationship-parties">${escapeHtml(item.sourceEntity?.entityValue || `#${item.sourceEntity?.entityId ?? '—'}`)} → ${escapeHtml(item.targetEntity?.entityValue || `#${item.targetEntity?.entityId ?? '—'}`)}</span>
        <span class="relationship-assessment ${String(item.assessment || '').toLowerCase()}">${escapeHtml(item.assessment || '—')}</span>
      </button>`).join('');
      $$('.relationship-row').forEach(row => row.addEventListener('click', () => selectRelationship(row.dataset.relationshipId)));
    } else {
      list.innerHTML = '<div class="empty-state compact"><strong>No relationships returned.</strong></div>';
    }

    const graph = state.investigation?.graph || { nodes: [], edges: [] };
    DavisGraph.render($('#relationship-graph'), graph, state.knownIndicator, selectRelationship, selectGraphNode);
    if (state.selectedRelationshipId == null && state.relationships.length) selectRelationship(state.relationships[0].relationshipId);
    else renderRelationshipDetail();
  }

  function renderRelationshipDetail() {
    const detail = $('#relationship-detail');
    if (!detail) return;
    const relationship = state.relationships.find(item => String(item.relationshipId) === String(state.selectedRelationshipId));
    if (!relationship) {
      detail.innerHTML = '<div class="empty-state compact"><span>Select a relationship to inspect it.</span></div>';
      return;
    }
    const evidenceMap = timelineRelationshipMap();
    const evidenceIds = evidenceMap.get(String(relationship.relationshipId)) || [];
    const linkedEvidence = state.evidence.filter(item => evidenceIds.includes(String(item.evidenceId)));

    detail.innerHTML = `<div class="relationship-detail">
      <div class="detail-header"><span class="tag">Relationship #${escapeHtml(relationship.relationshipId)} · ${escapeHtml(relationship.relationshipType)}</span><span class="relationship-assessment ${String(relationship.assessment || '').toLowerCase()}">${escapeHtml(relationship.assessment || '—')}</span></div>
      <p>${escapeHtml(relationship.description || '—')}</p>
      <div class="detail-parties"><span>Source</span><strong>${escapeHtml(relationship.sourceEntity?.entityValue || `#${relationship.sourceEntity?.entityId ?? '—'}`)}</strong><span>Target</span><strong>${escapeHtml(relationship.targetEntity?.entityValue || `#${relationship.targetEntity?.entityId ?? '—'}`)}</strong></div>
      <div class="detail-evidence"><div class="section-kicker">Linked evidence</div>${linkedEvidence.length ? linkedEvidence.map(item => `<button class="link-evidence" data-evidence-id="${escapeHtml(item.evidenceId)}" type="button">#${escapeHtml(item.evidenceId)} · ${escapeHtml(item.evidenceType)} · ${escapeHtml(item.direction)}</button>`).join('') : '<span class="muted">No linked evidence was returned through the investigation timeline.</span>'}</div>
    </div>`;

    $$('.link-evidence', detail).forEach(button => button.addEventListener('click', () => {
      state.selectedEvidenceId = String(button.dataset.evidenceId);
      setActiveTab('evidence');
      renderEvidence();
    }));
  }

  function renderEvidence() {
    if (state.caseLoadError) {
      $('#evidence-content').innerHTML = `<div class="empty-state error-state"><strong>Unable to load evidence.</strong><span>${escapeHtml(state.caseLoadError)}</span></div>`;
      return;
    }
    if (!state.investigationAvailable) {
      $('#evidence-content').innerHTML = '<div class="empty-state"><strong>Investigation not run yet.</strong><span>Evidence is available after the backend investigation succeeds.</span></div>';
      return;
    }
    if (!state.evidence.length) {
      $('#evidence-content').innerHTML = '<div class="empty-state"><strong>No evidence available.</strong><span>The backend returned no evidence for this case.</span></div>';
      return;
    }
    const detail = state.evidence.find(item => String(item.evidenceId) === String(state.selectedEvidenceId));
    $('#evidence-content').innerHTML = `<div class="evidence-layout">
      <div class="table-wrap evidence-table-wrap"><table class="data-table compact"><thead><tr><th>ID</th><th>Evidence type</th><th>Source</th><th>Strength</th><th>Reliability</th><th>Direction</th><th>Observed</th></tr></thead><tbody>
      ${state.evidence.map(item => `<tr class="evidence-row ${String(item.evidenceId) === String(state.selectedEvidenceId) ? 'selected' : ''}" data-evidence-id="${escapeHtml(item.evidenceId)}"><td>#${escapeHtml(item.evidenceId)}</td><td>${escapeHtml(item.evidenceType)}</td><td>${escapeHtml(item.source)}</td><td>${escapeHtml(item.strength)}</td><td>${escapeHtml(item.reliability)}</td><td><span class="direction-pill ${String(item.direction || '').toLowerCase()}">${escapeHtml(item.direction)}</span></td><td>${escapeHtml(safeDate(item.observedAt))}</td></tr>`).join('')}
      </tbody></table></div>
      <div class="evidence-detail" id="evidence-detail"></div>
    </div>`;

    $$('.evidence-row').forEach(row => row.addEventListener('click', () => {
      state.selectedEvidenceId = row.dataset.evidenceId;
      renderEvidence();
    }));

    const evidenceDetail = $('#evidence-detail');
    if (!detail) {
      evidenceDetail.innerHTML = '<div class="empty-state compact"><strong>Select evidence</strong><span>Choose a row to inspect the full description.</span></div>';
      return;
    }
    const relationMap = timelineRelationshipMap();
    let linkedRelationshipId = null;
    relationMap.forEach((ids, relationshipId) => { if (ids.includes(String(detail.evidenceId))) linkedRelationshipId = relationshipId; });
    evidenceDetail.innerHTML = `<div class="detail-card">
      <div class="section-kicker">EVIDENCE #${escapeHtml(detail.evidenceId)}</div>
      <h2>${escapeHtml(detail.evidenceType)}</h2>
      <div class="detail-grid">
        <div><span>Source</span><strong>${escapeHtml(detail.source)}</strong></div>
        <div><span>Observed</span><strong>${escapeHtml(safeDate(detail.observedAt))}</strong></div>
        <div><span>Strength</span><strong>${escapeHtml(detail.strength)}</strong></div>
        <div><span>Reliability</span><strong>${escapeHtml(detail.reliability)}</strong></div>
        <div><span>Direction</span><strong>${escapeHtml(detail.direction)}</strong></div>
        <div><span>Independence group</span><strong>${escapeHtml(detail.independenceGroup)}</strong></div>
        ${linkedRelationshipId ? `<div><span>Relationship</span><strong>#${escapeHtml(linkedRelationshipId)}</strong></div>` : ''}
      </div>
      <div class="detail-description">${escapeHtml(detail.description || '—')}</div>
    </div>`;
  }

  function renderAssessment() {
    if (state.caseLoadError) {
      $('#assessment-content').innerHTML = `<div class="empty-state error-state"><strong>Unable to load assessment.</strong><span>${escapeHtml(state.caseLoadError)}</span></div>`;
      return;
    }
    if (!state.investigationAvailable) {
      $('#assessment-content').innerHTML = '<div class="empty-state"><strong>Investigation not run yet.</strong><span>Run the investigation before requesting a confidence assessment or stress test.</span></div>';
      return;
    }
    $('#assessment-content').innerHTML = `<div class="section-heading"><div><div class="section-kicker">ASSESSMENT</div><h2>Confidence and robustness</h2><p>Assessment values are read from the backend; the frontend does not recalculate them.</p></div></div>
      ${DavisConfidence.render(state.confidence)}
      <div class="section-block stress-block">
        <div class="section-heading"><div><div class="section-kicker">STRESS TEST</div><h2>Remove one signal temporarily</h2><p>The backend returns the baseline and challenged assessment. Original evidence is not permanently changed.</p></div></div>
        <div id="stress-panel">${DavisStress.renderSelector(state.evidence, state.stressResult)}</div>
      </div>`;

    bindStressPanel();
  }

  function bindStressPanel() {
    const run = $('#stress-run');
    const reset = $('#stress-reset');
    const result = $('#stress-result');
    if (!run || !reset) return;
    run.addEventListener('click', async () => {
      const value = $('#stress-evidence-select')?.value;
      if (!value) return;
      run.disabled = true;
      run.textContent = 'Testing…';
      try {
        state.stressResult = await DavisApi.runStressTest(state.caseData.caseId, Number(value));
        result.innerHTML = DavisStress.renderResult(state.stressResult);
      } catch (error) {
        result.innerHTML = `<div class="inline-alert error">Unable to run stress test: ${escapeHtml(error.message)}</div>`;
      } finally {
        run.disabled = false;
        run.textContent = 'Run stress test';
      }
    });
    reset.addEventListener('click', async () => {
      reset.disabled = true;
      reset.textContent = 'Resetting…';
      try {
        const resetResult = await DavisApi.resetStressTest(state.caseData.caseId);
        state.stressResult = null;
        result.innerHTML = DavisStress.renderResetResult(resetResult);
      } catch (error) {
        result.innerHTML = `<div class="inline-alert error">Unable to reset stress test: ${escapeHtml(error.message)}</div>`;
      } finally {
        reset.disabled = false;
        reset.textContent = 'Reset';
      }
    });
  }

  function renderReview() {
    if (state.caseLoadError) {
      $('#review-content').innerHTML = `<div class="empty-state error-state"><strong>Unable to load review.</strong><span>${escapeHtml(state.caseLoadError)}</span></div>`;
      return;
    }
    if (!state.investigationAvailable) {
      $('#review-content').innerHTML = '<div class="empty-state"><strong>Investigation not run yet.</strong><span>Investigator review becomes available after the backend generates a finding.</span></div>';
      return;
    }
    const review = state.investigation?.review;
    if (!review) {
      $('#review-content').innerHTML = '<div class="empty-state"><strong>No review record available.</strong><span>The backend returned no review object for this case, so there is nothing to update.</span></div>';
      return;
    }
    $('#review-content').innerHTML = `<div class="review-layout">
      <div class="primary-panel"><div class="section-heading"><div><div class="section-kicker">INVESTIGATOR REVIEW</div><h2>Review status</h2></div></div>
        <form id="review-form" class="review-form">
          <label><span>Status</span><select name="status">
            ${REVIEW_STATUSES.map(option => `<option value="${option}" ${String(review.status).toUpperCase() === option ? 'selected' : ''}>${option}</option>`).join('')}
          </select></label>
          <label><span>Investigator notes</span><textarea name="note" rows="6">${escapeHtml(review.note || '')}</textarea></label>
          <div class="review-actions"><button class="btn primary" type="submit">Save review</button></div>
          <div class="form-error" id="review-error" role="alert"></div>
        </form>
      </div>
      <div class="secondary-panel"><div class="section-kicker">REVIEW RECORD</div><div class="review-meta"><span>Finding ID</span><strong>#${escapeHtml(review.findingId)}</strong></div><div class="review-meta"><span>Last updated</span><strong>${escapeHtml(safeDate(review.updatedAt))}</strong></div><p>Review updates do not modify or delete evidence.</p></div>
    </div>`;

    $('#review-form').addEventListener('submit', async (event) => {
      event.preventDefault();
      const form = event.currentTarget;
      const button = $('button[type="submit"]', form);
      const error = $('#review-error');
      error.textContent = '';
      button.disabled = true;
      try {
        state.investigation.review = await DavisApi.updateReview(state.caseData.caseId, {
          status: form.elements.status.value,
          note: form.elements.note.value
        });
        state.report = null;
        renderReview();
        showCaseAlert('Review saved.', 'success');
      } catch (err) {
        error.textContent = `Unable to save review: ${err.message}`;
      } finally {
        button.disabled = false;
      }
    });
  }

  function renderReportPanel() {
    if (!state.investigationAvailable) {
      $('#report-content').innerHTML = '<div class="empty-state"><strong>Investigation not run yet.</strong><span>The backend report becomes available after investigation results exist.</span></div>';
      return;
    }
    $('#report-content').innerHTML = DavisReport.render(state.report);
    const viewButton = $('#view-report');
    if (viewButton) viewButton.addEventListener('click', loadReport);
    bindExport('export-pdf', DavisApi.exportPdf, 'application/pdf', '.pdf');
    bindExport('export-csv', DavisApi.exportCsv, 'text/csv', '.csv');
    bindExport('export-json', DavisApi.exportJson, 'application/json', '.json');
  }

  function bindExport(buttonId, fn, fallbackType, extension) {
    const button = $('#' + buttonId);
    if (!button) return;
    button.addEventListener('click', async () => {
      button.disabled = true;
      const original = button.textContent;
      button.textContent = 'Exporting…';
      try {
        const result = await fn(state.caseData.caseId);
        const finalBlob = result.blob instanceof Blob ? result.blob : new Blob([result.blob], { type: result.type || fallbackType });
        const link = document.createElement('a');
        link.href = URL.createObjectURL(finalBlob);
        link.download = result.filename || `report${extension}`;
        document.body.appendChild(link);
        link.click();
        setTimeout(() => { URL.revokeObjectURL(link.href); link.remove(); }, 1000);
      } catch (error) {
        showCaseAlert(`Export failed: ${error.message}`);
      } finally {
        button.disabled = false;
        button.textContent = original;
      }
    });
  }

  async function loadReport() {
    const button = $('#view-report');
    if (button) { button.disabled = true; button.textContent = 'Loading report…'; }
    try {
      state.report = await DavisApi.getReport(state.caseData.caseId);
      renderReportPanel();
    } catch (error) {
      showCaseAlert(`Unable to load report: ${error.message}`);
    } finally {
      const current = $('#view-report');
      if (current) { current.disabled = false; current.textContent = state.report ? 'Refresh report' : 'View report'; }
    }
  }

  async function runInvestigation() {
    if (!state.caseData?.caseId) return;
    if (!state.knownIndicator) {
      showCaseAlert('Add a known indicator before running the investigation.');
      setActiveTab('overview');
      return;
    }
    state.loading = true;
    $('#run-investigation').disabled = true;
    $('#investigation-loading').hidden = false;
    showCaseAlert('');
    try {
      const investigationResult = await DavisApi.runInvestigation(state.caseData.caseId);
      if (!investigationResult || typeof investigationResult !== 'object' || !investigationResult.case || investigationResult.case.caseId == null) {
        throw new Error('The investigation response is missing the required case object.');
      }
      setInvestigationData(investigationResult);
      state.report = null;
      renderCaseChrome();
      renderAllCasePanels();
      setActiveTab('overview');
      showCaseAlert('Investigation request succeeded; results shown are the backend response.', 'success');
      await loadCases();
    } catch (error) {
      showCaseAlert(`Investigation failed: ${error.message}`);
    } finally {
      state.loading = false;
      $('#run-investigation').disabled = false;
      $('#investigation-loading').hidden = true;
    }
  }

  function openCaseModal() {
    const modal = $('#case-modal');
    $('#case-form').reset();
    $('#case-form-error').textContent = '';
    if (typeof modal.showModal === 'function') modal.showModal();
    else modal.setAttribute('open', '');
  }

  function openIndicatorModal() {
    const modal = $('#indicator-modal');
    $('#indicator-form').reset();
    $('#indicator-form-error').textContent = '';
    if (typeof modal.showModal === 'function') modal.showModal();
    else modal.setAttribute('open', '');
  }

  async function submitCase(event) {
    if (event.submitter?.value === 'cancel') return;
    event.preventDefault();
    const form = event.currentTarget;
    const submit = $('#create-case-submit');
    const error = $('#case-form-error');
    error.textContent = '';
    submit.disabled = true;
    try {
      const payload = {
        caseName: form.elements.caseName.value.trim(),
        caseType: form.elements.caseType.value,
        category: form.elements.category.value,
        description: form.elements.description.value.trim(),
        status: CASE_CREATE_STATUS
      };
      const created = await DavisApi.createCase(payload);
      if (!created || created.caseId == null) throw new Error('The case-create response is missing caseId.');
      $('#case-modal').close();
      await loadCases();
      await openCase(created.caseId);
    } catch (err) {
      error.textContent = `Unable to create case: ${err.message}`;
    } finally {
      submit.disabled = false;
    }
  }

  async function submitIndicator(event) {
    if (event.submitter?.value === 'cancel') return;
    event.preventDefault();
    if (!state.caseData?.caseId) return;
    const form = event.currentTarget;
    const submit = $('#add-indicator-submit');
    const error = $('#indicator-form-error');
    error.textContent = '';
    submit.disabled = true;
    try {
      const createdIndicator = await DavisApi.addIndicator(state.caseData.caseId, {
        indicatorType: form.elements.indicatorType.value,
        indicatorValue: form.elements.indicatorValue.value.trim(),
        description: form.elements.description.value.trim()
      });
      if (!createdIndicator || createdIndicator.indicatorId == null) {
        throw new Error('The indicator response is missing indicatorId.');
      }
      if (!Array.isArray(state.indicators)) state.indicators = [];
      state.indicators = [...state.indicators, createdIndicator];
      if (!state.knownIndicator) state.knownIndicator = createdIndicator;
      $('#indicator-modal').close();
      state.report = null;
      await loadCaseData(state.caseData.caseId);
      renderCaseChrome();
      renderAllCasePanels();
      setActiveTab('overview');
      showCaseAlert('Indicator added. Investigation has not been re-run yet.', 'success');
    } catch (err) {
      error.textContent = `Unable to add indicator: ${err.message}`;
    } finally {
      submit.disabled = false;
    }
  }

  function wireEvents() {
    $('#create-case-open').addEventListener('click', openCaseModal);
    $('#rail-new-case').addEventListener('click', openCaseModal);
    $('#rail-cases').addEventListener('click', async () => {
      updateUrl(null);
      setView('home');
      await loadCases();
    });
    $('#add-indicator-open').addEventListener('click', openIndicatorModal);
    $('#run-investigation').addEventListener('click', runInvestigation);
    $('#back-to-cases').addEventListener('click', async () => {
      updateUrl(null);
      setView('home');
      await loadCases();
    });
    $('#case-form').addEventListener('submit', submitCase);
    $('#indicator-form').addEventListener('submit', submitIndicator);

    document.addEventListener('click', (event) => {
      const openButton = event.target.closest('[data-open-case]');
      if (openButton) openCase(openButton.dataset.openCase);
      const tab = event.target.closest('[data-tab]');
      if (tab && (tab.classList.contains('tab') || tab.classList.contains('side-tab'))) setActiveTab(tab.dataset.tab);
    });
  }

  function boot() {
    wireEvents();
    const caseId = new URL(window.location.href).searchParams.get('caseId');
    if (caseId) {
      openCase(caseId);
    } else {
      setView('home');
      loadCases();
    }
  }

  boot();
})();
