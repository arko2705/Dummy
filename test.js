import http from 'k6/http';
import { sleep } from 'k6';

const BASE = __ENV.BASE_URL || 'http://localhost:8080';

export const options = {
  vus: __ENV.VUS ? parseInt(__ENV.VUS) : 50,
  duration: __ENV.DURATION || '2m',
};

export default function () {
  http.get(`${BASE}/api/products`);
  http.get(`${BASE}/api/cart`);
  http.get(`${BASE}/api/orders`);
  http.get(`${BASE}/api/payments`);
  sleep(0.5);
}
