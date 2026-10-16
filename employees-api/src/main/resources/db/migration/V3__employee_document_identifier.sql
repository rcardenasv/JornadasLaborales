ALTER TABLE employees ADD COLUMN document_type VARCHAR(2) NOT NULL DEFAULT 'CC';
ALTER TABLE employees ADD CONSTRAINT chk_employees_document_type
    CHECK (document_type IN ('CC', 'CE', 'PA'));
ALTER TABLE employees DROP CONSTRAINT IF EXISTS employees_document_number_key;
ALTER TABLE employees ADD CONSTRAINT uq_employees_document
    UNIQUE (document_type, document_number);

UPDATE employees
SET document_number = CASE email
    WHEN 'john.doe@example.com' THEN '10000001'
    WHEN 'jane.smith@example.com' THEN '10000002'
    WHEN 'bob.johnson@example.com' THEN '10000003'
    ELSE document_number
END
WHERE email IN (
    'john.doe@example.com',
    'jane.smith@example.com',
    'bob.johnson@example.com'
);

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM employees
        WHERE document_number !~ '^[0-9]{3,20}$'
    ) THEN
        RAISE EXCEPTION 'Legacy employee documents require an explicit document type before V3 can be applied';
    END IF;
END $$;

ALTER TABLE work_shifts ADD COLUMN employee_document_type VARCHAR(2);
ALTER TABLE work_shifts ADD COLUMN employee_document_number VARCHAR(20);

UPDATE work_shifts AS shifts
SET employee_document_type = employees.document_type,
    employee_document_number = employees.document_number
FROM employees
WHERE shifts.employee_id = employees.id;

ALTER TABLE work_shifts ALTER COLUMN employee_document_type SET NOT NULL;
ALTER TABLE work_shifts ALTER COLUMN employee_document_number SET NOT NULL;
ALTER TABLE work_shifts DROP CONSTRAINT IF EXISTS work_shifts_employee_id_fkey;
ALTER TABLE work_shifts DROP CONSTRAINT IF EXISTS chk_work_shift_date_unique;
ALTER TABLE work_shifts DROP CONSTRAINT IF EXISTS chk_work_shift_time;
ALTER TABLE work_shifts DROP COLUMN employee_id;
ALTER TABLE work_shifts ADD CONSTRAINT fk_work_shifts_employee_document
    FOREIGN KEY (employee_document_type, employee_document_number)
    REFERENCES employees (document_type, document_number)
    ON DELETE CASCADE;
CREATE INDEX idx_work_shifts_employee_date
    ON work_shifts (employee_document_type, employee_document_number, date);
ALTER TABLE work_shifts ADD CONSTRAINT uq_work_shifts_employee_date_time
    UNIQUE (employee_document_type, employee_document_number, date, start_time);
