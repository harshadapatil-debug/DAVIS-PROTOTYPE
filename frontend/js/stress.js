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

  function renderSelector(evidence, previousResult) {
    if (!Array.isArray(evidence) || !evidence.length) {
      return '<div class="empty-state"><strong>No evidence available for stress testing.</strong><span>The backend must return evidence before a remove-one-signal test can run.</span></div>';
    }
    return `<div class="stress-form">
      <label class="grow">
        <span>Evidence to temporarily remove</span>
        <select id="stress-evidence-select">
          ${evidence.map(item => `<option value="${escapeHtml(item.evidenceId)}">#${escapeHtml(item.evidenceId)} · ${escapeHtml(item.evidenceType)} · ${escapeHtml(item.direction)}</option>`).join('')}
        </select>
      </label>
      <button class="btn primary" id="stress-run" type="button">Run stress test</button>
      <button class="btn secondary" id="stress-reset" type="button">Reset</button>
    </div>
    <div id="stress-result">${previousResult ? renderResult(previousResult) : ''}</div>`;
  }

  function renderResult(result) {
    if (!result) return '';
    const scoreChange = Number(result.scoreChange);
    const changeText = Number.isFinite(scoreChange) ? scoreChange.toFixed(2) : String(result.scoreChange ?? '—');
    return `<div class="stress-result">
      <div class="stress-result-header">
        <div>
          <div class="eyebrow">STRESS TEST RESULT</div>
          <h3>Evidence #${escapeHtml(result.removedEvidenceId)} removed temporarily</h3>
        </div>
        <span class="status-pill">${escapeHtml(result.robustnessIndicator || '—')}</span>
      </div>
      <div class="stress-metrics">
        <div><span>Baseline score</span><strong>${escapeHtml(result.baselineScore ?? '—')}</strong></div>
        <div><span>Challenged score</span><strong>${escapeHtml(result.challengedScore ?? '—')}</strong></div>
        <div><span>Score change</span><strong>${escapeHtml(changeText)}</strong></div>
        <div><span>Baseline risk</span><strong>${escapeHtml(result.baselineRiskLevel ?? '—')}</strong></div>
        <div><span>Challenged risk</span><strong>${escapeHtml(result.challengedRiskLevel ?? '—')}</strong></div>
        <div><span>Removed type</span><strong>${escapeHtml(result.removedEvidenceType ?? '—')}</strong></div>
      </div>
      <div class="stress-explanation">${escapeHtml(result.explanation || '—')}</div>
    </div>`;
  }

  function renderResetResult(result) {
    if (!result) return '<div class="empty-state compact"><span>No reset response returned.</span></div>';
    return `<div class="inline-alert success">${escapeHtml(result.message || 'Stress test reset.')}
      <div class="reset-summary"><span>Status <strong>${escapeHtml(result.status || '—')}</strong></span><span>Score <strong>${escapeHtml(result.score ?? result.currentScore ?? '—')}</strong></span><span>Risk level <strong>${escapeHtml(result.riskLevel ?? result.currentRiskLevel ?? '—')}</strong></span></div>
    </div>`;
  }

  window.DavisStress = { renderSelector, renderResult, renderResetResult };
})();
