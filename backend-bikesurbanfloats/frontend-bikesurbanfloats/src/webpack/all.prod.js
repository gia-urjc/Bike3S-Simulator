'use strict';

const merge = require('webpack-merge');
const UglifyJsPlugin = require('uglifyjs-webpack-plugin');
const { DefinePlugin } = require('webpack');

const ENV = process.env.NODE_ENV = process.env.target = 'production';

module.exports = merge(require('./all.common'), {
    plugins: [
        new DefinePlugin({
            'process.env': {
                'target': JSON.stringify(ENV)
            }
        }),

        new UglifyJsPlugin({
            uglifyOptions: {
                ie8: false,
                ecma: 8,
                mangle: true,
                compress: {
                    warnings: true
                }
            }
        })
    ]
});