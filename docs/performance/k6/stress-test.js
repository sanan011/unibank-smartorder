import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    stages: [
        { duration: '30s', target: 20 },   // ramp up to 20 users
        { duration: '1m', target: 50 },    // ramp up to 50 users
        { duration: '1m', target: 100 },   // ramp up to 100 users (stress)
        { duration: '2m', target: 100 },   // stay at 100 users to test stability
        { duration: '30s', target: 0 },    // ramp down to 0 users
    ],
    thresholds: {
        http_req_duration: ['p(95)<500'], // 95% of requests must complete below 500ms
        http_req_failed: ['rate<0.01'],   // error rate must be less than 1%
    },
};

const BASE_URL = 'http://localhost:8080';

export default function () {
    const res = http.get(`${BASE_URL}/api/v1/products`);
    
    check(res, {
        'status is 200': (r) => r.status === 200,
        'response time < 500ms': (r) => r.timings.duration < 500,
    });
    
    sleep(1);
}
