const { mergeWith, isArray, cloneDeep } = require('lodash');

// uses lodash's deep merge for objects but concatenates arrays instead of overwriting values at equal indexes
// this prevents for example two conflicting 'required' being overridden
// deep clone input objects to prevent them being modified
module.exports = (...sources) => {
    return mergeWith(...sources.map(cloneDeep), (targetValue, sourceValue) => {
        if (isArray(targetValue)) return targetValue.concat(sourceValue);
    });
};