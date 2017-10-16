'use strict';

const merge = require('webpack-merge');

module.exports = merge(require('./all.prod'), require('./main.common'));