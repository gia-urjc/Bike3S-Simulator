'use strict';

const path = require('path');
const CopyWebpackPlugin = require('copy-webpack-plugin');

module.exports = {
    output: {
        path: path.resolve(process.cwd(), 'build'),
        filename: '[name].js',
        chunkFilename: '[id].chunk.js'
    },

    resolve: {
        extensions: ['.ts', '.js']
    },

    module: {
        rules: [
            {
                test: /\.ts$/,
                use: [
                    {
                        loader: 'awesome-typescript-loader',
                        options: { configFileName: './src/tsconfig.json' }
                    }
                ]
            }
        ]
    },

    plugins: [
        new CopyWebpackPlugin([{
            from: path.resolve(process.cwd(), 'src', 'main', 'package.json'),
            dest: path.resolve(process.cwd(), 'build', 'package.json')
        }])
    ],

    node: {
        __dirname: false,
        __filename: false
    }
};