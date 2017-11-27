#!/usr/bin/env node
'use strict';

const program = require('commander');

program
    .version('0.0.1')
    .command('verify <jsonInput> <jsonSchema>')
    .alias('v')
    .description('Verifies if a json is valid given a jsonSchema')
    .action((jsonInput, jsonSchema) => {
        //TODO: Verifification process
    });

program.parse(process.argv);

