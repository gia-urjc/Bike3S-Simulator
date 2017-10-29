module.exports = {
    Max: (value) => ({
        maximum: value
    }),
    Min: (value) => ({
        minimum: value
    }),
    XMax: (value) => ({
        exclusiveMaximum: value
    }),
    XMin: (value) => ({
        exclusiveMinimum: value
    }),
    Multiple: (value) => ({
        multipleOf: value
    }),
    Require: (...properties) => ({
        required: properties
    }),
    RequireAll: () => module.exports.RequireAll,
};