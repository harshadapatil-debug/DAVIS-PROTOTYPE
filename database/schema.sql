CREATE DATABASE IF NOT EXISTS davis_db;

USE davis_db;


-- =========================================================
-- 1. CASES
-- =========================================================

CREATE TABLE cases (
    case_id BIGINT PRIMARY KEY AUTO_INCREMENT,

    case_name VARCHAR(150) NOT NULL,

    case_type VARCHAR(50) NOT NULL
        DEFAULT 'DARK_WEB_INTELLIGENCE',

    category VARCHAR(100) NOT NULL
        DEFAULT 'THREAT_ACTOR_ATTRIBUTION',

    description TEXT,

    status VARCHAR(30) NOT NULL
        DEFAULT 'OPEN',

    created_at DATETIME NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    last_scan_at DATETIME NULL,

    CONSTRAINT chk_case_status
        CHECK (status IN (
            'OPEN',
            'PROCESSED',
            'UNDER_REVIEW',
            'CLOSED'
        ))
);


-- =========================================================
-- 2. INDICATORS
-- =========================================================

CREATE TABLE indicators (
    indicator_id BIGINT PRIMARY KEY AUTO_INCREMENT,

    case_id BIGINT NOT NULL,

    indicator_type VARCHAR(50) NOT NULL,

    indicator_value VARCHAR(500) NOT NULL,

    description TEXT,

    CONSTRAINT fk_indicator_case
        FOREIGN KEY (case_id)
        REFERENCES cases(case_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);


-- =========================================================
-- 3. ENTITIES
-- =========================================================

CREATE TABLE entities (
    entity_id BIGINT PRIMARY KEY AUTO_INCREMENT,

    case_id BIGINT NOT NULL,

    entity_type VARCHAR(50) NOT NULL,

    entity_value VARCHAR(500) NOT NULL,

    description TEXT,

    discovery_confidence DECIMAL(5,4) NOT NULL
        DEFAULT 0.0000,

    CONSTRAINT chk_discovery_confidence
        CHECK (
            discovery_confidence >= 0.0000
            AND discovery_confidence <= 1.0000
        ),

    CONSTRAINT fk_entity_case
        FOREIGN KEY (case_id)
        REFERENCES cases(case_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT uq_case_entity
        UNIQUE (case_id, entity_type, entity_value)
);


-- =========================================================
-- 4. RELATIONSHIPS
-- =========================================================

CREATE TABLE relationships (
    relationship_id BIGINT PRIMARY KEY AUTO_INCREMENT,

    case_id BIGINT NOT NULL,

    source_entity_id BIGINT NOT NULL,

    target_entity_id BIGINT NOT NULL,

    relationship_type VARCHAR(80) NOT NULL,

    description TEXT,

    assessment VARCHAR(20) NOT NULL
        DEFAULT 'INFERRED',

    CONSTRAINT fk_relationship_case
        FOREIGN KEY (case_id)
        REFERENCES cases(case_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_relationship_source
        FOREIGN KEY (source_entity_id)
        REFERENCES entities(entity_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_relationship_target
        FOREIGN KEY (target_entity_id)
        REFERENCES entities(entity_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT chk_relationship_assessment
        CHECK (
            assessment IN (
                'OBSERVED',
                'INFERRED'
            )
        ),

    CONSTRAINT chk_relationship_entities
        CHECK (
            source_entity_id <> target_entity_id
        ),

    CONSTRAINT uq_relationship
        UNIQUE (
            case_id,
            source_entity_id,
            target_entity_id,
            relationship_type
        )
);


-- =========================================================
-- 5. EVIDENCE
-- =========================================================

CREATE TABLE evidence (
    evidence_id BIGINT PRIMARY KEY AUTO_INCREMENT,

    relationship_id BIGINT NOT NULL,

    evidence_type VARCHAR(80) NOT NULL,

    source VARCHAR(255) NOT NULL,

    description TEXT NOT NULL,

    strength VARCHAR(20) NOT NULL,

    reliability VARCHAR(20) NOT NULL,

    direction VARCHAR(20) NOT NULL,

    independence_group VARCHAR(100) NOT NULL,

    observed_at DATETIME NOT NULL,

    CONSTRAINT fk_evidence_relationship
        FOREIGN KEY (relationship_id)
        REFERENCES relationships(relationship_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT chk_evidence_strength
        CHECK (
            strength IN (
                'LOW',
                'MEDIUM',
                'HIGH'
            )
        ),

    CONSTRAINT chk_evidence_reliability
        CHECK (
            reliability IN (
                'LOW',
                'MEDIUM',
                'HIGH'
            )
        ),

    CONSTRAINT chk_evidence_direction
        CHECK (
            direction IN (
                'SUPPORTS',
                'WEAKENS',
                'CONTRADICTS',
                'NEUTRAL'
            )
        )
);


-- =========================================================
-- 6. CONFIDENCE SCORES
-- =========================================================

CREATE TABLE confidence_scores (
    confidence_id BIGINT PRIMARY KEY AUTO_INCREMENT,

    case_id BIGINT NOT NULL,

    score DECIMAL(5,2) NOT NULL,

    risk_level VARCHAR(20) NOT NULL,

    explanation TEXT NOT NULL,

    factors_json TEXT,

    calculated_at DATETIME NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_confidence_case
        FOREIGN KEY (case_id)
        REFERENCES cases(case_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT chk_confidence_score
        CHECK (
            score >= 0.00
            AND score <= 100.00
        ),

    CONSTRAINT chk_confidence_risk
        CHECK (
            risk_level IN (
                'LOW',
                'MEDIUM',
                'HIGH'
            )
        ),

    CONSTRAINT uq_case_confidence
        UNIQUE (case_id)
);


-- =========================================================
-- 7. REVIEWS
-- =========================================================

CREATE TABLE reviews (
    finding_id BIGINT PRIMARY KEY AUTO_INCREMENT,

    case_id BIGINT NOT NULL,

    status VARCHAR(20) NOT NULL
        DEFAULT 'PENDING',

    note TEXT,

    updated_at DATETIME NOT NULL
        DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_review_case
        FOREIGN KEY (case_id)
        REFERENCES cases(case_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT chk_review_status
        CHECK (
            status IN (
                'PENDING',
                'ACCEPTED',
                'FLAGGED',
                'REJECTED'
            )
        ),

    CONSTRAINT uq_case_review
        UNIQUE (case_id)
);


-- =========================================================
-- INDEXES
-- =========================================================

CREATE INDEX idx_indicators_case
    ON indicators(case_id);

CREATE INDEX idx_entities_case
    ON entities(case_id);

CREATE INDEX idx_relationships_case
    ON relationships(case_id);

CREATE INDEX idx_relationships_source
    ON relationships(source_entity_id);

CREATE INDEX idx_relationships_target
    ON relationships(target_entity_id);

CREATE INDEX idx_evidence_relationship
    ON evidence(relationship_id);

CREATE INDEX idx_evidence_observed_at
    ON evidence(observed_at);

CREATE INDEX idx_confidence_case
    ON confidence_scores(case_id);

CREATE INDEX idx_reviews_case
    ON reviews(case_id);