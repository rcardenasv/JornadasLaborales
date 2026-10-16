-- Seed employees
INSERT INTO employees (id, document_number, first_name, last_name, email, position, hire_date) VALUES
    ('550e8400-e29b-41d4-a716-446655440001', 'EMP001', 'John', 'Doe', 'john.doe@example.com', 'Software Engineer', '2024-01-15'),
    ('550e8400-e29b-41d4-a716-446655440002', 'EMP002', 'Jane', 'Smith', 'jane.smith@example.com', 'Product Manager', '2023-06-01'),
    ('550e8400-e29b-41d4-a716-446655440003', 'EMP003', 'Bob', 'Johnson', 'bob.johnson@example.com', 'QA Engineer', '2024-03-20');

-- Seed work shifts
INSERT INTO work_shifts (id, employee_id, date, start_time, end_time) VALUES
    ('650e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440001', '2024-01-15', '09:00:00', '17:00:00'),
    ('650e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440001', '2024-01-16', '09:00:00', '17:00:00'),
    ('650e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440002', '2024-01-15', '08:00:00', '16:00:00'),
    ('650e8400-e29b-41d4-a716-446655440004', '550e8400-e29b-41d4-a716-446655440003', '2024-01-16', '10:00:00', '18:00:00'),
    ('650e8400-e29b-41d4-a716-446655440005', '550e8400-e29b-41d4-a716-446655440001', '2024-01-17', '08:30:00', '16:30:00');