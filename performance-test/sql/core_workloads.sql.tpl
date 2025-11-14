-- Auto-generated via envsubst in performance-test/scripts/run-sql-benchmarks.sh
SET @member_id := ${MEMBER_ID};
SET @start_date := DATE '${START_DATE}';
SET @end_date := DATE '${END_DATE}';
SET @food_prefix := '${FOOD_PREFIX}';
SET @budget_month := '${BUDGET_MONTH}';
SET @daily_date := DATE '${DAILY_DATE}';

-- 1) Food autocomplete plan / runtime
EXPLAIN ANALYZE
SELECT food_id, food_name, price
FROM food
WHERE deleted_at IS NULL
  AND food_name LIKE CONCAT(@food_prefix, '%')
ORDER BY is_main DESC, registered_dt DESC
LIMIT 20;

-- 2) Expenditure list window
EXPLAIN ANALYZE
SELECT e.expenditure_id,
       e.expended_date,
       e.expended_time,
       e.meal_type,
       e.amount,
       e.discount
FROM expenditure e
WHERE e.member_id = @member_id
  AND e.expended_date BETWEEN @start_date AND @end_date
  AND e.deleted = FALSE
ORDER BY e.expended_date DESC, e.expended_time DESC
LIMIT 200;

-- 3) Expenditure category aggregation
EXPLAIN ANALYZE
SELECT e.category_id,
       SUM(e.amount) AS total_amount
FROM expenditure e
WHERE e.member_id = @member_id
  AND e.expended_date BETWEEN @start_date AND @end_date
  AND e.deleted = FALSE
GROUP BY e.category_id
ORDER BY total_amount DESC;

-- 4) Daily budget + meal breakdown
EXPLAIN ANALYZE
SELECT d.budget_date,
       d.daily_food_budget,
       d.daily_used_amount,
       m.meal_type,
       m.meal_budget,
       m.used_amount
FROM daily_budget d
LEFT JOIN meal_budget m ON m.daily_budget_id = d.budget_id
WHERE d.member_id = @member_id
  AND d.budget_date = @daily_date
ORDER BY m.meal_type;

-- 5) Monthly budget snapshot
EXPLAIN ANALYZE
SELECT monthly_food_budget,
       monthly_used_amount
FROM monthly_budget
WHERE member_id = @member_id
  AND budget_month = @budget_month;
