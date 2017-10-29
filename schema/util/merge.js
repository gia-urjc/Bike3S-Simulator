const { mergeWith, isArray } = require('lodash');

// uses lodash's deep merge for objects but concatenates arrays instead of overwriting values at equal indexes
// this prevents for example two conflicting 'required'
module.exports = (target, ...sources) => {
    return mergeWith(target, ...sources, (targetValue, sourceValue) => {
        if (isArray(targetValue)) return targetValue.concat(sourceValue);
    });
};