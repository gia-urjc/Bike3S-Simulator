const { Min, Max, XMin, XMax, Multiple } = require('./constraints');

module.exports = (...constraints) => Object.assign({
    type: 'number'
}, ...constraints.map((constraint) => {
    switch (constraint.type) {
        case Min: return {
            minimum: constraint.args[0]
        };
        case Max: return {
            maximum: constraint.args[0]
        };
        case XMin: return {
            exclusiveMinimum: constraint.args[0]
        };
        case XMax: return {
            exclusiveMaximum: constraint.args[0]
        };
        case Multiple: return {
            multipleOf: constraint.args[0]
        };
        default: {
            console.trace(`Unsupported constraint '${constraint.type.name}' for type 'number'`);
            return {};
        }
    }
}));