(function () {
  'use strict';

  const base = String(window.DAVIS_API_BASE || 'http://localhost:8080').replace(/\/$/, '');

  async function request(path, options = {}) {
    const response = await fetch(base + path, {
      ...options,
      headers: {
        Accept: 'application/json',
        ...(options.body ? { 'Content-Type': 'application/json' } : {}),
        ...(options.headers || {})
      }
    });

    const text = await response.text();
    let data = null;
    if (text) {
      try { data = JSON.parse(text); } catch { data = text; }
    }

    if (!response.ok) {
      const detail = typeof data === 'string'
        ? data
        : (data && (data.message || data.error)) || response.statusText;
      const error = new Error(`${response.status} ${detail}`);
      error.status = response.status;
      error.data = data;
      throw error;
    }

    return data;
  }

  async function blob(path) {
    const response = await fetch(base + path, { headers: { Accept: '*/*' } });
    if (!response.ok) {
      const text = await response.text();
      let message = response.statusText;
      try {
        const parsed = JSON.parse(text);
        message = parsed.message || message;
      } catch {}
      throw new Error(`${response.status} ${message}`);
    }

    const disposition = response.headers.get('Content-Disposition') || '';
    const match = disposition.match(/filename\*?=(?:UTF-8''|"?)([^";]+)/i);
    const filename = match ? decodeURIComponent(match[1].trim().replace(/^"|"$/g, '')) : null;

    return {
      blob: await response.blob(),
      filename,
      type: response.headers.get('Content-Type') || ''
    };
  }

  window.DavisApi = {
    base,
    listCases: () => request('/api/cases'),
    getCase: (caseId) => request(`/api/cases/${encodeURIComponent(caseId)}`),
    createCase: (payload) => request('/api/cases', {
      method: 'POST',
      body: JSON.stringify(payload)
    }),
    addIndicator: (caseId, payload) => request(`/api/cases/${encodeURIComponent(caseId)}/indicators`, {
      method: 'POST',
      body: JSON.stringify(payload)
    }),
    runInvestigation: (caseId) => request(`/api/cases/${encodeURIComponent(caseId)}/investigate`, {
      method: 'POST'
    }),
    getInvestigation: (caseId) => request(`/api/cases/${encodeURIComponent(caseId)}/investigation`),
    updateReview: (caseId, payload) => request(`/api/cases/${encodeURIComponent(caseId)}/review`, {
      method: 'PATCH',
      body: JSON.stringify(payload)
    }),
    runStressTest: (caseId, evidenceId) => request(`/api/cases/${encodeURIComponent(caseId)}/stress-test`, {
      method: 'POST',
      body: JSON.stringify({ evidenceId })
    }),
    resetStressTest: (caseId) => request(`/api/cases/${encodeURIComponent(caseId)}/stress-test/reset`, {
      method: 'POST'
    }),
    getReport: (caseId) => request(`/api/cases/${encodeURIComponent(caseId)}/report`),
    exportPdf: (caseId) => blob(`/api/cases/${encodeURIComponent(caseId)}/export/pdf`),
    exportCsv: (caseId) => blob(`/api/cases/${encodeURIComponent(caseId)}/export/csv`),
    exportJson: (caseId) => blob(`/api/cases/${encodeURIComponent(caseId)}/export/json`)
  };
})();
