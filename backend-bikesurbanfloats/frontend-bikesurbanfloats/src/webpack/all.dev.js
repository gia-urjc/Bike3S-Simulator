'use strict';

const merge = require('webpack-merge');
const path = require('path');

module.exports = merge(require('./all.common'), {
    devtool: 'cheap-module-eval-source-map',

    module: {
        rules: [
            {
                enforce: 'pre',
                test: /\.[jt]s$/,
                use: 'source-map-loader',
                exclude: [
                    path.resolve(process.cwd(), 'node_modules/@angular')
                ]
            }
        ]
    }
});