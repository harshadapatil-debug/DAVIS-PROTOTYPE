(function () {
  'use strict';

  function escapeHtml(value) {
    return String(value ?? '')
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#039;');
  }

  function render(report) {
    const toolbar = `<div class="report-toolbar">
      <button class="btn secondary" id="view-report" type="button">${report ? 'Refresh report' : 'View report'}</button>
      <div class="export-actions">
        <button class="btn secondary" id="export-pdf" type="button">Export PDF</button>
        <button class="btn secondary" id="export-csv" type="button">Export CSV</button>
        <button class="btn secondary" id="export-json" type="button">Export JSON</button>
      </div>
    </div>`;

    if (!report) {
      return toolbar + '<div class="empty-state"><strong>No report loaded.</strong><span>Use View report to fetch the current backend report.</span></div>';
    }

    const caseData = report.case || {};
    const indicator = report.knownIndicator || {};
    const summary = report.summary || {};
    const confidence = report.confidence || {};
    const review = report.review || {};

    return toolbar + `<article class="report-sheet">
      <div class="report-title-row">
        <div>
          <div class="eyebrow">BACKEND REPORT</div>
          <h2>${escapeHtml(report.reportTitle || '—')}</h2>
          <p>Generated ${escapeHtml(report.generatedAt || '—')}</p>
        </div>
        <span class="status-pill">${escapeHtml(caseData.status || '—')}</span>
      </div>

      <div class="report-grid">
        <div><span>Case</span><strong>${escapeHtml(caseData.caseName || '—')}</strong></div>
        <div><span>Case ID</span><strong>#${escapeHtml(caseData.caseId || '—')}</strong></div>
        <div><span>Indicator</span><strong>${escapeHtml(indicator.indicatorType || '—')} · ${escapeHtml(indicator.indicatorValue || '—')}</strong></div>
        <div><span>Confidence score</span><strong>${escapeHtml(confidence.score ?? '—')}</strong></div>
        <div><span>Risk level</span><strong>${escapeHtml(confidence.riskLevel || '—')}</strong></div>
      </div>

      <div class="report-section"><h3>Summary</h3><p>${escapeHtml(caseData.description || '—')}</p>
        <div class="summary-inline">
          <span>Indicators <b>${escapeHtml(summary.indicatorCount ?? '—')}</b></span>
          <span>Entities <b>${escapeHtml(summary.entityCount ?? '—')}</b></span>
          <span>Relationships <b>${escapeHtml(summary.relationshipCount ?? '—')}</b></span>
          <span>Evidence <b>${escapeHtml(summary.evidenceCount ?? '—')}</b></span>
        </div>
      </div>

      <div class="report-section"><h3>Assessment</h3><p>${escapeHtml(confidence.explanation || '—')}</p></div>
      <div class="report-section"><h3>Review</h3><p>Status: ${escapeHtml(review.status || '—')}</p><p>${escapeHtml(review.note || '—')}</p></div>
      <div class="report-disclaimer">${escapeHtml(report.disclaimer || '—')}</div>
    </article>`;
  }

  window.DavisReport = { render };
})();
