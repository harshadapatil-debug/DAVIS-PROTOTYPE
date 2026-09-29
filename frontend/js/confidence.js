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

  function parseFactors(factorsJson) {
    if (!factorsJson) return [];
    if (Array.isArray(factorsJson)) return factorsJson;
    try {
      const value = JSON.parse(factorsJson);
      return Array.isArray(value) ? value : [];
    } catch {
      return [];
    }
  }

  function render(confidence) {
    if (!confidence) {
      return '<div class="empty-state"><strong>No confidence assessment available.</strong><span>Run the investigation to generate an assessment from the backend.</span></div>';
    }

    const rawScore = Number(confidence.score);
    const score = Number.isFinite(rawScore) ? rawScore : null;
    const factors = parseFactors(confidence.factorsJson);

    const riskClass = String(confidence.riskLevel || '').toLowerCase().replace(/[^a-z0-9_-]/g, '');
    const factorsRows = factors.map((factor) => `<tr>
      <td>#${escapeHtml(factor.evidenceId)}</td>
      <td>${escapeHtml(factor.type)}</td>
      <td>${escapeHtml(factor.weight)}</td>
      <td>${escapeHtml(factor.strength)}</td>
      <td>${escapeHtml(factor.reliability)}</td>
      <td><span class="direction-pill ${String(factor.direction || '').toLowerCase()}">${escapeHtml(factor.direction)}</span></td>
    </tr>`).join('');

    return `<div class="assessment-grid">
      <div class="score-panel">
        <div class="eyebrow">CONFIDENCE SCORE</div>
        <div class="score-number">${score === null ? '—' : escapeHtml(score.toFixed(2))}</div>
        <div class="score-track" role="img" aria-label="Confidence score ${score === null ? 'not available' : score.toFixed(2) + ' out of 100'}"><span style="width:${score === null ? 0 : Math.max(0, Math.min(100, score))}%"></span></div>
        <div class="score-scale"><span>0</span><span>100</span></div>
        <div class="score-label-row"><span>Risk level</span><strong class="risk-value ${riskClass}">${escapeHtml(confidence.riskLevel || '—')}</strong></div>
        <div class="score-label-row"><span>Calculated</span><strong>${escapeHtml(confidence.calculatedAt || '—')}</strong></div>
      </div>

      <div class="explanation-panel">
        <div class="section-kicker">Assessment explanation</div>
        <p>${escapeHtml(confidence.explanation || '—')}</p>
        <div class="disclaimer"><strong>Interpretation:</strong> This score is an explainable prototype assessment. It is not a calibrated probability and not identity proof.</div>
      </div>
    </div>
    <div class="section-block">
      <div class="section-heading"><div><h2>Assessment factors</h2><p>Values below are returned by the backend confidence engine.</p></div></div>
      <div class="table-wrap">
        <table class="data-table compact">
          <thead><tr><th>Evidence</th><th>Type</th><th>Weight</th><th>Strength</th><th>Reliability</th><th>Direction</th></tr></thead>
          <tbody>${factorsRows || '<tr><td colspan="6" class="state-cell">No factor details returned.</td></tr>'}</tbody>
        </table>
      </div>
    </div>`;
  }

  window.DavisConfidence = { render };
})();
