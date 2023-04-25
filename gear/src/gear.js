#!js name=lib

async function execute(async_client, cmd) {
    let cmdArr = cmd.split(' ');
    let metric = cmdArr.slice(-1).toString();
    async_client.block((client) => {
        let arr = client.call(...cmdArr);
        results = [];
        for (let i=1; i <= arr[0]; i++) {
            results.push(`{ "symbol": "${arr[i][1]}", "${metric}": "${arr[i][3]}" }`);
        }
        if (results.length) {
            client.call('XADD', 'clientstream', '*', metric, results.toString());
        }
    });
}

redis.register_notifications_consumer('consumer', 'doc:', async (async_client, data) => {
    let key = data.key;
    let symbol = key.split(":")[1];
    let metric = key.split(":")[2];
    let cmd;
    switch (metric) {
        case "M1":
            cmd = `FT.AGGREGATE doc_idx @symbol:{${symbol}} GROUPBY 1 @symbol REDUCE AVG 1 @M1 AS m1_ave`;
            break;
        case "M2":
            cmd = `FT.AGGREGATE doc_idx @symbol:{${symbol}} GROUPBY 1 @symbol REDUCE SUM 1 @M2 AS m2_sum`;
            break;
        case "M3":
            cmd = `FT.AGGREGATE doc_idx @symbol:{${symbol}} GROUPBY 1 @symbol REDUCE QUANTILE 2 @M3 0.99 AS m3_p99`;
            break;
    }
    if (cmd) {
        execute(async_client, cmd);
    }
});