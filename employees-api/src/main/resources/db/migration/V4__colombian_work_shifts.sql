ALTER TABLE work_shifts ADD CONSTRAINT chk_work_shift_time
    CHECK (end_time <> start_time);

INSERT INTO work_shifts (
    id, employee_document_type, employee_document_number, date, start_time, end_time
)
SELECT
    '750e8400-e29b-41d4-a716-446655440001',
    'CC',
    '10000001',
    DATE '2026-10-12',
    TIME '08:00',
    TIME '12:00'
WHERE EXISTS (
    SELECT 1 FROM employees WHERE document_type = 'CC' AND document_number = '10000001'
);

INSERT INTO work_shifts (
    id, employee_document_type, employee_document_number, date, start_time, end_time
)
SELECT
    '750e8400-e29b-41d4-a716-446655440002',
    'CC',
    '10000001',
    DATE '2026-10-12',
    TIME '13:00',
    TIME '17:00'
WHERE EXISTS (
    SELECT 1 FROM employees WHERE document_type = 'CC' AND document_number = '10000001'
);

INSERT INTO work_shifts (
    id, employee_document_type, employee_document_number, date, start_time, end_time
)
SELECT
    '750e8400-e29b-41d4-a716-446655440003',
    'CC',
    '10000002',
    DATE '2026-10-13',
    TIME '22:00',
    TIME '06:00'
WHERE EXISTS (
    SELECT 1 FROM employees WHERE document_type = 'CC' AND document_number = '10000002'
);

INSERT INTO work_shifts (
    id, employee_document_type, employee_document_number, date, start_time, end_time
)
SELECT
    '750e8400-e29b-41d4-a716-446655440004',
    'CC',
    '10000003',
    DATE '2026-12-08',
    TIME '08:00',
    TIME '16:00'
WHERE EXISTS (
    SELECT 1 FROM employees WHERE document_type = 'CC' AND document_number = '10000003'
);

INSERT INTO work_shifts (
    id, employee_document_type, employee_document_number, date, start_time, end_time
)
SELECT
    '750e8400-e29b-41d4-a716-446655440005',
    'CC',
    '10000001',
    DATE '2026-10-13',
    TIME '08:00',
    TIME '12:00'
WHERE EXISTS (
    SELECT 1 FROM employees WHERE document_type = 'CC' AND document_number = '10000001'
);

INSERT INTO work_shifts (
    id, employee_document_type, employee_document_number, date, start_time, end_time
)
SELECT
    '750e8400-e29b-41d4-a716-446655440006',
    'CC',
    '10000001',
    DATE '2026-10-13',
    TIME '13:00',
    TIME '17:00'
WHERE EXISTS (
    SELECT 1 FROM employees WHERE document_type = 'CC' AND document_number = '10000001'
);

INSERT INTO work_shifts (
    id, employee_document_type, employee_document_number, date, start_time, end_time
)
SELECT
    '750e8400-e29b-41d4-a716-446655440007',
    'CC',
    '10000001',
    DATE '2026-10-14',
    TIME '08:00',
    TIME '12:00'
WHERE EXISTS (
    SELECT 1 FROM employees WHERE document_type = 'CC' AND document_number = '10000001'
);

INSERT INTO work_shifts (
    id, employee_document_type, employee_document_number, date, start_time, end_time
)
SELECT
    '750e8400-e29b-41d4-a716-446655440008',
    'CC',
    '10000001',
    DATE '2026-10-14',
    TIME '13:00',
    TIME '17:00'
WHERE EXISTS (
    SELECT 1 FROM employees WHERE document_type = 'CC' AND document_number = '10000001'
);

INSERT INTO work_shifts (
    id, employee_document_type, employee_document_number, date, start_time, end_time
)
SELECT
    '750e8400-e29b-41d4-a716-446655440009',
    'CC',
    '10000001',
    DATE '2026-10-15',
    TIME '08:00',
    TIME '12:00'
WHERE EXISTS (
    SELECT 1 FROM employees WHERE document_type = 'CC' AND document_number = '10000001'
);

INSERT INTO work_shifts (
    id, employee_document_type, employee_document_number, date, start_time, end_time
)
SELECT
    '750e8400-e29b-41d4-a716-446655440010',
    'CC',
    '10000001',
    DATE '2026-10-15',
    TIME '13:00',
    TIME '17:00'
WHERE EXISTS (
    SELECT 1 FROM employees WHERE document_type = 'CC' AND document_number = '10000001'
);

INSERT INTO work_shifts (
    id, employee_document_type, employee_document_number, date, start_time, end_time
)
SELECT
    '750e8400-e29b-41d4-a716-446655440011',
    'CC',
    '10000001',
    DATE '2026-10-16',
    TIME '08:00',
    TIME '12:00'
WHERE EXISTS (
    SELECT 1 FROM employees WHERE document_type = 'CC' AND document_number = '10000001'
);

INSERT INTO work_shifts (
    id, employee_document_type, employee_document_number, date, start_time, end_time
)
SELECT
    '750e8400-e29b-41d4-a716-446655440012',
    'CC',
    '10000001',
    DATE '2026-10-16',
    TIME '13:00',
    TIME '17:00'
WHERE EXISTS (
    SELECT 1 FROM employees WHERE document_type = 'CC' AND document_number = '10000001'
);
