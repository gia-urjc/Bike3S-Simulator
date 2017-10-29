const JSNumber = require('./number');

module.exports = (...constraints) => Object.assign(JSNumber(...constraints), {
    type: 'integer'
});