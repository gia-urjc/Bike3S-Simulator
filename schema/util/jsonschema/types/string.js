const { Min, Max, Pattern } = require('../constraints');

module.exports = (...constraints) => Object.assign({
    type: 'string'
}, ...constraints.map((constraint) => {
    switch (constraint.type) {
        case Min: return {
            minLength: constraint.argument
        };
        case Max: return {
            maxLength: constraint.argument
        };
        case Pattern: return {
            pattern: new RegExp(constraint.argument).source
        };
        default: {
            console.trace(`Unsupported constraint '${constraint.type.name}' for type 'string'`);
            return {};
        }
    }
}));