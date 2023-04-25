const webpack = require('webpack');

module.exports = {
    entry: './src/gear.js',
    mode: "production",
    plugins: [
        new webpack.BannerPlugin({
			banner:'#!js name=lib',
            raw: true,
            entryOnly: true,
	    })
	]
}