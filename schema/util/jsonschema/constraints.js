const Constraint = (type, args) => ({
    type: type,
    args: args || []
});

module.exports = {
    Min: (value) => Constraint(module.exports.Min, [value]),
    Max: (value) => Constraint(module.exports.Max, [value]),
    XMin: (value) => Constraint(module.exports.XMin, [value]),
    XMax: (value) => Constraint(module.exports.XMax, [value]),
    Multiple: (value) => Constraint(module.exports.Multiple, [value]),
    Require: (...properties) => Constraint(module.exports.Require, properties),
    RequireAll: () => Constraint(module.exports.RequireAll),
};