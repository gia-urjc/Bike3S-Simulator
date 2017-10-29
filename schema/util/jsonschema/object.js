const { Min, Max, Require, RequireAll } = require('./constraints');

module.exports = (properties, ...constraints) => Object.assign({
    type: 'object',
    additionalProperties: false,
    properties: properties
}, ...constraints.map((constraint) => {
    switch (constraint.type) {
        case Min: return {
            minProperties: constraint.args[0]
        };
        case Max: return {
            maxProperties: constraint.args[0]
        };
        case Require: return {
            required: constraint.args
        };
        case RequireAll: return {
            required: Object.keys(properties)
        };
        default: {
            console.trace(`Unsupported constraint '${constraint.type.name}' for type 'object'`);
            return {};
        }
    }
}));