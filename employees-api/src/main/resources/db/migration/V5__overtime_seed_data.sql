-- Semilla de horas extra (Frente 3): la semana ISO 2026-W42 del empleado CC/10000001
-- pasa de 40 a 46 horas con un turno sabatino de 06:00 a 12:00.
-- Con el limite ordinario vigente de 42 h (Ley 2101 de 2021 desde 2026-07-15),
-- la semana reporta 4 h de horas extra (dentro del tope semanal de 12 h),
-- visible en GET .../work-shifts bajo weeklySummaries.overtimeHours.
INSERT INTO work_shifts (
    id, employee_document_type, employee_document_number, date, start_time, end_time
)
SELECT
    '750e8400-e29b-41d4-a716-446655440013',
    'CC',
    '10000001',
    DATE '2026-10-17',
    TIME '06:00',
    TIME '12:00'
WHERE EXISTS (
    SELECT 1 FROM employees WHERE document_type = 'CC' AND document_number = '10000001'
);
