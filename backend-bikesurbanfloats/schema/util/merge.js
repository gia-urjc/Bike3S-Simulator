const _ = require('lodash');

// uses lodash's deep merge for objects but concatenates arrays instead of overwriting values at equal indexes
// this prevents for example two conflicting 'required'
module.exports = (target, ...sources) => {
    sources.forEach((source) => {
        _.mergeWith(target, source, (targetValue, sourceValue) => {
            if (_.isArray(targetValue)) return targetValue.concat(sourceValue);
        });
    });

    return target;
};