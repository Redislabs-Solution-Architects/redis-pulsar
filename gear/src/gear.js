#!js name=lib

/** 
 * This function accepts a FT.AGGREGATE command as input string.  That strings is subsequently parsed
 * into a comma delimited list to pass to a Gears command call to Redis.  The results of that call are then 
 * parsed into a stock symbol and aggregate metric that then sent to a Redis stream as a JSON object.
 * 
 * @param   cmd String representing a Redis FT.AGGREGATE command
*/
async function execute(async_client, cmd) {
    let cmdArr = cmd.split(' ');
    let metric = cmdArr.slice(-1).toString();
    async_client.block((client) => {
        let arr = client.call(...cmdArr);  //send command to redis as a list of strings
        results = [];
        for (let i=1; i <= arr[0]; i++) {  //parse out aggregate results 
            results.push(`{ "symbol": "${arr[i][1]}", "${metric}": "${arr[i][3]}" }`);
        }
        if (results.length) {  //write out aggregate results to stream
            client.call('XADD', 'clientstream', '*', metric, results.toString());
        }
    });
}

/** 
 * This is a key space trigger that monitors keys with the prefix of 'doc:'.  The input to this function would look
 * this -  key = doc:<symbol name>:<metric name>.  Example:  doc:AA:M1.  Symbol name represents a notional stock ticker
 * symbol  Metric name represents a data metric on that symbol.  A JSON.SET event on that key means there was an update
 * to that particular symbol's metric.  This activity then generates an aggregation on that key and metric that is
 * subsequently written to a Redis stream.
*/
redis.register_notifications_consumer('consumer', 'doc:', async (async_client, data) => {
    if (data.event === 'json.set') {
        let key = data.key;
        let symbol = key.split(":")[1];  //parse out the symbol name
        let metric = key.split(":")[2];  //parse out the metric name
        let cmd;

        // Based on the metric name, create a Redis Aggregation command for the given symbol 
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

        // Execute the aggregate command and send the results to a stream 
        if (cmd) {
            execute(async_client, cmd);
        }
    }
});