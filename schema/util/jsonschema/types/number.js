const { Min, Max, XMin, XMax, Multiple } = require('../constraints');

module.exports = (...constraints) => Object.assign({
    type: 'number'
}, ...constraints.map((constraint) => {
    switch (constraint.type) {
        case Min: return {
            minimum: constraint.argument
        };
        case Max: return {
            maximum: constraint.argument
        };
        case XMin: return {
            exclusiveMinimum: constraint.argument
        };
        case XMax: return {
            exclusiveMaximum: constraint.argument
        };
        case Multiple: return {
            multipleOf: constraint.argument
        };
        default: {
            console.trace(`Unsupported constraint '${constraint.type.name}' for type 'number'`);
            return {};
        }
    }
}));