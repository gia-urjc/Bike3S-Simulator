const { min, max, pattern } = require('../constraints');

module.exports = (...constraints) => Object.assign({
    type: 'string'
}, ...constraints.map((constraint) => {
    switch (constraint.type) {
        case min: return {
            minLength: constraint.argument
        };
        case max: return {
            maxLength: constraint.argument
        };
        case pattern: return {
            pattern: new RegExp(constraint.argument).source
        };
        default: {
            console.trace(`Unsupported constraint '${constraint.type.name}' for type 'string'`);
            return {};
        }
    }
}));