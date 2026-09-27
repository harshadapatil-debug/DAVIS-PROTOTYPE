USE davis_db;

-- =========================================================
-- DAVIS GOLDEN DEMO SEED
-- CONTROLLED / SYNTHETIC DATA ONLY
-- =========================================================


-- =========================================================
-- 0. CLEAR EXISTING DATA
-- =========================================================

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE reviews;
TRUNCATE TABLE confidence_scores;
TRUNCATE TABLE evidence;
TRUNCATE TABLE relationships;
TRUNCATE TABLE entities;
TRUNCATE TABLE indicators;
TRUNCATE TABLE cases;

SET FOREIGN_KEY_CHECKS = 1;


-- =========================================================
-- 1. CASE
-- =========================================================

INSERT INTO cases
(
    case_id,
    case_name,
    case_type,
    category,
    description,
    status,
    created_at,
    last_scan_at
)
VALUES
(
    1,
    'Operation Monsoon',
    'DARK_WEB_INTELLIGENCE',
    'THREAT_ACTOR_ATTRIBUTION',
    'Controlled synthetic investigation demonstrating cross-marketplace, infrastructure, wallet, PGP, email and persona correlation.',
    'PROCESSED',
    '2026-09-20 10:00:00',
    '2026-09-20 10:15:00'
);


-- =========================================================
-- 2. KNOWN INDICATOR
-- =========================================================

INSERT INTO indicators
(
    indicator_id,
    case_id,
    indicator_type,
    indicator_value,
    description
)
VALUES
(
    1,
    1,
    'USERNAME',
    'r4v3n_mh',
    'Known starting username supplied to DAVIS.'
);


-- =========================================================
-- 3. ENTITIES
-- =========================================================

INSERT INTO entities
(
    entity_id,
    case_id,
    entity_type,
    entity_value,
    description,
    discovery_confidence
)
VALUES
(1, 1, 'USERNAME', 'r4v3n_mh',
 'Known username supplied as the initial investigation indicator.', 1.0000),

(2, 1, 'RELATED_HANDLE', 'raven_alt',
 'Related handle discovered in controlled marketplace intelligence.', 0.8500),

(3, 1, 'EMAIL', 'raven.mh@example.test',
 'Synthetic email address associated with the investigated account.', 0.9200),

(4, 1, 'PGP_KEY', '0xA1B2C3D4E5F6',
 'Synthetic PGP key fingerprint observed in controlled intelligence.', 0.9500),

(5, 1, 'WALLET', 'bc1qraven2026syntheticwallet',
 'Synthetic wallet identifier associated with observed activity.', 0.8800),

(6, 1, 'MARKETPLACE', 'DarkBazaar',
 'Synthetic marketplace used in the controlled investigation dataset.', 0.9000),

(7, 1, 'ONION_SERVICE', 'r4v3nmarketexample.onion',
 'Synthetic onion-service identifier used for infrastructure correlation.', 0.8200),

(8, 1, 'DOMAIN', 'raven-services.example',
 'Synthetic clearnet domain used for infrastructure correlation.', 0.8100),

(9, 1, 'SSL_CERTIFICATE', 'SHA256-SIMULATED-CERT-RAVEN-2026',
 'Synthetic SSL certificate fingerprint.', 0.9000),

(10, 1, 'BEHAVIOR_PATTERN', 'NIGHT_ACTIVITY_01_00_03_30',
 'Synthetic activity pattern showing repeated activity in the same time window.', 0.7600),

(11, 1, 'WRITING_SIGNATURE', 'RAVEN_STYLE_SIGNATURE_V1',
 'Synthetic writing-style representation used for similarity analysis.', 0.7800);


-- =========================================================
-- 4. RELATIONSHIPS
-- =========================================================

INSERT INTO relationships
(
    relationship_id,
    case_id,
    source_entity_id,
    target_entity_id,
    relationship_type,
    description,
    assessment
)
VALUES
(1, 1, 1, 3,
 'USES_EMAIL',
 'The investigated username is associated with the observed synthetic email address.',
 'OBSERVED'),

(2, 1, 1, 4,
 'USES_PGP_KEY',
 'The investigated username is associated with the observed synthetic PGP key.',
 'OBSERVED'),

(3, 1, 1, 5,
 'LINKED_WALLET',
 'Controlled intelligence links the investigated username to the observed synthetic wallet.',
 'INFERRED'),

(4, 1, 1, 6,
 'ACTIVE_ON',
 'The investigated username was observed active on the synthetic marketplace.',
 'OBSERVED'),

(5, 1, 2, 6,
 'ACTIVE_ON',
 'The related handle was observed on the same synthetic marketplace.',
 'OBSERVED'),

(6, 1, 1, 2,
 'ALIAS_OF',
 'Multiple controlled signals suggest that raven_alt may be related to r4v3n_mh.',
 'INFERRED'),

(7, 1, 7, 8,
 'LINKED_TO_CLEARNET',
 'Controlled infrastructure records associate the onion service with the synthetic clearnet domain.',
 'INFERRED'),

(8, 1, 8, 9,
 'CERTIFICATE_MATCH',
 'The synthetic clearnet domain is associated with the observed synthetic certificate.',
 'OBSERVED'),

(9, 1, 2, 11,
 'SIMILAR_WRITING',
 'Controlled text samples from the related handle show similarity to the known persona writing profile.',
 'INFERRED'),

(10, 1, 2, 10,
 'SIMILAR_BEHAVIOR',
 'The related handle shows a similar synthetic activity-time pattern.',
 'INFERRED'),

(11, 1, 5, 6,
 'TRANSACTED_WITH',
 'Controlled transaction records associate the wallet with marketplace activity.',
 'OBSERVED');


-- =========================================================
-- 5. EVIDENCE
-- =========================================================

INSERT INTO evidence
(
    evidence_id,
    relationship_id,
    evidence_type,
    source,
    description,
    strength,
    reliability,
    direction,
    independence_group,
    observed_at
)
VALUES
(1, 1,
 'EMAIL_MATCH',
 'Synthetic Intelligence Record S-01',
 'The same synthetic email address was observed in account records associated with the investigated handle.',
 'HIGH', 'HIGH', 'SUPPORTS', 'IDENTITY_CONTACT',
 '2026-09-18 11:30:00'),

(2, 2,
 'PGP_KEY_MATCH',
 'Synthetic Intelligence Record S-02',
 'The same synthetic PGP key fingerprint was observed with the investigated account.',
 'HIGH', 'HIGH', 'SUPPORTS', 'CRYPTOGRAPHIC',
 '2026-09-18 12:10:00'),

(3, 3,
 'WALLET_MATCH',
 'Synthetic Intelligence Record S-03',
 'The synthetic wallet was repeatedly associated with the investigated username in the controlled dataset.',
 'HIGH', 'HIGH', 'SUPPORTS', 'FINANCIAL',
 '2026-09-18 13:00:00'),

(4, 4,
 'MARKETPLACE_OVERLAP',
 'Synthetic Intelligence Record S-04',
 'The investigated username was observed active on DarkBazaar.',
 'MEDIUM', 'HIGH', 'SUPPORTS', 'MARKETPLACE_ACTIVITY',
 '2026-09-18 14:00:00'),

(5, 6,
 'HANDLE_SIMILARITY',
 'Synthetic Intelligence Record S-05',
 'The related handle raven_alt shares a distinctive naming pattern with r4v3n_mh.',
 'LOW', 'MEDIUM', 'SUPPORTS', 'HANDLE',
 '2026-09-18 14:30:00'),

(6, 7,
 'CLEARNET_INFRASTRUCTURE_MATCH',
 'Synthetic Intelligence Record S-06',
 'Controlled infrastructure records associate the onion service with the synthetic clearnet domain.',
 'HIGH', 'HIGH', 'SUPPORTS', 'INFRASTRUCTURE',
 '2026-09-19 09:00:00'),

(7, 8,
 'SSL_CERTIFICATE_MATCH',
 'Synthetic Intelligence Record S-07',
 'The same synthetic certificate fingerprint appears in the controlled infrastructure records.',
 'HIGH', 'HIGH', 'SUPPORTS', 'INFRASTRUCTURE',
 '2026-09-19 09:15:00'),

(8, 9,
 'STYLOMETRIC_SIMILARITY',
 'Synthetic Intelligence Record S-08',
 'Controlled text samples show stylistic similarity between the known and related personas.',
 'MEDIUM', 'MEDIUM', 'SUPPORTS', 'BEHAVIORAL',
 '2026-09-19 10:00:00'),

(9, 10,
 'BEHAVIOURAL_SIMILARITY',
 'Synthetic Intelligence Record S-09',
 'The two controlled accounts show a similar activity window during repeated observations.',
 'MEDIUM', 'MEDIUM', 'SUPPORTS', 'BEHAVIORAL',
 '2026-09-19 10:30:00'),

(10, 11,
 'TRANSACTION_LINK',
 'Synthetic Intelligence Record S-10',
 'Synthetic transaction records associate the wallet with marketplace activity.',
 'MEDIUM', 'HIGH', 'SUPPORTS', 'FINANCIAL',
 '2026-09-19 11:00:00'),

(11, 6,
 'CONTRADICTING_SIGNAL',
 'Synthetic Intelligence Record S-11',
 'One controlled observation shows a temporary activity-window mismatch for the related handle.',
 'LOW', 'MEDIUM', 'CONTRADICTS', 'BEHAVIORAL',
 '2026-09-19 11:30:00'),

(12, 6,
 'SOURCE_CORROBORATION',
 'Synthetic Intelligence Record S-12',
 'A second controlled intelligence record references raven_alt in connection with the same marketplace cluster.',
 'MEDIUM', 'HIGH', 'SUPPORTS', 'MARKETPLACE_CORROBORATION',
 '2026-09-19 12:00:00');


-- =========================================================
-- 6. INITIAL ANALYST REVIEW
-- =========================================================

INSERT INTO reviews
(
    finding_id,
    case_id,
    status,
    note,
    updated_at
)
VALUES
(
    1,
    1,
    'PENDING',
    'Synthetic golden case loaded. Inferred relationships require investigator review.',
    '2026-09-20 10:15:00'
);


-- =========================================================
-- 7. VERIFICATION
-- =========================================================

SELECT 'GOLDEN CASE CREATED' AS message;

SELECT
    case_id,
    case_name,
    status
FROM cases
WHERE case_id = 1;

SELECT
    indicator_id,
    indicator_type,
    indicator_value
FROM indicators
WHERE case_id = 1;

SELECT
    COUNT(*) AS entity_count
FROM entities
WHERE case_id = 1;

SELECT
    COUNT(*) AS relationship_count
FROM relationships
WHERE case_id = 1;

SELECT
    COUNT(*) AS evidence_count
FROM evidence e
INNER JOIN relationships r
    ON e.relationship_id = r.relationship_id
WHERE r.case_id = 1;

SELECT
    COUNT(*) AS confidence_records
FROM confidence_scores
WHERE case_id = 1;