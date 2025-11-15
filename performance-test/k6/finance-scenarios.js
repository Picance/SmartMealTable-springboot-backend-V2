import http from 'k6/http';
import { check } from 'k6';
import { Trend } from 'k6/metrics';
import { randomIntBetween } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const JWT = __ENV.JWT_TOKEN || '';
const START_DATE = __ENV.START_DATE || '2025-01-01';
const END_DATE = __ENV.END_DATE || '2025-03-31';
const DAILY_DATE = __ENV.DAILY_DATE || '2025-02-15';
const BUDGET_YEAR = __ENV.BUDGET_YEAR || '2025';
const BUDGET_MONTH = __ENV.BUDGET_MONTH || '02';
const KEYWORDS = (__ENV.FOOD_KEYWORDS || 'PERF,BURGER,NOODLE')
  .split(',')
  .map((value) => value.trim())
  .filter(Boolean);
const FOOD_LIMIT = Number(__ENV.AUTOCOMPLETE_LIMIT || 10);
const TEST_DURATION = __ENV.TEST_DURATION || '60s';

const RECO_LATITUDE = __ENV.RECO_LATITUDE || '37.497942';
const RECO_LONGITUDE = __ENV.RECO_LONGITUDE || '127.027621';
const RECO_RADIUS = __ENV.RECO_RADIUS || '1.0';
const RECO_LIMIT = Number(__ENV.RECO_LIMIT || 20);
const RECO_KEYWORDS = (__ENV.RECO_KEYWORDS || '김치찌개,파스타,곱창,회')
  .split(',')
  .map((value) => value.trim())
  .filter(Boolean);
const RECO_SORT_BY = __ENV.RECO_SORT_BY || 'SCORE';
const RECO_KEYWORD_RATIO = Number(__ENV.RECO_KEYWORD_RATIO || 0.6);

const EXP_CRUD_MIN_AMOUNT = Number(__ENV.EXP_CRUD_MIN_AMOUNT || 5000);
const EXP_CRUD_MAX_AMOUNT = Number(__ENV.EXP_CRUD_MAX_AMOUNT || 45000);
const EXP_CRUD_CATEGORY_ID = Number(__ENV.EXP_CRUD_CATEGORY_ID || 1);
const EXP_CRUD_MEAL_TYPES = (__ENV.EXP_CRUD_MEAL_TYPES || 'BREAKFAST,LUNCH,DINNER,OTHER')
  .split(',')
  .map((value) => value.trim())
  .filter(Boolean);
const EXP_CRUD_START_DATE = __ENV.EXP_CRUD_START_DATE || '2024-01-01';
const EXP_CRUD_END_DATE = __ENV.EXP_CRUD_END_DATE || '2025-12-31';
const EXP_CRUD_MEMO_PREFIX = __ENV.EXP_CRUD_MEMO_PREFIX || 'PERF-CRUD';

const BUDGET_DAILY_DATES = (__ENV.BUDGET_DAILY_DATES || '2025-02-15,2025-02-16,2025-02-17')
  .split(',')
  .map((value) => value.trim())
  .filter(Boolean);
const BUDGET_APPLY_FORWARD = (__ENV.BUDGET_APPLY_FORWARD || 'false').toLowerCase() === 'true';

const BUDGET_CREATE_BASE_YEAR = Number(__ENV.BUDGET_CREATE_BASE_YEAR || 2090);
const BUDGET_CREATE_YEAR_SPAN = Number(__ENV.BUDGET_CREATE_YEAR_SPAN || 40);

const metrics = {
  food: new Trend('food_autocomplete_duration_ms'),
  recommendation: new Trend('recommendation_duration_ms'),
  expenditureList: new Trend('expenditure_list_duration_ms'),
  expenditureStats: new Trend('expenditure_stats_duration_ms'),
  budgetMonthly: new Trend('budget_monthly_duration_ms'),
  budgetDaily: new Trend('budget_daily_duration_ms'),
  expenditureCreate: new Trend('expenditure_create_duration_ms'),
  expenditureUpdate: new Trend('expenditure_update_duration_ms'),
  expenditureDelete: new Trend('expenditure_delete_duration_ms'),
  budgetMonthlyWrite: new Trend('budget_monthly_update_duration_ms'),
  budgetDailyWrite: new Trend('budget_daily_update_duration_ms'),
  budgetMonthlyCreate: new Trend('budget_monthly_create_duration_ms'),
};

export const options = {
  scenarios: {
    food_autocomplete: {
      executor: 'constant-arrival-rate',
      rate: Number(__ENV.AUTOCOMPLETE_RPS || 75),
      timeUnit: '1s',
      duration: TEST_DURATION,
      preAllocatedVUs: Number(__ENV.AUTOCOMPLETE_VUS || 20),
      exec: 'foodAutocomplete',
    },
    recommendations: {
      executor: 'constant-arrival-rate',
      rate: Number(__ENV.RECOMMEND_RPS || 80),
      timeUnit: '1s',
      duration: TEST_DURATION,
      preAllocatedVUs: Number(__ENV.RECOMMEND_VUS || 30),
      exec: 'recommendations',
    },
    expenditure_list: {
      executor: 'constant-arrival-rate',
      rate: Number(__ENV.EXPENDITURE_RPS || 40),
      timeUnit: '1s',
      duration: TEST_DURATION,
      preAllocatedVUs: Number(__ENV.EXPENDITURE_VUS || 15),
      exec: 'expenditureList',
    },
    expenditure_stats: {
      executor: 'constant-arrival-rate',
      rate: Number(__ENV.EXP_STATS_RPS || 20),
      timeUnit: '1s',
      duration: TEST_DURATION,
      preAllocatedVUs: Number(__ENV.EXP_STATS_VUS || 10),
      exec: 'expenditureStats',
    },
    budget_monthly: {
      executor: 'constant-arrival-rate',
      rate: Number(__ENV.BUDGET_MONTHLY_RPS || 20),
      timeUnit: '1s',
      duration: TEST_DURATION,
      preAllocatedVUs: Number(__ENV.BUDGET_MONTHLY_VUS || 10),
      exec: 'budgetMonthly',
    },
    budget_daily: {
      executor: 'constant-arrival-rate',
      rate: Number(__ENV.BUDGET_DAILY_RPS || 20),
      timeUnit: '1s',
      duration: TEST_DURATION,
      preAllocatedVUs: Number(__ENV.BUDGET_DAILY_VUS || 10),
      exec: 'budgetDaily',
    },
    expenditure_crud: {
      executor: 'constant-arrival-rate',
      rate: Number(__ENV.EXP_CRUD_RPS || 25),
      timeUnit: '1s',
      duration: TEST_DURATION,
      preAllocatedVUs: Number(__ENV.EXP_CRUD_VUS || 15),
      exec: 'expenditureCrudChain',
    },
    budget_monthly_update: {
      executor: 'constant-arrival-rate',
      rate: Number(__ENV.BUDGET_UPDATE_RPS || 15),
      timeUnit: '1s',
      duration: TEST_DURATION,
      preAllocatedVUs: Number(__ENV.BUDGET_UPDATE_VUS || 8),
      exec: 'budgetMonthlyUpdate',
    },
    budget_daily_update: {
      executor: 'constant-arrival-rate',
      rate: Number(__ENV.BUDGET_DAILY_UPDATE_RPS || 15),
      timeUnit: '1s',
      duration: TEST_DURATION,
      preAllocatedVUs: Number(__ENV.BUDGET_DAILY_UPDATE_VUS || 8),
      exec: 'budgetDailyUpdate',
    },
    budget_monthly_create: {
      executor: 'constant-arrival-rate',
      rate: Number(__ENV.BUDGET_CREATE_RPS || 5),
      timeUnit: '1s',
      duration: TEST_DURATION,
      preAllocatedVUs: Number(__ENV.BUDGET_CREATE_VUS || 4),
      exec: 'budgetMonthlyCreate',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],
    food_autocomplete_duration_ms: ['p(95)<250'],
    recommendation_duration_ms: ['p(95)<600'],
    expenditure_list_duration_ms: ['p(95)<450'],
    expenditure_stats_duration_ms: ['p(95)<450'],
    budget_monthly_duration_ms: ['p(95)<350'],
    budget_daily_duration_ms: ['p(95)<350'],
    expenditure_create_duration_ms: ['p(95)<650'],
    expenditure_update_duration_ms: ['p(95)<500'],
    expenditure_delete_duration_ms: ['p(95)<350'],
    budget_monthly_update_duration_ms: ['p(95)<400'],
    budget_daily_update_duration_ms: ['p(95)<400'],
    budget_monthly_create_duration_ms: ['p(95)<550'],
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
  const keyword = pickRandom(KEYWORDS, 'PERF');
  const url = `${BASE_URL}/api/v1/foods/autocomplete?keyword=${encodeURIComponent(keyword)}&limit=${FOOD_LIMIT}`;
  const res = http.get(url, { headers: baseHeaders });
  metrics.food.add(res.timings.duration);
  check(res, {
    'food autocomplete 200': (r) => r.status === 200,
  });
}

export function recommendations() {
  const url = buildRecommendationUrl();
  const res = http.get(url, { headers: authHeaders() });
  metrics.recommendation.add(res.timings.duration);
  check(res, {
    'recommendations 200': (r) => r.status === 200,
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

export function expenditureCrudChain() {
  const createPayload = buildExpenditurePayload();
  const createRes = http.post(`${BASE_URL}/api/v1/expenditures`, JSON.stringify(createPayload), {
    headers: authHeaders(),
  });
  metrics.expenditureCreate.add(createRes.timings.duration);
  const createBody = safeJson(createRes);
  check(createRes, { 'expenditure create 201': (r) => r.status === 201 });
  if (!createBody || !createBody.data) {
    return;
  }

  const expenditureId = createBody.data.expenditureId;
  if (!expenditureId) {
    return;
  }

  const updatePayload = {
    ...createPayload,
    memo: `${createPayload.memo}-updated`,
    amount: createPayload.amount + randomIntBetween(100, 500),
  };
  updatePayload.items = updatePayload.items.map((item) => ({
    ...item,
    price: updatePayload.amount,
  }));

  const updateRes = http.put(`${BASE_URL}/api/v1/expenditures/${expenditureId}`, JSON.stringify(updatePayload), {
    headers: authHeaders(),
  });
  metrics.expenditureUpdate.add(updateRes.timings.duration);
  check(updateRes, { 'expenditure update 200': (r) => r.status === 200 });

  const deleteRes = http.del(`${BASE_URL}/api/v1/expenditures/${expenditureId}`, null, {
    headers: authHeaders(),
  });
  metrics.expenditureDelete.add(deleteRes.timings.duration);
  check(deleteRes, { 'expenditure delete 204': (r) => r.status === 204 });
}

export function budgetMonthlyUpdate() {
  const payload = buildBudgetMonthlyPayload();
  const res = http.put(`${BASE_URL}/api/v1/budgets`, JSON.stringify(payload), {
    headers: authHeaders(),
  });
  metrics.budgetMonthlyWrite.add(res.timings.duration);
  check(res, { 'budget monthly update 200': (r) => r.status === 200 });
}

export function budgetDailyUpdate() {
  const date = pickRandom(BUDGET_DAILY_DATES, DAILY_DATE);
  const payload = buildBudgetDailyPayload();
  const res = http.put(`${BASE_URL}/api/v1/budgets/daily/${date}`, JSON.stringify(payload), {
    headers: authHeaders(),
  });
  metrics.budgetDailyWrite.add(res.timings.duration);
  check(res, { 'budget daily update 200': (r) => r.status === 200 });
}

export function budgetMonthlyCreate() {
  const payload = {
    monthlyFoodBudget: randomIntBetween(200000, 600000),
    budgetMonth: randomBudgetMonth(),
  };
  const res = http.post(`${BASE_URL}/api/v1/budgets/monthly`, JSON.stringify(payload), {
    headers: authHeaders(),
  });
  metrics.budgetMonthlyCreate.add(res.timings.duration);
  check(res, {
    'budget monthly create success': (r) => r.status === 201 || r.status === 409,
  });
}

function buildRecommendationUrl() {
  let url = `${BASE_URL}/api/v1/recommendations?latitude=${RECO_LATITUDE}&longitude=${RECO_LONGITUDE}&radius=${RECO_RADIUS}&limit=${RECO_LIMIT}&sortBy=${RECO_SORT_BY}`;
  if (Math.random() < RECO_KEYWORD_RATIO && RECO_KEYWORDS.length > 0) {
    const keyword = pickRandom(RECO_KEYWORDS, '맛집');
    url += `&keyword=${encodeURIComponent(keyword)}`;
  }
  if (__ENV.RECO_INCLUDE_DISLIKED === 'true') {
    url += '&includeDisliked=true';
  }
  if (__ENV.RECO_OPEN_NOW === 'true') {
    url += '&openNow=true';
  }
  return url;
}

function buildExpenditurePayload() {
  const amount = randomIntBetween(EXP_CRUD_MIN_AMOUNT, EXP_CRUD_MAX_AMOUNT);
  const mealType = pickRandom(EXP_CRUD_MEAL_TYPES, 'LUNCH');
  const expendedDate = randomDateBetween(EXP_CRUD_START_DATE, EXP_CRUD_END_DATE);
  const expendedTime = randomTimeString();
  return {
    storeName: `PERF_STORE_${randomIntBetween(1, 1_000_000)}`,
    amount,
    expendedDate,
    expendedTime,
    categoryId: EXP_CRUD_CATEGORY_ID,
    mealType,
    memo: `${EXP_CRUD_MEMO_PREFIX} ${expendedDate} ${expendedTime}`,
    items: [
      {
        foodName: `${mealType}_SET`,
        quantity: 1,
        price: amount,
      },
    ],
  };
}

function buildBudgetMonthlyPayload() {
  return {
    monthlyFoodBudget: randomIntBetween(200000, 650000),
    dailyFoodBudget: randomIntBetween(8000, 20000),
  };
}

function buildBudgetDailyPayload() {
  return {
    dailyFoodBudget: randomIntBetween(8000, 20000),
    applyForward: BUDGET_APPLY_FORWARD,
  };
}

function pickRandom(list, fallback) {
  if (!list || list.length === 0) {
    return fallback;
  }
  const idx = Math.floor(Math.random() * list.length);
  return list[idx];
}

function randomDateBetween(start, end) {
  const startTime = Date.parse(start);
  const endTime = Date.parse(end);
  if (Number.isNaN(startTime) || Number.isNaN(endTime) || endTime <= startTime) {
    return start;
  }
  const value = startTime + Math.floor(Math.random() * (endTime - startTime));
  return new Date(value).toISOString().slice(0, 10);
}

function randomTimeString() {
  const hour = String(randomIntBetween(8, 22)).padStart(2, '0');
  const minute = String(randomIntBetween(0, 59)).padStart(2, '0');
  const second = String(randomIntBetween(0, 59)).padStart(2, '0');
  return `${hour}:${minute}:${second}`;
}

function randomBudgetMonth() {
  const yearOffset = Math.max(BUDGET_CREATE_YEAR_SPAN - 1, 0);
  const year = BUDGET_CREATE_BASE_YEAR + randomIntBetween(0, yearOffset || 0);
  const month = String(randomIntBetween(1, 12)).padStart(2, '0');
  return `${year}-${month}`;
}

function safeJson(res) {
  try {
    return res.json();
  } catch (error) {
    return null;
  }
}
