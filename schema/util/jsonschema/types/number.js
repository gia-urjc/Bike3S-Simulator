const { min, max, xMin, xMax, multipleOf } = require('../constraints');

module.exports = (...constraints) => Object.assign({
    type: 'number'
}, ...constraints.map((constraint) => {
    switch (constraint.type) {
        case min: return {
            minimum: constraint.argument
        };
        case max: return {
            maximum: constraint.argument
        };
        case xMin: return {
            exclusiveMinimum: constraint.argument
        };
        case xMax: return {
            exclusiveMaximum: constraint.argument
        };
        case multipleOf: return {
            multipleOf: constraint.argument
        };
        default: {
            console.trace(`Unsupported constraint '${constraint.type.name}' for type 'number'`);
            return {};
        }
    }
}));