-- Seed 20 users (IDs 1000-1019)
WITH RECURSIVE seq AS (
    SELECT 0 AS n
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 19
)
INSERT INTO users (id, email, password, display_name, role, provider, provider_id, status, created_at, updated_at)
SELECT
    1000 + n,
    CONCAT('seed', LPAD(n + 1, 2, '0'), '@groo.local'),
    '$2a$10$T0JfM1g4vkuOqvQ/g1S.ZuONm3aMc.Q.O1X8/5duIQWkYMU6kCe3K', -- bcrypt hash for SeedUser123!
    CONCAT('Seed User ', n + 1),
    CASE WHEN n % 10 = 0 THEN 'ADMIN' ELSE 'USER' END,
    'LOCAL',
    CONCAT('seed-', n + 1),
    'ACTIVE',
    DATE_SUB(NOW(), INTERVAL (n + 1) DAY),
    DATE_SUB(NOW(), INTERVAL n DAY)
FROM seq
ON DUPLICATE KEY UPDATE
    display_name = VALUES(display_name),
    updated_at = VALUES(updated_at);

-- Seed 12 workspace groups (IDs 2000-2011)
WITH RECURSIVE seq AS (
    SELECT 0 AS n
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 11
)
INSERT INTO workspace_groups (id, owner_id, name, description, status, invitation_code, created_at, updated_at)
SELECT
    2000 + n,
    1000 + (n % 20),
    CONCAT('Lab Workspace ', n + 1),
    CONCAT('Auto generated workspace #', n + 1),
    CASE WHEN n % 5 = 0 THEN 'ARCHIVED' ELSE 'ACTIVE' END,
    CONCAT('INV', LPAD(HEX(2000 + n), 13, 'A')),
    DATE_SUB(NOW(), INTERVAL (n + 5) DAY),
    DATE_SUB(NOW(), INTERVAL (n + 2) DAY)
FROM seq
ON DUPLICATE KEY UPDATE
    description = VALUES(description),
    updated_at = VALUES(updated_at);

-- Seed 60 group memberships (IDs 2100+)
WITH RECURSIVE group_seq AS (
    SELECT 0 AS g
    UNION ALL
    SELECT g + 1 FROM group_seq WHERE g < 11
),
member_seq AS (
    SELECT 0 AS m
    UNION ALL
    SELECT m + 1 FROM member_seq WHERE m < 4
)
INSERT INTO group_memberships (id, group_id, user_id, role, joined_at)
SELECT
    2100 + (group_seq.g * 5 + member_seq.m),
    2000 + group_seq.g,
    1000 + ((group_seq.g * 5 + member_seq.m) % 20),
    CASE
        WHEN member_seq.m = 0 THEN 'OWNER'
        WHEN member_seq.m = 1 THEN 'ADMIN'
        ELSE 'MEMBER'
    END,
    DATE_SUB(NOW(), INTERVAL (group_seq.g + member_seq.m) DAY)
FROM group_seq
CROSS JOIN member_seq
ON DUPLICATE KEY UPDATE role = VALUES(role);

-- Seed 24 projects (IDs 3000-3023)
WITH RECURSIVE seq AS (
    SELECT 0 AS n
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 23
)
INSERT INTO projects (id, group_id, created_by, name, description, status, created_at, updated_at)
SELECT
    3000 + n,
    2000 + (n % 12),
    1000 + (n % 20),
    CONCAT('Project ', n + 1),
    CONCAT('Seed project description ', n + 1),
    CASE WHEN n % 6 = 0 THEN 'ARCHIVED' ELSE 'ACTIVE' END,
    DATE_SUB(NOW(), INTERVAL (n + 3) DAY),
    DATE_SUB(NOW(), INTERVAL (n + 1) DAY)
FROM seq
ON DUPLICATE KEY UPDATE
    description = VALUES(description),
    status = VALUES(status),
    updated_at = VALUES(updated_at);

-- Seed 80 tasks (IDs 4000-4079)
WITH RECURSIVE seq AS (
    SELECT 0 AS n
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 79
)
INSERT INTO tasks (id, project_id, created_by, title, description, assignee_id, due_date, status, priority, created_at, updated_at)
SELECT
    4000 + n,
    3000 + (n % 24),
    1000 + (n % 20),
    CONCAT('Seed Task ', n + 1),
    CONCAT('Detailed description for seed task ', n + 1),
    CONCAT('user-', ((n % 20) + 1)),
    DATE_ADD(NOW(), INTERVAL (n % 15) DAY),
    CASE
        WHEN n % 3 = 0 THEN 'TODO'
        WHEN n % 3 = 1 THEN 'DOING'
        ELSE 'DONE'
    END,
    CASE
        WHEN n % 4 = 0 THEN 'HIGH'
        WHEN n % 4 = 1 THEN 'MEDIUM'
        ELSE 'LOW'
    END,
    DATE_SUB(NOW(), INTERVAL (n + 2) DAY),
    DATE_SUB(NOW(), INTERVAL n DAY)
FROM seq
ON DUPLICATE KEY UPDATE
    description = VALUES(description),
    status = VALUES(status),
    priority = VALUES(priority),
    updated_at = VALUES(updated_at);

-- Seed 80 subtasks (IDs 5000-5079)
WITH RECURSIVE seq AS (
    SELECT 0 AS n
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 79
)
INSERT INTO task_subtasks (id, task_id, title, done, created_at)
SELECT
    5000 + n,
    4000 + (n % 80),
    CONCAT('Subtask ', n + 1),
    CASE WHEN n % 2 = 0 THEN 1 ELSE 0 END,
    DATE_SUB(NOW(), INTERVAL (n % 30) DAY)
FROM seq
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    done = VALUES(done);

-- Seed 40 comments (IDs 6000-6039)
WITH RECURSIVE seq AS (
    SELECT 0 AS n
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 39
)
INSERT INTO task_comments (id, task_id, author_id, content, created_at)
SELECT
    6000 + n,
    4000 + (n % 80),
    1000 + (n % 20),
    CONCAT('Progress note #', n + 1, ' for task ', (n % 80) + 1),
    DATE_SUB(NOW(), INTERVAL (n % 25) DAY)
FROM seq
ON DUPLICATE KEY UPDATE
    content = VALUES(content),
    created_at = VALUES(created_at);

-- Seed 30 task activities (IDs 7000-7029)
WITH RECURSIVE seq AS (
    SELECT 0 AS n
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 29
)
INSERT INTO task_activity (id, task_id, user_id, message, created_at)
SELECT
    7000 + n,
    4000 + (n % 80),
    1000 + (n % 20),
    CONCAT('Task updated by seed user ', (n % 20) + 1),
    DATE_SUB(NOW(), INTERVAL (n % 18) DAY)
FROM seq
ON DUPLICATE KEY UPDATE
    message = VALUES(message),
    created_at = VALUES(created_at);
