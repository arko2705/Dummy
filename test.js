import http from 'k6/http';

export default function () {
  http.get('http://localhost:8080/products');
}

export let options = {
  vus: 150,        // 150 virtual users
  duration: '30s' // run for 30 seconds
};