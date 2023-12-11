import http from 'k6/http';

export const options = {
    scenarios: {
        constant_request_rate: {
            executor: 'constant-arrival-rate',
            rate: 50,
            timeUnit: '1s',
            duration: '2h',
            preAllocatedVUs: 10,
            maxVUs: 100,
        },
    },
};

export default function () {
    http.get('http://localhost:8080');
}
