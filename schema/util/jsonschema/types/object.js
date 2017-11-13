const { min, max, required, requireBut, requireAll } = require('../constraints');

module.exports = (properties, ...constraints) => Object.assign({
    type: 'object',
    additionalProperties: false,
    properties: properties
}, ...constraints.map((constraint) => {
    switch (constraint.type) {
        case min: return {
            minProperties: constraint.argument
        };
        case max: return {
            maxProperties: constraint.argument
        };
        case required: return {
            required: constraint.argument
        };
        case requireBut: return {
            required: Object.keys(properties).filter((x) => !constraint.argument.includes(x))
        };
        case requireAll: return {
            required: Object.keys(properties)
        };
        default: {
            console.trace(`Unsupported constraint '${constraint.type.name}' for type 'object'`);
            return {};
        }
    }
}));