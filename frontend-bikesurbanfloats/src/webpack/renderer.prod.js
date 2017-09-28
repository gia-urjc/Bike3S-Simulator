'use strict';

const merge = require('webpack-merge');
const { LoaderOptionsPlugin } = require('webpack');
const { AotPlugin } = require('@ngtools/webpack');
const path = require('path');

module.exports = merge.smartStrategy({
    plugins: 'prepend'
})(require('./all.prod'), merge(require('./renderer.common'), {
    plugins: [
        new LoaderOptionsPlugin({
            htmlLoader: {
                minimize: false
            }
        }),

        new AotPlugin({
            tsConfigPath: path.resolve(process.cwd(), 'src/tsconfig.json'),
            entryModule: path.resolve(process.cwd(), 'src/renderer/app/app.module#AppModule'),
            skipCodeGeneration: true
        })
    ]
}));