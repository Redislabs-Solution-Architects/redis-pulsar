import { createClient, commandOptions } from 'redis';

const REDIS_URL = 'redis://default:@localhost:6379';

(async () => {
    const redis = createClient({url: REDIS_URL});
    await redis.connect();
    
    let currentId = '0-0';
    console.log('stream client awaiting messages')
    while (true) {
        let response = await redis.xRead(
            commandOptions({isolated: true}),
            [{key: 'clientstream', id: '$'}],
            {COUNT: 1, BLOCK: 5000}
        );

        if (response) {
            console.log(JSON.stringify(response[0].messages[0]));
        }
    }

})();

