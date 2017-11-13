const sNumber = require('./number');

module.exports = (...constraints) => Object.assign(sNumber(...constraints), {
    type: 'integer'
});