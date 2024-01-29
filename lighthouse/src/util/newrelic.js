import Encryptor from 'encryptor-node'

export function sendMetricToNewRelic(metricName, value, attributes) {
const initAttributes = {'app_name':'LH-demo'}
const finalAttributes = Object.assign(initAttributes, attributes)

const apiUrl = 'https://metric-api.eu.newrelic.com/metric/v1';
const data = [{
    "metrics": [
        {
            "name": metricName,
            // "type":"gauge", 
            "value": value,
            "timestamp": new Date().getTime(),
            "attributes": finalAttributes
        }
    ]
}];



const requestOptions = {
    method: 'POST',
    headers: {
        'Content-Type': 'application/json',
        "Api-Key": `${Encryptor.decrypt(`${process.env.CRYPTO_KEY}`,`${process.env.NEW_RELIC_LICENSE_KEY}`)}`
    },
    body: JSON.stringify(data),
};

fetch(apiUrl, requestOptions)
    .then(response => {
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
    })
    .catch(error => {
        console.error('Error:', error);
    });
}