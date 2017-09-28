'use strict';

const merge = require('webpack-merge');

module.exports = merge.smart(require('./all.dev'), require('./renderer.common'));