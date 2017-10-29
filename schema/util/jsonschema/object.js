const { Require, RequireAll } = require('./constraints');

module.exports = (properties, ...constraints) => Object.assign({
    type: 'object',
    additionalProperties: false,
    properties: properties
}, ...constraints.map((constraint) => {
    if (constraint === RequireAll) {
        return Require(...Object.keys(properties));
    }
    return constraint;
}));