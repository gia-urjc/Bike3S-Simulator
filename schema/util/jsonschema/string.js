const { Min, Max } = require('./constraints');

module.exports = (...constraints) => Object.assign({
    type: 'string'
}, ...constraints.map((constraint) => {
    switch (constraint.type) {
        case Min: return {
            minLength: constraint.args[0]
        };
        case Max: return {
            maxLength: constraint.args[0]
        };
        default: {
            console.trace(`Unsupported constraint '${constraint.type.name}' for type 'string'`);
            return {};
        }
    }
}));