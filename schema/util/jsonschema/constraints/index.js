const constraint = (type, argument) => ({
    type: type,
    argument: argument
});

module.exports = {
    min: (value) => constraint(module.exports.min, value),
    max: (value) => constraint(module.exports.max, value),
    xMin: (value) => constraint(module.exports.xMin, value),
    xMax: (value) => constraint(module.exports.xMax, value),
    multipleOf: (value) => constraint(module.exports.multipleOf, value),
    required: (...properties) => constraint(module.exports.required, properties),
    requireBut: (...properties) => constraint(module.exports.requireBut, properties),
    requireAll: () => constraint(module.exports.requireAll),
    pattern: (regex) => constraint(module.exports.pattern, regex),
};