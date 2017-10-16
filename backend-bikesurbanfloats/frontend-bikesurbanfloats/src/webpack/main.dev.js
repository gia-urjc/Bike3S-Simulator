'use strict';

const merge = require('webpack-merge');

module.exports = merge(require('./all.dev'), require('./main.common'));