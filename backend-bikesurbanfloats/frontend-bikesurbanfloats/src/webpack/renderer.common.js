'use strict';

const path = require('path');
const ExtractTextPlugin = require('extract-text-webpack-plugin');
const { NoEmitOnErrorsPlugin, ContextReplacementPlugin } = require('webpack');
const { CommonsChunkPlugin } = require('webpack').optimize;
const HtmlWebpackPlugin = require('html-webpack-plugin');

module.exports = {
    entry: {
        polyfills: './src/renderer/polyfills.ts',
        vendor: './src/renderer/vendor.ts',
        renderer: './src/renderer/renderer.ts'
    },

    module: {
        rules: [
            {
                test: /\.ts$/,
                use: [ 'angular2-template-loader' ]
            },
            {
                test: /\.html$/,
                use: 'html-loader'
            },
            {
                test: /\.(png|jpe?g|gif|svg|woff|woff2|ttf|eot|ico)$/,
                use: 'file-loader?name=assets/[name].[hash].[ext]'
            },
            {
                test: /\.css$/,
                exclude: path.resolve(process.cwd(), 'src/renderer/app'),
                use: ExtractTextPlugin.extract({ fallback: 'style-loader', use: 'css-loader?sourceMap' })
            },
            {
                test: /\.css$/,
                include: path.resolve(process.cwd(), 'src/renderer/app'),
                use: 'raw-loader'
            }
        ]
    },

    plugins: [
        new NoEmitOnErrorsPlugin(),

        new ContextReplacementPlugin(/angular[\\/]core[\\/]@angular/, path.resolve(process.cwd(), 'src/renderer'), {
            // routes
        }),

        new CommonsChunkPlugin({
            name: ['renderer', 'vendor', 'polyfills']
        }),

        new HtmlWebpackPlugin({
            template: './src/renderer/index.html'
        }),

        new ExtractTextPlugin('[name].css')
    ],

    target: 'electron-renderer'
};