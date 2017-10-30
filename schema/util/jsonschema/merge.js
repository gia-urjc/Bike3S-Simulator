const { mergeWith, isArray, cloneDeep } = require('lodash');

// uses lodash's deep merge for objects but concatenates arrays instead of overwriting values at equal indexes
// this prevents for example two conflicting 'required'
module.exports = (target, ...sources) => {
    return mergeWith(cloneDeep(target), ...sources.map(cloneDeep), (targetValue, sourceValue) => {
        if (isArray(targetValue)) return targetValue.concat(sourceValue);
    });
};