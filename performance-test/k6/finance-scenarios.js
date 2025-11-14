import http from 'k6/http';
import { check } from 'k6';
import { Trend } from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const JWT = __ENV.JWT_TOKEN || '';
const START_DATE = __ENV.START_DATE || '2025-01-01';
const END_DATE = __ENV.END_DATE || '2025-03-31';
const DAILY_DATE = __ENV.DAILY_DATE || '2025-02-15';
const BUDGET_YEAR = __ENV.BUDGET_YEAR || '2025';
const BUDGET_MONTH = __ENV.BUDGET_MONTH || '02';
const KEYWORDS = (__ENV.FOOD_KEYWORDS || 'PERF,BURGER,NOODLE').split(',');
const FOOD_LIMIT = Number(__ENV.AUTOCOMPLETE_LIMIT || 10);

const metrics = {
  food: new Trend('food_autocomplete_duration_ms'),
  expenditureList: new Trend('expenditure_list_duration_ms'),
  expenditureStats: new Trend('expenditure_stats_duration_ms'),
  budgetMonthly: new Trend('budget_monthly_duration_ms'),
  budgetDaily: new Trend('budget_daily_duration_ms'),
};

export const options = {
  scenarios: {
    food_autocomplete: {
      executor: 'constant-arrival-rate',
      rate: Number(__ENV.AUTOCOMPLETE_RPS || 75),
      timeUnit: '1s',
      duration: __ENV.TEST_DURATION || '60s',
      preAllocatedVUs: Number(__ENV.AUTOCOMPLETE_VUS || 20),
      exec: 'foodAutocomplete',
    },
    expenditure_list: {
      executor: 'constant-arrival-rate',
      rate: Number(__ENV.EXPENDITURE_RPS || 40),
      timeUnit: '1s',
      duration: __ENV.TEST_DURATION || '60s',
      preAllocatedVUs: Number(__ENV.EXPENDITURE_VUS || 15),
      exec: 'expenditureList',
    },
    expenditure_stats: {
      executor: 'constant-arrival-rate',
      rate: Number(__ENV.EXP_STATS_RPS || 20),
      timeUnit: '1s',
      duration: __ENV.TEST_DURATION || '60s',
      preAllocatedVUs: Number(__ENV.EXP_STATS_VUS || 10),
      exec: 'expenditureStats',
    },
    budget_monthly: {
      executor: 'constant-arrival-rate',
      rate: Number(__ENV.BUDGET_MONTHLY_RPS || 20),
      timeUnit: '1s',
      duration: __ENV.TEST_DURATION || '60s',
      preAllocatedVUs: Number(__ENV.BUDGET_MONTHLY_VUS || 10),
      exec: 'budgetMonthly',
    },
    budget_daily: {
      executor: 'constant-arrival-rate',
      rate: Number(__ENV.BUDGET_DAILY_RPS || 20),
      timeUnit: '1s',
      duration: __ENV.TEST_DURATION || '60s',
      preAllocatedVUs: Number(__ENV.BUDGET_DAILY_VUS || 10),
      exec: 'budgetDaily',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],
    food_autocomplete_duration_ms: ['p(95)<250'],
    expenditure_list_duration_ms: ['p(95)<400'],
    expenditure_stats_duration_ms: ['p(95)<400'],
    budget_monthly_duration_ms: ['p(95)<350'],
    budget_daily_duration_ms: ['p(95)<350'],
  },
};

const baseHeaders = {
  'Content-Type': 'application/json',
};

function authHeaders() {
  if (!JWT) {
    throw new Error('JWT_TOKEN environment variable is required for secured scenarios.');
  }
  return { ...baseHeaders, Authorization: `Bearer ${JWT}` };
}

export function foodAutocomplete() {
  const keyword = KEYWORDS[Math.floor(Math.random() * KEYWORDS.length)].trim();
  const url = `${BASE_URL}/api/v1/foods/autocomplete?keyword=${encodeURIComponent(keyword)}&limit=${FOOD_LIMIT}`;
  const res = http.get(url, { headers: baseHeaders });
  metrics.food.add(res.timings.duration);
  check(res, {
    'food autocomplete 200': (r) => r.status === 200,
  });
}

export function expenditureList() {
  const url = `${BASE_URL}/api/v1/expenditures?startDate=${START_DATE}&endDate=${END_DATE}&page=0&size=${__ENV.EXP_PAGE_SIZE || 50}`;
  const res = http.get(url, { headers: authHeaders() });
  metrics.expenditureList.add(res.timings.duration);
  check(res, { 'expenditure list 200': (r) => r.status === 200 });
}

export function expenditureStats() {
  const url = `${BASE_URL}/api/v1/expenditures/statistics?startDate=${START_DATE}&endDate=${END_DATE}`;
  const res = http.get(url, { headers: authHeaders() });
  metrics.expenditureStats.add(res.timings.duration);
  check(res, { 'expenditure stats 200': (r) => r.status === 200 });
}

export function budgetMonthly() {
  const url = `${BASE_URL}/api/v1/budgets/monthly?year=${BUDGET_YEAR}&month=${BUDGET_MONTH}`;
  const res = http.get(url, { headers: authHeaders() });
  metrics.budgetMonthly.add(res.timings.duration);
  check(res, { 'budget monthly 200': (r) => r.status === 200 });
}

export function budgetDaily() {
  const url = `${BASE_URL}/api/v1/budgets/daily?date=${DAILY_DATE}`;
  const res = http.get(url, { headers: authHeaders() });
  metrics.budgetDaily.add(res.timings.duration);
  check(res, { 'budget daily 200': (r) => r.status === 200 });
}
