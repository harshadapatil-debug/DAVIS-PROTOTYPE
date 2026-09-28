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

  function compactLabel(value, max = 18) {
    const text = String(value ?? '');
    return text.length > max ? text.slice(0, max - 1) + '…' : text;
  }

  function layout(nodes, width, height) {
    const cx = width / 2;
    const cy = height / 2;
    const radius = Math.max(110, Math.min(width, height) * 0.34);
    return nodes.map((node, index) => {
      const angle = (Math.PI * 2 * index) / Math.max(nodes.length, 1) - Math.PI / 2;
      return { ...node, x: cx + radius * Math.cos(angle), y: cy + radius * Math.sin(angle) };
    });
  }

  function render(container, graph, knownIndicator, onEdgeSelect) {
    const nodes = Array.isArray(graph?.nodes) ? graph.nodes : [];
    const edges = Array.isArray(graph?.edges) ? graph.edges : [];

    if (!nodes.length) {
      container.innerHTML = '<div class="empty-state"><strong>No graph data.</strong><span>The backend returned no entities for this investigation.</span></div>';
      return;
    }

    const width = Math.max(640, container.clientWidth || 820);
    const height = 520;
    const positioned = layout(nodes, width, height);
    const byId = new Map(positioned.map(node => [String(node.id ?? node.entityId), node]));
    const knownValue = String(knownIndicator?.indicatorValue ?? '');

    const edgeSvg = edges.map((edge) => {
      const source = byId.get(String(edge.source));
      const target = byId.get(String(edge.target));
      if (!source || !target) return '';
      const assessment = String(edge.assessment || '').toUpperCase();
      const classes = `graph-edge ${assessment === 'OBSERVED' ? 'observed' : assessment === 'INFERRED' ? 'inferred' : ''}`;
      return `<g class="edge-group" data-edge-id="${escapeHtml(edge.id ?? edge.relationshipId)}" tabindex="0" role="button" aria-label="${escapeHtml(edge.relationshipType || 'Relationship')}">
        <line class="${classes}" x1="${source.x}" y1="${source.y}" x2="${target.x}" y2="${target.y}"></line>
        <title>${escapeHtml(edge.relationshipType || '')} · ${escapeHtml(assessment)}</title>
        <text class="edge-label" x="${(source.x + target.x) / 2}" y="${(source.y + target.y) / 2 - 6}" text-anchor="middle">${escapeHtml(compactLabel(edge.relationshipType, 17))}</text>
      </g>`;
    }).join('');

    const nodeSvg = positioned.map((node) => {
      const isKnown = knownValue && String(node.label ?? '') === knownValue;
      const entityId = node.entityId ?? node.id;
      return `<g class="graph-node ${isKnown ? 'known-node' : ''}" data-node-id="${escapeHtml(entityId)}">
        <circle cx="${node.x}" cy="${node.y}" r="28"></circle>
        <circle class="node-ring" cx="${node.x}" cy="${node.y}" r="28"></circle>
        <text class="node-type" x="${node.x}" y="${node.y - 42}" text-anchor="middle">${escapeHtml(compactLabel(node.entityType, 14))}</text>
        <text class="node-label" x="${node.x}" y="${node.y + 5}" text-anchor="middle">${escapeHtml(compactLabel(node.label, 16))}</text>
        <text class="node-id" x="${node.x}" y="${node.y + 18}" text-anchor="middle">#${escapeHtml(entityId)}</text>
        <title>${escapeHtml(node.label)} · ${escapeHtml(node.entityType || '')}</title>
      </g>`;
    }).join('');

    container.innerHTML = `<div class="graph-shell">
      <div class="graph-legend">
        <span><i class="legend-line observed"></i>Observed</span>
        <span><i class="legend-line inferred"></i>Inferred</span>
        <span><i class="legend-node"></i>Entity</span>
        ${knownValue ? '<span><i class="legend-known"></i>Known indicator</span>' : ''}
      </div>
      <div class="graph-canvas" aria-label="Relationship graph">
        <svg viewBox="0 0 ${width} ${height}" role="img" aria-label="Entity relationship graph">
          ${edgeSvg}
          ${nodeSvg}
        </svg>
      </div>
      ${edges.length ? '<div class="graph-hint">Select a relationship line to inspect its description and linked evidence.</div>' : '<div class="graph-hint">No relationship edges returned by the backend.</div>'}
    </div>`;

    container.querySelectorAll('.edge-group').forEach((element) => {
      const activate = () => onEdgeSelect?.(String(element.dataset.edgeId));
      element.addEventListener('click', activate);
      element.addEventListener('keydown', (event) => {
        if (event.key === 'Enter' || event.key === ' ') { event.preventDefault(); activate(); }
      });
    });
  }

  window.DavisGraph = { render };
})();
