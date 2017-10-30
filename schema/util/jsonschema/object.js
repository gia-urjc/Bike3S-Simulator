const { Min, Max, Require, RequireAll } = require('./constraints');

module.exports = (properties, ...constraints) => Object.assign({
    type: 'object',
    additionalProperties: false,
    properties: properties
}, ...constraints.map((constraint) => {
    switch (constraint.type) {
        case Min: return {
            minProperties: constraint.argument
        };
        case Max: return {
            maxProperties: constraint.argument
        };
        case Require: return {
            required: constraint.argument
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