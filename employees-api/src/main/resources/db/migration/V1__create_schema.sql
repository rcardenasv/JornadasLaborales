-- Create employees table
CREATE TABLE employees (
    id UUID PRIMARY KEY,
    document_number VARCHAR(20) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    position VARCHAR(100) NOT NULL,
    hire_date DATE NOT NULL
);

-- Create work_shifts table
CREATE TABLE work_shifts (
    id UUID PRIMARY KEY,
    employee_id UUID NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    CONSTRAINT chk_work_shift_time CHECK (end_time > start_time),
    CONSTRAINT chk_work_shift_date_unique UNIQUE (employee_id, date, start_time)
);

-- Create indexes for performance
CREATE INDEX idx_work_shifts_employee_id ON work_shifts(employee_id);
CREATE INDEX idx_work_shifts_date ON work_shifts(date);