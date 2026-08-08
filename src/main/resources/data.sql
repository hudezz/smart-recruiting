-- Seed data for local/dev runs, loaded into the fresh in-memory H2 DB on every
-- startup (see spring.sql.init.mode in application.properties). Explicit IDs
-- keep the sample data deterministic across restarts, which is why the two
-- ALTER TABLE statements below restart each identity sequence past the last
-- seeded ID — otherwise H2 wouldn't know rows 1-16 are "taken" and the first
-- applicant created through the app (POST /applicants) would collide with one.
INSERT INTO job_listing (id, job_name, job_description, minimum_work_experience, required_certification) VALUES
  (1, 'Senior Backend Engineer', 'Own service architecture for the platform team: API design, data modeling, and production reliability.', 4, true),
  (2, 'Product Designer', 'Shape the end-to-end product experience, from discovery through polished UI.', 3, false),
  (3, 'Data Analyst', 'Turn raw product and business data into decisions the team can act on.', 2, true);

INSERT INTO applicant (id, first_name, last_name, email, phone_number, years_of_experience, has_certification, job_listing_id, status) VALUES
  (1, 'Elena', 'Kowalski', 'e.kowalski@mail.com', '555-0101', 6, true, 1, 'APPROVED'),
  (2, 'Marcus', 'Chen', 'm.chen@mail.com', '555-0102', 4, true, 1, 'APPROVED'),
  (3, 'Fatima', 'Al-Rashid', 'f.alrashid@mail.com', '555-0103', 8, true, 1, 'PENDING'),
  (4, 'Priya', 'Nair', 'p.nair@mail.com', '555-0104', 3, true, 1, 'PENDING'),
  (5, 'Tomas', 'Herrera', 't.herrera@mail.com', '555-0105', 5, false, 1, 'PENDING'),
  (6, 'Dmitri', 'Volkov', 'd.volkov@mail.com', '555-0106', 7, false, 1, 'PENDING'),
  (7, 'Ingrid', 'Solberg', 'i.solberg@mail.com', '555-0107', 2, true, 1, 'PENDING'),
  (8, 'Samuel', 'Osei', 's.osei@mail.com', '555-0108', 4, false, 1, 'REJECTED'),
  (9, 'Aiko', 'Tanaka', 'a.tanaka@mail.com', '555-0109', 1, true, 1, 'PENDING'),
  (10, 'Noah', 'Bergstrom', 'n.bergstrom@mail.com', '555-0110', 0, false, 1, 'REJECTED'),
  (11, 'Julia', 'Novak', 'j.novak@mail.com', '555-0111', 5, false, 2, 'APPROVED'),
  (12, 'Ben', 'Okafor', 'b.okafor@mail.com', '555-0112', 3, false, 2, 'PENDING'),
  (13, 'Chiara', 'Rossi', 'c.rossi@mail.com', '555-0113', 2, false, 2, 'PENDING'),
  (14, 'Leon', 'Fischer', 'l.fischer@mail.com', '555-0114', 6, true, 2, 'APPROVED'),
  (15, 'Hana', 'Kobayashi', 'h.kobayashi@mail.com', '555-0115', 3, true, 3, 'APPROVED'),
  (16, 'Victor', 'Marin', 'v.marin@mail.com', '555-0116', 2, true, 3, 'PENDING');

-- Resume each identity sequence right after the last explicit ID inserted above.
ALTER TABLE job_listing ALTER COLUMN id RESTART WITH 4;
ALTER TABLE applicant ALTER COLUMN id RESTART WITH 17;
