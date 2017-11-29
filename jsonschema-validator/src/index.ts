import * as program from 'commander';
import * as Ajv from 'ajv'
import * as fs from 'fs-extra';

const validate = async (dirInput: string, dirSchema: string) => {
    try {   
        let jsonInput = await fs.readJson(dirInput);
        let jsonSchema = await fs.readJson(dirSchema);
        let ajv = new Ajv({
            allErrors: true
        });
        let valid = ajv.validate(jsonSchema, jsonInput);
        if(!valid) return ajv.errors;
        return 'OK';
    }
    catch(error) {
        console.log(error);
    }; 
}
    

program
    .version('0.0.1')
    .description('Verifies if a json is valid given a jsonSchema')
    .command('verify')
    .option('-i, --input [input]', 'Json to verify')
    .option('-s, --schema [schema]', 'Json schema for validations')
    .action((options: any) => {
        validate(options.input, options.schema).then((data: any) =>{
            console.log(data);
        })
        .catch((error) => {
            console.log(error);
        });
    });

program.parse(process.argv);
