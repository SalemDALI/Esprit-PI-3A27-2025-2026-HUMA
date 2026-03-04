-- Run this SQL on your database. Execute each block; ignore errors if column/table already exists.
-- MySQL / MariaDB:
-- If you get "Duplicate column name" or "Table already exists", skip that statement.

-- 1) Add category and status to feedback
ALTER TABLE feedback ADD COLUMN category VARCHAR(50) DEFAULT 'Autre';
ALTER TABLE feedback ADD COLUMN status VARCHAR(20) DEFAULT 'nouveau';

-- 2) Create feedback_comments table
CREATE TABLE feedback_comments (
  id INT AUTO_INCREMENT PRIMARY KEY,
  feedback_id INT NOT NULL,
  user_id INT NOT NULL,
  contenu TEXT NOT NULL,
  date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_feedback_comments_feedback_id (feedback_id),
  INDEX idx_feedback_comments_user_id (user_id)
);
