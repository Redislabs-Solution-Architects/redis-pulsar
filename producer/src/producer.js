/**
 * @fileoverview Build an index in Redis and then sends Pulsar a message stream that is inserted into 
 * Redis as JSON.
 * @maker Joey Whelan
 */

import { createRequire } from 'module';
const require = createRequire(import.meta.url);
const Pulsar = require('pulsar-client');
import { createClient, SchemaFieldTypes } from 'redis';

const REDIS_URL = 'redis://default:@localhost:6379';
const IDX_NAME = 'doc_idx';
const PREFIX = 'doc:';
const NUM_MESSAGES = 10;
const SYMBOLS = ['AA', 'BB', 'CC', 'DD'];
const METRICS = ['M1', 'M2', 'M3'];

async function index(client) {
    try {await client.ft.dropIndex(IDX_NAME)} catch(err){}
    await client.ft.create(IDX_NAME, {
        '$.symbol': {
            type: SchemaFieldTypes.TAG,
            AS: 'symbol'
        },
        '$.M1': {
            type: SchemaFieldTypes.NUMERIC,
            AS: 'M1'
        }, 
        '$.M2': {
            type: SchemaFieldTypes.NUMERIC,
            AS: 'M2'
        },
        '$.M3': {
            type: SchemaFieldTypes.NUMERIC,
            AS: 'M3'
        }
    }, { ON: 'JSON', PREFIX: PREFIX});
}

async function sleep(ms) {
    return new Promise(resolve => setTimeout(resolve,ms));
}

async function produce(client) {
    const producer = await client.createProducer({ topic: 'ingress-topic' });

    for (let i=0; i < NUM_MESSAGES; i++) {
        let symbol = SYMBOLS[Math.floor(Math.random() * SYMBOLS.length)];
        let metric = METRICS[Math.floor(Math.random() * METRICS.length)];
        let value = {'symbol': symbol};
        switch (metric) {
            case "M1":
                value["M1"] = Math.round( (Math.random() * (50-5) + 5) * 100) / 100;
                break;
            case "M2":
                value["M2"] = Math.round(Math.random() * 1000000 * 100) / 100;
                break;
            case "M3":
                value["M3"] = Math.floor(Math.random() * 1000);
        }

        let obj = { 
            'key': `doc:${symbol}:${metric}:${i}`,
            'path': '$',
            'value': value
        }
        console.log(`sending: ${JSON.stringify(obj)}`);
        await producer.send({data: Buffer.from(JSON.stringify(obj))});
        await sleep(1000);
    }
    await producer.close();
}

(async () => {
    const redis = createClient({url: REDIS_URL});
    await redis.connect();
    await index(redis);
    await redis.quit();

    const pulsar = new Pulsar.Client({ serviceUrl: 'pulsar://localhost:6650'});
    await produce(pulsar);
    await pulsar.close();
})();